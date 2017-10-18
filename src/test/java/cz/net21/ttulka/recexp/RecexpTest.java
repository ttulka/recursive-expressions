package cz.net21.ttulka.recexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RecexpTest {

    @Test
    public void simpleRecursiveTest() {
        RecursiveGrammar grammar = new RecursiveGrammar("a($this)b");

        assertThat(grammar.matches("ab"), is(true));
        assertThat(grammar.matches("aabb"), is(true));
        assertThat(grammar.matches("aaabbb"), is(true));

        assertThat(grammar.matches(""), is(false));
        assertThat(grammar.matches("a"), is(false));
        assertThat(grammar.matches("b"), is(false));
        assertThat(grammar.matches("ba"), is(false));
        assertThat(grammar.matches("abb"), is(false));
    }

    @Test
    public void doubleRecursiveTest() {
        RecursiveGrammar grammar = new RecursiveGrammar("a($this)b($this)c");

        assertThat(grammar.matches("abc"), is(true));
        assertThat(grammar.matches("abc"), is(true));
        assertThat(grammar.matches("ababcc"), is(true));
        assertThat(grammar.matches("aabcbc"), is(true));
        assertThat(grammar.matches("aabcbabcc"), is(true));
        assertThat(grammar.matches("aaabcbabccbaabcbabccc"), is(true));

        assertThat(grammar.matches(""), is(false));
        assertThat(grammar.matches("a"), is(false));
        assertThat(grammar.matches("ab"), is(false));
        assertThat(grammar.matches("ac"), is(false));
        assertThat(grammar.matches("bc"), is(false));
        assertThat(grammar.matches("cba"), is(false));
        assertThat(grammar.matches("aabc"), is(false));
        assertThat(grammar.matches("aabcc"), is(false));
        assertThat(grammar.matches("aabbcc"), is(false));
        assertThat(grammar.matches("aabbc"), is(false));
        assertThat(grammar.matches("aacbc"), is(false));
        assertThat(grammar.matches("ababc"), is(true));
    }
}
