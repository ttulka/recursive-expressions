package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
abstract public class RecexpMatcher extends RecexpGroup {

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

    protected RecexpMatcher(String name, String value, RecexpGroup[] groups) {
        super(name, value, groups);
    }

    /**
     * Attempts to match the entire input against the grammar.
     *
     * @return true if, and only if, the entire input sequence matches this matcher's grammar
     */
    abstract public boolean matches();
}
