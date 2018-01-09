package cz.net21.ttulka.recexp;

/**
 * Grammar contains a cyclic rule exception.
 *
 * @author ttulka
 * @see Recexp
 */
public class RecexpCyclicRuleException extends RecexpException {

    protected RecexpCyclicRuleException(String expression) {
        super(expression);
    }
}
