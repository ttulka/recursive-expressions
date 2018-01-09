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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author ttulka
 */
public class GrammarTest {

    @Test(expected = IllegalStateException.class)
    public void emptyBuilderTest() {
        Recexp.RecexpBuilder builder = Recexp.builder();
        assertThat(builder, not(nullValue()));
        builder.build();    // rules set cannot be empty
    }

    @Test
    public void builderTest() {
        Recexp.RecexpBuilder builder = Recexp.builder();
        assertThat(builder, not(nullValue()));
        builder.rule("a");
        builder.rule("b");

        Recexp grammar = builder.build();
        assertThat(grammar, not(nullValue()));
        assertThat(grammar.rules.size(), is(2 + 1));
    }

    @Test
    public void builderNamedRulesTest() {
        Recexp.RecexpBuilder builder = Recexp.builder();
        assertThat(builder, not(nullValue()));
        builder.rule("A", "a");
        builder.rule("B", "b");

        Recexp grammar = builder.build();
        assertThat(grammar, not(nullValue()));
        assertThat(grammar.rules.size(), is(2 + 1));
    }

    @Test
    public void builderCombinedRulesTest() {
        Recexp.RecexpBuilder builder = Recexp.builder();
        assertThat(builder, not(nullValue()));
        builder.rule("a");
        builder.rule("B", "b");

        Recexp grammar = builder.build();
        assertThat(grammar, not(nullValue()));
        assertThat(grammar.rules.size(), is(2 + 1));
    }

    @Test
    public void rulesConstructorTest() {
        Recexp grammar1 = Recexp.compile("");
        assertThat(grammar1.rules.size(), is(1 + 1));

        Recexp grammar = Recexp.compile("a");
        assertThat(grammar.rules.size(), is(1 + 1));

        Recexp grammar2 = Recexp.compile("a", "b");
        assertThat(grammar2.rules.size(), is(2 + 1));

        Recexp grammar3 = Recexp.compile("a", "b", "a");
        assertThat(grammar3.rules.size(), is(2 + 1));
    }

