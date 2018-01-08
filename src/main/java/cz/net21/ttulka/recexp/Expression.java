package cz.net21.ttulka.recexp;

/**
 * Object representation of a recursive expression.
 *
 * @author ttulka
 * @see ExpressionTree
 */
class Expression {

    public static final char REFERENCE_PREFIX = '@';
    public static final String THIS_REFERENCE_NAME = "this";
    public static final String EPSILON_REFERENCE_NAME = "eps";

    public static final Expression EPSILON = new Expression("", null, false);

    private final String text;
    private final String quantifier;
    private final boolean reference;

    public Expression(String text, String quantifier, boolean reference) {
        this.text = text;
        this.quantifier = quantifier;
        this.reference = reference;
    }

    public String getText() {
        return text;
    }

    public String getQuantifier() {
        return quantifier;
    }

    public boolean isQuantified() {
        return quantifier != null && !quantifier.isEmpty();
    }

    public boolean isReference() {
        return reference;
    }

    public boolean isEpsilon() {
        return EPSILON.getText().equals(this.getText());
    }

    public String toWord() {
        return toWord(false);
    }

    String toWord(boolean closeIntoBrackets) {
        if (isEpsilon()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        if (closeIntoBrackets || (!isReference() && isQuantified())) {
            sb.append("(");
        }

        if (isReference()) {
            sb.append(REFERENCE_PREFIX);
        }

        sb.append(getText());

        if (closeIntoBrackets || (!isReference() && isQuantified())) {
            sb.append(")");
        }

        if (isQuantified()) {
            sb.append(getQuantifier());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Expression that = (Expression) o;

        if (reference != that.reference) {
            return false;
        }
        if (!text.equals(that.text)) {
            return false;
        }
        return quantifier != null ? quantifier.equals(that.quantifier) : that.quantifier == null;
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + (quantifier != null ? quantifier.hashCode() : 0);
        result = 31 * result + (reference ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return toWord();
    }
}
