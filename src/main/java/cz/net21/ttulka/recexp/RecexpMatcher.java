package cz.net21.ttulka.recexp;

/**
 * Derivation result matcher.
 *
 * @author ttulka
 * @see Recexp
 */
abstract public class RecexpMatcher extends RecexpGroup {

    /**
     * @param name   the name of the expression
     * @param value  the parsed input value
     * @param groups the children groups
     */
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
        return new RecexpMatcher(null, input, null) {
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
