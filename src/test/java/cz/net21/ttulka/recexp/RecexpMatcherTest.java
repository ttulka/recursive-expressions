package cz.net21.ttulka.recexp;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author ttulka
 */
public class RecexpMatcherTest {

    @Test
    public void constructorTest() {
        String input = "abc";

        Set<RecexpRule> rules = new HashSet<RecexpRule>();
        rules.add(new RecexpRule("a"));
        rules.add(new RecexpRule("b"));

        RecexpMatcher matcher = new RecexpMatcher(input, rules);

        assertThat(matcher.input, is(input));
        assertThat(matcher.rules, containsInAnyOrder(rules.toArray()));
    }

    @Test
    public void valueTest() {
        String input = "abc";

        RecexpMatcher matcher = new RecexpMatcher(input, null);

        assertThat(matcher.getValue(), is(input));
    }
}
