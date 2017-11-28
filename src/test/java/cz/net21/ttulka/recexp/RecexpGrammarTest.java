package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author ttulka
 */
public class RecexpGrammarTest {

    @Test
    public void emptyConstructorTest() {
        RecexpGrammar grammar = new RecexpGrammar();
        assertThat(grammar.rules, empty());
    }

    @Test
    public void rulesConstructorTest() {
        RecexpGrammar grammar1 = new RecexpGrammar(new RecexpRule(""));
        assertThat(grammar1.rules.size(), is(1));

        RecexpGrammar grammar2 = new RecexpGrammar(new RecexpRule("a"), new RecexpRule("b"));
        assertThat(grammar2.rules.size(), is(2));

        RecexpGrammar grammar3 = new RecexpGrammar(new RecexpRule("a"), new RecexpRule("b"), new RecexpRule("a"));
        assertThat(grammar3.rules.size(), is(2));
    }

    @Test
    public void stringsConstructorTest() {
        RecexpGrammar grammar1 = new RecexpGrammar("");
        assertThat(grammar1.rules.size(), is(1));

        RecexpGrammar grammar2 = new RecexpGrammar("a", "b");
        assertThat(grammar2.rules.size(), is(2));

        RecexpGrammar grammar3 = new RecexpGrammar("a", "b", "a");
        assertThat(grammar3.rules.size(), is(2));
    }

    @Test
    public void addRuleTest() {
        RecexpGrammar grammar1 = new RecexpGrammar()
                .addRule(new RecexpRule(""));
        assertThat(grammar1.rules.size(), is(1));

        RecexpGrammar grammar2 = new RecexpGrammar()
                .addRule(new RecexpRule("a"))
                .addRule(new RecexpRule("b"));
        assertThat(grammar2.rules.size(), is(2));

        RecexpGrammar grammar3 = new RecexpGrammar()
                .addRule(new RecexpRule("a"))
                .addRule(new RecexpRule("b"))
                .addRule(new RecexpRule("a"));
        assertThat(grammar3.rules.size(), is(2));
    }

    @Test
    public void addStringRuleTest() {
        RecexpGrammar grammar1 = new RecexpGrammar()
                .addRule("");
        assertThat(grammar1.rules.size(), is(1));

        RecexpGrammar grammar2 = new RecexpGrammar()
                .addRule("a")
                .addRule("b");
        assertThat(grammar2.rules.size(), is(2));

        RecexpGrammar grammar3 = new RecexpGrammar()
                .addRule("a")
                .addRule("b")
                .addRule("a");
        assertThat(grammar3.rules.size(), is(2));
    }

    @Test
    public void addStringNamedRuleTest() {
        RecexpGrammar grammar1 = new RecexpGrammar()
                .addRule("E", "");
        assertThat(grammar1.rules.size(), is(1));

        RecexpGrammar grammar2 = new RecexpGrammar()
                .addRule("A", "a")
                .addRule("B", "b");
        assertThat(grammar2.rules.size(), is(2));

        RecexpGrammar grammar3 = new RecexpGrammar()
                .addRule("A", "a")
                .addRule("B", "b")
                .addRule("A", "a");
        assertThat(grammar3.rules.size(), is(2));
    }

    @Test
    public void matcherTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("A", "a")
                .addRule("B", "b")
                .addRule("A", "a");

        RecexpMatcher matcher = grammar.matcher("abc");

        assertThat(matcher.value(), is("abc"));

