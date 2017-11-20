package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree representation of an expression.
 *
 * @author ttulka
 */
class ExpressionTree {

    private final Node root;

    public ExpressionTree(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public String getSentence() {
        return getSentence(root, new StringBuilder()).toString();
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

    public List<Node> getLeaves() {
        return root.getLeaves();
    }

    /**
     * Node in the expression tree.
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

        @Override
        public String toString() {
            return expression.toString();
        }
    }
}
