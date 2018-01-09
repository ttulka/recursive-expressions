package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Recursive Grammar.
 * <p>
 * Defined as a set of recursive rules.
 * <p>
 * From a grammar a derivative tree can be generated based on an input string. The result is represented as a {@link RecexpMatcher matcher} and the derivative
 * as a tree of {@link RecexpGroup groups}.
 *
 * @author ttulka
 * @see RecexpMatcher
 * @see RecexpGroup
 * @see java.util.regex.Pattern
 */
public class RecexpGrammar {

    protected final Set<Rule> rules;
    protected final int flags;

    /**
     * The only one constructor.
     *
     * @param rules the rules
     * @param flags the match flags, a bit mask that may include the flags from {@link java.util.regex.Pattern}
     */
    private RecexpGrammar(Collection<Rule> rules, int flags) {
        Set<Rule> ruleSet = new HashSet<Rule>(rules);
        // add implicit rules
        ruleSet.add(ImplicitRule.EPSILON_RULE);

        this.rules = Collections.unmodifiableSet(ruleSet);
        this.flags = flags;
    }

    /**
     * Constructs a grammar object from the rules.
     *
     * @param rule  the first rule
     * @param rules the next rules
     * @return the constructed grammar object
     */
    public static RecexpGrammar compile(String rule, String... rules) {
        Set<Rule> ruleSet = new HashSet<Rule>(rules.length);
        ruleSet.add(new Rule(rule, rule));
        for (String r : rules) {
            ruleSet.add(new Rule(r, r));
        }
        return new RecexpGrammar(ruleSet, 0);
    }

    /**
     * Constructs a grammar object from the rule with the flags.
     *
     * @param rule  the first rule
     * @param flags the match flags, a bit mask that may include the flags from {@link java.util.regex.Pattern}
     * @return the constructed grammar object
     */
    public static RecexpGrammar compile(String rule, int flags) {
        return new RecexpGrammar(Collections.singleton(new Rule(rule, rule)), flags);
    }

    /**
     * Creates a grammar builder.
     *
     * @return the builder
     */
    public static RecexpGrammarBuilder builder() {
        return new RecexpGrammarBuilder();
    }

    /**
     * Convenient method. See {@link RecexpMatcher#matches()}.
     *
     * @param input the input string
     * @return true if the grammar accepts the string, otherwise false
     */
    public boolean matches(String input) {
        return matcher(input).matches();
    }

    /**
     * Creates a matcher from this grammar for an input string with a starting rule.
     *
     * @param startingRuleName the name of the starting rule
     * @param input            the input string
     * @return the matcher
     * @throws RecexpEmptyRulesException when there are no rules
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    public RecexpMatcher matcher(String startingRuleName, String input) {
        return matcher(getNamedRules(startingRuleName), input);
    }

    /**
     * Creates a matcher from this grammar for an input string.
     *
     * @param input the input string
     * @return the matcher
     * @throws RecexpEmptyRulesException when there are no rules
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    public RecexpMatcher matcher(String input) {
        return matcher(getAllExplicitRules(), input);
    }

    private RecexpMatcher matcher(Set<Rule> rules, String input) {
        checkEmptyRules(rules);
        checkCyclicRules(rules);

        for (Rule rule : rules) {
            ExpressionTree.Node derivative = deriveTree(rule.getExpression().getRoot(), input, new HashSet<String>());
            if (derivative != null) {
                RecexpGroup group = nodeToGroup(derivative, input, flags);
                return RecexpMatcher.matcher(rule.toString(), input, group.groups());
            }
        }
        return RecexpMatcher.emptyMatcher(input);
    }

    /**
     * @throws RecexpEmptyRulesException when there are no rules
     */
    void checkEmptyRules(Set<Rule> rules) {
        if (rules.isEmpty()) {
            throw new RecexpEmptyRulesException();
        }
    }

