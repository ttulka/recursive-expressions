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

    public static final String THIS_REFERENCE_NAME = "this";

    public static final char REFERENCE_PREFIX = '@';

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
            List<RecexpGroup> groups = getGroups(candidate, input, alreadySeen);
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
        Set<LeafCombination> combinations = generateCombinations(tree.getLeaves());
        return getCartesianProduct(combinations);
    }

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

    private Set<LeafCombination> generateCombinations(Iterable<ExpressionTree.Leaf> leaves) {
        Set<LeafCombination> combinations = new HashSet<LeafCombination>();

        for (ExpressionTree.Leaf leaf : leaves) {
            if (leaf.isReference()) {
                combinations.add(new LeafCombination(leaf, generateCombinations(leaf)));
            }
        }
        return combinations;
    }

    Set<String> generateCombinations(ExpressionTree.Leaf leaf) {
        return null; // TODO
    }

    ExpressionTree createTree(String expression) {
        return null; // TODO
    }

    ExpressionTree extendTree(ExpressionTree tree, Set<LeafCandidate> leafCandidates) {
        return null; // TODO
    }

    List<RecexpGroup> reduceTree(ExpressionTree tree) {
        return null; // TODO
    }

    // replaces references with (.*)
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

    class ExpressionTree {

        private Leaf root;

        public String getSentence() {
            return null; // TODO
        }

        public Set<Leaf> getLeaves() {
            return null; // TODO
        }

        public class Leaf {

            public boolean isReference() {
                return false; // TODO
            }
        }
    }
}
