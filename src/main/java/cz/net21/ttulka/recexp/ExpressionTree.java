package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

        enum SubNodesConnectionType {
            SINGLE, AND, OR
        }

        private final Expression expression;
        private final SubNodesConnectionType subNodesConnectionType;
        private final List<Node> subNodes;

        public Node(Expression expression) {
            this(expression, SubNodesConnectionType.SINGLE);
        }

        public Node(Expression expression, SubNodesConnectionType subNodesConnectionType) {
            this(expression, subNodesConnectionType, new ArrayList<Node>());
        }

        public Node(Expression expression, SubNodesConnectionType subNodesConnectionType, List<Node> subNodes) {
            this.expression = expression;
            this.subNodesConnectionType = subNodesConnectionType;
            this.subNodes = new ArrayList<Node>(subNodes);
        }

        public static ExpressionTree.Node parseNode(String expression) {
            try {
                Pattern.compile(expression);

            } catch (PatternSyntaxException rethrow) {
                throw new RecexpSyntaxException(rethrow.getMessage());
            }

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

            List<String> orParts = ExpressionUtils.splitORs(expression);

            if (orParts.size() > 1) {
                List<Node> subNodes = new ArrayList<Node>();
                for (String part : orParts) {
                    subNodes.add(parseNode(part));
                }

                return new ExpressionTree.Node(
                        new Expression(expression, quantifier, isReference),
                        SubNodesConnectionType.OR,
                        subNodes);
            }

            List<String> andParts = ExpressionUtils.splitANDs(expression);

            if (andParts.size() > 1) {
                List<Node> subNodes = new ArrayList<Node>();
                for (String part : andParts) {
                    subNodes.add(parseNode(part));
                }

                return new ExpressionTree.Node(
                        new Expression(expression, quantifier, isReference),
                        SubNodesConnectionType.AND,
                        subNodes);

            }

            if (ExpressionUtils.isClosedInBrackets(expression, true)) {
                List<Node> subNodes = new ArrayList<Node>();
                subNodes.add(parseNode(expression));

                return new ExpressionTree.Node(
                        new Expression(expression, quantifier, isReference),
                        SubNodesConnectionType.OR,
                        subNodes);
            }

            return new ExpressionTree.Node(
                    new Expression(expression, quantifier, isReference),
                    SubNodesConnectionType.SINGLE);
        }

        public Expression getExpression() {
            return expression;
        }

        public SubNodesConnectionType getSubNodesConnectionType() {
            return subNodesConnectionType;
        }

        public List<Node> getSubNodes() {
            return subNodes;
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

            if (node.getSubNodes().isEmpty()) {
                if (!node.isEmpty()) {
                    leaves.add(node);
                }

            } else {
                for (Node l : node.getSubNodes()) {
                    leaves.addAll(getLeaves(l));
                }
            }
            return leaves;
        }

        public String getSentence() {
            return getSentence(this, false).toString();
        }

        private StringBuilder getSentence(Node node, boolean inBrackets) {
            StringBuilder sb = new StringBuilder();

            if (node.getSubNodes().isEmpty()) {
                if (inBrackets) {
                    sb.append("(");
                }
                sb.append(node.toWord());

                if (inBrackets) {
                    sb.append(")");
                }
                return sb;
            }

            if (inBrackets || node.getExpression().isQuantified()) {
                sb.append("(");
            }

            int nodeIndex = 0;
            for (Node subNode : node.getSubNodes()) {
                nodeIndex++;
                Node nextNode = nodeIndex < node.getSubNodes().size() ? node.getSubNodes().get(nodeIndex) : null;

                boolean closeNodeIntoBrackets = nextNode != null && !nextNode.toWord().isEmpty() &&
                                                subNode.getExpression().isReference() && !subNode.getExpression().isQuantified() &&
                                                Pattern.matches("\\w", String.valueOf(nextNode.toWord().charAt(0)));

                if (SubNodesConnectionType.OR == node.subNodesConnectionType && nodeIndex > 1) {
                    sb.append("|");
                }

                sb.append(getSentence(subNode, closeNodeIntoBrackets));
            }

            if (inBrackets || node.getExpression().isQuantified()) {
                sb.append(")");
            }

            if (node.getExpression().isQuantified()) {
                sb.append(node.getExpression().getQuantifier());
            }
            return sb;
        }

        boolean isOrNode() {
            return ExpressionTree.Node.SubNodesConnectionType.OR == subNodesConnectionType;
        }

        boolean isThisReference() {
            return expression.isReference() && Expression.THIS_REFERENCE_NAME.equals(expression.getText());
        }

        @Override
        public String toString() {
            return expression.toString();
        }
    }
}
