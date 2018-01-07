package cz.net21.ttulka.recexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ttulka
 */
public class RuleTest {

    @Test
    public void noNameConstructorTest() {
        Rule rule = new Rule("a");

        assertThat(rule.getName(), is("a"));
        assertThat(rule.getExpression().getRoot().getExpression().getText(), is("a"));
    }

    @Test
    public void nameConstructorTest() {
        Rule rule = new Rule("A", "a");

        assertThat(rule.getName(), is("A"));
        assertThat(rule.getExpression().getRoot().getExpression().getText(), is("a"));
    }

    @Test
    public void equalsTest() {
        Rule rule1 = new Rule("a");
        Rule rule2 = new Rule("b");

        assertThat(rule1.equals(rule2), is(false));

        Rule rule3 = new Rule("a");

        assertThat(rule1.equals(rule3), is(true));

        Rule ruleA = new Rule("A", "a");
        Rule ruleB = new Rule("B", "a");

        assertThat(ruleA.equals(ruleB), is(false));

        // it is possible to define another rule with an existing name
        // this is interpreted as an OR part of one rule
        Rule ruleA_ = new Rule("A", "c");

        assertThat(ruleA.equals(ruleA_), is(false));

        Rule ruleA2 = new Rule("A", "a");

        assertThat(ruleA.equals(ruleA2), is(true));
    }

    @Test
    public void epsilonImplicitNotUsedTest() {
        RecexpGrammar grammar = RecexpGrammar.compile("ab");

        assertThat(grammar.matches("ab"), is(true));
        assertThat(grammar.matches(""), is(false));
    }

    @Test
    public void epsilonReferenceExplicitlyUsedTest() {
        RecexpGrammar grammar = RecexpGrammar.builder()
                .rule("E", "@EPS")
                .build();

        assertThat(grammar.matches("a"), is(false));
        assertThat(grammar.matches(""), is(true));
    }

    @Test
    public void epsilonReferenceExplicitlyUsedInOrTest() {
        RecexpGrammar grammar = RecexpGrammar.builder()
                .rule("A", "a|@EPS")
                .build();

        assertThat(grammar.matches("a"), is(true));
        assertThat(grammar.matches(""), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void epsilonExplicitlyDefinedExceptionTest() {
        RecexpGrammar.builder()
                .rule("EPS", "a")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void thisExplicitlyDefinedExceptionTest() {
        RecexpGrammar.builder()
                .rule("this", "a")
                .build();
    }

    @Test
    public void thisExplicitlyDefinedCaseSensitiveTest() {
        RecexpGrammar.builder()
                .rule("This", "a")
                .build();

        RecexpGrammar.builder()
                .rule("tHiS", "a")
                .build();

        RecexpGrammar.builder()
                .rule("THIS", "a")
                .build();
    }
}
