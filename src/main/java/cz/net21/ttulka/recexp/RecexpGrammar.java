package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author ttulka
 */
public class RecexpGrammar {

    public static final char REFERENCE_PREFIX = '@';
    public static final String THIS_REFERENCE_NAME = "this";
    public static final String EPSILON = "";

    private static final String REGEXP_REFERENCE = REFERENCE_PREFIX + "((\\w)+)";
    private static final String REGEXP_QUANTIFIER = "(([?*+]|\\{\\d+,?\\d*})[?+]?)";

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
     * Adds a rule .
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
     * @param expression the rule expression
     * @return this grammar
     */
    public RecexpGrammar addRule(String name, String expression) {
        this.rules.add(new RecexpRule(name, expression));
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param expression the rule expression
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

            List<RecexpGroup> groups = getGroups(createTree(rule.getExpression()), input, new HashSet<String>());
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
     * Returns groups for the expression and input, or <code>null</code> if it doesn't match.
     */
    List<RecexpGroup> getGroups(ExpressionTree tree, String input, Set<String> alreadySeen) {
        String sentence = tree.getSentence();

        if (alreadySeen.contains(sentence)) {
            return null;
        }
        alreadySeen.add(sentence);

        String hydratedSentence = hydrateExpression(sentence);

        if (!Pattern.matches(hydratedSentence, input)) {
            return null;
        }

        // no more references
        if (hydratedSentence.equals(sentence)) {
            return reduceTree(tree);
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
        Set<LeafCombination> combinations = generateCombinations(tree.getEndLeaves());
        return getCartesianProduct(combinations);
    }

    /**
     * Generates the Cartesian product for the leaf combinations.
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

                for (String candidate : head.getCombinations()) {
                    Set<LeafCandidate> product = new HashSet<LeafCandidate>(tail);
                    product.add(new LeafCandidate(head.getLeaf(), candidate));

                    cartesianProduct.add(product);
                }
            }

        } else {
            for (String candidate : head.getCombinations()) {
                Set<LeafCandidate> product = new HashSet<LeafCandidate>();
                product.add(new LeafCandidate(head.getLeaf(), candidate));

                cartesianProduct.add(product);
            }
        }
        return cartesianProduct;
    }

    private Set<LeafCombination> generateCombinations(List<ExpressionTree.Leaf> leaves) {
        Set<LeafCombination> combinations = new HashSet<LeafCombination>();

        for (ExpressionTree.Leaf leaf : leaves) {
            if (leaf.isReference()) {
                combinations.add(new LeafCombination(leaf, generateCombinations(leaf)));
            }
        }
        return combinations;
    }

    /**
     * Generates combination for the leaf - epsilon and references substitution.
     */
    Set<String> generateCombinations(ExpressionTree.Leaf leaf) {
        Set<String> combinations = new HashSet<String>();

        String word = leaf.getWord();

        if (hasEpsilon(word)) {
            combinations.add(EPSILON);
        }

        if (!leaf.isReference()) {
            combinations.add(word);

        } else {
            for (RecexpRule rule : rules) {
                if (rule.getName().equals(leaf.getExpression())) {
                    word = rule.getExpression();
                    if (!word.isEmpty()) {
                        word = "(" + word + ")";
                    }
                    combinations.add(word);

                    if (hasEpsilon(word)) {
                        combinations.add(EPSILON);
                    }
                }
            }
        }
        return combinations;
    }

    private boolean hasEpsilon(String expression) {
        return Pattern.matches(expression, EPSILON);
    }

    /**
     * Creates a tree from an expression.
     */
    ExpressionTree createTree(String expression) {
        return null; // TODO
    }

    /**
     * Normalize the tree to the normal form (every end leaf is either a simple reference or a terminal containing no reference whatsoever).
     */
    ExpressionTree normalizeTree(ExpressionTree tree) {
        return null; // TODO
    }

    /**
     * Expands the tree's end leaves by the leaf candidates.
     */
    ExpressionTree extendTree(ExpressionTree tree, Set<LeafCandidate> leafCandidates) {
        return null; // TODO
    }

    /**
     * Reduces the tree to groups.
     */
    List<RecexpGroup> reduceTree(ExpressionTree tree) {
        return null; // TODO
    }

    /**
     * Replaces references with (.*).
     */
    String hydrateExpression(String expression) {
        return resetEscapedReference(
                replaceEscapedReference(expression).replaceAll(REGEXP_REFERENCE, "(.*)")
        );
    }

    private String replaceEscapedReference(String expression) {
        return expression.replaceAll("\\\\" + REGEXP_REFERENCE, "\\\\__RecexpRefPrefix__$1");
    }

    private String resetEscapedReference(String expression) {
        return expression.replaceAll("\\\\__RecexpRefPrefix__", "\\\\" + REFERENCE_PREFIX);
    }

    @Override
    public String toString() {
        return this.rules.toString();
    }

    /**
     * Tree representation of an expression.
     */
    static class ExpressionTree {

        private final Leaf root;

        public ExpressionTree(Leaf root) {
            this.root = root;
        }

        public Leaf getRoot() {
            return root;
        }

        public String getSentence() {
            StringBuilder sb = new StringBuilder();

            for (Leaf leaf : getEndLeaves()) {
                sb.append(leaf.getWord());
            }
            return sb.toString();
        }

        public List<Leaf> getEndLeaves() {
            return root.getEndLeaves();
        }

        public static class Leaf {

            private final String expression;
            private final String quantifier;
            private final boolean reference;

            private final List<Leaf> leaves = new ArrayList<Leaf>();

            public Leaf(String expression, String quantifier, boolean reference) {
                this.expression = expression;
                this.quantifier = quantifier;
                this.reference = reference;
            }

            public String getExpression() {
                return expression;
            }

            public String getQuantifier() {
                return quantifier;
            }

            public boolean isReference() {
                return reference;
            }

            public boolean isEpsilon() {
                return expression == null || expression.isEmpty();
            }

            public List<Leaf> getLeaves() {
                return leaves;
            }

            public String getWord() {
                if (isEpsilon()) {
                    return EPSILON;
                }
                StringBuilder sb = new StringBuilder()
                        .append("(");

                if (isReference()) {
                    sb.append(REFERENCE_PREFIX);
                }
                sb.append(getExpression())
                        .append(")");

                if (getQuantifier() != null) {
                    sb.append(getQuantifier());
                }
                return sb.toString();
            }

            List<Leaf> getEndLeaves() {
                return getEndLeaves(this);
            }

            private List<Leaf> getEndLeaves(Leaf leaf) {
                List<Leaf> leaves = new ArrayList<Leaf>();

                if (leaf.getLeaves().isEmpty()) {
                    if (!leaf.isEpsilon()) {
                        leaves.add(leaf);
                    }

                } else {
                    for (Leaf l : leaf.getLeaves()) {
                        leaves.addAll(getEndLeaves(l));
                    }
                }
                return leaves;
            }
        }
    }

    /**
     * Expression combination for a leaf.
     */
    class LeafCombination {

        final ExpressionTree.Leaf leaf;
        final Set<String> combinations;

        public LeafCombination(ExpressionTree.Leaf leaf, Set<String> combinations) {
            this.leaf = leaf;
            this.combinations = combinations;
        }

        public ExpressionTree.Leaf getLeaf() {
            return leaf;
        }

        public Set<String> getCombinations() {
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

            return leaf.equals(that.leaf);
        }

        @Override
        public int hashCode() {
            return leaf.hashCode();
        }
    }

    /**
     * Expression candidate for a leaf.
     */
    class LeafCandidate {

        final ExpressionTree.Leaf leaf;
        final String expression;

        public LeafCandidate(ExpressionTree.Leaf leaf, String expression) {
            this.leaf = leaf;
            this.expression = expression;
        }

        public ExpressionTree.Leaf getLeaf() {
            return leaf;
        }

        public String getExpression() {
            return expression;
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

            if (!leaf.equals(candidate.leaf)) {
                return false;
            }
            return expression.equals(candidate.expression);
        }

        @Override
        public int hashCode() {
            int result = leaf.hashCode();
            result = 31 * result + expression.hashCode();
            return result;
        }
    }
}