        // TODO more tests
    }

    @Test(expected = RecexpEmptyRulesException.class)
    public void emptyMatcherExceptionTest() {
        new RecexpGrammar().matcher("");

        fail("Cannot create a matcher from an empty grammar.");
    }

    @Test
    public void getCartesianProductTest() {
        Collection<RecexpGrammar.LeafCombination> combinations = Arrays.asList(
                createLeafCombination("A", "B"),
                createLeafCombination("o"),
                createLeafCombination("x", "y", "z"));

        Set<Set<RecexpGrammar.LeafCandidate>> product = new RecexpGrammar().getCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "Aox", "Aoy", "Aoz", "Box", "Boy", "Boz"
        ));
    }

    @Test
    public void getCartesianProductWithEpsilonTest() {
        Collection<RecexpGrammar.LeafCombination> combinations = Arrays.asList(
                createLeafCombination("a", "b"),
                createLeafCombination("x", ""));

        Set<Set<RecexpGrammar.LeafCandidate>> product = new RecexpGrammar().getCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "ax", "bx", "a", "b"
        ));
    }

    private RecexpGrammar.LeafCombination createLeafCombination(String... combinations) {
        Set<ExpressionTree.Node> combinationNodes = new HashSet<ExpressionTree.Node>();
        for (String c : combinations) {
            combinationNodes.add(ExpressionTree.Node.parseNode(c));
        }
        RecexpGrammar.LeafCombination lc = mock(RecexpGrammar.LeafCombination.class);
        when(lc.getCombinations()).thenReturn(combinationNodes);
        when(lc.getNode()).thenReturn(mock(ExpressionTree.Node.class));
        return lc;
    }

    private Set<String> getStringsFromCartesianProduct(Set<Set<RecexpGrammar.LeafCandidate>> product) {
        Set<String> candidates = new HashSet<String>();

        for (Set<RecexpGrammar.LeafCandidate> candidateSet : product) {
            List<RecexpGrammar.LeafCandidate> candidateList = new ArrayList<RecexpGrammar.LeafCandidate>(candidateSet);
            Collections.sort(candidateList, new Comparator<RecexpGrammar.LeafCandidate>() {
                @Override
                public int compare(RecexpGrammar.LeafCandidate o1, RecexpGrammar.LeafCandidate o2) {
                    return o1.getCandidate().toWord().compareTo(o2.getCandidate().toWord());
                }
            });
            StringBuilder sb = new StringBuilder();
            for (RecexpGrammar.LeafCandidate candidate : candidateList) {
                sb.append(candidate.getCandidate().getExpression().getText());
            }
            candidates.add(sb.toString());
        }
        return candidates;
    }

    @Test
    public void generateCombinationsTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("A", "a@this")
                .addRule("A", "a")
                .addRule("A", "")

                .addRule("B", "b?")

                .addRule("C", "c");

        ExpressionTree.Node node;

        node = new ExpressionTree.Node(new Expression("A", null, false));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(A)"));

        node = new ExpressionTree.Node(new Expression("A", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(a@this)", "(a)", ""));

        node = new ExpressionTree.Node(new Expression("B", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(b)?", ""));

        node = new ExpressionTree.Node(new Expression("C", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(c)"));

        node = new ExpressionTree.Node(new Expression("C", "?", true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(c)?", ""));

        node = new ExpressionTree.Node(new Expression("this", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(@this)"));

        node = new ExpressionTree.Node(new Expression("this", "?", true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(@this)?", ""));
    }

    private Set<String> combinationsToString(Set<ExpressionTree.Node> combinations) {
        Set<String> strings = new HashSet<String>();
        for (ExpressionTree.Node node : combinations) {
            strings.add(node.toWord());
        }
        return strings;
    }

    @Test
    public void createTreeTest() {
        ExpressionTree tree;

        tree = ExpressionTree.parseTree("a");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(a)"));
        assertThat(tree.getSentence(), is("(a)"));
        assertThat(tree.getLeaves().size(), is(1));

        tree = ExpressionTree.parseTree("ab");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(ab)"));
        assertThat(tree.getSentence(), is("(ab)"));
        assertThat(tree.getLeaves().size(), is(1));

        tree = ExpressionTree.parseTree("@A");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(@A)"));
        assertThat(tree.getSentence(), is("(@A)"));
        assertThat(tree.getLeaves().size(), is(1));

        tree = ExpressionTree.parseTree("x(@A)y");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(x(@A)y)"));
        assertThat(tree.getSentence(), is("(x)(@A)(y)"));
        assertThat(tree.getLeaves().size(), is(3));
        assertThat(tree.getRoot().getNodes().size(), is(3));
        assertThat(tree.getRoot().getNodes().get(0).toWord(), is("(x)"));
        assertThat(tree.getRoot().getNodes().get(1).toWord(), is("(@A)"));
        assertThat(tree.getRoot().getNodes().get(2).toWord(), is("(y)"));

        tree = ExpressionTree.parseTree("@A@B");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(@A@B)"));
        assertThat(tree.getSentence(), is("(@A)(@B)"));
        assertThat(tree.getLeaves().size(), is(2));
        assertThat(tree.getRoot().getNodes().size(), is(2));
        assertThat(tree.getRoot().getNodes().get(0).toWord(), is("(@A)"));
        assertThat(tree.getRoot().getNodes().get(1).toWord(), is("(@B)"));

        tree = ExpressionTree.parseTree("a@this?b");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(a@this?b)"));
        assertThat(tree.getSentence(), is("(a)(@this)?(b)"));
        assertThat(tree.getLeaves().size(), is(3));
        assertThat(tree.getRoot().getNodes().size(), is(3));
        assertThat(tree.getRoot().getNodes().get(0).toWord(), is("(a)"));
        assertThat(tree.getRoot().getNodes().get(1).toWord(), is("(@this)?"));
        assertThat(tree.getRoot().getNodes().get(2).toWord(), is("(b)"));

        tree = ExpressionTree.parseTree("a((@this(x))?)b");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(a((@this(x))?)b)"));
        assertThat(tree.getSentence(), is("(a)(((@this)(x))?)(b)"));
        assertThat(tree.getLeaves().size(), is(4));
        assertThat(tree.getRoot().getNodes().size(), is(3));
        assertThat(tree.getRoot().getNodes().get(0).toWord(), is("(a)"));
        assertThat(tree.getRoot().getNodes().get(1).toWord(), is("((@this(x))?)"));
        assertThat(tree.getRoot().getNodes().get(1).getNodes().size(), is(1));
        assertThat(tree.getRoot().getNodes().get(1).getNodes().get(0).toWord(), is("(@this(x))?"));
        assertThat(tree.getRoot().getNodes().get(1).getNodes().get(0).getNodes().size(), is(2));
        assertThat(tree.getRoot().getNodes().get(1).getNodes().get(0).getNodes().get(0).toWord(), is("(@this)"));
        assertThat(tree.getRoot().getNodes().get(1).getNodes().get(0).getNodes().get(1).toWord(), is("(x)"));
        assertThat(tree.getRoot().getNodes().get(2).toWord(), is("(b)"));

        tree = ExpressionTree.parseTree("ab(12)@this@A?(@B)(@C?)cd?((@D))((@E)(@F))?(a@REF)");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("(ab(12)@this@A?(@B)(@C?)cd?((@D))((@E)(@F))?(a@REF))"));
        assertThat(tree.getSentence(), is("(ab)(12)(@this)(@A)?(@B)(@C?)(cd)?((@D))((@E)(@F))?((a)(@REF))"));
        assertThat(tree.getLeaves().size(), is(12));
        assertThat(tree.getRoot().getNodes().size(), is(10));
        assertThat(tree.getRoot().getNodes().get(0).toWord(), is("(ab)"));
        assertThat(tree.getRoot().getNodes().get(1).toWord(), is("(12)"));
        assertThat(tree.getRoot().getNodes().get(2).toWord(), is("(@this)"));
        assertThat(tree.getRoot().getNodes().get(3).toWord(), is("(@A)?"));
        assertThat(tree.getRoot().getNodes().get(4).toWord(), is("(@B)"));
        assertThat(tree.getRoot().getNodes().get(5).toWord(), is("(@C?)"));
        assertThat(tree.getRoot().getNodes().get(6).toWord(), is("(cd)?"));
        assertThat(tree.getRoot().getNodes().get(7).toWord(), is("((@D))"));
        assertThat(tree.getRoot().getNodes().get(7).getNodes().size(), is(1));
        assertThat(tree.getRoot().getNodes().get(7).getNodes().get(0).toWord(), is("(@D)"));
        assertThat(tree.getRoot().getNodes().get(8).toWord(), is("((@E)(@F))?"));
        assertThat(tree.getRoot().getNodes().get(8).getNodes().size(), is(2));
        assertThat(tree.getRoot().getNodes().get(8).getNodes().get(0).toWord(), is("(@E)"));
        assertThat(tree.getRoot().getNodes().get(8).getNodes().get(1).toWord(), is("(@F)"));
        assertThat(tree.getRoot().getNodes().get(9).toWord(), is("(a@REF)"));
        assertThat(tree.getRoot().getNodes().get(9).getNodes().size(), is(2));
        assertThat(tree.getRoot().getNodes().get(9).getNodes().get(0).toWord(), is("(a)"));
        assertThat(tree.getRoot().getNodes().get(9).getNodes().get(1).toWord(), is("(@REF)"));
    }

    @Test
    public void extendTreeTest() {
        RecexpGrammar grammar = new RecexpGrammar();
        ExpressionTree tree = ExpressionTree.parseTree("@A@B");

        RecexpGrammar.LeafCandidate candidate1 = grammar. new LeafCandidate(
                tree.getLeaves().get(0), ExpressionTree.Node.parseNode("a")
        );
        RecexpGrammar.LeafCandidate candidate2 = grammar. new LeafCandidate(
                tree.getLeaves().get(1), ExpressionTree.Node.parseNode("b")
        );

        ExpressionTree extended = grammar.extendTree(tree, new HashSet<RecexpGrammar.LeafCandidate>(
                Arrays.asList(candidate1, candidate2)
        ));

        assertThat(extended, not(nullValue()));
        assertThat(extended.getRoot().toWord(), is("(@A@B)"));
        assertThat(extended.getSentence(), is("(a)(b)"));
        assertThat(extended.getLeaves().size(), is(2));
        assertThat(extended.getRoot().getNodes().size(), is(2));
        assertThat(extended.getRoot().getNodes().get(0).toWord(), is("(a)"));
        assertThat(extended.getRoot().getNodes().get(1).toWord(), is("(b)"));
    }

    @Test
    public void reduceTreeTest() {
        RecexpGrammar grammar = new RecexpGrammar();
        ExpressionTree tree = ExpressionTree.parseTree("@A@B");

        List<RecexpGroup> groups = grammar.reduceTree(tree);

        assertThat(groups, not(nullValue()));
        assertThat(groups.size(), is(1));
        assertThat(groups.get(0).name(), is("(@A@B)"));
        assertThat(groups.get(0).value(), is("(@A)(@B)"));
        assertThat(groups.get(0).groupCount(), is(2));
        assertThat(groups.get(0).group(1).name(), is("(@A)"));
        assertThat(groups.get(0).group(1).value(), is("(@A)"));
        assertThat(groups.get(0).group(1).groupCount(), is(0));
        assertThat(groups.get(0).group(2).name(), is("(@B)"));
        assertThat(groups.get(0).group(2).value(), is("(@B)"));
        assertThat(groups.get(0).group(2).groupCount(), is(0));
    }
}
