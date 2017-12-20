package cz.net21.ttulka.recexp;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author ttulka
 */
public class RecexpGrammar {

    final Map<String, Set<String>> buildingRules = new HashMap<String, Set<String>>();
    final Set<RecexpRule> rules = new HashSet<RecexpRule>();

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
                Set<String> values = buildingRules.get(rule);
                if (values == null) {
                    values = new HashSet<String>();
                }
                values.add(rule);

                buildingRules.put(rule, values);
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
        Set<String> values = buildingRules.get(name);
        if (values == null) {
            values = new HashSet<String>();
        }
        values.add(expression);
        buildingRules.put(name, values);
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param expression the rule candidate
     * @return this grammar
     */
    public RecexpGrammar addRule(String expression) {
        Set<String> values = buildingRules.get(expression);
        if (values == null) {
            values = new HashSet<String>();
        }
        values.add(expression);
        this.buildingRules.put(expression, values);
        return this;
    }

    /**
     * Convenient method. See {@link RecexpMatcher#matches()}.
     *
     * @param input the input string
     * @return true if the grammar accepts the string, otherwise false
     */
    public boolean accepts(String input) {
        return this.matcher(input).matches();
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
        buildRules();

        for (RecexpRule rule : this.rules) {

            RecexpGroup group = asGroup(rule.getExpression(), input, new HashSet<String>());
            if (group != null) {
                return RecexpMatcher.matcher(group.name(), group.value(), group.groups());
            }
        }
        return RecexpMatcher.emptyMatcher(input);
    }

