package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author ttulka
 */
public class RecexpGrammar {

    protected final Set<RecexpRule> rules = new HashSet<RecexpRule>();

    /**
     * Empty constructor.
     */
    public RecexpGrammar() {
    }

    /**
     * Rules constructor.
     *
     * @param rules the rules
     */
    public RecexpGrammar(String... rules) {
        if (rules != null && rules.length > 0) {
            for (String rule : rules) {
                this.rules.add(new RecexpRule(rule));
            }
        }
    }

    /**
     * Adds a rule.
     *
     * @param name       the rule name
     * @param expression the rule candidate
     * @return this grammar
     */
    public RecexpGrammar addRule(String name, String expression) {
        rules.add(new RecexpRule(name, expression));
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param expression the rule candidate
     * @return this grammar
     */
    public RecexpGrammar addRule(String expression) {
        rules.add(new RecexpRule(expression, expression));
        return this;
    }

    /**
     * Convenient method. See {@link RecexpMatcher#matches()}.
     *
     * @param input the input string
     * @return true if the grammar accepts the string, otherwise false
     */
    public boolean accepts(String input) {
        return matcher(input).matches();
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
        checkEmptyRules();
        checkCyclicRules();

        for (RecexpRule rule : rules) {

            RecexpGroup group = asGroup(rule.getExpression().getRoot(), input, new HashSet<String>());
            if (group != null) {
                return RecexpMatcher.matcher(group.name(), group.value(), group.groups());
            }
        }
        return RecexpMatcher.emptyMatcher(input);
    }

    /**
     * @throws RecexpEmptyRulesException when there are no rules
     */
    void checkEmptyRules() {
        if (rules.isEmpty()) {
            throw new RecexpEmptyRulesException();
        }
    }

    /**
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    void checkCyclicRules() {
        for (RecexpRule rule : rules) {
            if (!checkCyclicRules(rule, rule.getExpression().getRoot())) {
                throw new RecexpCyclicRuleException(rule.getName());
            }
        }
    }

    /**
     * @return true if the rule has no self-reference, otherwise false.
     */
    private boolean checkCyclicRules(RecexpRule rule, ExpressionTree.Node node) {
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
        for (RecexpRule rule : rules) {
            if (rule.getName().equals(ruleName)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a group for the candidate and input, or <code>null</code> if it doesn't match.
     */
    RecexpGroup asGroup(ExpressionTree.Node root, String input, Set<String> alreadySeen) {
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

            if (!matchesIgnoreReferences(sentence, input)) {
                continue;
            }

            if (matches(sentence, input)) {
                return reduceTree(candidate, input);
            }

            // generate new candidates from this candidate tree and add them to the queue
            for (ExpressionTree.Node node : generateCandidates(candidate, root)) {
                candidatesQueue.add(node);
            }
        }
        return null;
    }

    private boolean matchesIgnoreReferences(String expression, String input) {
        return Pattern.matches(ExpressionUtils.hydrateExpression(expression), input);
    }

    private boolean matches(String expression, String input) {
        if (expression.equals(input)) {
            return true;
        }
        // when matches for a substitution with X and Y too, it's obviously expendable
        return Pattern.matches(ExpressionUtils.hydrateExpression(expression, "X"), input) &&
               Pattern.matches(ExpressionUtils.hydrateExpression(expression, "Y"), input);
    }

    private Set<ExpressionTree.Node> generateCandidates(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<ExpressionTree.Node> candidates = new HashSet<ExpressionTree.Node>();

        for (Set<LeafCandidate> leafCandidates : getCartesianProduct(node, root)) {
            candidates.add(copyNode(node, leafCandidates));
        }
        return candidates;
    }

    private Set<Set<LeafCandidate>> getCartesianProduct(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<LeafCombination> combinations = new HashSet<LeafCombination>();

        if (node.isOrNode()) {
            combinations.add(new LeafCombination(node, new HashSet<ExpressionTree.Node>(node.getSubNodes())));
        } else {
            combinations.addAll(generateCombinationsFromLeaves(node, root));
        }
        return getCartesianProduct(combinations);
    }

    /**
     * Generates the Cartesian product for the node combinations.
     */
    Set<Set<LeafCandidate>> getCartesianProduct(Collection<LeafCombination> combinations) {
        Set<Set<LeafCandidate>> cartesianProduct = new HashSet<Set<LeafCandidate>>();

        if (combinations.isEmpty()) {
            return cartesianProduct;
        }

        List<LeafCombination> combinationList = new ArrayList<LeafCombination>(combinations);

        LeafCombination head = combinationList.get(0);

        if (combinations.size() > 1) {
            for (Set<LeafCandidate> tail : getCartesianProduct(combinationList.subList(1, combinationList.size()))) {

                for (ExpressionTree.Node candidate : head.getCombinations()) {
                    Set<LeafCandidate> product = new HashSet<LeafCandidate>(tail);
                    product.add(new LeafCandidate(head.getNode(), candidate));

                    cartesianProduct.add(product);
                }
            }

        } else {
            for (ExpressionTree.Node candidate : head.getCombinations()) {
                Set<LeafCandidate> product = new HashSet<LeafCandidate>();
                product.add(new LeafCandidate(head.getNode(), candidate));

                cartesianProduct.add(product);
            }
        }
        return cartesianProduct;
    }

    private Set<LeafCombination> generateCombinationsFromLeaves(ExpressionTree.Node node, ExpressionTree.Node root) {
        Set<LeafCombination> combinations = new HashSet<LeafCombination>();

        for (ExpressionTree.Node leaf : node.getLeaves()) {
            if (leaf.getExpression().isReference()) {
                combinations.add(new LeafCombination(leaf, generateCombinations(leaf, root)));
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
                for (RecexpRule rule : rules) {
                    if (rule.getName().equals(node.getExpression().getText())) {
                        if (rule.getExpression().getRoot().isOrNode()) {
                            for (ExpressionTree.Node n : rule.getExpression().getRoot().getSubNodes()) {
                                combinations.add(toCombination(node, n.getExpression()));
                            }
                        } else {
                            combinations.add(toCombination(node, rule.getExpression().getRoot().getExpression()));
                        }
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

    private ExpressionTree.Node toCombination(ExpressionTree.Node leaf, Expression expression) {
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

    private ExpressionTree.Node copyNode(ExpressionTree.Node node, Set<LeafCandidate> leafCandidates) {
        ExpressionTree.Node candidate = findCandidate(node, leafCandidates);

        if (candidate != null) {
            return new ExpressionTree.Node(
                    node.getExpression(),
                    ExpressionTree.Node.SubNodesConnectionType.SINGLE,
                    Collections.singletonList(candidate));

        } else {
            List<ExpressionTree.Node> subNodes = new ArrayList<ExpressionTree.Node>();
            for (ExpressionTree.Node subNode : node.getSubNodes()) {
                subNodes.add(copyNode(subNode, leafCandidates));
            }
            return new ExpressionTree.Node(
                    node.getExpression(), node.getSubNodesConnectionType(), subNodes);
        }
    }

    private ExpressionTree.Node findCandidate(ExpressionTree.Node node, Set<LeafCandidate> leafCandidates) {
        for (LeafCandidate candidate : leafCandidates) {
            if (candidate.getNode().equals(node)) {
                return candidate.getCandidate();
            }
        }
        return null;
    }

    /**
     * Reduces the tree to a group.
     */
    RecexpGroup reduceTree(ExpressionTree.Node root, String input) {
        return nodeToGroup(root, input);
    }

    private RecexpGroup nodeToGroup(ExpressionTree.Node node, String input) {
        if (input.isEmpty()) {
            return new RecexpGroup(node.getExpression().toWord(), input, new RecexpGroup[0]);
        }

        if (node.isOrNode()) {
            for (ExpressionTree.Node subNode : node.getSubNodes()) {
                try {
                    return nodeToGroup(subNode, input);

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

            String value = getInputPartForNodeByLeftReduction(restInput, subNode, node.getSubNodes().subList(i + 1, node.getSubNodes().size()));
            subGroups.add(nodeToGroup(subNode, value));

            restInput = restInput.substring(value.length());
        }

        RecexpGroup[] groups = new RecexpGroup[subGroups.size()];
        for (int i = 0; i < subGroups.size(); i++) {
            groups[i] = subGroups.get(i);
        }
        return new RecexpGroup(node.getExpression().toWord(), input, groups);
    }

    private String getInputPartForNodeByLeftReduction(String input, ExpressionTree.Node node, List<ExpressionTree.Node> rightNodes) {
        String nodeSentence = node.getSentence();
        String rightNodesSentence = getNodesSentence(rightNodes);

        StringBuilder sb = new StringBuilder();

        for (int index = 0; index <= input.length(); index++) {

            String candidate = sb.toString();
            String restString = input.substring(candidate.length());

            if (matches(nodeSentence, sb.toString())
                && matches(rightNodesSentence, restString)) {
                return sb.toString();
            }

            if (index < input.length()) {
                sb.append(input.charAt(index));
            }
        }
        // TODO move the exception throw a level up
        throw new IllegalStateException("Cannot reduce: input '" + input + "' doesn't match the expression: " + node.toWord() + rightNodesSentence);
    }

    private String getNodesSentence(List<ExpressionTree.Node> nodes) {
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
     * Expression combination for a node.
     */
    class LeafCombination {

        final ExpressionTree.Node node;
        final Set<ExpressionTree.Node> combinations;

        public LeafCombination(ExpressionTree.Node node, Set<ExpressionTree.Node> combinations) {
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

            LeafCombination that = (LeafCombination) o;

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
    class LeafCandidate {

        final ExpressionTree.Node node;
        final ExpressionTree.Node candidate;

        public LeafCandidate(ExpressionTree.Node node, ExpressionTree.Node candidate) {
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

            LeafCandidate candidate = (LeafCandidate) o;

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
