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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.fail;

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
    }

    @Test(expected = RecexpEmptyRulesException.class)
    public void emptyMatcherExceptionTest() {
        new RecexpGrammar().matcher("");

        fail("Cannot create a matcher from an empty grammar.");
    }

    @Test
    public void hydrateExpressionTest() {
        RecexpGrammar grammar = new RecexpGrammar();

        assertThat(grammar.hydrateExpression("\\@this"), is("\\@this"));
        assertThat(grammar.hydrateExpression("ab\\@this"), is("ab\\@this"));
        assertThat(grammar.hydrateExpression("ab\\@this@"), is("ab\\@this@"));
        assertThat(grammar.hydrateExpression("@this"), is("(.*)"));
        assertThat(grammar.hydrateExpression("ab@this"), is("ab(.*)"));
        assertThat(grammar.hydrateExpression("ab@this@"), is("ab(.*)@"));
        assertThat(grammar.hydrateExpression("ab(@this)"), is("ab((.*))"));
    }

    @Test
    public void getCartesianProductTest() {
        RecexpGrammar g = new RecexpGrammar();
        RecexpGrammar.ExpressionTree tree = g.new ExpressionTree();

        RecexpGrammar.ExpressionTree.Leaf leaf1 = tree.new Leaf();
        RecexpGrammar.ExpressionTree.Leaf leaf2 = tree.new Leaf();
        RecexpGrammar.ExpressionTree.Leaf leaf3 = tree.new Leaf();

        RecexpGrammar.LeafCombination lc1 = g.new LeafCombination(leaf1, new HashSet<String>(Arrays.asList("A", "B")));
        RecexpGrammar.LeafCombination lc2 = g.new LeafCombination(leaf2, new HashSet<String>(Arrays.asList("o")));
        RecexpGrammar.LeafCombination lc3 = g.new LeafCombination(leaf3, new HashSet<String>(Arrays.asList("x", "y", "z")));

        Collection<RecexpGrammar.LeafCombination> combinations = Arrays.asList(lc1, lc2, lc3);

        Set<Set<RecexpGrammar.LeafCandidate>> product = g.getCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "Aox", "Aoy", "Aoz", "Box", "Boy", "Boz"
        ));
    }

    @Test
    public void getCartesianProductWithEpsilonTest() {
        RecexpGrammar g = new RecexpGrammar();
        RecexpGrammar.ExpressionTree tree = g.new ExpressionTree();

        RecexpGrammar.ExpressionTree.Leaf leaf1 = tree.new Leaf();
        RecexpGrammar.ExpressionTree.Leaf leaf2 = tree.new Leaf();

        RecexpGrammar.LeafCombination lc1 = g.new LeafCombination(leaf1, new HashSet<String>(Arrays.asList("a", "b")));
        RecexpGrammar.LeafCombination lc2 = g.new LeafCombination(leaf2, new HashSet<String>(Arrays.asList("x", "")));

        Collection<RecexpGrammar.LeafCombination> combinations = Arrays.asList(lc1, lc2);

        Set<Set<RecexpGrammar.LeafCandidate>> product = g.getCartesianProduct(combinations);

        assertThat(getStringsFromCartesianProduct(product), containsInAnyOrder(
                "ax", "bx", "a", "b"
        ));
    }

    private Set<String> getStringsFromCartesianProduct(Set<Set<RecexpGrammar.LeafCandidate>> product) {
        Set<String> candidates = new HashSet<String>();

        for (Set<RecexpGrammar.LeafCandidate> candidateSet : product) {
            List<RecexpGrammar.LeafCandidate> candidateList = new ArrayList<RecexpGrammar.LeafCandidate>(candidateSet);
            Collections.sort(candidateList, new Comparator<RecexpGrammar.LeafCandidate>() {
                @Override
                public int compare(RecexpGrammar.LeafCandidate o1, RecexpGrammar.LeafCandidate o2) {
                    return o1.getExpression().compareTo(o2.getExpression());
                }
            });
            StringBuilder sb = new StringBuilder();
            for (RecexpGrammar.LeafCandidate candidate : candidateList) {
                sb.append(candidate.getExpression());
            }
            candidates.add(sb.toString());
        }
        return candidates;
    }

    // ########################################################################################
    // ########################################################################################
    // ########################################################################################
    // ########################################################################################

