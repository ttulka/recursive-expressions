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

    private static final String REGEXP_REFERENCE = "@((\\w)+)";

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

            if (isApplicable(rule.getExpression(), input)) {
                List<RecexpGroup> groups = getGroups(rule.getExpression(), input);
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

    boolean isApplicable(String expression, String input) {
        return isApplicable(expression, input, expression);
    }

    private boolean isApplicable(String expression, String input, String originalExpression) {
        if (Pattern.matches(expression, input)) {
            return true;
        }
        if (!Pattern.matches(hydrateExpression(expression), input)) {
            return false;
        }
        List<Set<String>> candidates = new ArrayList<Set<String>>();

        int terminalsCount = 0;
        for (String part : getExpressionParts(expression)) {
            candidates.add(getCandidates(part, originalExpression));

            if (!isReference(part)) {
                terminalsCount++;
            }
        }

        if (terminalsCount > input.length()) {  // input is already longer than possible expression result
            return false;
        }

        for (String candidateExp : getCartesianProduct(candidates)) {
            if (isApplicable(candidateExp, input, originalExpression)) {
                return true;
            }
        }
        return false;
    }

    List<String> getCartesianProduct(List<Set<String>> candidates) {
        List<String> cartesianProduct = new ArrayList<String>();

        if (candidates.size() > 1) {
            for (String product : getCartesianProduct(candidates.subList(1, candidates.size()))) {

                for (String candidate : candidates.get(0)) {
                    cartesianProduct.add(candidate + product);
                }
            }

        } else {
            return new ArrayList<String>(candidates.get(0));
        }
        return cartesianProduct;
    }

    Set<String> getCandidates(String part, String originalExpression) {
        Set<String> candidates = new HashSet<String>();

        if (!isReference(part)) {
            candidates.add(part);

        } else {
            candidates.addAll(
                    findExpressionsByReference(part.substring(1), originalExpression));
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

    List<String> getExpressionParts(String expression) {
        List<String> parts = new ArrayList<String>();

        expression = replaceReference(expression);

        Matcher matcher = Pattern.compile(REGEXP_REFERENCE).matcher(expression);
        int restStarts = 0;

        while (matcher.find()) {
            if (matcher.start() > restStarts) {
                parts.add(resetReference(
                        expression.substring(restStarts, matcher.start())
                ));
            }
            restStarts = matcher.end();
            parts.add(resetReference(
                    expression.substring(matcher.start(), matcher.end())
            ));
        }
        if (restStarts < expression.length()) {
            parts.add(resetReference(
                    expression.substring(restStarts)
            ));
        }
        return parts;
    }

    List<RecexpGroup> getGroups(String expression, String input) {
        List<RecexpGroup> groups = new ArrayList<RecexpGroup>();

        for (String groupExp : separateGroups(expression)) {
            // TODO
            // 1. put groupExps into RegExp groups: ( groupExp1 ) + .. + ( groupExpN )
            // 2. replace variables with (.*)
            // 3. parse Recexp groups from Regexp groups
            // 4. recursively solve each sub-group
        }

        return groups;
    }

    List<String> separateGroups(String expression) {
        List<String> groups = new ArrayList<String>();

        int endBracketNeeded = 0;
        char previous = '\0';

        StringBuilder sb = new StringBuilder();

        int i = 0;
        while (i < expression.length()) {
            char ch = expression.charAt(i);

            if (ch == '@' && previous != '\\' && endBracketNeeded == 0) {
                String var = getVariable(expression, i);

                if (var != null) {
                    groups.add(sb.toString());
                    sb = new StringBuilder();

                    i += var.length() - 1;
                    groups.add(var);

                } else {
                    sb.append(ch);
                }

            } else if (ch == '(' && previous != '\\') {
                int endBracketPosition = pairEndBracketPosition(expression, i);

                if (endBracketPosition != -1) {
                    if (sb.length() > 0) {
                        groups.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    groups.add(expression.substring(i + 1, endBracketPosition));
                    i = endBracketPosition;

                } else {
                    sb.append(ch);
                }

            } else {
                sb.append(ch);
            }
            previous = ch;
            i++;
        }

        if (sb.length() > 0) {
            groups.add(sb.toString());
        }

        return groups;
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
        sb.append(expression.charAt(i++));  // @
        sb.append(expression.charAt(i++));  // first letter

        while (Pattern.matches("\\w", Character.toString(expression.charAt(i)))) {
            sb.append(expression.charAt(i++));
        }

        // try to get quantifiers
        String rest = expression.substring(i);
        Matcher quantifiersMatcher = Pattern.compile("(([?*+]|\\{\\d+,?\\d*})[?+]?)(.*)").matcher(rest);
        if (quantifiersMatcher.matches()) {
            sb.append(quantifiersMatcher.group(1));
        }

        return sb.toString();
    }

    // replace rule references with (.*)
    String hydrateExpression(String expression) {
        return resetReference(
                replaceReference(expression).replaceAll(REGEXP_REFERENCE, "(.*)")
        );
    }

    private String replaceReference(String expression) {
        return expression.replaceAll("[\\\\]" + REGEXP_REFERENCE, "\\\\__RecexpAt__$1");
    }

    private String resetReference(String expression) {
        return expression.replaceAll("\\\\__RecexpAt__", "\\\\@");
    }

    @Override
    public String toString() {
        return this.rules.toString();
    }

    class GroupCandidate {
        String expression;
        String name;
        boolean isRuleReference;
    }
}
