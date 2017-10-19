package cz.net21.ttulka.recexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

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
    public void doubleRecursiveTest() {
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

    @Test(expected = RecexpCyclicRuleException.class)
    public void cyclicRuleTest() {
        new RecexpGrammar("a($this)b").accepts("ab");
        fail("Cyclic rule should throw an exception.");
    }
}
