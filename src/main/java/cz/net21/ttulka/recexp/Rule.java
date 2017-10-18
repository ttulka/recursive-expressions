package cz.net21.ttulka.recexp;

public class Rule {

    private final String name;
    private final String expression;

    public Rule(String expression) {
        this.name = expression;
        this.expression = expression;
    }

    public Rule(String name, String expression) {
        this.name = name;
        this.expression = expression;
    }

    public String getName() {
        return name;
    }

    public String getExpression() {
        return expression;
    }

    @Override
    public String toString() {
        return !name.equals(expression) ? name + "=" + expression : expression;
    }
}
