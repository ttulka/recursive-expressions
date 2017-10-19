package cz.net21.ttulka.recexp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RecexpGrammar {

    private final Set<RecexpRule> rules = new HashSet<RecexpRule>();

    public RecexpGrammar(RecexpRule... rules) {
        if (rules != null && rules.length > 0) {
            this.rules.addAll(Arrays.asList(rules));
        }
    }

    public RecexpGrammar(String... rules) {
        if (rules != null && rules.length > 0) {
            for (String rule : rules) {
                this.rules.add(new RecexpRule(rule));
            }
        }
    }

    public RecexpGrammar addRule(RecexpRule rule) {
        this.rules.add(rule);
        return this;
    }

    public RecexpGrammar addRule(String name, String expression) {
        this.rules.add(new RecexpRule(name, expression));
        return this;
    }

    public RecexpGrammar addRule(String expression) {
        this.rules.add(new RecexpRule(expression));
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
