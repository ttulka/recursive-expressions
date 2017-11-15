package cz.net21.ttulka.recexp;

import java.util.ArrayList;
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
    }

    @Test
    public void getExpressionPartsTest() {
        RecexpGrammar grammar = new RecexpGrammar();

        assertThat(grammar.getExpressionParts("").toString(), is("[]"));
        assertThat(grammar.getExpressionParts("a").toString(), is("[a]"));
        assertThat(grammar.getExpressionParts("ab").toString(), is("[ab]"));
        assertThat(grammar.getExpressionParts("a(b)").toString(), is("[a, (b)]"));
        assertThat(grammar.getExpressionParts("a(b)@this").toString(), is("[a, (b), @this]"));
        assertThat(grammar.getExpressionParts("a(b)(@this)").toString(), is("[a, (b), (@this)]"));
        assertThat(grammar.getExpressionParts("a(b)(\\@this)").toString(), is("[a, (b), (\\@this)]"));
        assertThat(grammar.getExpressionParts("a(b)(\\@this)@this").toString(), is("[a, (b), (\\@this), @this]"));
        assertThat(grammar.getExpressionParts("a(b)(@this)@this").toString(), is("[a, (b), (@this), @this]"));
        assertThat(grammar.getExpressionParts("a(b)(@AB)@CD").toString(), is("[a, (b), (@AB), @CD]"));

        assertThat(grammar.getExpressionParts("(a)(@A)?").toString(), is("[(a), (@A)?]"));

        assertThat(grammar.getExpressionParts("@A@this").toString(), is("[@A, @this]"));
        assertThat(grammar.getExpressionParts("@A@this?").toString(), is("[@A, @this?]"));
        assertThat(grammar.getExpressionParts("@A(@this)?").toString(), is("[@A, (@this)?]"));
        assertThat(grammar.getExpressionParts("(@A(@this)?)").toString(), is("[(@A(@this)?)]"));

        assertThat(grammar.getExpressionParts("()").toString(), is("[()]"));
        assertThat(grammar.getExpressionParts("(())").toString(), is("[(())]"));
        assertThat(grammar.getExpressionParts("()()").toString(), is("[(), ()]"));
        assertThat(grammar.getExpressionParts("(()())").toString(), is("[(()())]"));

        assertThat(grammar.getExpressionParts("a@this?b").toString(), is("[a, @this?, b]"));
        assertThat(grammar.getExpressionParts("a(@this?)b").toString(), is("[a, (@this?), b]"));
        assertThat(grammar.getExpressionParts("a(@this?)b").toString(), is("[a, (@this?), b]"));
        assertThat(grammar.getExpressionParts("(a)(@this?)(b)").toString(), is("[(a), (@this?), (b)]"));
        assertThat(grammar.getExpressionParts("((a))((@this?))((b))").toString(), is("[((a)), ((@this?)), ((b))]"));
        assertThat(grammar.getExpressionParts("a(@this?)b\\)\\)").toString(), is("[a, (@this?), b\\)\\)]"));

        assertThat(grammar.getExpressionParts("a@this?").toString(), is("[a, @this?]"));
        assertThat(grammar.getExpressionParts("a@this*").toString(), is("[a, @this*]"));
        assertThat(grammar.getExpressionParts("a@this+").toString(), is("[a, @this+]"));
        assertThat(grammar.getExpressionParts("a@this{1}").toString(), is("[a, @this{1}]"));
        assertThat(grammar.getExpressionParts("a@this{123}").toString(), is("[a, @this{123}]"));
        assertThat(grammar.getExpressionParts("a@this{1,}").toString(), is("[a, @this{1,}]"));
        assertThat(grammar.getExpressionParts("a@this{123,}").toString(), is("[a, @this{123,}]"));
        assertThat(grammar.getExpressionParts("a@this{1,2}").toString(), is("[a, @this{1,2}]"));
        assertThat(grammar.getExpressionParts("a@this{1,23}").toString(), is("[a, @this{1,23}]"));
        assertThat(grammar.getExpressionParts("a@this??").toString(), is("[a, @this??]"));
        assertThat(grammar.getExpressionParts("a@this*?").toString(), is("[a, @this*?]"));
        assertThat(grammar.getExpressionParts("a@this+?").toString(), is("[a, @this+?]"));
        assertThat(grammar.getExpressionParts("a@this{1}?").toString(), is("[a, @this{1}?]"));
        assertThat(grammar.getExpressionParts("a@this{123}?").toString(), is("[a, @this{123}?]"));
        assertThat(grammar.getExpressionParts("a@this{1,}?").toString(), is("[a, @this{1,}?]"));
        assertThat(grammar.getExpressionParts("a@this{123,}?").toString(), is("[a, @this{123,}?]"));
        assertThat(grammar.getExpressionParts("a@this{1,2}?").toString(), is("[a, @this{1,2}?]"));
        assertThat(grammar.getExpressionParts("a@this{1,23}?").toString(), is("[a, @this{1,23}?]"));
        assertThat(grammar.getExpressionParts("a@this?+").toString(), is("[a, @this?+]"));
        assertThat(grammar.getExpressionParts("a@this*+").toString(), is("[a, @this*+]"));
        assertThat(grammar.getExpressionParts("a@this++").toString(), is("[a, @this++]"));
        assertThat(grammar.getExpressionParts("a@this{1}+").toString(), is("[a, @this{1}+]"));
        assertThat(grammar.getExpressionParts("a@this{123}+").toString(), is("[a, @this{123}+]"));
        assertThat(grammar.getExpressionParts("a@this{1,}+").toString(), is("[a, @this{1,}+]"));
        assertThat(grammar.getExpressionParts("a@this{123,}+").toString(), is("[a, @this{123,}+]"));
        assertThat(grammar.getExpressionParts("a@this{1,2}+").toString(), is("[a, @this{1,2}+]"));
        assertThat(grammar.getExpressionParts("a@this{1,23}+").toString(), is("[a, @this{1,23}+]"));
        assertThat(grammar.getExpressionParts("a@this{,}++").toString(), is("[a, @this, {,}++]"));
        assertThat(grammar.getExpressionParts("a@this{}++").toString(), is("[a, @this, {}++]"));
        assertThat(grammar.getExpressionParts("a@this{,1}++").toString(), is("[a, @this, {,1}++]"));

        try {
            grammar.getExpressionParts("@A(@this)?)");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            grammar.getExpressionParts("(@A(@this)?");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            grammar.getExpressionParts("(");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            grammar.getExpressionParts(")");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            grammar.getExpressionParts("())");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }

        try {
            grammar.getExpressionParts("(()");
            fail("RecexpSyntaxException expected");

        } catch (RecexpSyntaxException expected) {
        }
    }

    @Test
    public void getCartesianProductTest() {
        List<Set<String>> candidates = new ArrayList<Set<String>>();

        Set<String> candidate1 = new HashSet<String>();
        candidate1.add("a");
        candidate1.add("b");

        candidates.add(candidate1);

        Set<String> candidate2 = new HashSet<String>();
        candidate2.add("A");

        candidates.add(candidate2);

        Set<String> candidate3 = new HashSet<String>();
        candidate3.add("1");
        candidate3.add("2");
        candidate3.add("3");

        candidates.add(candidate3);

        RecexpGrammar grammar = new RecexpGrammar();

        assertThat(grammar.getCartesianProduct(candidates), containsInAnyOrder(
                "(a)(A)(1)", "(a)(A)(2)", "(a)(A)(3)", "(b)(A)(1)", "(b)(A)(2)", "(b)(A)(3)"));
    }

    @Test
    public void getCartesianProductWithEpsilonTest() {
        List<Set<String>> candidates = new ArrayList<Set<String>>();

        Set<String> candidate1 = new HashSet<String>();
        candidate1.add("a");
        candidate1.add("b");

        candidates.add(candidate1);

        Set<String> candidate2 = new HashSet<String>();
        candidate2.add("1");
        candidate2.add("");

        candidates.add(candidate2);

        RecexpGrammar grammar = new RecexpGrammar();

        assertThat(grammar.getCartesianProduct(candidates), containsInAnyOrder(
                "(a)(1)", "(b)(1)", "(a)", "(b)"));
    }

    @Test
    public void isClosedInBracketsTest() {
        RecexpGrammar grammar = new RecexpGrammar();

        assertThat(grammar.isClosedInBrackets("", true), is(false));
        assertThat(grammar.isClosedInBrackets("(", true), is(false));
        assertThat(grammar.isClosedInBrackets(")", true), is(false));
        assertThat(grammar.isClosedInBrackets(")(", true), is(false));
        assertThat(grammar.isClosedInBrackets("()()", true), is(false));
        assertThat(grammar.isClosedInBrackets("()(", true), is(false));
        assertThat(grammar.isClosedInBrackets(")()", true), is(false));
        assertThat(grammar.isClosedInBrackets("()(())", true), is(false));
        assertThat(grammar.isClosedInBrackets("())", true), is(false));
        assertThat(grammar.isClosedInBrackets("()))", true), is(false));
        assertThat(grammar.isClosedInBrackets("())()", true), is(false));
        assertThat(grammar.isClosedInBrackets("(()", true), is(false));
        assertThat(grammar.isClosedInBrackets("((()", true), is(false));

        assertThat(grammar.isClosedInBrackets("a", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a", true), is(false));
        assertThat(grammar.isClosedInBrackets("a)", true), is(false));
        assertThat(grammar.isClosedInBrackets(")a(", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a)a(a)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a)(", true), is(false));
        assertThat(grammar.isClosedInBrackets(")(a)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a)((a))", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a))", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a))a)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a))(a)", true), is(false));
        assertThat(grammar.isClosedInBrackets("((a)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(a(a(a)", true), is(false));

        assertThat(grammar.isClosedInBrackets("()", true), is(true));
        assertThat(grammar.isClosedInBrackets("(())", true), is(true));
        assertThat(grammar.isClosedInBrackets("(()())", true), is(true));
        assertThat(grammar.isClosedInBrackets("(()()())", true), is(true));
        assertThat(grammar.isClosedInBrackets("((()()))", true), is(true));

        assertThat(grammar.isClosedInBrackets("(\\)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(()\\)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(()()\\)", true), is(false));
        assertThat(grammar.isClosedInBrackets("(()()()\\)", true), is(false));
        assertThat(grammar.isClosedInBrackets("((()())\\)", true), is(false));

        assertThat(grammar.isClosedInBrackets("\\()", true), is(false));
        assertThat(grammar.isClosedInBrackets("\\(())", true), is(false));
        assertThat(grammar.isClosedInBrackets("\\(()())", true), is(false));
        assertThat(grammar.isClosedInBrackets("\\(()()())", true), is(false));
        assertThat(grammar.isClosedInBrackets("\\((()()))", true), is(false));

        assertThat(grammar.isClosedInBrackets("(a)", true), is(true));
        assertThat(grammar.isClosedInBrackets("((a))", true), is(true));
        assertThat(grammar.isClosedInBrackets("((a)(a))", true), is(true));
        assertThat(grammar.isClosedInBrackets("(()(a)())", true), is(true));
        assertThat(grammar.isClosedInBrackets("(a(a(a)a(a)a)a)", true), is(true));

        assertThat(grammar.isClosedInBrackets("(a)?", true), is(true));
        assertThat(grammar.isClosedInBrackets("(a)+", true), is(true));
        assertThat(grammar.isClosedInBrackets("(a)*", true), is(true));
        assertThat(grammar.isClosedInBrackets("(a){1,2}", true), is(true));

        assertThat(grammar.isClosedInBrackets("(a)?", false), is(false));
        assertThat(grammar.isClosedInBrackets("(a)+", false), is(false));
        assertThat(grammar.isClosedInBrackets("(a)*", false), is(false));
        assertThat(grammar.isClosedInBrackets("(a){1,2}", false), is(false));
    }

    @Test
    public void isTerminalTest() {
        RecexpGrammar g = new RecexpGrammar();

        assertThat(g.isTerminal(g.new ExpressionPart("a", null, 0, false)), is(true));

        assertThat(g.isTerminal(g.new ExpressionPart("a", "?", 1, false)), is(false));
        assertThat(g.isTerminal(g.new ExpressionPart("this", null, 0, true)), is(false));
        assertThat(g.isTerminal(g.new ExpressionPart("this", "?", 0, true)), is(false));
        assertThat(g.isTerminal(g.new ExpressionPart("this", "?", 1, true)), is(false));
    }

    @Test
    public void getGroupsTest() {
        List<RecexpGroup> groups;

//        groups = new RecexpGrammar()
//                .getGroups("a", "a");
//
//        assertThat(groups.size(), is(1));
//
//        assertThat(groups.get(0).name(), is("a"));
//        assertThat(groups.get(0).value(), is("a"));
//        assertThat(groups.get(0).groupCount(), is(0));

        groups = new RecexpGrammar()
                .getGroups("a?", "a");

        assertThat(groups.size(), is(1));

        assertThat(groups.get(0).name(), is("(a)?"));
        assertThat(groups.get(0).value(), is("a"));
        assertThat(groups.get(0).groupCount(), is(0));

        groups = new RecexpGrammar()
                .getGroups("a|b", "a");

        assertThat(groups.size(), is(1));

        assertThat(groups.get(0).name(), is("a|b"));
        assertThat(groups.get(0).value(), is("a"));
        assertThat(groups.get(0).groupCount(), is(0));

        groups = new RecexpGrammar()
                .getGroups("a@this?b", "ab");

        assertThat(groups.size(), is(2));

        assertThat(groups.get(0).name(), is("a"));
        assertThat(groups.get(0).value(), is("a"));
        assertThat(groups.get(0).groupCount(), is(0));

        assertThat(groups.get(1).name(), is("b"));
        assertThat(groups.get(1).value(), is("b"));
        assertThat(groups.get(1).groupCount(), is(0));

        groups = new RecexpGrammar()
                .getGroups("a(@this?)b", "ab");

        assertThat(groups.size(), is(2));

        assertThat(groups.get(0).name(), is("a"));
        assertThat(groups.get(0).value(), is("a"));
        assertThat(groups.get(0).groupCount(), is(0));

        assertThat(groups.get(1).name(), is("b"));
        assertThat(groups.get(1).value(), is("b"));
        assertThat(groups.get(1).groupCount(), is(0));

        groups = new RecexpGrammar()
                .getGroups("a(@this)?b", "ab");

        assertThat(groups.size(), is(2));

        assertThat(groups.get(0).name(), is("a"));
        assertThat(groups.get(0).value(), is("a"));
        assertThat(groups.get(0).groupCount(), is(0));

        assertThat(groups.get(1).name(), is("b"));
        assertThat(groups.get(1).value(), is("b"));
        assertThat(groups.get(1).groupCount(), is(0));

        groups = new RecexpGrammar()
                .getGroups("a(@this?)b", "aabb");

        assertThat(groups.size(), is(3));

        assertThat(groups.get(1).name(), is("a"));
        assertThat(groups.get(1).value(), is("a"));
        assertThat(groups.get(1).groupCount(), is(0));

        assertThat(groups.get(2).name(), is("@this?"));
        assertThat(groups.get(2).value(), is("ab"));
        assertThat(groups.get(2).groupCount(), is(2));

        assertThat(groups.get(3).name(), is("b"));
        assertThat(groups.get(3).value(), is("b"));
        assertThat(groups.get(3).groupCount(), is(0));

        assertThat(groups.get(2).group(1).name(), is("a"));
        assertThat(groups.get(2).group(1).value(), is("a"));
        assertThat(groups.get(2).group(1).groupCount(), is(0));

        assertThat(groups.get(2).group(2).name(), is("b"));
        assertThat(groups.get(2).group(2).value(), is("b"));
        assertThat(groups.get(2).group(2).groupCount(), is(0));

        groups = new RecexpGrammar()
                .addRule("A", "a")
                .addRule("B", "b")
                .getGroups("@A@this?@B", "aabb");

        assertThat(groups.size(), is(3));

        assertThat(groups.get(1).name(), is("@A"));
        assertThat(groups.get(1).value(), is("a"));
        assertThat(groups.get(1).groupCount(), is(0));

        assertThat(groups.get(2).name(), is("@this?"));
        assertThat(groups.get(2).value(), is("ab"));
        assertThat(groups.get(2).groupCount(), is(2));

        assertThat(groups.get(3).name(), is("@B"));
        assertThat(groups.get(3).value(), is("b"));
        assertThat(groups.get(3).groupCount(), is(0));

        assertThat(groups.get(2).group(1).name(), is("@A"));
        assertThat(groups.get(2).group(1).value(), is("a"));
        assertThat(groups.get(2).group(1).groupCount(), is(0));

        assertThat(groups.get(2).group(2).name(), is("@B"));
        assertThat(groups.get(2).group(2).value(), is("b"));
        assertThat(groups.get(2).group(2).groupCount(), is(0));
    }
}