//    @Test
//    public void getExpressionPartsTest() {
//        RecexpGrammar grammar = new RecexpGrammar();
//
//        assertThat(grammar.getExpressionParts("").toString(), is("[]"));
//        assertThat(grammar.getExpressionParts("a").toString(), is("[a]"));
//        assertThat(grammar.getExpressionParts("ab").toString(), is("[ab]"));
//        assertThat(grammar.getExpressionParts("a(b)").toString(), is("[a, (b)]"));
//        assertThat(grammar.getExpressionParts("a(b)@this").toString(), is("[a, (b), @this]"));
//        assertThat(grammar.getExpressionParts("a(b)(@this)").toString(), is("[a, (b), (@this)]"));
//        assertThat(grammar.getExpressionParts("a(b)(\\@this)").toString(), is("[a, (b), (\\@this)]"));
//        assertThat(grammar.getExpressionParts("a(b)(\\@this)@this").toString(), is("[a, (b), (\\@this), @this]"));
//        assertThat(grammar.getExpressionParts("a(b)(@this)@this").toString(), is("[a, (b), (@this), @this]"));
//        assertThat(grammar.getExpressionParts("a(b)(@AB)@CD").toString(), is("[a, (b), (@AB), @CD]"));
//
//        assertThat(grammar.getExpressionParts("(a)(@A)?").toString(), is("[(a), (@A)?]"));
//
//        assertThat(grammar.getExpressionParts("@A@this").toString(), is("[@A, @this]"));
//        assertThat(grammar.getExpressionParts("@A@this?").toString(), is("[@A, @this?]"));
//        assertThat(grammar.getExpressionParts("@A(@this)?").toString(), is("[@A, (@this)?]"));
//        assertThat(grammar.getExpressionParts("(@A(@this)?)").toString(), is("[(@A(@this)?)]"));
//
//        assertThat(grammar.getExpressionParts("()").toString(), is("[()]"));
//        assertThat(grammar.getExpressionParts("(())").toString(), is("[(())]"));
//        assertThat(grammar.getExpressionParts("()()").toString(), is("[(), ()]"));
//        assertThat(grammar.getExpressionParts("(()())").toString(), is("[(()())]"));
//
//        assertThat(grammar.getExpressionParts("a@this?b").toString(), is("[a, @this?, b]"));
//        assertThat(grammar.getExpressionParts("a(@this?)b").toString(), is("[a, (@this?), b]"));
//        assertThat(grammar.getExpressionParts("a(@this?)b").toString(), is("[a, (@this?), b]"));
//        assertThat(grammar.getExpressionParts("(a)(@this?)(b)").toString(), is("[(a), (@this?), (b)]"));
//        assertThat(grammar.getExpressionParts("((a))((@this?))((b))").toString(), is("[((a)), ((@this?)), ((b))]"));
//        assertThat(grammar.getExpressionParts("a(@this?)b\\)\\)").toString(), is("[a, (@this?), b\\)\\)]"));
//
//        assertThat(grammar.getExpressionParts("a@this?").toString(), is("[a, @this?]"));
//        assertThat(grammar.getExpressionParts("a@this*").toString(), is("[a, @this*]"));
//        assertThat(grammar.getExpressionParts("a@this+").toString(), is("[a, @this+]"));
//        assertThat(grammar.getExpressionParts("a@this{1}").toString(), is("[a, @this{1}]"));
//        assertThat(grammar.getExpressionParts("a@this{123}").toString(), is("[a, @this{123}]"));
//        assertThat(grammar.getExpressionParts("a@this{1,}").toString(), is("[a, @this{1,}]"));
//        assertThat(grammar.getExpressionParts("a@this{123,}").toString(), is("[a, @this{123,}]"));
//        assertThat(grammar.getExpressionParts("a@this{1,2}").toString(), is("[a, @this{1,2}]"));
//        assertThat(grammar.getExpressionParts("a@this{1,23}").toString(), is("[a, @this{1,23}]"));
//        assertThat(grammar.getExpressionParts("a@this??").toString(), is("[a, @this??]"));
//        assertThat(grammar.getExpressionParts("a@this*?").toString(), is("[a, @this*?]"));
//        assertThat(grammar.getExpressionParts("a@this+?").toString(), is("[a, @this+?]"));
//        assertThat(grammar.getExpressionParts("a@this{1}?").toString(), is("[a, @this{1}?]"));
//        assertThat(grammar.getExpressionParts("a@this{123}?").toString(), is("[a, @this{123}?]"));
//        assertThat(grammar.getExpressionParts("a@this{1,}?").toString(), is("[a, @this{1,}?]"));
//        assertThat(grammar.getExpressionParts("a@this{123,}?").toString(), is("[a, @this{123,}?]"));
//        assertThat(grammar.getExpressionParts("a@this{1,2}?").toString(), is("[a, @this{1,2}?]"));
//        assertThat(grammar.getExpressionParts("a@this{1,23}?").toString(), is("[a, @this{1,23}?]"));
//        assertThat(grammar.getExpressionParts("a@this?+").toString(), is("[a, @this?+]"));
//        assertThat(grammar.getExpressionParts("a@this*+").toString(), is("[a, @this*+]"));
//        assertThat(grammar.getExpressionParts("a@this++").toString(), is("[a, @this++]"));
//        assertThat(grammar.getExpressionParts("a@this{1}+").toString(), is("[a, @this{1}+]"));
//        assertThat(grammar.getExpressionParts("a@this{123}+").toString(), is("[a, @this{123}+]"));
//        assertThat(grammar.getExpressionParts("a@this{1,}+").toString(), is("[a, @this{1,}+]"));
//        assertThat(grammar.getExpressionParts("a@this{123,}+").toString(), is("[a, @this{123,}+]"));
//        assertThat(grammar.getExpressionParts("a@this{1,2}+").toString(), is("[a, @this{1,2}+]"));
//        assertThat(grammar.getExpressionParts("a@this{1,23}+").toString(), is("[a, @this{1,23}+]"));
//        assertThat(grammar.getExpressionParts("a@this{,}++").toString(), is("[a, @this, {,}++]"));
//        assertThat(grammar.getExpressionParts("a@this{}++").toString(), is("[a, @this, {}++]"));
//        assertThat(grammar.getExpressionParts("a@this{,1}++").toString(), is("[a, @this, {,1}++]"));
//
//        try {
//            grammar.getExpressionParts("@A(@this)?)");
//            fail("RecexpSyntaxException expected");
//
//        } catch (RecexpSyntaxException expected) {
//        }
//
//        try {
//            grammar.getExpressionParts("(@A(@this)?");
//            fail("RecexpSyntaxException expected");
//
//        } catch (RecexpSyntaxException expected) {
//        }
//
//        try {
//            grammar.getExpressionParts("(");
//            fail("RecexpSyntaxException expected");
//
//        } catch (RecexpSyntaxException expected) {
//        }
//
//        try {
//            grammar.getExpressionParts(")");
//            fail("RecexpSyntaxException expected");
//
//        } catch (RecexpSyntaxException expected) {
//        }
//
//        try {
//            grammar.getExpressionParts("())");
//            fail("RecexpSyntaxException expected");
//
//        } catch (RecexpSyntaxException expected) {
//        }
//
//        try {
//            grammar.getExpressionParts("(()");
//            fail("RecexpSyntaxException expected");
//
//        } catch (RecexpSyntaxException expected) {
//        }
//    }
//
//    @Test
//    public void isClosedInBracketsTest() {
//        RecexpGrammar grammar = new RecexpGrammar();
//
//        assertThat(grammar.isClosedInBrackets("", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(", true), is(false));
//        assertThat(grammar.isClosedInBrackets(")", true), is(false));
//        assertThat(grammar.isClosedInBrackets(")(", true), is(false));
//        assertThat(grammar.isClosedInBrackets("()()", true), is(false));
//        assertThat(grammar.isClosedInBrackets("()(", true), is(false));
//        assertThat(grammar.isClosedInBrackets(")()", true), is(false));
//        assertThat(grammar.isClosedInBrackets("()(())", true), is(false));
//        assertThat(grammar.isClosedInBrackets("())", true), is(false));
//        assertThat(grammar.isClosedInBrackets("()))", true), is(false));
//        assertThat(grammar.isClosedInBrackets("())()", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(()", true), is(false));
//        assertThat(grammar.isClosedInBrackets("((()", true), is(false));
//
//        assertThat(grammar.isClosedInBrackets("a", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a", true), is(false));
//        assertThat(grammar.isClosedInBrackets("a)", true), is(false));
//        assertThat(grammar.isClosedInBrackets(")a(", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a)a(a)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a)(", true), is(false));
//        assertThat(grammar.isClosedInBrackets(")(a)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a)((a))", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a))", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a))a)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a))(a)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("((a)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(a(a(a)", true), is(false));
//
//        assertThat(grammar.isClosedInBrackets("()", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(())", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(()())", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(()()())", true), is(true));
//        assertThat(grammar.isClosedInBrackets("((()()))", true), is(true));
//
//        assertThat(grammar.isClosedInBrackets("(\\)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(()\\)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(()()\\)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("(()()()\\)", true), is(false));
//        assertThat(grammar.isClosedInBrackets("((()())\\)", true), is(false));
//
//        assertThat(grammar.isClosedInBrackets("\\()", true), is(false));
//        assertThat(grammar.isClosedInBrackets("\\(())", true), is(false));
//        assertThat(grammar.isClosedInBrackets("\\(()())", true), is(false));
//        assertThat(grammar.isClosedInBrackets("\\(()()())", true), is(false));
//        assertThat(grammar.isClosedInBrackets("\\((()()))", true), is(false));
//
//        assertThat(grammar.isClosedInBrackets("(a)", true), is(true));
//        assertThat(grammar.isClosedInBrackets("((a))", true), is(true));
//        assertThat(grammar.isClosedInBrackets("((a)(a))", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(()(a)())", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(a(a(a)a(a)a)a)", true), is(true));
//
//        assertThat(grammar.isClosedInBrackets("(a)?", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(a)+", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(a)*", true), is(true));
//        assertThat(grammar.isClosedInBrackets("(a){1,2}", true), is(true));
//
//        assertThat(grammar.isClosedInBrackets("(a)?", false), is(false));
//        assertThat(grammar.isClosedInBrackets("(a)+", false), is(false));
//        assertThat(grammar.isClosedInBrackets("(a)*", false), is(false));
//        assertThat(grammar.isClosedInBrackets("(a){1,2}", false), is(false));
//    }
//
//    @Test
//    public void isTerminalTest() {
//        RecexpGrammar g = new RecexpGrammar();
//
//        assertThat(g.isTerminal(g.new ExpressionPart("a", null, 0, false)), is(true));
//
//        assertThat(g.isTerminal(g.new ExpressionPart("a", "?", 1, false)), is(false));
//        assertThat(g.isTerminal(g.new ExpressionPart("this", null, 0, true)), is(false));
//        assertThat(g.isTerminal(g.new ExpressionPart("this", "?", 0, true)), is(false));
//        assertThat(g.isTerminal(g.new ExpressionPart("this", "?", 1, true)), is(false));
//    }
}
