package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
public class RecexpCyclicRuleException extends RecexpException {

    protected RecexpCyclicRuleException(String expression) {
        super(expression);
    }
}
