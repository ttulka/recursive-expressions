package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
class RecexpRule {

    private final String name;
    private final ExpressionTree expression;

    public RecexpRule(String expression) {
        this.name = expression;
        this.expression = ExpressionTree.parseTree(expression);
    }

    public RecexpRule(String name, String expression) {
        this.name = name;
        this.expression = ExpressionTree.parseTree(expression);
    }

    public String getName() {
        return name;
    }

    public ExpressionTree getExpression() {
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
        return expression.getSentence().equals(that.expression.getSentence());
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
