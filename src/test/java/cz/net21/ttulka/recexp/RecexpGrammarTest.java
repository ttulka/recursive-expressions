package cz.net21.ttulka.recexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

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

        RecexpMatcher matcher = grammar.matcher("");

        assertThat(matcher.rules, containsInAnyOrder(grammar.rules.toArray()));
    }
}
