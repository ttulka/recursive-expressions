package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Arrays;
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

            List<RecexpGroup> groups = getGroups(rule.getExpression(), input);
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

    List<RecexpGroup> getGroups(String expression, String input) {
        return getGroups(expression, input, expression);
    }

    private List<RecexpGroup> getGroups(String expression, String input, String originalExpression) {
        if (isClosedInBrackets(expression)) {
            return getGroups(removeClosingBrackets(expression), input, originalExpression);
        }
        if (Pattern.matches(expression, input)) {
            return Collections.singletonList(new RecexpGroup(expression, input, new RecexpGroup[0]));
        }
        if (!Pattern.matches(hydrateExpression(expression), input)) {
            return null;
        }
        List<Set<String>> candidates = new ArrayList<Set<String>>();

        int terminalsCount = 0;
        for (ExpressionPart part : getExpressionParts(expression)) {
            candidates.add(getCandidates(part, originalExpression));

            if (isTerminal(part)) {
                terminalsCount++;
            }
        }

        if (terminalsCount > input.length()) {  // input is already longer than possible expression result
            return null;
        }

        for (String candidateExp : getCartesianProduct(candidates)) {
            if (!candidateExp.equals(expression)) { // avoid a loop

                List<RecexpGroup> groups = getGroups(candidateExp, input, originalExpression);
                if (groups != null && !groups.isEmpty()) {
                    return groups;
                }
            }
        }
        return null;
    }

    boolean isTerminal(ExpressionPart expressionPart) {
        if (expressionPart.isReference()) {
            return false;
        }
        if (expressionPart.isQuantified()) {
            return false;
        }
        return true;
    }

    boolean isClosedInBrackets(String expression) {
        if (isQuantified(expression)) {
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
        return expression.substring(1, expression.length() - 1);
    }

    private String closeIntoBrackets(String expression, int bracketLevel) {
        StringBuilder sb = new StringBuilder(expression.length() + bracketLevel * 2)
                .append(expression);

        for (int i = 0; i < bracketLevel; i++) {
            sb.insert(0, "(").append(")");
        }
        return sb.toString();
    }

    List<String> getCartesianProduct(List<Set<String>> candidates) {
        List<String> cartesianProduct = new ArrayList<String>();

        Set<String> head = candidates.get(0);

        if (candidates.size() > 1) {
            for (String product : getCartesianProduct(candidates.subList(1, candidates.size()))) {

                for (String candidate : head) {
                    cartesianProduct.add(putIntoBrackets(candidate) + product);
                }
            }

        } else {
            Set<String> result = new HashSet<String>(head.size());
            for (String candidate : head) {
                result.add(putIntoBrackets(candidate));
            }
            return new ArrayList<String>(result);
        }
        return cartesianProduct;
    }

    private String putIntoBrackets(String expression) {
        if (isClosedInBrackets(expression) || isQuantifier(expression)
            || "(".equals(expression) || ")".equals(expression)
            || "\\(".equals(expression) || "\\)".equals(expression)) {
            return expression;
        }
        return "(" + expression + ")";
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

    Set<String> getCandidates(ExpressionPart part, String originalExpression) {
        Set<String> candidates = new HashSet<String>();

        if (!part.isReference()) {
            candidates.add(part.getWholeExpression());

        } else {
            for (String exp : findExpressionsByReference(part.getExpression(), originalExpression)) {
                exp = closeIntoBrackets(exp, 1) + part.getQuantifierString();
                candidates.add(closeIntoBrackets(exp, part.getBracketLevel()));
            }
        }
        return candidates;
    }

    private Set<String> findExpressionsByReference(String referenceName, String originalExpression) {
        if (THIS_REFERENCE_NAME.equals(referenceName)) {
            return Collections.singleton(originalExpression);
        }

        Set<String> expressions = new HashSet<String>();

        for (RecexpRule rule : this.rules) {
            if (rule.getName().equals(referenceName)) {
                expressions.add(rule.getExpression());
            }
        }
        if (!expressions.isEmpty()) {
            return expressions;
        }
        throw new RecexpRuleNotFoundException(referenceName);
    }

    boolean isReference(String expression) {
        return Pattern.matches(REGEXP_REFERENCE, expression);
    }

    List<ExpressionPart> getExpressionParts(String expression) {
        List<String> parts = new ArrayList<String>();

        for (String part : getExpressionPartsCutByBrackets(expression)) {
            parts.addAll(getExpressionPartsCutByReferences(part));
        }

        List<ExpressionPart> expressionParts = new ArrayList<ExpressionPart>();

        for (String part : parts) {
            expressionParts.add(createExpressionPart(part));
        }

        return expressionParts;
    }

    private ExpressionPart createExpressionPart(String expression) {
        String quantifier = getQuantifier(expression);
        if (quantifier != null) {
            expression = expression.substring(0, expression.length() - quantifier.length());
        }

        int bracketLevel = 0;
        while (isClosedInBrackets(expression)) {
            bracketLevel++;
            expression = removeClosingBrackets(expression);

            if (isQuantified(expression)) {
                break;
            }
        }

        boolean isReference = isReference(expression);
        if (isReference) {
            expression = expression.substring(1);
        }

        return new ExpressionPart(expression, quantifier, bracketLevel, isReference);
    }

    private List<String> getExpressionPartsCutByBrackets(String expression) {
        List<String> parts = new ArrayList<String>();

        StringBuilder sb = new StringBuilder(expression.length());
        char previous = '\0';
        int bracketsLevel = 0;
        int lastOpeningBracketIndex = -1;

        int index = 0;
        while(index < expression.length()) {
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
            index ++;
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
        if (isClosedInBrackets(expression)) {
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

    private int pairEndBracketPosition(String expression, int start) {
        char previous = expression.charAt(start);
        int skip = 1;

        for (int i = start + 1; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == '(' && previous != '\\') {
                skip++;

            } else if (ch == ')' && previous != '\\') {
                skip--;
                if (skip == 0) {
                    return i;
                }
            }

            previous = ch;
        }
        return -1;
    }

    private String getVariable(String expression, int start) {
        // must start with letter or underscore
        if (!Pattern.matches("[a-zA-Z_]", Character.toString(expression.charAt(start + 1)))) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        int i = start;
        sb.append(expression.charAt(i++));  // REFERENCE_PREFIX
        sb.append(expression.charAt(i++));  // first letter

        while (Pattern.matches("\\w", Character.toString(expression.charAt(i)))) {
            sb.append(expression.charAt(i++));
        }

        // try to get quantifiers
        String rest = expression.substring(i);
        Matcher quantifiersMatcher = Pattern.compile(REGEXP_QUANTIFIER + "(.*)").matcher(rest);
        if (quantifiersMatcher.matches()) {
            sb.append(quantifiersMatcher.group(1));
        }

        return sb.toString();
    }

    // replace rule references with (.*)
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

    protected class ExpressionPart {

        private final String expression;
        private final String quantifier;
        private final int bracketLevel;
        private final boolean reference;

        public ExpressionPart(String expression, String quantifier, int bracketLevel, boolean reference) {
            this.expression = expression;
            this.quantifier = quantifier;
            this.bracketLevel = bracketLevel;
            this.reference = reference;
        }

        public String getWholeExpression() {
            StringBuilder sb = new StringBuilder(expression.length() + getQuantifierString().length() + bracketLevel * 2 + (reference ? 1 : 0))
                    .append(expression);

            if (reference) {
                sb.insert(0, REFERENCE_PREFIX);
            }

            String exp = closeIntoBrackets(sb.toString(), bracketLevel);

            if (quantifier != null) {
                exp += quantifier;
            }
            return exp;
        }

        public String getExpression() {
            return expression;
        }

        public String getQuantifier() {
            return quantifier;
        }

        public boolean isQuantified() {
            return quantifier != null && !quantifier.isEmpty();
        }

        public String getQuantifierString() {
            return isQuantified() ? quantifier : "";
        }

        public int getBracketLevel() {
            return bracketLevel;
        }

        public boolean isReference() {
            return reference;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ExpressionPart that = (ExpressionPart) o;

            if (bracketLevel != that.bracketLevel) {
                return false;
            }
            if (reference != that.reference) {
                return false;
            }
            if (!expression.equals(that.expression)) {
                return false;
            }
            return quantifier != null ? quantifier.equals(that.quantifier) : that.quantifier == null;
        }

        @Override
        public int hashCode() {
            int result = expression.hashCode();
            result = 31 * result + (quantifier != null ? quantifier.hashCode() : 0);
            result = 31 * result + bracketLevel;
            result = 31 * result + (reference ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return getWholeExpression();
        }
    }
}
