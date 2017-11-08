package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ttulka
 */
public class RecexpGrammar {

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

        /*
        for each rule:
            1. split the input by '(..)' and create candidate groups for the actual level
                "a(b(c))$D" -> part("a") + part("b(c)",isGroup=true) + part("$D",isGroup=true,name=D)

            2. replace groups with (.*) and try to match, if matches then level += 1 and go to 3

            3. try to resolve named groups, this = actual group
                input^i+1 = resolve(part1) + .. + resolve(partN)

            4. go to 1
         */
        for (RecexpRule rule : this.rules) {

            if (isApplicable(rule.getExpression(), input)) {
                List<RecexpGroup> groups = getGroups(rule.getName(), rule.getExpression(), input);
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
        if (!Pattern.matches(hydrateExpression(expression), input)) {
            return false;
        }
        List<Set<String>> candidates = new ArrayList<Set<String>>();

        for (String part : getExpressionParts(expression)) {
            candidates.add(getCandidates(part));
        }

        for (String candidateExp : getCartesianProduct(candidates)) {
            if (isApplicable(candidateExp, input)) {
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

    Set<String> getCandidates(String part) {
        Set<String> candidates = new HashSet<String>();

        if (isReference(part)) {
            candidates.add(part);

        } else {
            String referenceName = part.substring(1);

            for (RecexpRule rule : this.rules) {
                if (rule.getName().equals(referenceName)) {
                    candidates.add(rule.getExpression());
                }
            }
        }
        return candidates;
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
            if (matcher.start() > 0) {
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

    List<RecexpGroup> getGroups(String name, String expression, String input) {
        return null;    // TODO
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
