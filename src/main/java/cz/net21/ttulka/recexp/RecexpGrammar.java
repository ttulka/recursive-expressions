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
        return new ExpressionTree(createLeaf(expression));
    }

    private ExpressionTree.Leaf createLeaf(String expression) {
        String quantifier = getQuantifier(expression);
        if (quantifier != null && !quantifier.isEmpty()) {
            expression = expression.substring(0, expression.length() - quantifier.length());
        }

        // TODO get the quantifier even if it is in the brackets
        boolean isClosedInBrackets = isClosedInBrackets(expression, true);
        if (isClosedInBrackets) {
            expression = removeClosingBrackets(expression);
        }

        boolean isReference = isReference(expression);
        if (isReference) {
            expression = removeReference(expression);
        }

        ExpressionTree.Leaf leaf = new ExpressionTree.Leaf(expression, quantifier, isReference, isClosedInBrackets);

        List<String> expressionParts = new ArrayList<String>();

        for (String bracketGroup : getExpressionPartsCutByBrackets(expression)) {
            for (String referenceGroup : getExpressionPartsCutByReferences(bracketGroup)) {
                expressionParts.add(referenceGroup);
            }
        }

        if (expressionParts.size() > 1 || !expressionParts.get(0).equals(expression)) {
            for (String part : expressionParts) {
                leaf.getLeaves().add(createLeaf(part));
            }

        } else {
            if (isClosedInBrackets(expression, true)) {
                leaf.getLeaves().add(createLeaf(expression));
            }
        }

        return leaf;
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

    private List<String> getExpressionPartsCutByBrackets(String expression) {
        List<String> parts = new ArrayList<String>();

        StringBuilder sb = new StringBuilder(expression.length());
        char previous = '\0';
        int bracketsLevel = 0;
        int lastOpeningBracketIndex = -1;

        int index = 0;
        while (index < expression.length()) {
            char ch = expression.charAt(index);
            sb.append(ch);

            if (ch == '(' && previous != '\\') {
                if (sb.length() > 1 && bracketsLevel == 0) {
                    parts.add(sb.toString().substring(0, sb.length() - 1));
                    sb = new StringBuilder(expression.length() - index).append('(');
                }

                bracketsLevel++;
                lastOpeningBracketIndex = index;

            } else if (ch == ')' && previous != '\\') {
                if (bracketsLevel > 0) {
                    bracketsLevel--;

                    if (bracketsLevel == 0) {
                        String exp = sb.toString();

                        // has this part a quantifier?
                        String rest = expression.substring(index + 1);
                        Matcher matcher = Pattern.compile(REGEXP_QUANTIFIER + "(.*)").matcher(rest);
                        if (matcher.matches()) {
                            exp += matcher.group(1);
                            index += matcher.group(1).length();
                        }

                        parts.add(exp);
                        sb = new StringBuilder(expression.length() - index);
                    }

                } else {
                    throw new RecexpSyntaxException("Unmatched closing ')' near index " + index + "\n" + expression);
                }
            }

            previous = ch;
            index++;
        }

        if (bracketsLevel > 0) {
            throw new RecexpSyntaxException("Unmatched opening '(' near index " + lastOpeningBracketIndex + "\n" + expression);
        }

        if (sb.length() > 0) {
            parts.add(sb.toString());
        }
        return parts;
    }

    private List<String> getExpressionPartsCutByReferences(String expression) {
        if (isClosedInBrackets(expression, true)) {
            return Collections.singletonList(expression);
        }

        Matcher matcher = Pattern.compile(REGEXP_REFERENCE + REGEXP_QUANTIFIER + "?").matcher(expression);

        List<String> parts = new ArrayList<String>();

        expression = replaceEscapedReference(expression);

        int restStarts = 0;

        while (matcher.find()) {
            if (matcher.start() > restStarts) {
                parts.add(resetEscapedReference(
                        expression.substring(restStarts, matcher.start())
                ));
            }
            restStarts = matcher.end();
            parts.add(resetEscapedReference(
                    expression.substring(matcher.start(), matcher.end())
            ));
        }
        if (restStarts < expression.length()) {
            parts.add(resetEscapedReference(
                    expression.substring(restStarts)
            ));
        }
        return parts;
    }

    boolean isClosedInBrackets(String expression, boolean acceptQuantified) {
        if (acceptQuantified && isQuantified(expression)) {
            expression = expression.substring(0, expression.length() - getQuantifier(expression).length());
        }
        if (!expression.startsWith("(") || !expression.endsWith(")")) {
            return false;
        }
        int openBrackets = 0;
        char previous = '\0';

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == '(' && previous != '\\' && i < expression.length()) {
                openBrackets++;

            } else if (ch == ')' && previous != '\\' && i > 0) {
                openBrackets--;

                if (openBrackets == 0 && i < expression.length() - 1) {
                    return false;
                }
            }
            previous = ch;
        }
        return openBrackets == 0;
    }

    private String removeClosingBrackets(String expression) {
        if (isClosedInBrackets(expression, false)) {
            return expression.substring(1, expression.length() - 1);
        }
        return expression;
    }

    boolean isReference(String expression) {
        return Pattern.matches(REGEXP_REFERENCE, expression);
    }

    private String removeReference(String expression) {
        if (isReference(expression)) {
            return expression.substring(1);
        }
        return expression;
    }

    private boolean isQuantifier(String expression) {
        return Pattern.matches(REGEXP_QUANTIFIER, expression);
    }

    private boolean isQuantified(String expression) {
        return Pattern.matches("(.+)" + REGEXP_QUANTIFIER, expression);
    }

    private String getQuantifier(String expression) {
        Matcher matcher = Pattern.compile("(.+)" + REGEXP_QUANTIFIER).matcher(expression);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        return null;
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
            return getSentence(root, new StringBuilder()).toString();
        }

        private StringBuilder getSentence(Leaf leaf, StringBuilder sb) {
            if (leaf.getLeaves().isEmpty()) {
                sb.append(leaf.getWord());

            } else {
                if (leaf.isQuantified() || leaf.isClosedInBrackets()) {
                    sb.append("(");
                }

                for (Leaf l : leaf.getLeaves()) {
                    sb = getSentence(l, sb);
                }

                if (leaf.isQuantified() || leaf.isClosedInBrackets()) {
                    sb.append(")");
                }
                if (leaf.isQuantified()) {
                    sb.append(leaf.getQuantifier());
                }
            }
            return sb;
        }

        public List<Leaf> getEndLeaves() {
            return root.getEndLeaves();
        }

        public static class Leaf {

            private final String expression;
            private final String quantifier;
            private final boolean reference;
            private final boolean closedInBrackets;

            private final List<Leaf> leaves = new ArrayList<Leaf>();

            public Leaf(String expression, String quantifier, boolean reference, boolean closedInBrackets) {
                this.expression = expression;
                this.quantifier = quantifier;
                this.reference = reference;
                this.closedInBrackets = closedInBrackets;
            }

            public Leaf(String expression, String quantifier, boolean reference) {
                this(expression, quantifier, reference, false);
            }

            public String getExpression() {
                return expression;
            }

            public String getQuantifier() {
                return quantifier;
            }

            public boolean isClosedInBrackets() {
                return closedInBrackets;
            }

            public boolean isQuantified() {
                return quantifier != null && !quantifier.isEmpty();
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

            @Override
            public String toString() {
                return "Leaf{" +
                       "expression='" + expression + '\'' +
                       ", quantifier='" + quantifier + '\'' +
                       ", reference=" + reference +
                       ", leaves=" + leaves +
                       '}';
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
