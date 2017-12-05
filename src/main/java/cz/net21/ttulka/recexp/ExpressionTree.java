package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree representation of an candidate.
 *
 * @author ttulka
 */
class ExpressionTree {

    private final Node root;

    public ExpressionTree(Node root) {
        this.root = root;
    }

    public static ExpressionTree parseTree(String expression) {
        return new ExpressionTree(ExpressionTree.Node.parseNode(expression));
    }

    public Node getRoot() {
        return root;
    }

    public List<Node> getLeaves() {
        return root.getLeaves();
    }

    public String getSentence() {
        return root.getSentence();
    }

    @Override
    public String toString() {
        return root.toString();
    }

    /**
     * Node in the candidate tree.
     */
    static class Node {

        private final Expression expression;
        private final boolean closedInBrackets;

        private final List<Node> nodes = new ArrayList<Node>();

        public Node(Expression expression, boolean closedInBrackets) {
            this.expression = expression;
            this.closedInBrackets = closedInBrackets;
        }

        public Node(Expression expression) {
            this(expression, false);
        }

        public static ExpressionTree.Node parseNode(String expression) {
            String quantifier = null;

            // TODO get the quantifier even if it is in the brackets
            boolean isClosedInBrackets = ExpressionUtils.isClosedInBrackets(expression, true);
            if (isClosedInBrackets) {
                quantifier = ExpressionUtils.getQuantifier(expression);
                if (quantifier != null && !quantifier.isEmpty()) {
                    expression = expression.substring(0, expression.length() - quantifier.length());
                }
                expression = ExpressionUtils.removeClosingBrackets(expression);
            }

            boolean isReference = ExpressionUtils.isReference(expression);
            if (isReference) {
                expression = ExpressionUtils.removeReferencePrefix(expression);

                if (quantifier == null) {
                    quantifier = ExpressionUtils.getQuantifier(expression);
                    if (quantifier != null && !quantifier.isEmpty()) {
                        expression = expression.substring(0, expression.length() - quantifier.length());
                    }
                }
            }

            ExpressionTree.Node node = new ExpressionTree.Node(
                    new Expression(expression, quantifier, isReference), isClosedInBrackets);

            List<String> expressionParts = ExpressionUtils.split(expression);

            if (expressionParts.size() > 1 || !expressionParts.get(0).equals(expression)) {
                for (String part : expressionParts) {
                    node.getNodes().add(parseNode(part));
                }

            } else {
                if (ExpressionUtils.isClosedInBrackets(expression, true)) {
                    node.getNodes().add(parseNode(expression));
                }
            }

            return node;
        }

        public Expression getExpression() {
            return expression;
        }

        public boolean isClosedInBrackets() {
            return closedInBrackets;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public String toWord() {
            return expression.toWord();
        }

        public boolean isEmpty() {
            return expression.isEpsilon();
        }

        public List<Node> getLeaves() {
            return getLeaves(this);
        }

        private List<Node> getLeaves(Node node) {
            List<Node> leaves = new ArrayList<Node>();

            if (node.getNodes().isEmpty()) {
                if (!node.isEmpty()) {
                    leaves.add(node);
                }

            } else {
                for (Node l : node.getNodes()) {
                    leaves.addAll(getLeaves(l));
                }
            }
            return leaves;
        }

        public String getSentence() {
            return getSentence(this, new StringBuilder()).toString();
        }

        private StringBuilder getSentence(Node node, StringBuilder sb) {
            if (node.getNodes().isEmpty()) {
                sb.append(node.toWord());

            } else {
                if (node.getExpression().isQuantified() || node.isClosedInBrackets()) {
                    sb.append("(");
                }

                for (Node l : node.getNodes()) {
                    sb = getSentence(l, sb);
                }

                if (node.getExpression().isQuantified() || node.isClosedInBrackets()) {
                    sb.append(")");
                }
                if (node.getExpression().isQuantified()) {
                    sb.append(node.getExpression().getQuantifier());
                }
            }
            return sb;
        }

        @Override
        public String toString() {
            return expression.toString();
        }
    }
}
