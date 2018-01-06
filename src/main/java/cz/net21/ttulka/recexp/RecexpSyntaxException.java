package cz.net21.ttulka.recexp;

/**
 * Syntax error in a recursive expression exception.
 *
 * @author ttulka
 * @see RecexpGrammar
 */
public class RecexpSyntaxException extends RecexpException {

    protected RecexpSyntaxException(String description) {
        super(description);
    }
}