    /**
     * @throws RecexpEmptyRulesException when there are no rules
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    void buildRules() {
        if (buildingRules.isEmpty()) {
            throw new RecexpEmptyRulesException();
        }
        for (Map.Entry<String, Set<String>> ruleEntry : buildingRules.entrySet()) {
            for (String rule : ruleEntry.getValue()) {
                rules.add(new RecexpRule(ruleEntry.getKey(), rule));
            }
        }
    }

    /**
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    void checkCyclicRules() {
        for (RecexpRule rule : this.rules) {

            if (!checkCyclicRules(rule, rule.getExpression())) {
                throw new RecexpCyclicRuleException(rule.getName());
            }
        }
    }

    /**
     * @return true if the rule has no self-reference, otherwise false.
     */
    private boolean checkCyclicRules(RecexpRule rule, ExpressionTree tree) {
        for (ExpressionTree candidate : generateCandidates(tree)) {

            for (ExpressionTree.Node leaf : candidate.getLeaves()) {
                Expression expression = leaf.getExpression();

                if (Pattern.matches("", leaf.toWord())) {
                    continue;
                }

                if (rule.getExpression().getRoot().getExpression().equals(leaf.getExpression())) {
                    return false;
                }

                if (expression.isReference()) {
                    if (Expression.THIS_REFERENCE_NAME.equals(leaf.getExpression().getText())) {
                        return false;
                    }
                    if (rule.getName().equals(leaf.getExpression().getText())) {
                        return false;
                    }
                }

                if (!checkCyclicRules(rule, candidate)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a group for the candidate and input, or <code>null</code> if it doesn't match.
     */
    RecexpGroup asGroup(ExpressionTree tree, String input, Set<String> alreadySeen) {
        String sentence = tree.getSentence();

        if (alreadySeen.contains(sentence)) {
            return null;
        }
        alreadySeen.add(sentence);

        String hydratedSentence = ExpressionUtils.hydrateExpression(sentence);

        if (!Pattern.matches(hydratedSentence, input)) {
            return null;
        }

        // no more references
        if (hydratedSentence.equals(sentence)) {
            return reduceTree(tree, input);
        }

        for (ExpressionTree candidate : generateCandidates(tree)) {
            RecexpGroup group = asGroup(candidate, input, alreadySeen);
            if (group != null) {
                return group;
            }
        }
        return null;
    }

    private Set<ExpressionTree> generateCandidates(ExpressionTree tree) {
        Set<ExpressionTree> candidates = new HashSet<ExpressionTree>();

        for (Set<LeafCandidate> leafCandidates : getCartesianProduct(tree)) {
            candidates.add(extendTree(tree, leafCandidates));
        }
        return candidates;
    }

    private Set<Set<LeafCandidate>> getCartesianProduct(ExpressionTree tree) {
        Set<LeafCombination> combinations = generateCombinations(tree);
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

    private Set<LeafCombination> generateCombinations(ExpressionTree tree) {
        Set<LeafCombination> combinations = new HashSet<LeafCombination>();

        for (ExpressionTree.Node leaf : tree.getLeaves()) {
            if (leaf.getExpression().isReference()) {
                combinations.add(new LeafCombination(leaf, generateCombinations(leaf, tree.getRoot())));
            }
        }
        return combinations;
    }

    /**
     * Generates combination for the node - epsilon and references substitution.
     */
    Set<ExpressionTree.Node> generateCombinations(ExpressionTree.Node leaf, ExpressionTree.Node root) {
        Set<ExpressionTree.Node> combinations = new HashSet<ExpressionTree.Node>();

        // this
        if (leaf.getExpression().isReference()
            && Expression.THIS_REFERENCE_NAME.equals(leaf.getExpression().getText())) {

            if (leaf.getExpression().isQuantified()) {
                combinations.add(ExpressionTree.Node.parseNode("(" + root.toWord() + ")" + leaf.getExpression().getQuantifier()));

            } else {
                combinations.add(root);
            }

        } else {
            if (!leaf.getExpression().isReference()) {
                combinations.add(leaf);

            } else {
                for (RecexpRule rule : rules) {
                    if (rule.getName().equals(leaf.getExpression().getText())) {
                        combinations.add(toCombination(leaf, rule.getExpression().getRoot().getExpression()));
                    }
                }
            }
        }

        for (ExpressionTree.Node expression : combinations) {
            if (ExpressionUtils.containsEpsilon(expression.getExpression().toWord())) {
                combinations.add(new ExpressionTree.Node(Expression.EPSILON));
                break;
            }
        }

        return combinations;
    }

    private ExpressionTree.Node toCombination(ExpressionTree.Node leaf, Expression expression) {
        ExpressionTree.Node combination;

        if (!expression.isEpsilon()
            && (leaf.isClosedInBrackets() || leaf.getExpression().isQuantified())) {
            combination = ExpressionTree.Node.parseNode(
                    expression.toWord() + (leaf.getExpression().isQuantified() ? leaf.getExpression().getQuantifier() : ""));
        } else {
            combination = new ExpressionTree.Node(
                    new Expression(expression.getText(), expression.getQuantifier(), expression.isReference()));
        }
        return combination;
    }

    /**
     * Expands the tree's end leaves by the node candidates.
     */
    ExpressionTree extendTree(ExpressionTree tree, Set<LeafCandidate> leafCandidates) {
        ExpressionTree.Node expandedRoot = copyNode(tree.getRoot(), leafCandidates);
        return new ExpressionTree(expandedRoot);
    }

    private ExpressionTree.Node copyNode(ExpressionTree.Node node, Set<LeafCandidate> leafCandidates) {
        ExpressionTree.Node copy = new ExpressionTree.Node(
                node.getExpression(), node.isClosedInBrackets()
        );

        ExpressionTree.Node candidate = findCandidate(node, leafCandidates);

        if (candidate != null) {
            copy.getNodes().add(candidate);

        } else {
            for (ExpressionTree.Node subNode : node.getNodes()) {
                copy.getNodes().add(copyNode(subNode, leafCandidates));
            }
        }
        return copy;
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
    RecexpGroup reduceTree(ExpressionTree tree, String input) {
        return nodeToGroup(tree.getRoot(), input);
    }

    private RecexpGroup nodeToGroup(ExpressionTree.Node node, String input) {
        if (input.isEmpty()) {
            return new RecexpGroup(node.getExpression().toWord(), input, new RecexpGroup[0]);
        }

        List<RecexpGroup> subGroups = new ArrayList<RecexpGroup>();

        String restInput = input;

        for (int i = 0; i < node.getNodes().size(); i++) {
            ExpressionTree.Node subNode = node.getNodes().get(i);

            String value = getInputPartForNodeByLeftReduction(restInput, subNode, node.getNodes().subList(i + 1, node.getNodes().size()));
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

            if (Pattern.matches(nodeSentence, sb.toString())
                && Pattern.matches(rightNodesSentence, restString)) {
                return sb.toString();
            }

            if (index < input.length()) {
                sb.append(input.charAt(index));
            }
        }
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
