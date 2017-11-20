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
        Set<LeafCombination> combinations = generateCombinations(tree.getLeaves());
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

                for (String candidate : head.getCombinations()) {
                    Set<LeafCandidate> product = new HashSet<LeafCandidate>(tail);
                    product.add(new LeafCandidate(head.getNode(), candidate));

                    cartesianProduct.add(product);
                }
            }

        } else {
            for (String candidate : head.getCombinations()) {
                Set<LeafCandidate> product = new HashSet<LeafCandidate>();
                product.add(new LeafCandidate(head.getNode(), candidate));

                cartesianProduct.add(product);
            }
        }
        return cartesianProduct;
    }

    private Set<LeafCombination> generateCombinations(List<ExpressionTree.Node> leaves) {
        Set<LeafCombination> combinations = new HashSet<LeafCombination>();

        for (ExpressionTree.Node node : leaves) {
            if (node.getExpression().isReference()) {
                combinations.add(new LeafCombination(node, generateCombinations(node)));
            }
        }
        return combinations;
    }

    /**
     * Generates combination for the node - epsilon and references substitution.
     */
    Set<String> generateCombinations(ExpressionTree.Node node) {
        Set<String> combinations = new HashSet<String>();

        String word = node.toWord();

        if (containsEpsilon(word)) {
            combinations.add(EPSILON);
        }

        if (!node.getExpression().isReference()) {
            combinations.add(word);

        } else {
            for (RecexpRule rule : rules) {
                if (rule.getName().equals(node.getExpression().getText())) {
                    word = rule.getExpression();
                    if (!word.isEmpty()) {
                        word = "(" + word + ")";
                    }
                    combinations.add(word);

                    if (containsEpsilon(word)) {
                        combinations.add(EPSILON);
                    }
                }
            }
        }
        return combinations;
    }

    private boolean containsEpsilon(String expression) {
        return Pattern.matches(expression, EPSILON);
    }

    /**
     * Creates a tree from an expression.
     */
    ExpressionTree createTree(String expression) {
        return new ExpressionTree(createLeaf(expression));
    }

    private ExpressionTree.Node createLeaf(String expression) {
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

        ExpressionTree.Node node = new ExpressionTree.Node(
                new Expression(expression, quantifier, isReference), isClosedInBrackets);

        List<String> expressionParts = new ArrayList<String>();

        for (String bracketGroup : getExpressionPartsCutByBrackets(expression)) {
            for (String referenceGroup : getExpressionPartsCutByReferences(bracketGroup)) {
                expressionParts.add(referenceGroup);
            }
        }

        if (expressionParts.size() > 1 || !expressionParts.get(0).equals(expression)) {
            for (String part : expressionParts) {
                node.getNodes().add(createLeaf(part));
            }

        } else {
            if (isClosedInBrackets(expression, true)) {
                node.getNodes().add(createLeaf(expression));
            }
        }

        return node;
    }

    /**
     * Normalize the tree to the normal form (every end node is either a simple reference or a terminal containing no reference whatsoever).
     */
    ExpressionTree normalizeTree(ExpressionTree tree) {
        return null; // TODO
    }

    /**
     * Expands the tree's end leaves by the node candidates.
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
     * Expression combination for a node.
     */
    class LeafCombination {

        final ExpressionTree.Node node;
        final Set<String> combinations;

        public LeafCombination(ExpressionTree.Node node, Set<String> combinations) {
            this.node = node;
            this.combinations = combinations;
        }

        public ExpressionTree.Node getNode() {
            return node;
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
        final String expression;

        public LeafCandidate(ExpressionTree.Node node, String expression) {
            this.node = node;
            this.expression = expression;
        }

        public ExpressionTree.Node getNode() {
            return node;
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

            if (!node.equals(candidate.node)) {
                return false;
            }
            return expression.equals(candidate.expression);
        }

        @Override
        public int hashCode() {
            int result = node.hashCode();
            result = 31 * result + expression.hashCode();
            return result;
        }
    }
}
