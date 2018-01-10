package cz.net21.ttulka.recexp;

/**
 * Cyclic rule definition exception.
 *
 * @author ttulka
 * @see Recexp
 */
public class RecexpCyclicRuleException extends RecexpException {

    /**
     * @param name the rule name
     */
    protected RecexpCyclicRuleException(String name) {
        super(name);
    }
}
