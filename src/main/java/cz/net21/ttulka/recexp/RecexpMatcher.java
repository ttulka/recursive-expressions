package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
public class RecexpMatcher extends RecexpGroup {

    protected RecexpMatcher(String name, String value, RecexpGroup[] groups) {
        super(name, value, groups);
    }

    /**
     * Attempts to match the entire input against the grammar.
     *
     * @return true if, and only if, the entire input sequence matches this matcher's grammar
     */
    public boolean matches() {
        return groupCount() > 0;
    }
}
