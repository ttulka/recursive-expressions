package cz.net21.ttulka.recexp;

/**
 * Referenced rule not found exception.
 *
 * @author ttulka
 * @see Recexp
 */
public class RecexpRuleNotFoundException extends RecexpException {

    /**
     * @param name the rule name
     */
    protected RecexpRuleNotFoundException(String name) {
        super(name);
    }
}