    @Test
    public void matcherTest() {
        Recexp grammar;
        RecexpMatcher matcher;

        grammar = Recexp.builder()
                .rule("@A@B")
                .rule("A", "a")
                .rule("B", "b")
                .build();

        matcher = grammar.matcher("ab");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.name(), is("@A@B"));
        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.groupCount(), is(2));
        assertThat(matcher.group(1).name(), is("@A"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).name(), is("a"));
        assertThat(matcher.group(1).group(1).value(), is("a"));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));
        assertThat(matcher.group(2).name(), is("@B"));
        assertThat(matcher.group(2).value(), is("b"));
        assertThat(matcher.group(2).groupCount(), is(1));
        assertThat(matcher.group(2).group(1).name(), is("b"));
        assertThat(matcher.group(2).group(1).value(), is("b"));
        assertThat(matcher.group(2).group(1).groupCount(), is(0));

        grammar = Recexp.compile("a@this?b");
        matcher = grammar.matcher("ab");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.name(), is("a@this?b"));
        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).name(), is("@this?"));
        assertThat(matcher.group(2).value(), is(""));
        assertThat(matcher.group(2).groupCount(), is(0));
        assertThat(matcher.group(3).name(), is("b"));
        assertThat(matcher.group(3).value(), is("b"));
        assertThat(matcher.group(3).groupCount(), is(0));

        grammar = Recexp.compile("a(@this?)b");
        matcher = grammar.matcher("ab");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.name(), is("a(@this?)b"));
        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).name(), is("@this?"));
        assertThat(matcher.group(2).value(), is(""));
        assertThat(matcher.group(2).groupCount(), is(0));
        assertThat(matcher.group(3).name(), is("b"));
        assertThat(matcher.group(3).value(), is("b"));
        assertThat(matcher.group(3).groupCount(), is(0));

        grammar = Recexp.compile("a(@this)?b");
        matcher = grammar.matcher("ab");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.name(), is("a(@this)?b"));
        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).name(), is("@this?"));
        assertThat(matcher.group(2).value(), is(""));
        assertThat(matcher.group(2).groupCount(), is(0));
        assertThat(matcher.group(3).name(), is("b"));
        assertThat(matcher.group(3).value(), is("b"));
        assertThat(matcher.group(3).groupCount(), is(0));

        grammar = Recexp.builder()
                .rule("@A@this?@B")
                .rule("A", "a")
                .rule("B", "b")
                .build();
        matcher = grammar.matcher("aabb");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.name(), is("@A@this?@B"));
        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).name(), is("@A"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).name(), is("a"));
        assertThat(matcher.group(1).group(1).value(), is("a"));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));

        assertThat(matcher.group(2).name(), is("@this?"));
        assertThat(matcher.group(2).value(), is("ab"));
        assertThat(matcher.group(2).groupCount(), is(1));
        assertThat(matcher.group(2).group(1).name(), is("(@A@this?@B)?"));
        assertThat(matcher.group(2).group(1).value(), is("ab"));
        assertThat(matcher.group(2).group(1).groupCount(), is(3));

        assertThat(matcher.group(3).name(), is("@B"));
        assertThat(matcher.group(3).value(), is("b"));
        assertThat(matcher.group(3).groupCount(), is(1));
        assertThat(matcher.group(3).group(1).name(), is("b"));
        assertThat(matcher.group(3).group(1).value(), is("b"));
        assertThat(matcher.group(3).group(1).groupCount(), is(0));

        grammar = Recexp.builder()
                .rule("AB", "a(@CD)b|x")
                .rule("CD", "c(@AB)d")
                .build();
        matcher = grammar.matcher("cxd");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.name(), is("@CD"));
        assertThat(matcher.value(), is("cxd"));
        assertThat(matcher.groupCount(), is(3));

        assertThat(matcher.group(1).name(), is("c"));
        assertThat(matcher.group(1).value(), is("c"));
        assertThat(matcher.group(1).groupCount(), is(0));

        assertThat(matcher.group(2).name(), is("@AB"));
        assertThat(matcher.group(2).value(), is("x"));
        assertThat(matcher.group(2).groupCount(), is(1));
        assertThat(matcher.group(2).group(1).name(), is("x"));
        assertThat(matcher.group(2).group(1).value(), is("x"));
        assertThat(matcher.group(2).group(1).groupCount(), is(0));

        assertThat(matcher.group(3).name(), is("d"));
        assertThat(matcher.group(3).value(), is("d"));
        assertThat(matcher.group(3).groupCount(), is(0));

        // TODO more tests
    }

    @Test
    public void matcherStartingRuleTest() {
        Recexp grammar = Recexp.builder()
                .rule("A", "a")
                .rule("B", "b")
                .build();

        assertThat(grammar.matcher("a").matches(), is(true));
        assertThat(grammar.matcher("b").matches(), is(true));
        assertThat(grammar.matcher("").matches(), is(false));

        assertThat(grammar.matcher("A", "a").matches(), is(true));
        assertThat(grammar.matcher("B", "b").matches(), is(true));
        assertThat(grammar.matcher("B", "a").matches(), is(false));
        assertThat(grammar.matcher("A", "b").matches(), is(false));
        assertThat(grammar.matcher("").matches(), is(false));
    }

    @Test
    public void generateCartesianProductTest() {
        Collection<Recexp.NodeCombinationsHolder> combinations = Arrays.asList(
                createLeafCombination("A", "B"),
                createLeafCombination("o"),
                createLeafCombination("x", "y", "z"));

        Set<Set<Recexp.NodeCandidate>> product = Recexp.generateCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "Aox", "Aoy", "Aoz", "Box", "Boy", "Boz"
        ));
    }

    @Test
    public void generateCartesianProductWithEpsilonTest() {
        Collection<Recexp.NodeCombinationsHolder> combinations = Arrays.asList(
                createLeafCombination("a", "b"),
                createLeafCombination("x", ""));

        Set<Set<Recexp.NodeCandidate>> product = Recexp.generateCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "ax", "bx", "a", "b"
        ));
    }

    private Recexp.NodeCombinationsHolder createLeafCombination(String... combinations) {
        Set<ExpressionTree.Node> combinationNodes = new HashSet<ExpressionTree.Node>();
        for (String c : combinations) {
            combinationNodes.add(ExpressionTree.Node.parseNode(c));
        }
        Recexp.NodeCombinationsHolder lc = mock(Recexp.NodeCombinationsHolder.class);
        when(lc.getCombinations()).thenReturn(combinationNodes);
        when(lc.getNode()).thenReturn(mock(ExpressionTree.Node.class));
        return lc;
    }

    private Set<String> getStringsFromCartesianProduct(Set<Set<Recexp.NodeCandidate>> product) {
        Set<String> candidates = new HashSet<String>();

        for (Set<Recexp.NodeCandidate> candidateSet : product) {
            List<Recexp.NodeCandidate> candidateList = new ArrayList<Recexp.NodeCandidate>(candidateSet);
            Collections.sort(candidateList, new Comparator<Recexp.NodeCandidate>() {
                @Override
                public int compare(Recexp.NodeCandidate o1, Recexp.NodeCandidate o2) {
                    return o1.getCandidate().toWord().compareTo(o2.getCandidate().toWord());
                }
            });
            StringBuilder sb = new StringBuilder();
            for (Recexp.NodeCandidate candidate : candidateList) {
                sb.append(candidate.getCandidate().getExpression().getText());
            }
            candidates.add(sb.toString());
        }
        return candidates;
    }

    @Test
    public void generateCombinationsORTest() {
        Recexp grammar = Recexp.compile("a|b");

        ExpressionTree.Node node;

        node = ExpressionTree.Node.parseNode("a|b");
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("a", "b"));
    }

    @Test
    public void generateCombinationsTest() {
        Recexp grammar = Recexp.builder()
                .rule("A", "a@this")
                .rule("A", "a")
                .rule("A", "")

                .rule("B", "b?")

                .rule("C", "c")

                .build();

        ExpressionTree.Node node;

        node = new ExpressionTree.Node(new Expression("A", null, false));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("A"));

        node = new ExpressionTree.Node(new Expression("A", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("a@this", "a", ""));

        node = new ExpressionTree.Node(new Expression("B", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("b?", ""));

        node = new ExpressionTree.Node(new Expression("C", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("c"));

        node = new ExpressionTree.Node(new Expression("C", "?", true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("(c)?", ""));

        node = new ExpressionTree.Node(new Expression("this", null, true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("@this"));

        node = new ExpressionTree.Node(new Expression("this", "?", true));
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("@this??", ""));
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
        assertThat(tree.getRoot().toWord(), is("a"));
        assertThat(tree.getSentence(), is("a"));
        assertThat(tree.getLeaves().size(), is(1));

        tree = ExpressionTree.parseTree("ab");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("ab"));
        assertThat(tree.getSentence(), is("ab"));
        assertThat(tree.getLeaves().size(), is(1));

        tree = ExpressionTree.parseTree("@A");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("@A"));
        assertThat(tree.getSentence(), is("@A"));
        assertThat(tree.getLeaves().size(), is(1));

        tree = ExpressionTree.parseTree("x(@A)y");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("x(@A)y"));
        assertThat(tree.getSentence(), is("x(@A)y"));
        assertThat(tree.getLeaves().size(), is(3));
        assertThat(tree.getRoot().getSubNodes().size(), is(3));
        assertThat(tree.getRoot().getSubNodes().get(0).toWord(), is("x"));
        assertThat(tree.getRoot().getSubNodes().get(1).toWord(), is("@A"));
        assertThat(tree.getRoot().getSubNodes().get(2).toWord(), is("y"));

        tree = ExpressionTree.parseTree("@A@B");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("@A@B"));
        assertThat(tree.getSentence(), is("@A@B"));
        assertThat(tree.getLeaves().size(), is(2));
        assertThat(tree.getRoot().getSubNodes().size(), is(2));
        assertThat(tree.getRoot().getSubNodes().get(0).toWord(), is("@A"));
        assertThat(tree.getRoot().getSubNodes().get(1).toWord(), is("@B"));

        tree = ExpressionTree.parseTree("a@this?b");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("a@this?b"));
        assertThat(tree.getSentence(), is("a@this?b"));
        assertThat(tree.getLeaves().size(), is(3));
        assertThat(tree.getRoot().getSubNodes().size(), is(3));
        assertThat(tree.getRoot().getSubNodes().get(0).toWord(), is("a"));
        assertThat(tree.getRoot().getSubNodes().get(1).toWord(), is("@this?"));
        assertThat(tree.getRoot().getSubNodes().get(2).toWord(), is("b"));

        tree = ExpressionTree.parseTree("a((@this(x))?)b");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getSentence(), is("a((@this)x)?b"));
        assertThat(tree.getRoot().toWord(), is("a((@this(x))?)b"));
        assertThat(tree.getRoot().getSentence(), is("a((@this)x)?b"));
        assertThat(tree.getLeaves().size(), is(4));
        assertThat(tree.getRoot().getSubNodes().size(), is(3));
        assertThat(tree.getRoot().getSubNodes().get(0).toWord(), is("a"));
        assertThat(tree.getRoot().getSubNodes().get(0).getSentence(), is("a"));
        assertThat(tree.getRoot().getSubNodes().get(1).toWord(), is("(@this(x))?"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSentence(), is("((@this)x)?"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().size(), is(1));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).toWord(), is("(@this(x))?"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).getSentence(), is("((@this)x)?"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).getSubNodes().size(), is(2));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).getSubNodes().get(0).toWord(), is("@this"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).getSubNodes().get(0).getSentence(), is("@this"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).getSubNodes().get(1).toWord(), is("x"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSubNodes().get(0).getSubNodes().get(1).getSentence(), is("x"));
        assertThat(tree.getRoot().getSubNodes().get(2).toWord(), is("b"));
        assertThat(tree.getRoot().getSubNodes().get(2).getSentence(), is("b"));

        tree = ExpressionTree.parseTree("ab(12)@this@A?(@B)(@C?)cd?((@D))((@E)(@F))?(a@REF)");
        assertThat(tree, not(nullValue()));
        assertThat(tree.getRoot().toWord(), is("ab(12)@this@A?(@B)(@C?)cd?((@D))((@E)(@F))?(a@REF)"));
        assertThat(tree.getSentence(), is("ab12@this@A?@B@C?cd?@D(@E@F)?a@REF"));
        assertThat(tree.getLeaves().size(), is(12));
        assertThat(tree.getRoot().getSubNodes().size(), is(10));
        assertThat(tree.getRoot().getSubNodes().get(0).toWord(), is("ab"));
        assertThat(tree.getRoot().getSubNodes().get(0).getSentence(), is("ab"));
        assertThat(tree.getRoot().getSubNodes().get(1).toWord(), is("12"));
        assertThat(tree.getRoot().getSubNodes().get(1).getSentence(), is("12"));
        assertThat(tree.getRoot().getSubNodes().get(2).toWord(), is("@this"));
        assertThat(tree.getRoot().getSubNodes().get(2).getSentence(), is("@this"));
        assertThat(tree.getRoot().getSubNodes().get(3).toWord(), is("@A?"));
        assertThat(tree.getRoot().getSubNodes().get(3).getSentence(), is("@A?"));
        assertThat(tree.getRoot().getSubNodes().get(4).toWord(), is("@B"));
        assertThat(tree.getRoot().getSubNodes().get(4).getSentence(), is("@B"));
        assertThat(tree.getRoot().getSubNodes().get(5).toWord(), is("@C?"));
        assertThat(tree.getRoot().getSubNodes().get(5).getSentence(), is("@C?"));
        assertThat(tree.getRoot().getSubNodes().get(6).toWord(), is("cd?"));
        assertThat(tree.getRoot().getSubNodes().get(6).getSentence(), is("cd?"));
        assertThat(tree.getRoot().getSubNodes().get(7).toWord(), is("(@D)"));
        assertThat(tree.getRoot().getSubNodes().get(7).getSentence(), is("@D"));
        assertThat(tree.getRoot().getSubNodes().get(7).getSubNodes().size(), is(1));
        assertThat(tree.getRoot().getSubNodes().get(7).getSubNodes().get(0).toWord(), is("@D"));
        assertThat(tree.getRoot().getSubNodes().get(7).getSubNodes().get(0).getSentence(), is("@D"));
        assertThat(tree.getRoot().getSubNodes().get(8).toWord(), is("((@E)(@F))?"));
        assertThat(tree.getRoot().getSubNodes().get(8).getSentence(), is("(@E@F)?"));
        assertThat(tree.getRoot().getSubNodes().get(8).getSubNodes().size(), is(2));
        assertThat(tree.getRoot().getSubNodes().get(8).getSubNodes().get(0).toWord(), is("@E"));
        assertThat(tree.getRoot().getSubNodes().get(8).getSubNodes().get(0).getSentence(), is("@E"));
        assertThat(tree.getRoot().getSubNodes().get(8).getSubNodes().get(1).toWord(), is("@F"));
        assertThat(tree.getRoot().getSubNodes().get(8).getSubNodes().get(1).getSentence(), is("@F"));
        assertThat(tree.getRoot().getSubNodes().get(9).toWord(), is("a@REF"));
        assertThat(tree.getRoot().getSubNodes().get(9).getSentence(), is("a@REF"));
        assertThat(tree.getRoot().getSubNodes().get(9).getSubNodes().size(), is(2));
        assertThat(tree.getRoot().getSubNodes().get(9).getSubNodes().get(0).toWord(), is("a"));
        assertThat(tree.getRoot().getSubNodes().get(9).getSubNodes().get(0).getSentence(), is("a"));
        assertThat(tree.getRoot().getSubNodes().get(9).getSubNodes().get(1).toWord(), is("@REF"));
        assertThat(tree.getRoot().getSubNodes().get(9).getSubNodes().get(1).getSentence(), is("@REF"));
    }

    @Test
    public void nodeToGroupTest() {
        ExpressionTree tree;
        RecexpGroup group;

        tree = ExpressionTree.parseTree("(a?)(b?)");
        group = Recexp.nodeToGroup(tree.getRoot(), "ab", 0);

        assertThat(group, not(nullValue()));
        assertThat(group.groupCount(), is(2));
        assertThat(group.group(1).name(), is("a?"));
        assertThat(group.group(1).value(), is("a"));
        assertThat(group.group(1).groupCount(), is(0));
        assertThat(group.group(2).name(), is("b?"));
        assertThat(group.group(2).value(), is("b"));
        assertThat(group.group(2).groupCount(), is(0));

        tree = ExpressionTree.parseTree("a?b?");
        group = Recexp.nodeToGroup(tree.getRoot(), "ab", 0);

        assertThat(group, not(nullValue()));
        assertThat(group.groupCount(), is(0));
    }

    @Test(expected = IllegalStateException.class)
    public void nodeToGroupExceptionTest() {
        ExpressionTree tree;
        RecexpGroup group;

        tree = ExpressionTree.parseTree("(a)?(b)?");
        group = Recexp.nodeToGroup(tree.getRoot(), "x", 0);
        System.out.println(group);
    }

    @Test(expected = IllegalStateException.class)
    public void nodeToGroupOrExceptionTest() {
        ExpressionTree tree;
        RecexpGroup group;

        tree = ExpressionTree.parseTree("(a)?(a)?|(b)?(b)?");
        group = Recexp.nodeToGroup(tree.getRoot(), "x", 0);
        System.out.println(group);
    }

    @Test
    public void deriveTreeTest() {
        ExpressionTree.Node derivative;

        derivative = Recexp.compile("")
                .deriveTree(ExpressionTree.parseTree("a").getRoot(), "a", new HashSet<String>());

        assertThat(derivative, not(nullValue()));
        assertThat(derivative.getSubNodesConnectionType(), is(ExpressionTree.Node.SubNodesConnectionType.SINGLE));
        assertThat(derivative.getSubNodes().size(), is(0));

        derivative = Recexp.compile("")
                .deriveTree(ExpressionTree.parseTree("a?").getRoot(), "a", new HashSet<String>());

        assertThat(derivative, not(nullValue()));
        assertThat(derivative.getSubNodesConnectionType(), is(ExpressionTree.Node.SubNodesConnectionType.SINGLE));
        assertThat(derivative.getSubNodes().size(), is(0));

        derivative = Recexp.compile("")
                .deriveTree(ExpressionTree.parseTree("a|b").getRoot(), "a", new HashSet<String>());

        assertThat(derivative, not(nullValue()));
        assertThat(derivative.getSubNodesConnectionType(), is(ExpressionTree.Node.SubNodesConnectionType.SINGLE));
        assertThat(derivative.getSubNodes().size(), is(0));
    }

    @Test
    public void checkCyclicRulesFailTest() {
        try {
            Recexp.builder().rule("CYCLIC_RULE", "@CYCLIC_RULE").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder().rule("CYCLIC_RULE", "a@CYCLIC_RULE").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder().rule("CYCLIC_RULE", "(a)@CYCLIC_RULE(b)").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder()
                    .rule("RULE1", "@RULE2")
                    .rule("RULE2", "@RULE1")
                    .build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder()
                    .rule("RULE1", "@RULE2")
                    .rule("RULE2", "@RULE3")
                    .rule("RULE3", "@RULE1")
                    .build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder()
                    .rule("RULE1", "a@RULE2")
                    .rule("RULE2", "b@RULE3")
                    .rule("RULE3", "c@RULE1")
                    .build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder()
                    .rule("RULE1", "a@RULE2(1)")
                    .rule("RULE2", "b@RULE3(2)")
                    .rule("RULE3", "c@RULE1(3)")
                    .build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder().rule("RULE", "@this").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder().rule("RULE", "a@this").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder().rule("RULE", "a@this(b)").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder()
                    .rule("A", "a")
                    .rule("B", "b")
                    .rule("@A@this@B")
                    .build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            Recexp.builder().rule("@this|@this").build()
                    .matches("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
    }

    @Test
    public void checkCyclicRulesPassTest() {
        Recexp.builder().rule("RULE", "a").build().matches("");
        Recexp.builder().rule("RULE", "@RULE?").build().matches("");
        Recexp.builder().rule("RULE", "a@RULE?").build().matches("");
        Recexp.builder().rule("RULE", "a@RULE?b").build().matches("");
        Recexp.builder().rule("RULE", "@this?").build().matches("");
        Recexp.builder().rule("RULE", "a@this?").build().matches("");
        Recexp.builder().rule("RULE", "a@this?b").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE1").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2").rule("RULE2", "@RULE1?").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE1?").build().matches("");
        Recexp.builder().rule("RULE1", "a@RULE2?").rule("RULE2", "b@RULE1?").build().matches("");
        Recexp.builder().rule("RULE1", "a@RULE2?c").rule("RULE2", "b@RULE1?d").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2").rule("RULE2", "@RULE3").rule("RULE3", "@RULE1?").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2").rule("RULE2", "@RULE3?").rule("RULE3", "@RULE1").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE3").rule("RULE3", "@RULE1").build().matches("");
        Recexp.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE3?").rule("RULE3", "@RULE1?").build().matches("");
        Recexp.builder().rule("RULE1", "a@RULE2").rule("RULE2", "b@RULE3").rule("RULE3", "c@RULE1?").build().matches("");
        Recexp.builder().rule("RULE1", "a@RULE2").rule("RULE2", "b@RULE3").rule("RULE3", "c@RULE1?d").build().matches("");
        Recexp.builder().rule("RULE1", "a@RULE2?1").rule("RULE2", "b@RULE3?2").rule("RULE3", "c@RULE1?3").build().matches("");
        Recexp.builder().rule("A", "a").rule("B", "b").rule("@A@this?@B").build().matches("");
        Recexp.builder().rule("@this|a").build().matches("xx");
        Recexp.builder().rule("a|@this|b").build().matches("");
        Recexp.builder().rule("a|@this|b|@this").build().matches("");
        Recexp.builder().rule("(a|@this|b)|(@this|c)").build().matches("");
    }
}
