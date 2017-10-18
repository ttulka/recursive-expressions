package cz.net21.ttulka.recexp;

import java.util.Set;

public class RecexpMatcher {

    private final String input;
    private final Set<Rule> rules;

    RecexpMatcher(String input, Set<Rule> rules) {
        this.input = input;
        this.rules = rules;
    }

    public boolean matches() {
        return false; // TODO
    }
}
