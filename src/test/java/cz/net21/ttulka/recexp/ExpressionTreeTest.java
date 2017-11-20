package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author ttulka
 */
public class ExpressionTreeTest {

    @Test
    public void getRootTest() {
        ExpressionTree tree = createSimpleTree();

        assertThat(tree.getRoot().getExpression().getText(), is("A"));
    }

    @Test
    public void getLeavesTest() {
        ExpressionTree tree = createSimpleTree();

        List<String> leaves = new ArrayList<String>();

        for (ExpressionTree.Node node : tree.getLeaves()) {
            leaves.add(node.getExpression().getText());
        }
        assertThat(leaves, contains("D", "G", "F"));
    }

    @Test
    public void getSentenceTest() {
        assertThat(createSimpleTree().getSentence(), is("(D)(G)(F)"));

        ExpressionTree.Node node;

        node = new ExpressionTree.Node(new Expression("@A@B", null, false), true);
        node.getNodes().add(new ExpressionTree.Node(new Expression("A", null, true)));
        node.getNodes().add(new ExpressionTree.Node(new Expression("B", null, true)));
        assertThat(new ExpressionTree(node).getSentence(), is("((@A)(@B))"));

        node = new ExpressionTree.Node(new Expression("@A@B", "?", false));
        node.getNodes().add(new ExpressionTree.Node(new Expression("A", null, true)));
        node.getNodes().add(new ExpressionTree.Node(new Expression("B", null, true)));
        assertThat(new ExpressionTree(node).getSentence(), is("((@A)(@B))?"));

        node = new ExpressionTree.Node(new Expression("((a))", null, false));
        node.getNodes().add(new ExpressionTree.Node(new Expression("(a)", null, false), true));
        assertThat(new ExpressionTree(node).getSentence(), is("((a))"));
    }

    @Test
    public void getWordTest() {
        ExpressionTree.Node node;

        node = new ExpressionTree.Node(new Expression("", null, false));
        assertThat(node.toWord(), is(""));

        node = new ExpressionTree.Node(new Expression("", "?", true));
        assertThat(node.toWord(), is(""));

        node = new ExpressionTree.Node(new Expression("A", null, false));
        assertThat(node.toWord(), is("(A)"));

        node = new ExpressionTree.Node(new Expression("AB", null, false));
        assertThat(node.toWord(), is("(AB)"));

        node = new ExpressionTree.Node(new Expression("AB", "?", false));
        assertThat(node.toWord(), is("(AB)?"));

        node = new ExpressionTree.Node(new Expression("AB", "{1,2}", false));
        assertThat(node.toWord(), is("(AB){1,2}"));

        node = new ExpressionTree.Node(new Expression("AB", "{1,2}", true));
        assertThat(node.toWord(), is("(@AB){1,2}"));
    }

    //
    //            A
    //          /   \
    //         B     C
    //       / \    / \
    //      D   E  F   eps
    //           \
    //            G
    //
    private ExpressionTree createSimpleTree() {
        ExpressionTree.Node A = createSimpleLeaf("A");
        ExpressionTree.Node B = createSimpleLeaf("B");
        ExpressionTree.Node C = createSimpleLeaf("C");
        ExpressionTree.Node D = createSimpleLeaf("D");
        ExpressionTree.Node E = createSimpleLeaf("E");
        ExpressionTree.Node F = createSimpleLeaf("F");
        ExpressionTree.Node G = createSimpleLeaf("G");
        ExpressionTree.Node eps = createSimpleLeaf("");

        A.getNodes().add(B);
        A.getNodes().add(C);

        B.getNodes().add(D);
        B.getNodes().add(E);

        E.getNodes().add(G);

        C.getNodes().add(F);
        C.getNodes().add(eps);

        return new ExpressionTree(A);
    }

    private ExpressionTree.Node createSimpleLeaf(final String expression) {
        return new ExpressionTree.Node(new Expression(expression, null, false));
    }
}