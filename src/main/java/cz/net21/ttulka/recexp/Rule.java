package cz.net21.ttulka.recexp;

/**
 * Recursive rule.
 * <p>
 * A rule is defined as a entry of name and {@link ExpressionTree expression}. The name can contain only word characters (<code>a-z</code>, <code>A-Z</code>,
 * <code>0-9</code>, <code>_</code>).
 * <p>
 * A rule can contain a reference to itself declared as <code>@this</code> and/or a reference to another rule via name. A reference to another rule starts with
 * <code>@</code> and continues with the name of the rule (case-sensitive).
 *
 * @author ttulka
 * @see RecexpGrammar
 * @see ExpressionTree
 */
class Rule {

    private final String name;
    private final ExpressionTree expression;

    public Rule(String expression) {
        this.name = expression;
        this.expression = ExpressionTree.parseTree(expression);
    }

    public Rule(String name, String expression) {
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

        Rule that = (Rule) o;

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

/**
 * Explicitly named rule.
 *
 * @author ttulka
 */
class NamedRule extends Rule {

    public NamedRule(String name, String expression) {
        super(name, expression);
    }

    @Override
    public String toString() {
        return Expression.REFERENCE_PREFIX + super.getName();
    }
}
