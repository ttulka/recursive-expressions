package cz.net21.ttulka.recexp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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

        assertThat(grammar.getExpressionParts("a"), contains("a"));
        assertThat(grammar.getExpressionParts("ab"), contains("ab"));
        assertThat(grammar.getExpressionParts("a(b)"), contains("a(b)"));
        assertThat(grammar.getExpressionParts("a(b)@this"), contains("a(b)", "@this"));
        assertThat(grammar.getExpressionParts("a(b)(@this)"), contains("a(b)(", "@this", ")"));
        assertThat(grammar.getExpressionParts("a(b)(\\@this)"), contains("a(b)(\\@this)"));
        assertThat(grammar.getExpressionParts("a(b)(\\@this)@this"), contains("a(b)(\\@this)", "@this"));
        assertThat(grammar.getExpressionParts("a(b)(@this)@this"), contains("a(b)(", "@this", ")", "@this"));
        assertThat(grammar.getExpressionParts("a(b)(@AB)@CD"), contains("a(b)(", "@AB", ")", "@CD"));

        assertThat(grammar.getExpressionParts("@A@this"), contains("@A", "@this"));
        assertThat(grammar.getExpressionParts("@A@this?"), contains("@A", "@this", "?"));
        assertThat(grammar.getExpressionParts("@A(@this)?"), contains("@A", "(", "@this", ")?"));
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
                "aA1", "aA2", "aA3", "bA1", "bA2", "bA3"));
    }

    @Test
    public void isApplicableTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("A", "a")
                .addRule("A", "@A@this?");

        assertThat(grammar.isApplicable("@A", ""), is(false));
        assertThat(grammar.isApplicable("@A", "aa"), is(true));
        assertThat(grammar.isApplicable("@A", "aaa"), is(true));

        grammar
                .addRule("B", "b")
                .addRule("B", "@A");

        assertThat(grammar.isApplicable("@B", ""), is(false));
        assertThat(grammar.isApplicable("@B", "b"), is(true));
        assertThat(grammar.isApplicable("@B", "aa"), is(true));
        assertThat(grammar.isApplicable("@B", "aaa"), is(true));
    }

    @Test
    public void separateGroupsTest() {
        List<String> groups;

        groups = new RecexpGrammar().separateGroups("a@this?b");
        assertThat(groups, contains("a", "@this?", "b"));

        groups = new RecexpGrammar().separateGroups("a(@this?)b");
        assertThat(groups, contains("a", "@this?", "b"));

        groups = new RecexpGrammar().separateGroups("(a)(@this?)(b)");
        assertThat(groups, contains("a", "@this?", "b"));

        groups = new RecexpGrammar().separateGroups("((a))((@this?))((b))");
        assertThat(groups, contains("(a)", "(@this?)", "(b)"));

        groups = new RecexpGrammar().separateGroups("(a))@this?((b)");
        assertThat(groups, contains("a", ")", "@this?", "(", "b"));

        groups = new RecexpGrammar().separateGroups("\\((a))(\\(@this?))((b\\)\\)");
        assertThat(groups, contains("\\(", "a", ")", "\\(@this?", ")((b\\)\\)"));

        groups = new RecexpGrammar().separateGroups("(a(@this?)((b\\)\\)");
        assertThat(groups, contains("(a", "@this?", "((b\\)\\)"));

        groups = new RecexpGrammar().separateGroups("(a@this?");
        assertThat(groups, contains("(a", "@this?"));

        groups = new RecexpGrammar().separateGroups("(a@this*");
        assertThat(groups, contains("(a", "@this*"));

        groups = new RecexpGrammar().separateGroups("(a@this+");
        assertThat(groups, contains("(a", "@this+"));

        groups = new RecexpGrammar().separateGroups("(a@this{1}");
        assertThat(groups, contains("(a", "@this{1}"));

        groups = new RecexpGrammar().separateGroups("(a@this{123}");
        assertThat(groups, contains("(a", "@this{123}"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,}");
        assertThat(groups, contains("(a", "@this{1,}"));

        groups = new RecexpGrammar().separateGroups("(a@this{123,}");
        assertThat(groups, contains("(a", "@this{123,}"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,2}");
        assertThat(groups, contains("(a", "@this{1,2}"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,23}");
        assertThat(groups, contains("(a", "@this{1,23}"));

        groups = new RecexpGrammar().separateGroups("(a@this??");
        assertThat(groups, contains("(a", "@this??"));

        groups = new RecexpGrammar().separateGroups("(a@this*?");
        assertThat(groups, contains("(a", "@this*?"));

        groups = new RecexpGrammar().separateGroups("(a@this+?");
        assertThat(groups, contains("(a", "@this+?"));

        groups = new RecexpGrammar().separateGroups("(a@this{1}?");
        assertThat(groups, contains("(a", "@this{1}?"));

        groups = new RecexpGrammar().separateGroups("(a@this{123}?");
        assertThat(groups, contains("(a", "@this{123}?"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,}?");
        assertThat(groups, contains("(a", "@this{1,}?"));

        groups = new RecexpGrammar().separateGroups("(a@this{123,}?");
        assertThat(groups, contains("(a", "@this{123,}?"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,2}?");
        assertThat(groups, contains("(a", "@this{1,2}?"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,23}?");
        assertThat(groups, contains("(a", "@this{1,23}?"));

        groups = new RecexpGrammar().separateGroups("(a@this?+");
        assertThat(groups, contains("(a", "@this?+"));

        groups = new RecexpGrammar().separateGroups("(a@this*+");
        assertThat(groups, contains("(a", "@this*+"));

        groups = new RecexpGrammar().separateGroups("(a@this++");
        assertThat(groups, contains("(a", "@this++"));

        groups = new RecexpGrammar().separateGroups("(a@this{1}+");
        assertThat(groups, contains("(a", "@this{1}+"));

        groups = new RecexpGrammar().separateGroups("(a@this{123}+");
        assertThat(groups, contains("(a", "@this{123}+"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,}+");
        assertThat(groups, contains("(a", "@this{1,}+"));

        groups = new RecexpGrammar().separateGroups("(a@this{123,}+");
        assertThat(groups, contains("(a", "@this{123,}+"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,2}+");
        assertThat(groups, contains("(a", "@this{1,2}+"));

        groups = new RecexpGrammar().separateGroups("(a@this{1,23}+");
        assertThat(groups, contains("(a", "@this{1,23}+"));

        groups = new RecexpGrammar().separateGroups("(a@this{,}++");
        assertThat(groups, contains("(a", "@this", "{,}++"));

        groups = new RecexpGrammar().separateGroups("(a@this{}++");
        assertThat(groups, contains("(a", "@this", "{}++"));

        groups = new RecexpGrammar().separateGroups("(a@this{,1}++");
        assertThat(groups, contains("(a", "@this", "{,1}++"));
    }

    @Test
    public void getGroupsTest() {
        List<RecexpGroup> groups1 = new RecexpGrammar().getGroups("a(@this?)b", "ab");

        assertThat(groups1.size(), is(2));

        assertThat(groups1.get(0).name(), is("a"));
        assertThat(groups1.get(0).value(), is("a"));
        assertThat(groups1.get(0).groupCount(), is(0));

        assertThat(groups1.get(1).name(), is("b"));
        assertThat(groups1.get(1).value(), is("b"));
        assertThat(groups1.get(1).groupCount(), is(0));

        List<RecexpGroup> groups2 = new RecexpGrammar().getGroups("a(@this?)b", "aabb");

        assertThat(groups2.size(), is(3));

        assertThat(groups2.get(1).name(), is("a"));
        assertThat(groups2.get(1).value(), is("a"));
        assertThat(groups2.get(1).groupCount(), is(0));

        assertThat(groups2.get(2).name(), is("@this?"));
        assertThat(groups2.get(2).value(), is("ab"));
        assertThat(groups2.get(2).groupCount(), is(2));

        assertThat(groups2.get(3).name(), is("b"));
        assertThat(groups2.get(3).value(), is("b"));
        assertThat(groups2.get(3).groupCount(), is(0));

        assertThat(groups2.get(2).group(1).name(), is("a"));
        assertThat(groups2.get(2).group(1).value(), is("a"));
        assertThat(groups2.get(2).group(1).groupCount(), is(0));

        assertThat(groups2.get(2).group(2).name(), is("b"));
        assertThat(groups2.get(2).group(2).value(), is("b"));
        assertThat(groups2.get(2).group(2).groupCount(), is(0));

        List<RecexpGroup> groups3 = new RecexpGrammar()
                .addRule("A", "a")
                .addRule("B", "b")
                .getGroups("@A@this?@B", "aabb");

        assertThat(groups3.size(), is(3));

        assertThat(groups3.get(1).name(), is("@A"));
        assertThat(groups3.get(1).value(), is("a"));
        assertThat(groups3.get(1).groupCount(), is(0));

        assertThat(groups3.get(2).name(), is("@this?"));
        assertThat(groups3.get(2).value(), is("ab"));
        assertThat(groups3.get(2).groupCount(), is(2));

        assertThat(groups3.get(3).name(), is("@B"));
        assertThat(groups3.get(3).value(), is("b"));
        assertThat(groups3.get(3).groupCount(), is(0));

        assertThat(groups3.get(2).group(1).name(), is("@A"));
        assertThat(groups3.get(2).group(1).value(), is("a"));
        assertThat(groups3.get(2).group(1).groupCount(), is(0));

        assertThat(groups3.get(2).group(2).name(), is("@B"));
        assertThat(groups3.get(2).group(2).value(), is("b"));
        assertThat(groups3.get(2).group(2).groupCount(), is(0));
    }
}
