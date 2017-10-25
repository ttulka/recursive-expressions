package cz.net21.ttulka.recexp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        assertThat(matcher.value(), is(input));
    }

    @Test
    public void groupTest() {
        RecexpRule rule = new RecexpRule("a(b(c)(d))e");
        RecexpMatcher matcher = new RecexpMatcher("abcde", Collections.singleton(rule));

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
    }

    @Test
    public void groupExplicitZeroGroupTest() {
        RecexpRule rule = new RecexpRule("(a(b(c)(d))e)");
        RecexpMatcher matcher = new RecexpMatcher("abcde", Collections.singleton(rule));

        assertThat(matcher.groupCount(), is(2));    // 4 in case of RegExp
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).groupCount(), is(2));
        assertThat(matcher.group(1).group(1).group(1).groupCount(), is(0));
        assertThat(matcher.group(1).group(1).group(2).groupCount(), is(0));

        assertThat(matcher.value(), is("abcde"));
        assertThat(matcher.group(1).value(), is("abcde"));
        assertThat(matcher.group(1).group(1).value(), is("bcd"));
        assertThat(matcher.group(1).group(1).group(1).value(), is("c"));
        assertThat(matcher.group(1).group(1).group(2).value(), is("d"));

        assertThat(matcher.value(), is("abcde"));
        assertThat(matcher.group(1).value(), is("abcde"));
        assertThat(matcher.group(1).group(1).value(), is("bcd"));
        assertThat(matcher.group(1).group(1).group(1).value(), is("c"));
        assertThat(matcher.group(1).group(1).group(2).value(), is("d"));
    }

    @Test
    public void explicitDoubleGroupTest() {
        RecexpRule rule = new RecexpRule("a((b))");
        RecexpMatcher matcher = new RecexpMatcher("ab", Collections.singleton(rule));

        assertThat(matcher.groupCount(), is(1));    // 2 in case of RegExp
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("ab"));
        assertThat(matcher.group(1).value(), is("b"));
        assertThat(matcher.group(1).group(1).value(), is("b"));
    }

    @Test
    public void recursiveGroupTest() {
        RecexpRule rule = new RecexpRule("a$this?b");
        RecexpMatcher matcher = new RecexpMatcher("aabb", Collections.singleton(rule));

        assertThat(matcher.groupCount(), is(1));
        assertThat(matcher.group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.group(1).value(), is("ab"));
    }

    @Test
    public void recursiveGroupExplicitGroupTest() {
        RecexpRule rule = new RecexpRule("a($this?)b");
        RecexpMatcher matcher = new RecexpMatcher("aabb", Collections.singleton(rule));

        assertThat(matcher.groupCount(), is(1));
        assertThat(matcher.group(1).groupCount(), is(1));
        assertThat(matcher.group(1).group(1).groupCount(), is(0));

        assertThat(matcher.value(), is("aabb"));
        assertThat(matcher.group(1).value(), is("ab"));
        assertThat(matcher.group(1).group(1).value(), is("ab"));
    }
}
