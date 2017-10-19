package cz.net21.ttulka.recexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author ttulka
 */
public class RecexpTest {

    @Test
    public void simpleRecursiveTest() {
        RecexpGrammar grammar = new RecexpGrammar("a$this?b");

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
        RecexpGrammar grammar = new RecexpGrammar("a$this?b$this?c");

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
                new RecexpRule("RULE", "a$RULE?b$RULE?c"));

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
    public void cyclicSimpleThisTest() {
        new RecexpGrammar("a($this)b").accepts("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicDoubleThisTest() {
        new RecexpGrammar("a($this)b($this)c").accepts("abc");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicDoubleThis2Test() {
        new RecexpGrammar("a($this)b$this?c").accepts("abc");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicSimpleRuleTest() {
        new RecexpGrammar(new RecexpRule("RULE_CYCLIC", "a($RULE_CYCLIC)b"))
                .accepts("ab");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void transitiveCyclicTwoRulesTest() {
        new RecexpGrammar()
                .addRule("RULE_1", "a($RULE_2)b")
                .addRule("RULE_2", "c($RULE_1)d")
                .accepts("acdb");
        fail("Cyclic rule should throw an exception.");
    }

    @Test(expected = RecexpCyclicRuleException.class)
    public void transitiveCyclicThreeRulesTest() {
        new RecexpGrammar()
                .addRule("RULE_1", "a($RULE_2)b")
                .addRule("RULE_2", "c($RULE_1)d")
                .addRule("RULE_3", "x")
                .accepts("acxdb");
        fail("Cyclic rule should throw an exception.");
    }

    @Test
    public void orThisTest() {
        RecexpGrammar grammar = new RecexpGrammar("a($this)b|c");

        assertThat(grammar.accepts("c"), is(true));
        assertThat(grammar.accepts("acb"), is(true));
        assertThat(grammar.accepts("aacbb"), is(true));
        assertThat(grammar.accepts("aaacbbb"), is(true));

        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabb"), is(false));
        assertThat(grammar.accepts("abc"), is(false));
        assertThat(grammar.accepts("cc"), is(false));
        assertThat(grammar.accepts("accb"), is(false));
    }

    @Test
    public void twoRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("RULE_AB", "a($RULE_C)b")
                .addRule("RULE_C", "c");

        assertThat(grammar.accepts("c"), is(true));
        assertThat(grammar.accepts("acb"), is(true));
        assertThat(grammar.accepts("aacbb"), is(true));
        assertThat(grammar.accepts("aaacbbb"), is(true));

        assertThat(grammar.accepts("ab"), is(false));
        assertThat(grammar.accepts("aabb"), is(false));
        assertThat(grammar.accepts("abc"), is(false));
        assertThat(grammar.accepts("cc"), is(false));
        assertThat(grammar.accepts("accb"), is(false));
    }

    @Test
    public void orThreeRulesTest() {
        RecexpGrammar grammar = new RecexpGrammar()
                .addRule("RULE_AB", "a($RULE_C)b|$RULE_X")
                .addRule("RULE_CD", "c($RULE_A)d")
                .addRule("RULE_X", "x");

        assertThat(grammar.accepts("x"), is(true));
        assertThat(grammar.accepts("cxd"), is(true));
        assertThat(grammar.accepts("cacxdbd"), is(true));
        assertThat(grammar.accepts("cacacxdbdbd"), is(true));
        assertThat(grammar.accepts("cacacacxdbdbdbd"), is(true));
        assertThat(grammar.accepts("acxdb"), is(true));
        assertThat(grammar.accepts("acacxdbdb"), is(true));
        assertThat(grammar.accepts("acacacxdbdbdb"), is(true));

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
}
