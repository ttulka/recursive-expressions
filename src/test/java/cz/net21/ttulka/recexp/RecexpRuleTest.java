package cz.net21.ttulka.recexp;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ttulka
 */
public class RecexpRuleTest {

    @Test
    public void noNameConstructorTest() {
        RecexpRule rule = new RecexpRule("a");

        assertThat(rule.getName(), is("a"));
        assertThat(rule.getExpression(), is("a"));
    }

    @Test
    public void nameConstructorTest() {
        RecexpRule rule = new RecexpRule("A", "a");

        assertThat(rule.getName(), is("A"));
        assertThat(rule.getExpression(), is("a"));
    }

    @Test
    public void equalsTest() {
        RecexpRule rule1 = new RecexpRule("a");
        RecexpRule rule2 = new RecexpRule("b");

        assertThat(rule1.equals(rule2), is(false));

        RecexpRule rule3 = new RecexpRule("a");

        assertThat(rule1.equals(rule3), is(true));

        RecexpRule ruleA = new RecexpRule("A", "a");
        RecexpRule ruleB = new RecexpRule("B", "a");

        assertThat(ruleA.equals(ruleB), is(false));

        // it is possible to define another rule with an existing name
        // this is interpreted as an OR part of one rule
        RecexpRule ruleA_ = new RecexpRule("A", "c");

        assertThat(ruleA.equals(ruleA_), is(false));

        RecexpRule ruleA2 = new RecexpRule("A", "a");

        assertThat(ruleA.equals(ruleA2), is(true));
    }
}
