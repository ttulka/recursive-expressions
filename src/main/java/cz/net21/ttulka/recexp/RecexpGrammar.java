package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
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
    public RecexpGrammar(RecexpRule... rules) {
        if (rules != null && rules.length > 0) {
            this.rules.addAll(Arrays.asList(rules));
        }
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
     * @param rule the rule
     * @return this grammar
     */
    public RecexpGrammar addRule(RecexpRule rule) {
        this.rules.add(rule);
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param name       the rule name
     * @param expression the rule candidate
     * @return this grammar
     */
    public RecexpGrammar addRule(String name, String expression) {
        this.rules.add(new RecexpRule(name, expression));
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param expression the rule candidate
     * @return this grammar
     */
    public RecexpGrammar addRule(String expression) {
        this.rules.add(new RecexpRule(expression));
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
     * @throws RecexpEmptyRulesException when this grammar has no rules
     * @throws RecexpCyclicRuleException when there is a cyclic rule
     */
    public RecexpMatcher matcher(String input) {
        if (this.rules.isEmpty()) {
            throw new RecexpEmptyRulesException("Input: '" + input + "'.");
        }

        for (RecexpRule rule : this.rules) {

            List<RecexpGroup> groups = getGroups(rule.getExpression(), input, new HashSet<String>());
            if (groups != null && !groups.isEmpty()) {
                return new RecexpMatcher(rule.getName(), input, groups.toArray(new RecexpGroup[0]));
            }
        }
        return emptyMatcher(input);
    }

    private RecexpMatcher emptyMatcher(String input) {
        return new RecexpMatcher(null, input, new RecexpGroup[0]) {
            @Override
            public boolean matches() {
                return false;
            }
        };
    }

    /**
     * Returns groups for the candidate and input, or <code>null</code> if it doesn't match.
     */
    List<RecexpGroup> getGroups(ExpressionTree tree, String input, Set<String> alreadySeen) {
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
            List<RecexpGroup> groups = getGroups(normalizeTree(candidate), input, alreadySeen);
            if (groups != null) {
                return groups;
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
            combinations.add(root);

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
     * Normalize the tree to the normal form (every end node is either a simple reference or a terminal containing no reference whatsoever).
     */
    private ExpressionTree normalizeTree(ExpressionTree tree) {
        return ExpressionTree.parseTree(tree.getSentence());
    }

    /**
     * Expands the tree's end leaves by the node candidates.
     */
    ExpressionTree extendTree(ExpressionTree tree, Set<LeafCandidate> leafCandidates) {
        ExpressionTree.Node expandedRoot = copyNode(tree.getRoot(), leafCandidates);
        return new ExpressionTree(expandedRoot);
    }

    private ExpressionTree.Node copyNode(ExpressionTree.Node node, Set<LeafCandidate> leafCandidates) {
        ExpressionTree.Node copy;

        ExpressionTree.Node candidate = findCandidate(node, leafCandidates);

        if (candidate != null) {
            copy = candidate;
        } else {
            copy = new ExpressionTree.Node(
                    node.getExpression(), node.isClosedInBrackets()
            );
        }

        for (ExpressionTree.Node subNode : node.getNodes()) {
            copy.getNodes().add(copyNode(subNode, leafCandidates));
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
     * Reduces the tree to groups.
     */
    List<RecexpGroup> reduceTree(ExpressionTree tree, String input) {
        return Collections.singletonList(nodeToGroup(tree.getRoot(), input));
    }

    private RecexpGroup nodeToGroup(ExpressionTree.Node node, String input) {
        List<RecexpGroup> subGroups = new ArrayList<RecexpGroup>();

        Matcher matcher = Pattern.compile(node.toWord()).matcher(input);
        matcher.matches();

        for (int i = 0; i < node.getNodes().size(); i ++) {
            String value = matcher.group(i + 1);
            subGroups.add(nodeToGroup(node.getNodes().get(i), value));
        }

        RecexpGroup[] groups = new RecexpGroup[subGroups.size()];
        for (int i = 0; i < subGroups.size(); i++) {
            groups[i] = subGroups.get(i);
        }
        return new RecexpGroup(node.getExpression().toWord(), input, groups);
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
