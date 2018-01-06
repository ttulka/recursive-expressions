package cz.net21.ttulka.recexp;

/**
 * Derivate tree node as a candidate group.
 *
 * @author ttulka
 * @see RecexpMatcher
 */
public class RecexpGroup {

    private final String name;
    private final String value;
    private final RecexpGroup[] groups;

    protected RecexpGroup(String name, String value, RecexpGroup[] groups) {
        this.name = name;
        this.value = value;
        this.groups = groups;
    }

    /**
     * Returns the input subsequence captured by the given group.
     *
     * @return the input subsequence captured by the given group
     */
    public String value() {
        return value;
    }

    /**
     * Returns the name of capturing group.
     *
     * @return the name of capturing group
     */
    public String name() {
        return name;
    }

    /**
     * Returns the number of capturing groups in the input for this matcher's grammar.
     * <p>
     * Group zero denotes the entire pattern by convention. It is not included in this count.
     *
     * @return the number of capturing groups in the input for this matcher's grammar
     */
    public int groupCount() {
        return groups != null ? groups.length : 0;
    }

    /**
     * Returns all the capturing groups in the input for this matcher's grammar.
     *
     * @return the capturing groups in the input for this matcher's grammar
     */
    public RecexpGroup[] groups() {
        return groups;
    }

    /**
     * Returns the subgroup captured by the given group.
     * <p>
     * Captured subgroup are indexed from left to right, starting at one. Group zero denotes the entire pattern.
     *
     * @param group the index of a capturing group
     * @return The (possibly empty) subgroup captured by the group, or null if the group failed to match part of the input
     */
    public RecexpGroup group(int group) {
        if (groups == null || group < 0 || group > groups.length) {
            return null;
        }
        if (group == 0) {
            return this;
        }
        return groups[group - 1];
    }

    /**
     * Returns the subgroup captured by the given group.
     * <p>
     * Captured subgroups are indexed from left to right, starting at one. Group zero denotes the entire pattern.
     *
     * @param groupName the name of a capturing group
     * @return The (possibly empty) subgroup captured by the group, or null if the group failed to match part of the input
     */
    public RecexpGroup group(String groupName) {
        if (groupName.equals(name)) {
            return this;
        }
        for (RecexpGroup group : groups) {

            if (group.name().equals(groupName)) {
                return group;
            }
        }
        return null;
    }
}
