package cz.net21.ttulka.recexp;

import java.util.Arrays;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author ttulka
 */
public class GroupTest {

    @Test
    public void constructorTest() {
        String name = "(abc)";
        String value = "abc";
        RecexpGroup[] groups = new RecexpGroup[]{new RecexpGroup(
                name, value, new RecexpGroup[]{}
        )};

        RecexpGroup group = new RecexpGroup(name, value, groups);

        assertThat(group.name(), is(name));
        assertThat(group.value(), is(value));
        assertThat(Arrays.asList(group.groups()), containsInAnyOrder(groups));
    }
}
