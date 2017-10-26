package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
public class RecexpRuleNotFoundException extends RecexpException {

    protected RecexpRuleNotFoundException(String expression) {
        super(expression);
    }
}
