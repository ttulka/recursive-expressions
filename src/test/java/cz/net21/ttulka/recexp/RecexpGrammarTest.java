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
public class RecexpGrammarTest {

    @Test(expected = IllegalStateException.class)
    public void emptyBuilderTest() {
        RecexpGrammar.RecexpGrammarBuilder builder = RecexpGrammar.builder();
        assertThat(builder, not(nullValue()));
        builder.build();    // rules set cannot be empty
    }

    @Test
    public void builderTest() {
        RecexpGrammar.RecexpGrammarBuilder builder = RecexpGrammar.builder();
        assertThat(builder, not(nullValue()));
        builder.rule("a");
        builder.rule("b");

        RecexpGrammar grammar = builder.build();
        assertThat(grammar, not(nullValue()));
        assertThat(grammar.rules.size(), is(2 + 1));
    }

    @Test
    public void builderNamedRulesTest() {
        RecexpGrammar.RecexpGrammarBuilder builder = RecexpGrammar.builder();
        assertThat(builder, not(nullValue()));
        builder.rule("A", "a");
        builder.rule("B", "b");

        RecexpGrammar grammar = builder.build();
        assertThat(grammar, not(nullValue()));
        assertThat(grammar.rules.size(), is(2 + 1));
    }

    @Test
    public void builderCombinedRulesTest() {
        RecexpGrammar.RecexpGrammarBuilder builder = RecexpGrammar.builder();
        assertThat(builder, not(nullValue()));
        builder.rule("a");
        builder.rule("B", "b");

        RecexpGrammar grammar = builder.build();
        assertThat(grammar, not(nullValue()));
        assertThat(grammar.rules.size(), is(2 + 1));
    }

    @Test
    public void rulesConstructorTest() {
        RecexpGrammar grammar1 = RecexpGrammar.compile("");
        assertThat(grammar1.rules.size(), is(1 + 1));

        RecexpGrammar grammar = RecexpGrammar.compile("a");
        assertThat(grammar.rules.size(), is(1 + 1));

        RecexpGrammar grammar2 = RecexpGrammar.compile("a", "b");
        assertThat(grammar2.rules.size(), is(2 + 1));

        RecexpGrammar grammar3 = RecexpGrammar.compile("a", "b", "a");
        assertThat(grammar3.rules.size(), is(2 + 1));
    }

