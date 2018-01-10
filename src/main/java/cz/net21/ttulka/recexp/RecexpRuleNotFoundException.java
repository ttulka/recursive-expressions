package cz.net21.ttulka.recexp;

/**
 * Referenced rule not found exception.
 *
 * @author ttulka
 * @see Recexp
 */
public class RecexpRuleNotFoundException extends RecexpException {

    protected RecexpRuleNotFoundException(String name) {
        super("No rule with the name '" + name + "' found.");
    }
}
