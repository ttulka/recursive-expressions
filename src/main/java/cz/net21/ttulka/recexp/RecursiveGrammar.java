package cz.net21.ttulka.recexp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RecursiveGrammar {

    private final Set<Rule> rules = new HashSet<Rule>();

    public RecursiveGrammar(Rule... rules) {
        if (rules != null && rules.length > 0) {
            this.rules.addAll(Arrays.asList(rules));
        }
    }

    public RecursiveGrammar(String... rules) {
        if (rules != null && rules.length > 0) {
            for (String rule : rules) {
                this.rules.add(new Rule(rule));
            }
        }
    }

    public RecursiveGrammar addRule(Rule rule) {
        this.rules.add(rule);
        return this;
    }

    public RecursiveGrammar addRule(String name, String expression) {
        this.rules.add(new Rule(name, expression));
        return this;
    }

    public RecursiveGrammar addRule(String expression) {
        this.rules.add(new Rule(expression));
        return this;
    }

    public String[] split(CharSequence input) {
        return null; // TODO
    }

    public RecexpMatcher matcher(String input) {
        return new RecexpMatcher(input, Collections.unmodifiableSet(this.rules));
    }

    /**
     * Convenient method. See {@link RecexpMatcher#matches()}.
     *
     * @param input the input string
     * @return true if the grammar accepts the string, otherwise false
     */
    public boolean matches(String input) {
        return matcher(input).matches();
    }

    @Override
    public String toString() {
        return rules.toString();
    }
}
