package cz.net21.ttulka.recexp;

/**
 * @author ttulka
 */
class Expression {

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
        return text == null || text.isEmpty();
    }

    public String toWord() {
        if (isEpsilon()) {
            return RecexpGrammar.EPSILON;
        }
        StringBuilder sb = new StringBuilder()
                .append("(");

        if (isReference()) {
            sb.append(RecexpGrammar.REFERENCE_PREFIX);
        }
        sb.append(getText())
                .append(")");

        if (getQuantifier() != null) {
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
