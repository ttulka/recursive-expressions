package cz.net21.ttulka.recexp.test;

import org.junit.Test;

import cz.net21.ttulka.recexp.RecexpCyclicRuleException;
import cz.net21.ttulka.recexp.RecexpGrammar;
import cz.net21.ttulka.recexp.RecexpMatcher;
import cz.net21.ttulka.recexp.RecexpRule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author ttulka
 */
public class RecexpTest {

    @Test
    public void epsilonTest() {
        RecexpGrammar grammar = new RecexpGrammar("");

        assertThat(grammar.accepts(""), is(true));

        assertThat(grammar.accepts("a"), is(false));
    }

    @Test
    public void simpleTest() {
        RecexpGrammar grammar = new RecexpGrammar("a");

        assertThat(grammar.accepts("a"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("aa"), is(false));
        assertThat(grammar.accepts("aaa"), is(false));
    }

    @Test
    public void simpleRecursiveTest() {
        RecexpGrammar grammar = new RecexpGrammar("a@this?b");

        assertThat(grammar.accepts("ab"), is(true));
        assertThat(grammar.accepts("aabb"), is(true));
        assertThat(grammar.accepts("aaabbb"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("a"), is(false));
        assertThat(grammar.accepts("b"), is(false));
        assertThat(grammar.accepts("ba"), is(false));
        assertThat(grammar.accepts("abb"), is(false));
    }

    @Test
    public void doubleRecursiveThisTest() {
        RecexpGrammar grammar = new RecexpGrammar("a@this?b@this?c");

        assertThat(grammar.accepts("abc"), is(true));
        assertThat(grammar.accepts("abc"), is(true));
        assertThat(grammar.accepts("ababcc"), is(true));
        assertThat(grammar.accepts("aabcbc"), is(true));
        assertThat(grammar.accepts("aabcbabcc"), is(true));
        assertThat(grammar.accepts("aaabcbabccbaabcbabccc"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("a"), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("ac"), is(false));
        assertThat(grammar.accepts("bc"), is(false));
        assertThat(grammar.accepts("cba"), is(false));
        assertThat(grammar.accepts("aabc"), is(false));
        assertThat(grammar.accepts("aabcc"), is(false));
        assertThat(grammar.accepts("aabbcc"), is(false));
        assertThat(grammar.accepts("aabbc"), is(false));
        assertThat(grammar.accepts("aacbc"), is(false));
        assertThat(grammar.accepts("ababc"), is(true));
    }

    @Test
    public void doubleRecursiveRuleTest() {
        RecexpGrammar grammar = new RecexpGrammar(
                new RecexpRule("RULE", "a@RULE?b@RULE?c"));

        assertThat(grammar.accepts("abc"), is(true));
        assertThat(grammar.accepts("abc"), is(true));
        assertThat(grammar.accepts("ababcc"), is(true));
        assertThat(grammar.accepts("aabcbc"), is(true));
        assertThat(grammar.accepts("aabcbabcc"), is(true));
        assertThat(grammar.accepts("aaabcbabccbaabcbabccc"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("a"), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("ac"), is(false));
        assertThat(grammar.accepts("bc"), is(false));
        assertThat(grammar.accepts("cba"), is(false));
        assertThat(grammar.accepts("aabc"), is(false));
        assertThat(grammar.accepts("aabcc"), is(false));
        assertThat(grammar.accepts("aabbcc"), is(false));
        assertThat(grammar.accepts("aabbc"), is(false));
        assertThat(grammar.accepts("aacbc"), is(false));
        assertThat(grammar.accepts("ababc"), is(true));
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicOnlyThisTest() {
        new RecexpGrammar("@this").accepts("a");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicSimpleThisTest() {
        new RecexpGrammar("a(@this)b").accepts("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicDoubleThisTest() {
        new RecexpGrammar("a(@this)b(@this)c").accepts("abc");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicDoubleThis2Test() {
        new RecexpGrammar("a(@this)b@this?c").accepts("abc");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicSimpleRuleTest() {
        new RecexpGrammar(new RecexpRule("RULE_CYCLIC", "a(@RULE_CYCLIC)b"))
                .accepts("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void transitiveCyclicTwoRulesTest() {
        new RecexpGrammar()
                .addRule("RULE_1", "a(@RULE_2)b")
                .addRule("RULE_2", "c(@RULE_1)d")
                .accepts("acdb");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicTwoThisTest() {
        new RecexpGrammar()
                .addRule("a(@this)b")
                .addRule("c(@this)d");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void transitiveCyclicThreeRulesTest() {
        new RecexpGrammar()
                .addRule("RULE_1", "a(@RULE_2)b")
                .addRule("RULE_2", "c(@RULE_1)d")
                .addRule("RULE_3", "x")
                .accepts("acxdb");
        fail("Cyclic rule should throw an exception.");
    }

    @Test
    public void orThisTest() {
        RecexpGrammar grammar = new RecexpGrammar("a(@this)b|c");

        assertThat(grammar.accepts("c"), is(true));
        assertThat(grammar.accepts("acb"), is(true));
        assertThat(grammar.accepts("aacbb"), is(true));
        assertThat(grammar.accepts("aaacbbb"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabb"), is(false));
        assertThat(grammar.accepts("abc"), is(false));
        assertThat(grammar.accepts("cc"), is(false));
        assertThat(grammar.accepts("accb"), is(false));
    }

    @Test
    public void orThisByTwoRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("R", "a(@R)b")
                .addRule("R", "x");

        assertThat(grammar.accepts("x"), is(true));
        assertThat(grammar.accepts("axb"), is(true));
        assertThat(grammar.accepts("aaxbb"), is(true));
        assertThat(grammar.accepts("aaaxbbb"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("xx"), is(false));
        assertThat(grammar.accepts("xxx"), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabc"), is(false));
        assertThat(grammar.accepts("axxb"), is(false));
        assertThat(grammar.accepts("xabx"), is(false));
    }

    @Test
    public void orNamedThreeRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("R", "a(@R)b")
                .addRule("R", "x")
                .addRule("R", "");

        assertThat(grammar.accepts(""), is(true));
        assertThat(grammar.accepts("x"), is(true));
        assertThat(grammar.accepts("ab"), is(true));
        assertThat(grammar.accepts("aabb"), is(true));
        assertThat(grammar.accepts("aaabbb"), is(true));
        assertThat(grammar.accepts("axb"), is(true));
        assertThat(grammar.accepts("aaxbb"), is(true));
        assertThat(grammar.accepts("aaaxbbb"), is(true));

        assertThat(grammar.accepts("xx"), is(false));
        assertThat(grammar.accepts("xxx"), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabc"), is(false));
        assertThat(grammar.accepts("axxb"), is(false));
        assertThat(grammar.accepts("xabx"), is(false));
    }

    @Test
    public void orNamedTransitiveThreeRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("R", "a(@R)b")
                .addRule("R", "@X")
                .addRule("R", "@EPS")
                .addRule("X", "x")
                .addRule("EPS", "");

        assertThat(grammar.accepts(""), is(true));
        assertThat(grammar.accepts("x"), is(true));
        assertThat(grammar.accepts("ab"), is(true));
        assertThat(grammar.accepts("aabb"), is(true));
        assertThat(grammar.accepts("aaabbb"), is(true));
        assertThat(grammar.accepts("axb"), is(true));
        assertThat(grammar.accepts("aaxbb"), is(true));
        assertThat(grammar.accepts("aaaxbbb"), is(true));

        assertThat(grammar.accepts("xx"), is(false));
        assertThat(grammar.accepts("xxx"), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabc"), is(false));
        assertThat(grammar.accepts("axxb"), is(false));
        assertThat(grammar.accepts("xabx"), is(false));
    }

    @Test
    public void twoRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("RULE_AB", "a(@RULE_C)b")
                .addRule("RULE_C", "c");

        assertThat(grammar.accepts("c"), is(true));
        assertThat(grammar.accepts("acb"), is(true));
        assertThat(grammar.accepts("aacbb"), is(true));
        assertThat(grammar.accepts("aaacbbb"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabb"), is(false));
        assertThat(grammar.accepts("abc"), is(false));
        assertThat(grammar.accepts("cc"), is(false));
        assertThat(grammar.accepts("accb"), is(false));
    }

    @Test
    public void orThreeRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("RULE_AB", "a(@RULE_C)b|@RULE_X")
                .addRule("RULE_CD", "c(@RULE_A)d")
                .addRule("RULE_X", "x");

        assertThat(grammar.accepts("x"), is(true));
        assertThat(grammar.accepts("cxd"), is(true));
        assertThat(grammar.accepts("cacxdbd"), is(true));
        assertThat(grammar.accepts("cacacxdbdbd"), is(true));
        assertThat(grammar.accepts("cacacacxdbdbdbd"), is(true));
        assertThat(grammar.accepts("acxdb"), is(true));
        assertThat(grammar.accepts("acacxdbdb"), is(true));
        assertThat(grammar.accepts("acacacxdbdbdb"), is(true));

        assertThat(grammar.accepts(""), is(false));
        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("acdb"), is(false));
        assertThat(grammar.accepts("acabdb"), is(false));
        assertThat(grammar.accepts("acaxbdb"), is(false));
        assertThat(grammar.accepts("acaaxbbdb"), is(false));
        assertThat(grammar.accepts("acaaxbbdb"), is(false));
        assertThat(grammar.accepts("cd"), is(false));
        assertThat(grammar.accepts("cabd"), is(false));
        assertThat(grammar.accepts("caxbd"), is(false));
        assertThat(grammar.accepts("cacdbd"), is(false));
        assertThat(grammar.accepts("ccacxdbdd"), is(false));
    }

    @Test
    public void simpleGroupTest() {
        String input = "a";
        RecexpGrammar grammar = new RecexpGrammar("a");

        RecexpMatcher matcher = grammar.matcher(input);

        assertThat(matcher.value(), is(input));
        assertThat(matcher.groupCount(), is(0));
        assertThat(matcher.groups().length, is(0));
    }

    @Test
    public void basicRecursiveGroupTest() {
        RecexpGrammar grammar = new RecexpGrammar("a(@this?)b");

        RecexpMatcher matcher1 = grammar.matcher("ab");

        assertThat(matcher1.groupCount(), is(2));
        assertThat(matcher1.groups().length, is(2));

        assertThat(matcher1.value(), is("ab"));

        assertThat(matcher1.group(0).value(), is(matcher1.value()));
        assertThat(matcher1.group("a(@this?)b").value(), is(matcher1.value()));

        assertThat(matcher1.group(0).groupCount(), is(matcher1.groupCount()));
        assertThat(matcher1.group(0).groups().length, is(matcher1.groups().length));

        assertThat(matcher1.group(1).value(), is("a"));
        assertThat(matcher1.group("a").value(), is("a"));
        assertThat(matcher1.group(1).groupCount(), is(1));
        assertThat(matcher1.group(1).groups().length, is(matcher1.group(1).groupCount()));

        assertThat(matcher1.group(2).value(), is("b"));
        assertThat(matcher1.group("b").value(), is("b"));
        assertThat(matcher1.group(2).groupCount(), is(1));
        assertThat(matcher1.group(2).groups().length, is(matcher1.group(2).groupCount()));

        RecexpMatcher matcher2 = grammar.matcher("aabb");

        assertThat(matcher2.groupCount(), is(3));
        assertThat(matcher2.groups().length, is(matcher2.groupCount()));

        assertThat(matcher2.value(), is("aabb"));

        assertThat(matcher2.group(0).value(), is("aabb"));
        assertThat(matcher2.group("a(@this?)b").value(), is("aabb"));

        assertThat(matcher2.group(1).value(), is("a"));
        assertThat(matcher2.group("a").value(), is("a"));

        assertThat(matcher2.group(2).value(), is("ab"));
        assertThat(matcher2.group("@this?").value(), is("ab"));

        assertThat(matcher2.group(3).value(), is("b"));
        assertThat(matcher2.group("b").value(), is("b"));

        assertThat(matcher2.group(2).groupCount(), is(2));
        assertThat(matcher2.group(2).groups().length, is(matcher2.group(2).groupCount()));

        assertThat(matcher2.group(2).group(1).value(), is("a"));
        assertThat(matcher2.group(2).group("a").value(), is("a"));
        assertThat(matcher2.group(2).group(1).groupCount(), is(1));
        assertThat(matcher2.group(2).group(1).groups().length, is(matcher1.group(1).groupCount()));

        assertThat(matcher2.group(2).group(2).value(), is("b"));
        assertThat(matcher2.group(2).group("b").value(), is("b"));
        assertThat(matcher2.group(2).group(2).groupCount(), is(1));
        assertThat(matcher2.group(2).group(2).groups().length, is(matcher1.group(1).groupCount()));
    }

    @Test
    public void customSubgroupsTest() {
        RecexpGrammar grammar = new RecexpGrammar("fi(r)st(@this?)second");

        RecexpMatcher matcher1 = grammar.matcher("firstsecond");

        assertThat(matcher1.groupCount(), is(4));
        assertThat(matcher1.group(1).value(), is("fi"));
        assertThat(matcher1.group(2).value(), is("r"));
        assertThat(matcher1.group(3).value(), is("st"));
        assertThat(matcher1.group(4).value(), is("second"));

        RecexpMatcher matcher2 = grammar.matcher("firstfistsecondsecond");

        assertThat(matcher2.groupCount(), is(5));
        assertThat(matcher2.group(1).value(), is("fi"));
        assertThat(matcher2.group(2).value(), is("r"));
        assertThat(matcher2.group(3).value(), is("st"));
        assertThat(matcher2.group(4).value(), is("fistsecond"));
        assertThat(matcher2.group(5).value(), is("second"));

        assertThat(matcher2.group(4).groupCount(), is(3));
        assertThat(matcher2.group(4).group(1).value(), is("fi"));
        assertThat(matcher2.group(4).group(2).value(), is("st"));
        assertThat(matcher2.group(4).group(3).value(), is("second"));
    }

    @Test
    public void groupsTest() {
        RecexpGrammar grammar = new RecexpGrammar("a(b(c)(d))e");
        RecexpMatcher matcher = grammar.matcher("abcde");

        // groups are hierarchical - for a level
        // that differs from the RegExp
        assertThat(matcher.groupCount(), is(1));    // 3 in case of RegExp
        assertThat(matcher.group(1).groupCount(), is(2));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));
        assertThat(matcher.group(1).group(2).groupCount(), is(0));

        assertThat(matcher.value(), is("abcde"));
        assertThat(matcher.group(1).value(), is("bcd"));
        assertThat(matcher.group(1).group(1).value(), is("c"));
        assertThat(matcher.group(1).group(2).value(), is("d"));

        assertThat(matcher.name(), is("a(b(c)(d))e"));
        assertThat(matcher.group(1).name(), is("b(c)(d)"));
        assertThat(matcher.group(1).group(1).name(), is("c"));
        assertThat(matcher.group(1).group(2).name(), is("d"));
    }

    @Test
    public void groupsExplicitZeroGroupTest() {
        RecexpGrammar grammar = new RecexpGrammar("(a(b(c)(d))e)");
        RecexpMatcher matcher = grammar.matcher("abcde");

        assertThat(matcher.groupCount(), is(1));    // 3 in case of RegExp
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).groupCount(), is(2));
        assertThat(matcher.group(1).group(1).group(1).groupCount(), is(0));
        assertThat(matcher.group(1).group(1).group(2).groupCount(), is(0));

        assertThat(matcher.value(), is("abcde"));
        assertThat(matcher.group(1).value(), is("abcde"));
        assertThat(matcher.group(1).group(1).value(), is("bcd"));
        assertThat(matcher.group(1).group(1).group(1).value(), is("c"));
        assertThat(matcher.group(1).group(1).group(2).value(), is("d"));

        assertThat(matcher.name(), is("(a(b(c)(d))e)"));
        assertThat(matcher.group(1).name(), is("a(b(c)(d))e"));
        assertThat(matcher.group(1).group(1).name(), is("b(c)(d)"));
        assertThat(matcher.group(1).group(1).group(1).name(), is("c"));
        assertThat(matcher.group(1).group(1).group(2).name(), is("d"));
    }

    @Test
    public void explicitDoubleGroupTest() {
        RecexpGrammar grammar = new RecexpGrammar("a((b))");
        RecexpMatcher matcher = grammar.matcher("ab");

        assertThat(matcher.groupCount(), is(1));    // 2 in case of RegExp
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.group(1).value(), is("b"));
        assertThat(matcher.group(1).group(1).value(), is("b"));

        assertThat(matcher.name(), is("a((b))"));
        assertThat(matcher.group(1).name(), is("(b)"));
        assertThat(matcher.group(1).group(1).name(), is("b"));
    }

    @Test
    public void recursiveGroupTest() {
        RecexpGrammar grammar = new RecexpGrammar("a@this?b");
        RecexpMatcher matcher = grammar.matcher("aabb");

        assertThat(matcher.groupCount(), is(1));
        assertThat(matcher.group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.group(1).value(), is("ab"));

        assertThat(matcher.name(), is("a@this?b"));
        assertThat(matcher.group(1).name(), is("a@this?b"));
    }

    @Test
    public void recursiveGroupExplicitGroupTest() {
        RecexpGrammar grammar = new RecexpGrammar("a(@this?)b");
        RecexpMatcher matcher = grammar.matcher("aabb");

        assertThat(matcher.groupCount(), is(1));
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.group(1).value(), is("ab"));
        assertThat(matcher.group(1).group(1).value(), is("ab"));

        assertThat(matcher.name(), is("a(@this?)b"));
        assertThat(matcher.group(1).name(), is("@this?"));
        assertThat(matcher.group(1).group(1).name(), is("a(@this?)b"));
    }
}
