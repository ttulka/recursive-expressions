package cz.net21.ttulka.recexp.test;

import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;

import cz.net21.ttulka.recexp.Recexp;
import cz.net21.ttulka.recexp.RecexpCyclicRuleException;
import cz.net21.ttulka.recexp.RecexpGroup;
import cz.net21.ttulka.recexp.RecexpMatcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Acceptance tests.
 *
 * @author ttulka
 */
public class RecexpTest {

    @Test
    public void emptyTest() {
        Recexp recexp = Recexp.compile("");

        assertThat(recexp.matches(""), is(true));

        assertThat(recexp.matches("a"), is(false));
    }

    @Test
    public void noMatchTest() {
        RecexpMatcher matcher = Recexp.compile("").matcher("xxx");

        assertThat(matcher.matches(), is(false));
        assertThat(matcher.groupCount(), is(0));
        assertThat(matcher.group(0), is(nullValue()));
    }

    @Test
    public void simpleTest() {
        Recexp recexp = Recexp.compile("a");

        assertThat(recexp.matches("a"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("aa"), is(false));
        assertThat(recexp.matches("aaa"), is(false));
    }

    @Test
    public void simpleRecursiveTest() {
        Recexp recexp = Recexp.compile("a@this?b");

        assertThat(recexp.matches("ab"), is(true));
        assertThat(recexp.matches("aabb"), is(true));
        assertThat(recexp.matches("aaabbb"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("a"), is(false));
        assertThat(recexp.matches("b"), is(false));
        assertThat(recexp.matches("ba"), is(false));
        assertThat(recexp.matches("abb"), is(false));
    }

    @Test
    public void doubleRecursiveThisTest() {
        Recexp recexp = Recexp.compile("a@this?b@this?c");

        assertThat(recexp.matches("abc"), is(true));
        assertThat(recexp.matches("ababcc"), is(true));
        assertThat(recexp.matches("aabcbc"), is(true));
        assertThat(recexp.matches("aabcbabcc"), is(true));
        assertThat(recexp.matches("aaabcbabccbaabcbabccc"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("a"), is(false));
        assertThat(recexp.matches("ab"), is(false));
        assertThat(recexp.matches("ac"), is(false));
        assertThat(recexp.matches("bc"), is(false));
        assertThat(recexp.matches("cba"), is(false));
        assertThat(recexp.matches("aabc"), is(false));
        assertThat(recexp.matches("aabcc"), is(false));
        assertThat(recexp.matches("aabbcc"), is(false));
        assertThat(recexp.matches("aabbc"), is(false));
        assertThat(recexp.matches("aacbc"), is(false));
        assertThat(recexp.matches("ababcc"), is(true));
    }

    @Test
    public void doubleRecursiveRuleTest() {
        Recexp recexp = Recexp.builder()
                .rule("RULE", "a@RULE?b@RULE?c")
                .build();

        assertThat(recexp.matches("abc"), is(true));
        assertThat(recexp.matches("abc"), is(true));
        assertThat(recexp.matches("ababcc"), is(true));
        assertThat(recexp.matches("aabcbc"), is(true));
        assertThat(recexp.matches("aabcbabcc"), is(true));
        assertThat(recexp.matches("aaabcbabccbaabcbabccc"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("a"), is(false));
        assertThat(recexp.matches("ab"), is(false));
        assertThat(recexp.matches("ac"), is(false));
        assertThat(recexp.matches("bc"), is(false));
        assertThat(recexp.matches("cba"), is(false));
        assertThat(recexp.matches("aabc"), is(false));
        assertThat(recexp.matches("aabcc"), is(false));
        assertThat(recexp.matches("aabbcc"), is(false));
        assertThat(recexp.matches("aabbc"), is(false));
        assertThat(recexp.matches("aacbc"), is(false));
        assertThat(recexp.matches("ababcc"), is(true));
    }

    @Test
    public void flagsTest() {
        assertThat(Recexp.compile("a", Pattern.CASE_INSENSITIVE).matches("A"), is(true));
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicOnlyThisTest() {
        Recexp.compile("@this").matches("a");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicSimpleThisTest() {
        Recexp.compile("a(@this)b").matches("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicDoubleThisTest() {
        Recexp.compile("a(@this)b(@this)c").matches("abc");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicDoubleThis2Test() {
        Recexp.compile("a(@this)b@this?c").matches("abc");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicSimpleRuleTest() {
        Recexp.builder().rule("RULE_CYCLIC", "a(@RULE_CYCLIC)b").build()
                .matches("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void transitiveCyclicTwoRulesTest() {
        Recexp.builder()
                .rule("RULE_1", "a(@RULE_2)b")
                .rule("RULE_2", "c(@RULE_1)d")
                .build()
                .matches("acdb");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicTwoThisTest() {
        Recexp.builder()
                .rule("a(@this)b")
                .rule("c(@this)d")
                .build()
                .matches("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void transitiveCyclicThreeRulesTest() {
        Recexp.builder()
                .rule("RULE_1", "a(@RULE_2)b")
                .rule("RULE_2", "c(@RULE_1)d")
                .rule("RULE_3", "x")
                .build()
                .matches("acxdb");
        fail("Cyclic rule should throw an exception.");
    }

    @Test
    public void orThisTest() {
        Recexp recexp = Recexp.compile("a(@this)b|c");

        assertThat(recexp.matches("c"), is(true));
        assertThat(recexp.matches("acb"), is(true));
        assertThat(recexp.matches("aacbb"), is(true));
        assertThat(recexp.matches("aaacbbb"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("ab"), is(false));
        assertThat(recexp.matches("aabb"), is(false));
        assertThat(recexp.matches("abc"), is(false));
        assertThat(recexp.matches("cc"), is(false));
        assertThat(recexp.matches("accb"), is(false));
    }

    @Test
    public void orThisByTwoRulesTest() {
        Recexp recexp = Recexp.builder()
                .rule("R", "a(@R)b")
                .rule("R", "x")
                .build();

        assertThat(recexp.matches("x"), is(true));
        assertThat(recexp.matches("axb"), is(true));
        assertThat(recexp.matches("aaxbb"), is(true));
        assertThat(recexp.matches("aaaxbbb"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("xx"), is(false));
        assertThat(recexp.matches("xxx"), is(false));
        assertThat(recexp.matches("ab"), is(false));
        assertThat(recexp.matches("aabc"), is(false));
        assertThat(recexp.matches("axxb"), is(false));
        assertThat(recexp.matches("xabx"), is(false));
    }

    @Test
    public void orNamedThreeRulesTest() {
        Recexp recexp = Recexp.builder()
                .rule("R", "a(@R)b")
                .rule("R", "x")
                .rule("R", "")
                .build();

        assertThat(recexp.matches(""), is(true));
        assertThat(recexp.matches("x"), is(true));
        assertThat(recexp.matches("ab"), is(true));
        assertThat(recexp.matches("aabb"), is(true));
        assertThat(recexp.matches("aaabbb"), is(true));
        assertThat(recexp.matches("axb"), is(true));
        assertThat(recexp.matches("aaxbb"), is(true));
        assertThat(recexp.matches("aaaxbbb"), is(true));

        assertThat(recexp.matches("xx"), is(false));
        assertThat(recexp.matches("xxx"), is(false));
        assertThat(recexp.matches("aabc"), is(false));
        assertThat(recexp.matches("axxb"), is(false));
        assertThat(recexp.matches("xabx"), is(false));
    }

    @Test
    public void orNamedTransitiveThreeRulesTest() {
        Recexp recexp = Recexp.builder()
                .rule("R", "a(@R)b")
                .rule("R", "@X")
                .rule("R", "@eps")
                .rule("X", "x")
                .build();

        assertThat(recexp.matches(""), is(true));
        assertThat(recexp.matches("x"), is(true));
        assertThat(recexp.matches("ab"), is(true));
        assertThat(recexp.matches("aabb"), is(true));
        assertThat(recexp.matches("aaabbb"), is(true));
        assertThat(recexp.matches("axb"), is(true));
        assertThat(recexp.matches("aaxbb"), is(true));
        assertThat(recexp.matches("aaaxbbb"), is(true));

        assertThat(recexp.matches("xx"), is(false));
        assertThat(recexp.matches("xxx"), is(false));
        assertThat(recexp.matches("aabc"), is(false));
        assertThat(recexp.matches("axxb"), is(false));
        assertThat(recexp.matches("xabx"), is(false));
    }

    @Test
    public void twoRulesTest() {
        Recexp recexp = Recexp.builder()
                .rule("RULE_AB", "a(@RULE_C)b")
                .rule("RULE_C", "c")
                .build();

        assertThat(recexp.matches("c"), is(true));
        assertThat(recexp.matches("acb"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("ab"), is(false));
        assertThat(recexp.matches("aabb"), is(false));
        assertThat(recexp.matches("abc"), is(false));
        assertThat(recexp.matches("cc"), is(false));
        assertThat(recexp.matches("accb"), is(false));
        assertThat(recexp.matches("aacbb"), is(false));
        assertThat(recexp.matches("aaacbbb"), is(false));
    }

    @Test
    public void orThreeRulesTest() {
        Recexp recexp = Recexp.builder()
                .rule("RULE_AB", "a(@RULE_CD)b|@RULE_X")
                .rule("RULE_CD", "c(@RULE_AB)d")
                .rule("RULE_X", "x")
                .build();

        assertThat(recexp.matches("x"), is(true));
        assertThat(recexp.matches("cxd"), is(true));
        assertThat(recexp.matches("cacxdbd"), is(true));
        assertThat(recexp.matches("cacacxdbdbd"), is(true));
        assertThat(recexp.matches("cacacacxdbdbdbd"), is(true));
        assertThat(recexp.matches("acxdb"), is(true));
        assertThat(recexp.matches("acacxdbdb"), is(true));
        assertThat(recexp.matches("acacacxdbdbdb"), is(true));

        assertThat(recexp.matches(""), is(false));
        assertThat(recexp.matches("ab"), is(false));
        assertThat(recexp.matches("acdb"), is(false));
        assertThat(recexp.matches("acabdb"), is(false));
        assertThat(recexp.matches("acaxbdb"), is(false));
        assertThat(recexp.matches("acaaxbbdb"), is(false));
        assertThat(recexp.matches("acaaxbbdb"), is(false));
        assertThat(recexp.matches("cd"), is(false));
        assertThat(recexp.matches("cabd"), is(false));
        assertThat(recexp.matches("caxbd"), is(false));
        assertThat(recexp.matches("cacdbd"), is(false));
        assertThat(recexp.matches("ccacxdbdd"), is(false));
    }

    @Test
    public void simpleGroupTest() {
        String input = "a";
        Recexp recexp = Recexp.compile("a");

        RecexpMatcher matcher = recexp.matcher(input);

        assertThat(matcher.value(), is(input));
        assertThat(matcher.groupCount(), is(0));
        assertThat(matcher.groups().length, is(0));
    }

    @Test
    public void basicRecursiveGroupTest() {
        Recexp recexp = Recexp.compile("a(@this?)b");

        RecexpMatcher matcher1 = recexp.matcher("ab");

        assertThat(matcher1.groupCount(), is(3));
        assertThat(matcher1.groups().length, is(3));

        assertThat(matcher1.value(), is("ab"));

        assertThat(matcher1.group(0).value(), is(matcher1.value()));
        assertThat(matcher1.group("a(@this?)b").value(), is(matcher1.value()));

        assertThat(matcher1.group(0).groupCount(), is(matcher1.groupCount()));
        assertThat(matcher1.group(0).groups().length, is(matcher1.groups().length));

        assertThat(matcher1.group(1).value(), is("a"));
        assertThat(matcher1.group("a").value(), is("a"));
        assertThat(matcher1.group(1).groupCount(), is(0));

        assertThat(matcher1.group(2).value(), is(""));
        assertThat(matcher1.group("@this?").value(), is(""));
        assertThat(matcher1.group(2).groupCount(), is(0));

        assertThat(matcher1.group(3).value(), is("b"));
        assertThat(matcher1.group("b").value(), is("b"));
        assertThat(matcher1.group(3).groupCount(), is(0));

        RecexpMatcher matcher2 = recexp.matcher("aabb");

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

        assertThat(matcher2.group(2).groupCount(), is(1));

        assertThat(matcher2.group(2).group(1).value(), is("ab"));
        assertThat(matcher2.group(2).group("@this?").value(), is("ab"));
        assertThat(matcher2.group(2).group(1).groupCount(), is(3));
    }

    @Test
    public void customSubgroupsTest() {
        Recexp recexp = Recexp.compile("fi(r)st(@this?)second");

        RecexpMatcher matcher1 = recexp.matcher("firstsecond");

        assertThat(matcher1.groupCount(), is(5));
        assertThat(matcher1.group(1).value(), is("fi"));
        assertThat(matcher1.group(2).value(), is("r"));
        assertThat(matcher1.group(3).value(), is("st"));
        assertThat(matcher1.group(4).value(), is(""));
        assertThat(matcher1.group(5).value(), is("second"));

        RecexpMatcher matcher2 = recexp.matcher("firstfirstsecondsecond");

        assertThat(matcher2.groupCount(), is(5));
        assertThat(matcher2.group(1).value(), is("fi"));
        assertThat(matcher2.group(2).value(), is("r"));
        assertThat(matcher2.group(3).value(), is("st"));
        assertThat(matcher2.group(4).value(), is("firstsecond"));
        assertThat(matcher2.group(5).value(), is("second"));

        assertThat(matcher2.group(4).groupCount(), is(1));
        assertThat(matcher2.group(4).group(1).value(), is("firstsecond"));

        assertThat(matcher2.group(4).group(1).groupCount(), is(5));
        assertThat(matcher2.group(4).group(1).group(1).value(), is("fi"));
        assertThat(matcher2.group(4).group(1).group(2).value(), is("r"));
        assertThat(matcher2.group(4).group(1).group(3).value(), is("st"));
        assertThat(matcher2.group(4).group(1).group(4).value(), is(""));
        assertThat(matcher2.group(4).group(1).group(5).value(), is("second"));
    }

    @Test
    public void namedGroupsTest() {
        Recexp recexp = Recexp.builder()
                .rule("WORD", "\\w+")
                .rule("SENTENCE", "(@WORD\\s)+@WORD?[\\.\\!\\?]")
                .build();

        RecexpMatcher matcher = recexp.matcher("SENTENCE", "Hello Recexp!");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group("(@WORD\\s)+"), is(matcher.group(1)));
        assertThat(matcher.group("@WORD?"), is(matcher.group(2)));
        assertThat(matcher.group("[\\.\\!\\?]"), is(matcher.group(3)));
    }

    @Test
    public void findByRuleTest() {
        Recexp recexp = Recexp.builder()
                .rule("WORD", "@WORD\\s|\\w+")
                .rule("PERIOD", "[\\.\\!\\?]")
                .rule("SENTENCE", "@WORD+@PERIOD")
                .build();

        RecexpMatcher matcher = recexp.matcher("SENTENCE", "Hello Recexp!");

        assertThat(matcher.matches(), is(true));
        assertThat(matcher.groupCount(), is(2));

        RecexpGroup[] words = matcher.findByRule("WORD");
        assertThat(words.length, is(2));
    }

    @Test
    public void groupsTest() {
        Recexp recexp = Recexp.compile("a(b(c)(d))e");
        RecexpMatcher matcher = recexp.matcher("abcde");

        // groups are hierarchical - for a level
        // that differs from the RegExp
        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).groupCount(), is(3));
        assertThat(matcher.group(2).group(1).groupCount(), is(0));
        assertThat(matcher.group(2).group(2).groupCount(), is(0));
        assertThat(matcher.group(2).group(3).groupCount(), is(0));
        assertThat(matcher.group(3).groupCount(), is(0));

        assertThat(matcher.value(), is("abcde"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(2).value(), is("bcd"));
        assertThat(matcher.group(2).group(1).value(), is("b"));
        assertThat(matcher.group(2).group(2).value(), is("c"));
        assertThat(matcher.group(2).group(3).value(), is("d"));
        assertThat(matcher.group(3).value(), is("e"));

        assertThat(matcher.name(), is("a(b(c)(d))e"));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(2).name(), is("b(c)(d)"));
        assertThat(matcher.group(2).group(1).name(), is("b"));
        assertThat(matcher.group(2).group(2).name(), is("c"));
        assertThat(matcher.group(2).group(3).name(), is("d"));
        assertThat(matcher.group(3).name(), is("e"));
    }

    @Test
    public void explicitDoubleGroupTest() {
        Recexp recexp = Recexp.compile("a((b))");
        RecexpMatcher matcher = recexp.matcher("ab");

        assertThat(matcher.groupCount(), is(2));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).groupCount(), is(1));
        assertThat(matcher.group(2).group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(2).value(), is("b"));
        assertThat(matcher.group(2).group(1).value(), is("b"));

        assertThat(matcher.name(), is("a((b))"));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(2).name(), is("(b)"));
        assertThat(matcher.group(2).group(1).name(), is("b"));
    }

    @Test
    public void recursiveGroupTest() {
        Recexp recexp = Recexp.compile("a@this?b");
        RecexpMatcher matcher = recexp.matcher("aabb");

        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).groupCount(), is(1));
        assertThat(matcher.group(3).groupCount(), is(0));

        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(2).value(), is("ab"));
        assertThat(matcher.group(3).value(), is("b"));

        assertThat(matcher.name(), is("a@this?b"));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(2).name(), is("@this?"));
        assertThat(matcher.group(3).name(), is("b"));
    }

    @Test
    public void recursiveGroupExplicitGroupTest() {
        Recexp recexp = Recexp.compile("a(@this?)b");
        RecexpMatcher matcher = recexp.matcher("aabb");

        assertThat(matcher.groupCount(), is(3));
        assertThat(matcher.group(1).groupCount(), is(0));
        assertThat(matcher.group(2).groupCount(), is(1));
        assertThat(matcher.group(2).group(1).groupCount(), is(3));
        assertThat(matcher.group(2).group(1).group(1).groupCount(), is(0));
        assertThat(matcher.group(2).group(1).group(2).groupCount(), is(0));
        assertThat(matcher.group(2).group(1).group(3).groupCount(), is(0));
        assertThat(matcher.group(3).groupCount(), is(0));

        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.group(1).value(), is("a"));
        assertThat(matcher.group(2).value(), is("ab"));
        assertThat(matcher.group(2).group(1).value(), is("ab"));
        assertThat(matcher.group(3).value(), is("b"));

        assertThat(matcher.name(), is("a(@this?)b"));
        assertThat(matcher.group(1).name(), is("a"));
        assertThat(matcher.group(2).name(), is("@this?"));
        assertThat(matcher.group(3).name(), is("b"));
    }

    @Test
    public void recursiveReferenceWithSelfNameTest() {
        Recexp recexp = Recexp.builder()
                .rule("RULE1", "@A@RULE1?@B")
                .rule("A", "a")
                .rule("B", "b")
                .build();

        assertThat(recexp.matcher("a").matches(), is(true));
        assertThat(recexp.matcher("b").matches(), is(true));
        assertThat(recexp.matcher("ab").matches(), is(true));
        assertThat(recexp.matcher("aabb").matches(), is(true));
        assertThat(recexp.matcher("aaabbb").matches(), is(true));

        assertThat(recexp.matcher("").matches(), is(false));
        assertThat(recexp.matcher("ba").matches(), is(false));
        assertThat(recexp.matcher("aab").matches(), is(false));
        assertThat(recexp.matcher("abb").matches(), is(false));
    }

    @Test
    public void popularGrammars_palindromesTest() {
        Recexp palindromesGrammar1 = Recexp.compile(
                "0(@this)0|1(@this)1|0|1|@eps");
        assertThat(palindromesGrammar1.matches(""), is(true));
        assertThat(palindromesGrammar1.matches("0"), is(true));
        assertThat(palindromesGrammar1.matches("1"), is(true));
        assertThat(palindromesGrammar1.matches("11"), is(true));
        assertThat(palindromesGrammar1.matches("00"), is(true));
        assertThat(palindromesGrammar1.matches("010"), is(true));
        assertThat(palindromesGrammar1.matches("101"), is(true));
        assertThat(palindromesGrammar1.matches("000"), is(true));
        assertThat(palindromesGrammar1.matches("111"), is(true));
        assertThat(palindromesGrammar1.matches("0110"), is(true));
        assertThat(palindromesGrammar1.matches("1001"), is(true));
        assertThat(palindromesGrammar1.matches("10101"), is(true));
        assertThat(palindromesGrammar1.matches("10"), is(false));
        assertThat(palindromesGrammar1.matches("01"), is(false));
        assertThat(palindromesGrammar1.matches("1101"), is(false));

        Recexp palindromesGrammar2 = Recexp.builder()
                .rule("S", "0(@S)0|1(@S)1|0|1|@eps")
                .build();
        assertThat(palindromesGrammar2.matches(""), is(true));
        assertThat(palindromesGrammar2.matches("0"), is(true));
        assertThat(palindromesGrammar2.matches("1"), is(true));
        assertThat(palindromesGrammar2.matches("11"), is(true));
        assertThat(palindromesGrammar2.matches("00"), is(true));
        assertThat(palindromesGrammar2.matches("010"), is(true));
        assertThat(palindromesGrammar2.matches("101"), is(true));
        assertThat(palindromesGrammar2.matches("000"), is(true));
        assertThat(palindromesGrammar2.matches("111"), is(true));
        assertThat(palindromesGrammar2.matches("0110"), is(true));
        assertThat(palindromesGrammar2.matches("1001"), is(true));
        assertThat(palindromesGrammar2.matches("10101"), is(true));
        assertThat(palindromesGrammar2.matches("10"), is(false));
        assertThat(palindromesGrammar2.matches("01"), is(false));
        assertThat(palindromesGrammar2.matches("1101"), is(false));

        Recexp palindromesGrammar3 = Recexp.builder()
                .rule("S", "0")
                .rule("S", "1")
                .rule("S", "0(@S)0")
                .rule("S", "1(@S)1")
                .rule("S", "@eps")
                .build();
        assertThat(palindromesGrammar3.matches(""), is(true));
        assertThat(palindromesGrammar3.matches("0"), is(true));
        assertThat(palindromesGrammar3.matches("1"), is(true));
        assertThat(palindromesGrammar3.matches("11"), is(true));
        assertThat(palindromesGrammar3.matches("00"), is(true));
        assertThat(palindromesGrammar3.matches("010"), is(true));
        assertThat(palindromesGrammar3.matches("101"), is(true));
        assertThat(palindromesGrammar3.matches("000"), is(true));
        assertThat(palindromesGrammar3.matches("111"), is(true));
        assertThat(palindromesGrammar3.matches("0110"), is(true));
        assertThat(palindromesGrammar3.matches("1001"), is(true));
        assertThat(palindromesGrammar3.matches("10101"), is(true));
        assertThat(palindromesGrammar3.matches("10"), is(false));
        assertThat(palindromesGrammar3.matches("01"), is(false));
        assertThat(palindromesGrammar3.matches("1101"), is(false));
    }

    @Ignore
    @Test
    public void popularGrammars_stringWithSameNumberOf0sAnd1sTest() {
        Recexp stringWithSameNumberOf0sAnd1sGrammar1 = Recexp.compile(
                "0(@this)1(@this)|1(@this)0(@this)|@eps");
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches(""), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("0101"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("1010"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("1100"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("110010"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("110100"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("11000101"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("0"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("1"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("00"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("11"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("101"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar1.matches("010"), is(false));

        Recexp stringWithSameNumberOf0sAnd1sGrammar2 = Recexp.builder()
                .rule("S", "0(@S)1(@S)|1(@S)0(@S)|@eps")
                .build();
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches(""), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("0101"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("1010"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("1100"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("110010"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("110100"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("11000101"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("0"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("1"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("00"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("11"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("101"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar2.matches("010"), is(false));

        Recexp stringWithSameNumberOf0sAnd1sGrammar3 = Recexp.builder()
                .rule("S", "0(@S)1(@S)")
                .rule("S", "1(@S)0(@S)")
                .rule("S", "@eps")
                .build();
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches(""), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("0101"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("1010"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("1100"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("110010"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("110100"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("11000101"), is(true));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("0"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("1"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("00"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("11"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("101"), is(false));
        assertThat(stringWithSameNumberOf0sAnd1sGrammar3.matches("010"), is(false));
    }

    @Ignore
    @Test
    public void popularGrammars_arithmeticExpressionsTest() {
        Recexp arithmeticExpressionsGrammar = Recexp.builder()
                .rule("E", "@E±@T|@T")      // expressions
                .rule("T", "@T×@F|@F")      // terms
                .rule("F", "\\(@E\\)|X|Y")  // factors
                .build();
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X±Y").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X±X").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X×X").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X×Y").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X±Y)").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X×Y)").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X±X×Y").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X×X)±Y").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X±X)×Y").matches(), is(true));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X±X)×(Y×X)").matches(), is(true));

        assertThat(arithmeticExpressionsGrammar.matcher("E", "").matches(), is(false));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X±").matches(), is(false));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X ± X").matches(), is(false));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X × X").matches(), is(false));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "X××X").matches(), is(false));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X×X)×A").matches(), is(false));
        assertThat(arithmeticExpressionsGrammar.matcher("E", "(X×X)(Y×X)").matches(), is(false));
    }
}
