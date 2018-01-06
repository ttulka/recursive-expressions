package cz.net21.ttulka.recexp;

/**
 * Derivation result representation as a matcher.
 *
 * @author ttulka
 * @see RecexpGrammar
 */
abstract public class RecexpMatcher extends RecexpGroup {

    protected RecexpMatcher(String name, String value, RecexpGroup[] groups) {
        super(name, value, groups);
    }

    static RecexpMatcher matcher(String name, String input, RecexpGroup[] groups) {
        return new RecexpMatcher(name, input, groups) {
            @Override
            public boolean matches() {
                return true;
            }
        };
    }

    static RecexpMatcher emptyMatcher(String input) {
        return new RecexpMatcher(null, input, new RecexpGroup[0]) {
            @Override
            public boolean matches() {
                return false;
            }
        };
    }

    /**
     * Attempts to match the entire input against the grammar.
     *
     * @return true if, and only if, the entire input sequence matches this matcher's grammar
     */
    abstract public boolean matches();
}