    @Test
    public void matcherTest() {
        RecexpGrammar grammar;
        RecexpMatcher matcher;

        grammar = RecexpGrammar.builder()
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

        grammar = RecexpGrammar.compile("a@this?b");
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

        grammar = RecexpGrammar.compile("a(@this?)b");
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

        grammar = RecexpGrammar.compile("a(@this)?b");
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

        grammar = RecexpGrammar.builder()
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

        grammar = RecexpGrammar.builder()
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
    public void getCartesianProductTest() {
        Collection<RecexpGrammar.LeafCombination> combinations = Arrays.asList(
                createLeafCombination("A", "B"),
                createLeafCombination("o"),
                createLeafCombination("x", "y", "z"));

        Set<Set<RecexpGrammar.LeafCandidate>> product = RecexpGrammar.compile("")
                .getCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "Aox", "Aoy", "Aoz", "Box", "Boy", "Boz"
        ));
    }

    @Test
    public void getCartesianProductWithEpsilonTest() {
        Collection<RecexpGrammar.LeafCombination> combinations = Arrays.asList(
                createLeafCombination("a", "b"),
                createLeafCombination("x", ""));

        Set<Set<RecexpGrammar.LeafCandidate>> product = RecexpGrammar.compile("")
                .getCartesianProduct(combinations);

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
    public void generateCombinationsORTest() {
        RecexpGrammar grammar = RecexpGrammar.compile("a|b");

        ExpressionTree.Node node;

        node = ExpressionTree.Node.parseNode("a|b");
        assertThat(combinationsToString(grammar.generateCombinations(node, node)),
                   containsInAnyOrder("a", "b"));
    }

    @Test
    public void generateCombinationsTest() {
        RecexpGrammar grammar = RecexpGrammar.builder()
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
        group = RecexpGrammar.nodeToGroup(tree.getRoot(), "ab");

        assertThat(group, not(nullValue()));
        assertThat(group.groupCount(), is(2));
        assertThat(group.group(1).name(), is("a?"));
        assertThat(group.group(1).value(), is("a"));
        assertThat(group.group(1).groupCount(), is(0));
        assertThat(group.group(2).name(), is("b?"));
        assertThat(group.group(2).value(), is("b"));
        assertThat(group.group(2).groupCount(), is(0));

        tree = ExpressionTree.parseTree("a?b?");
        group = RecexpGrammar.nodeToGroup(tree.getRoot(), "ab");

        assertThat(group, not(nullValue()));
        assertThat(group.groupCount(), is(0));
    }

    @Test(expected = IllegalStateException.class)
    public void nodeToGroupExceptionTest() {
        ExpressionTree tree;
        RecexpGroup group;

        tree = ExpressionTree.parseTree("(a)?(b)?");
        group = RecexpGrammar.nodeToGroup(tree.getRoot(), "x");
        System.out.println(group);
    }

    @Test(expected = IllegalStateException.class)
    public void nodeToGroupOrExceptionTest() {
        ExpressionTree tree;
        RecexpGroup group;

        tree = ExpressionTree.parseTree("(a)?(a)?|(b)?(b)?");
        group = RecexpGrammar.nodeToGroup(tree.getRoot(), "x");
        System.out.println(group);
    }

    @Test
    public void derivateTreeTest() {
        ExpressionTree.Node derivate;

        derivate = RecexpGrammar.compile("")
                .derivateTree(ExpressionTree.parseTree("a").getRoot(), "a", new HashSet<String>());

        assertThat(derivate, not(nullValue()));
        assertThat(derivate.getSubNodesConnectionType(), is(ExpressionTree.Node.SubNodesConnectionType.SINGLE));
        assertThat(derivate.getSubNodes().size(), is(0));

        derivate = RecexpGrammar.compile("")
                .derivateTree(ExpressionTree.parseTree("a?").getRoot(), "a", new HashSet<String>());

        assertThat(derivate, not(nullValue()));
        assertThat(derivate.getSubNodesConnectionType(), is(ExpressionTree.Node.SubNodesConnectionType.SINGLE));
        assertThat(derivate.getSubNodes().size(), is(0));

        derivate = RecexpGrammar.compile("")
                .derivateTree(ExpressionTree.parseTree("a|b").getRoot(), "a", new HashSet<String>());

        assertThat(derivate, not(nullValue()));
        assertThat(derivate.getSubNodesConnectionType(), is(ExpressionTree.Node.SubNodesConnectionType.SINGLE));
        assertThat(derivate.getSubNodes().size(), is(0));
    }

    @Test
    public void checkCyclicRulesFailTest() {
        try {
            RecexpGrammar.builder().rule("CYCLIC_RULE", "@CYCLIC_RULE").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder().rule("CYCLIC_RULE", "a@CYCLIC_RULE").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder().rule("CYCLIC_RULE", "(a)@CYCLIC_RULE(b)").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder()
                    .rule("RULE1", "@RULE2")
                    .rule("RULE2", "@RULE1")
                    .build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder()
                    .rule("RULE1", "@RULE2")
                    .rule("RULE2", "@RULE3")
                    .rule("RULE3", "@RULE1")
                    .build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder()
                    .rule("RULE1", "a@RULE2")
                    .rule("RULE2", "b@RULE3")
                    .rule("RULE3", "c@RULE1")
                    .build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder()
                    .rule("RULE1", "a@RULE2(1)")
                    .rule("RULE2", "b@RULE3(2)")
                    .rule("RULE3", "c@RULE1(3)")
                    .build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder().rule("RULE", "@this").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder().rule("RULE", "a@this").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder().rule("RULE", "a@this(b)").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder()
                    .rule("A", "a")
                    .rule("B", "b")
                    .rule("@A@this@B")
                    .build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
        try {
            RecexpGrammar.builder().rule("@this|@this").build()
                    .accepts("");
            fail("Should throw a RecexpCyclicRuleException.");
        } catch (RecexpCyclicRuleException expected) {
        }
    }

    @Test
    public void checkCyclicRulesPassTest() {
        RecexpGrammar.builder().rule("RULE", "a").build().accepts("");
        RecexpGrammar.builder().rule("RULE", "@RULE?").build().accepts("");
        RecexpGrammar.builder().rule("RULE", "a@RULE?").build().accepts("");
        RecexpGrammar.builder()    .rule("RULE", "a@RULE?b").build().accepts("");
        RecexpGrammar.builder().rule("RULE", "@this?").build().accepts("");
        RecexpGrammar.builder().rule("RULE", "a@this?").build().accepts("");
        RecexpGrammar.builder().rule("RULE", "a@this?b").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE1").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2").rule("RULE2", "@RULE1?").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE1?").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "a@RULE2?").rule("RULE2", "b@RULE1?").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "a@RULE2?c").rule("RULE2", "b@RULE1?d").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2").rule("RULE2", "@RULE3").rule("RULE3", "@RULE1?").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2").rule("RULE2", "@RULE3?").rule("RULE3", "@RULE1").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE3").rule("RULE3", "@RULE1").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "@RULE2?").rule("RULE2", "@RULE3?").rule("RULE3", "@RULE1?").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "a@RULE2").rule("RULE2", "b@RULE3").rule("RULE3", "c@RULE1?").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "a@RULE2").rule("RULE2", "b@RULE3").rule("RULE3", "c@RULE1?d").build().accepts("");
        RecexpGrammar.builder().rule("RULE1", "a@RULE2?1").rule("RULE2", "b@RULE3?2").rule("RULE3", "c@RULE1?3").build().accepts("");
        RecexpGrammar.builder().rule("A", "a").rule("B", "b").rule("@A@this?@B").build().accepts("");
        RecexpGrammar.builder().rule("@this|a").build().accepts("xx");
        RecexpGrammar.builder().rule("a|@this|b").build().accepts("");
        RecexpGrammar.builder().rule("a|@this|b|@this").build().accepts("");
        RecexpGrammar.builder().rule("(a|@this|b)|(@this|c)").build().accepts("");
    }
}
