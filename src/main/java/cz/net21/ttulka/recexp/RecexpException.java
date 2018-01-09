package cz.net21.ttulka.recexp;

/**
 * Recursive expression general exception.
 *
 * @author ttulka
 * @see Recexp
 */
public class RecexpException extends RuntimeException {

    protected RecexpException() {
        super();
    }

    protected RecexpException(String message) {
        super(message);
    }

    protected RecexpException(String message, Throwable cause) {
        super(message, cause);
    }
}
