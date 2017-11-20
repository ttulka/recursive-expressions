package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
public class RecexpRule {

    private final String name;
    private final String expression;    // TODO is Expression object

    public RecexpRule(String expression) {
        this.name = expression;
        this.expression = expression;
    }

    public RecexpRule(String name, String expression) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RecexpRule that = (RecexpRule) o;

        if (!name.equals(that.name)) {
            return false;
        }
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + expression.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return !name.equals(expression) ? name + "=" + expression : expression;
    }
}
