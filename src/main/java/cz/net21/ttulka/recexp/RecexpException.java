package cz.net21.ttulka.recexp;

/**
 * @author ttulka
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