    /**
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    void checkCyclicRules(Set<Rule> rules) {
        for (Rule rule : rules) {
            if (!checkCyclicRules(rule, rule.getExpression().getRoot())) {
                throw new RecexpCyclicRuleException(rule.getName());
            }
        }
    }

    /**
     * @return true if the rule has no self-reference, otherwise false.
     */
    private boolean checkCyclicRules(Rule rule, ExpressionTree.Node node) {
        if (node.isOrNode()) {
            for (ExpressionTree.Node n : node.getSubNodes()) {
                if (checkCyclicRules(rule, n)) {
                    return true;
                }
            }
        }

        Set<ExpressionTree.Node> unresolvedReferences = new HashSet<ExpressionTree.Node>();

        for (ExpressionTree.Node leaf : node.getLeaves()) {
            Expression expression = leaf.getExpression();

            if (ExpressionUtils.matchesEpsilon(leaf.toWord())) {
                continue;
            }

            if (leaf.isThisReference()) {
                return false;
            }

            if (expression.isReference()) {
                if (rule.getName().equals(leaf.getExpression().getText())) {
                    if (numberOfRulesWithSameName(rule.getName()) == 1) {
                        return false;
                    }
                }
                unresolvedReferences.add(leaf);
            }
        }

        for (ExpressionTree.Node n : unresolvedReferences) {
            for (ExpressionTree.Node candidate : generateCandidates(n, node)) {
                if (checkCyclicRules(rule, candidate)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private int numberOfRulesWithSameName(String ruleName) {
        int count = 0;
        for (Rule rule : getAllExplicitRules()) {
            if (rule.getName().equals(ruleName)) {
                count++;
            }
        }
        return count;
    }

    private Set<Rule> getAllExplicitRules() {
        Set<Rule> explicitRules = new HashSet<Rule>();

        for (Rule rule : rules) {
            if (!(rule instanceof ImplicitRule)) {
                explicitRules.add(rule);
            }
        }
        return Collections.unmodifiableSet(explicitRules);
    }

    private Set<Rule> getNamedRules(String name) {
        Set<Rule> namedRules = new HashSet<Rule>();

        for (Rule rule : rules) {
            if (rule instanceof NamedRule && rule.getName().equals(name)) {
                namedRules.add(rule);
            }
        }
        if (namedRules.isEmpty()) {
            throw new IllegalArgumentException("No rule with the name '" + name + "' found.");
        }
        return namedRules;
    }

    /**
     * Returns a derivative tree for the candidate and input, or <code>null</code> if there is no such a derivation.
     */
    ExpressionTree.Node deriveTree(ExpressionTree.Node root, String input, Set<String> alreadySeen) {
        Queue<ExpressionTree.Node> candidatesQueue = new LinkedList<ExpressionTree.Node>();

        if (root.isOrNode()) {
            candidatesQueue.addAll(root.getSubNodes());
        } else {
            candidatesQueue.add(root);
        }

        while (!candidatesQueue.isEmpty()) {
            ExpressionTree.Node candidate = candidatesQueue.remove();
            String sentence = candidate.getSentence();

            if (alreadySeen.contains(sentence)) {
                continue;
            }
            alreadySeen.add(sentence);

            if (!ExpressionUtils.matchesIgnoreReferences(sentence, input, flags)) {
                continue;
            }

            if (ExpressionUtils.matches(sentence, input, flags)) {
                return candidate;
            }

            // generate new candidates from this candidate tree and add them to the queue
            // this is a level-based derivation (in contrast to depth-base derivation)
            for (ExpressionTree.Node node : generateCandidates(candidate, root)) {
                candidatesQueue.add(node);
            }
        }
        return null;
    }

    private Set<ExpressionTree.Node> generateCandidates(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<ExpressionTree.Node> candidates = new HashSet<ExpressionTree.Node>();

        for (Set<NodeCandidate> nodeCandidates : getCartesianProduct(node, root)) {
            candidates.add(copyNode(node, nodeCandidates));
        }
        return candidates;
    }

    private Set<Set<NodeCandidate>> getCartesianProduct(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<NodeCombinationsHolder> combinations = new HashSet<NodeCombinationsHolder>();

        if (node.isOrNode()) {
            combinations.add(new NodeCombinationsHolder(node, new HashSet<ExpressionTree.Node>(node.getSubNodes())));
        } else {
            combinations.addAll(generateCombinationsFromLeaves(node, root));
        }
        return generateCartesianProduct(combinations);
    }

    /**
     * Generates the Cartesian product for the node combinations.
     *
     * @return the set of sets of node candidates of the Cartesian product, each sub-set means a row in the matrix
     */
    static Set<Set<NodeCandidate>> generateCartesianProduct(Collection<NodeCombinationsHolder> combinations) {
        Set<Set<NodeCandidate>> cartesianProduct = new HashSet<Set<NodeCandidate>>();

        if (combinations.isEmpty()) {
            return cartesianProduct;
        }

        List<NodeCombinationsHolder> combinationList = new ArrayList<NodeCombinationsHolder>(combinations);

        NodeCombinationsHolder head = combinationList.get(0);

        if (combinations.size() > 1) {
            for (Set<NodeCandidate> tail : generateCartesianProduct(combinationList.subList(1, combinationList.size()))) {

                for (ExpressionTree.Node candidate : head.getCombinations()) {
                    Set<NodeCandidate> product = new HashSet<NodeCandidate>(tail);
                    product.add(new NodeCandidate(head.getNode(), candidate));

                    cartesianProduct.add(product);
                }
            }

        } else {
            for (ExpressionTree.Node candidate : head.getCombinations()) {
                Set<NodeCandidate> product = new HashSet<NodeCandidate>();
                product.add(new NodeCandidate(head.getNode(), candidate));

                cartesianProduct.add(product);
            }
        }
        return cartesianProduct;
    }

    private Set<NodeCombinationsHolder> generateCombinationsFromLeaves(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<NodeCombinationsHolder> combinations = new HashSet<NodeCombinationsHolder>();

        for (ExpressionTree.Node leaf : node.getLeaves()) {
            if (leaf.getExpression().isReference()) {
                combinations.add(new NodeCombinationsHolder(leaf, generateCombinations(leaf, root)));
            }
        }
        return combinations;
    }

    /**
     * Generates combination for the node - epsilon and references substitution.
     */
    Set<ExpressionTree.Node> generateCombinations(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<ExpressionTree.Node> combinations = new HashSet<ExpressionTree.Node>();

        if (node.isThisReference()) {
            if (node.getExpression().isQuantified()) {
                combinations.add(ExpressionTree.Node.parseNode(
                        "(" + root.getExpression().toWord() + ")" + node.getExpression().getQuantifier()));

            } else {
                if (root.isOrNode()) {
                    combinations.addAll(new HashSet<ExpressionTree.Node>(root.getSubNodes()));
                } else {
                    combinations.add(root);
                }
            }
        } else {
            if (node.getExpression().isReference()) {
                for (Rule rule : getNamedRules(node.getExpression().getText())) {
                    if (rule.getExpression().getRoot().isOrNode()) {
                        for (ExpressionTree.Node n : rule.getExpression().getRoot().getSubNodes()) {
                            combinations.add(toCombination(node, n.getExpression()));
                        }
                    } else {
                        combinations.add(toCombination(node, rule.getExpression().getRoot().getExpression()));
                    }
                }
            } else {
                if (node.isOrNode()) {
                    combinations.addAll(node.getSubNodes());
                } else {
                    combinations.add(node);
                }
            }
        }

        for (ExpressionTree.Node expression : combinations) {
            if (ExpressionUtils.matchesEpsilon(expression.getExpression().toWord())) {
                combinations.add(new ExpressionTree.Node(Expression.EPSILON));
                break;
            }
        }

        return combinations;
    }

    private static ExpressionTree.Node toCombination(ExpressionTree.Node leaf, Expression expression) {
        ExpressionTree.Node combination;

        if (!expression.isEpsilon()
            && leaf.getExpression().isQuantified()) {
            combination = ExpressionTree.Node.parseNode(
                    "(" + expression.toWord() + ")" + (leaf.getExpression().isQuantified() ? leaf.getExpression().getQuantifier() : ""));
        } else {
            combination = ExpressionTree.Node.parseNode(expression.toWord());
        }
        return combination;
    }

    private static ExpressionTree.Node copyNode(ExpressionTree.Node node, Set<NodeCandidate> nodeCandidates) {
        ExpressionTree.Node candidate = findCandidate(node, nodeCandidates);

        if (candidate != null) {
            return new ExpressionTree.Node(
                    node.getExpression(),
                    ExpressionTree.Node.SubNodesConnectionType.SINGLE,
                    Collections.singletonList(candidate));

        } else {
            List<ExpressionTree.Node> subNodes = new ArrayList<ExpressionTree.Node>();
            for (ExpressionTree.Node subNode : node.getSubNodes()) {
                subNodes.add(copyNode(subNode, nodeCandidates));
            }
            return new ExpressionTree.Node(
                    node.getExpression(), node.getSubNodesConnectionType(), subNodes);
        }
    }

    private static ExpressionTree.Node findCandidate(ExpressionTree.Node node, Set<NodeCandidate> nodeCandidates) {
        for (NodeCandidate candidate : nodeCandidates) {
            if (candidate.getNode().equals(node)) {
                return candidate.getCandidate();
            }
        }
        return null;
    }

    static RecexpGroup nodeToGroup(ExpressionTree.Node node, String input, int flags) {
        if (input.isEmpty()) {
            return new RecexpGroup(node.getExpression().toWord(), input, new RecexpGroup[0]);
        }

        if (node.isOrNode()) {
            for (ExpressionTree.Node subNode : node.getSubNodes()) {
                try {
                    return nodeToGroup(subNode, input, flags);

                } catch (IllegalStateException ignore) {
                    // continue
                }
            }
            throw new IllegalStateException("Cannot reduce: input '" + input + "' doesn't match the expression: " + node.toWord());
        }

        List<RecexpGroup> subGroups = new ArrayList<RecexpGroup>();

        String restInput = input;

        for (int i = 0; i < node.getSubNodes().size(); i++) {
            ExpressionTree.Node subNode = node.getSubNodes().get(i);

            if (restInput.isEmpty()) {
                subGroups.clear();
                break;
            }

            String value = getInputPartForNodeByLeftReduction(
                    restInput, flags, subNode, node.getSubNodes().subList(i + 1, node.getSubNodes().size()));

            if (value == null) {
                throw new IllegalStateException("Cannot reduce: input '" + input + "' doesn't match the expression: " + node.toWord());
            }
            subGroups.add(nodeToGroup(subNode, value, flags));

            restInput = restInput.substring(value.length());
        }

        RecexpGroup[] groups = new RecexpGroup[subGroups.size()];
        for (int i = 0; i < subGroups.size(); i++) {
            groups[i] = subGroups.get(i);
        }
        return new RecexpGroup(node.getExpression().toWord(), input, groups);
    }

    private static String getInputPartForNodeByLeftReduction(String input, int flags, ExpressionTree.Node node, List<ExpressionTree.Node> rightNodes) {
        String nodeSentence = node.getSentence();
        String rightNodesSentence = getNodesSentence(rightNodes);

        StringBuilder sb = new StringBuilder();

        for (int index = 0; index <= input.length(); index++) {

            String candidate = sb.toString();
            String restString = input.substring(candidate.length());

            if (ExpressionUtils.matches(nodeSentence, sb.toString(), flags)
                && ExpressionUtils.matches(rightNodesSentence, restString, flags)) {
                return sb.toString();
            }

            if (index < input.length()) {
                sb.append(input.charAt(index));
            }
        }
        return null;
    }

    private static String getNodesSentence(List<ExpressionTree.Node> nodes) {
        StringBuilder sb = new StringBuilder();

        for (ExpressionTree.Node node : nodes) {
            sb.append(node.getSentence());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.rules.toString();
    }

    /**
     * Builder for the {@link RecexpGrammar Recursive Grammar class}.
     */
    public static class RecexpGrammarBuilder {

        private final Set<Rule> ruleSet;
        private int flags;

        private RecexpGrammarBuilder() {
            this.ruleSet = new HashSet<Rule>();
            this.flags = 0;
        }

        /**
         * Adds a named rule.
         *
         * @param name       the name
         * @param expression the expression
         * @return the builder
         */
        public RecexpGrammarBuilder rule(String name, String expression) {
            this.ruleSet.add(new NamedRule(name, expression));
            return this;
        }

        /**
         * Adds a pure expression rule.
         *
         * @param expression the expression
         * @return the builder
         */
        public RecexpGrammarBuilder rule(String expression) {
            this.ruleSet.add(new Rule(expression, expression));
            return this;
        }

        /**
         * Sets the flags.
         *
         * @param flags the match flags, a bit mask that may include the flags from {@link java.util.regex.Pattern}
         * @return
         */
        public RecexpGrammarBuilder flags(int flags) {
            this.flags = flags;
            return this;
        }

        /**
         * Builds a grammar object.
         *
         * @return the grammar
         */
        public RecexpGrammar build() {
            if (this.ruleSet.isEmpty()) {
                throw new IllegalStateException("Rule set cannot be empty.");
            }
            RecexpGrammar grammar = new RecexpGrammar(this.ruleSet, this.flags);
            this.ruleSet.clear();
            return grammar;
        }
    }

    /**
     * Holder of possible expression combinations for a node.
     */
    static class NodeCombinationsHolder {

        final ExpressionTree.Node node;
        final Set<ExpressionTree.Node> combinations;

        public NodeCombinationsHolder(ExpressionTree.Node node, Set<ExpressionTree.Node> combinations) {
            this.node = node;
            this.combinations = combinations;
        }

        public ExpressionTree.Node getNode() {
            return node;
        }

        public Set<ExpressionTree.Node> getCombinations() {
            return combinations;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NodeCombinationsHolder that = (NodeCombinationsHolder) o;

            return node.equals(that.node);
        }

        @Override
        public int hashCode() {
            return node.hashCode();
        }
    }

    /**
     * Expression candidate for a node.
     */
    static class NodeCandidate {

        final ExpressionTree.Node node;
        final ExpressionTree.Node candidate;

        public NodeCandidate(ExpressionTree.Node node, ExpressionTree.Node candidate) {
            this.node = node;
            this.candidate = candidate;
        }

        public ExpressionTree.Node getNode() {
            return node;
        }

        public ExpressionTree.Node getCandidate() {
            return candidate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NodeCandidate candidate = (NodeCandidate) o;

            if (!node.equals(candidate.node)) {
                return false;
            }
            return this.candidate.equals(candidate.candidate);
        }

        @Override
        public int hashCode() {
            int result = node.hashCode();
            result = 31 * result + candidate.hashCode();
            return result;
        }
    }
}
