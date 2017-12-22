package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
     * Node in the expression tree.
     */
    static class Node {

        private final Expression expression;

        private final List<Node> nodes = new ArrayList<Node>();

        public Node(Expression expression) {
            this.expression = expression;
        }

        public static ExpressionTree.Node parseNode(String expression) {
            String quantifier = null;

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
                    new Expression(expression, quantifier, isReference));

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
            return getSentence(this, new StringBuilder(), false).toString();
        }

        private StringBuilder getSentence(Node node, StringBuilder sb, boolean inBrackets) {
            if (node.getNodes().isEmpty()) {
                if (inBrackets) {
                    sb.append("(");
                }
                sb.append(node.toWord());

                if (inBrackets) {
                    sb.append(")");
                }

            } else {
                if (inBrackets || node.getExpression().isQuantified()) {
                    sb.append("(");
                }

                int nodeIndex = 0;
                for (Node subNode : node.getNodes()) {
                    nodeIndex++;
                    Node nextNode = nodeIndex < node.getNodes().size() ? node.getNodes().get(nodeIndex) : null;

                    boolean closeNodeIntoBrackets = nextNode != null && !nextNode.toWord().isEmpty() &&
                            subNode.getExpression().isReference() && !subNode.getExpression().isQuantified() &&
                            Pattern.matches("\\w", String.valueOf(nextNode.toWord().charAt(0)));

                    sb = getSentence(subNode, sb, closeNodeIntoBrackets);
                }

                if (inBrackets || node.getExpression().isQuantified()) {
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
