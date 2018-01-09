package cz.net21.ttulka.recexp;

import java.util.regex.Pattern;

/**
 * Recursive rule.
 * <p>
 * A rule is defined as a entry of name and {@link ExpressionTree expression}.
 * <p>
 * A rule can contain a reference to itself declared as <code>@this</code> and/or a reference to another rule via name. A reference to another rule starts with
 * <code>@</code> and continues with the name of the rule (case-sensitive).
 *
 * @author ttulka
 * @see Recexp
 * @see ExpressionTree
 */
class Rule {

    private final String name;
    private final ExpressionTree expression;

    public Rule(String expression) {
        this(expression, ExpressionTree.parseTree(expression));
    }

    public Rule(String name, String expression) {
        this(name, ExpressionTree.parseTree(expression));
    }

    protected Rule(String name, ExpressionTree expressionTree) {
        this.name = name;
        this.expression = expressionTree;
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
 * Named rule.
 * <p>
 * The name can contain only word characters (<code>a-z</code>, <code>A-Z</code>, <code>0-9</code>, <code>_</code>).
 *
 * @author ttulka
 */
class NamedRule extends Rule {

    public NamedRule(String name, String expression) {
        super(name, expression);

        if (!Pattern.matches("((\\w)+)", name)) {
            throw new IllegalArgumentException("Rule name can contain only word characters (letters, digits and underscore), but was: " + name);
        }

        if (name.equals(Expression.THIS_REFERENCE_NAME)) {
            throw new IllegalArgumentException("'" + Expression.THIS_REFERENCE_NAME + "' is a reserved rule name for a self-reference.");
        }

        if (name.equals(Expression.EPSILON_REFERENCE_NAME)) {
            throw new IllegalArgumentException("'" + Expression.EPSILON_REFERENCE_NAME + "' is a reserved rule name for the epsilon (empty rule).");
        }
    }

    protected NamedRule(String name, Expression expression) {
        super(name, new ExpressionTree(new ExpressionTree.Node(expression)));
    }

    @Override
    public String toString() {
        return Expression.REFERENCE_PREFIX + super.getName();
    }
}

/**
 * Implicit defined rule.
 *
 * @author ttulka
 */
class ImplicitRule extends NamedRule {

    public static final ImplicitRule EPSILON_RULE = new ImplicitRule(Expression.EPSILON_REFERENCE_NAME, Expression.EPSILON);

    private ImplicitRule(String name, Expression expression) {
        super(name, expression);
    }
}