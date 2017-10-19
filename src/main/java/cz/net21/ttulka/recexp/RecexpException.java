package cz.net21.ttulka.recexp;

public class RecexpException extends RuntimeException {

    protected RecexpException(String message) {
        super(message);
    }

    protected RecexpException(String message, Throwable cause) {
        super(message, cause);
    }
}
