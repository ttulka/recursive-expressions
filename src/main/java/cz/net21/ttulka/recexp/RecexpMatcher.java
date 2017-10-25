package cz.net21.ttulka.recexp;

import java.util.Set;

/**
 * @author ttulka
 */
public class RecexpMatcher extends RecexpGroup {

    protected final String input;
    protected final Set<RecexpRule> rules;

    protected RecexpMatcher(String input, Set<RecexpRule> rules) {
        super();
        this.input = input;
        this.rules = rules;
    }

    /**
     * Attempts to match the entire input against the grammar.
     *
     * @return true if, and only if, the entire input sequence matches this matcher's grammar
     */
    public boolean matches() {
        return false; // TODO
    }

    @Override
    public String getValue() {
        return input;
    }
}
