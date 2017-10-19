package cz.net21.ttulka.recexp;

public class RecexpCyclicRuleException extends RecexpException {

    protected RecexpCyclicRuleException(String expression) {
        super(expression);
    }
}
