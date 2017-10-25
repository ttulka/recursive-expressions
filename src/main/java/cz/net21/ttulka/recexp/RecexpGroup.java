package cz.net21.ttulka.recexp;

/**
 * Class representing an expression group.
 *
 * @author ttulka
 */
public class RecexpGroup {

    protected RecexpGroup() {
    }

    /**
     * Returns the input subsequence captured by the given group.
     *
     * @return the input subsequence captured by the given group
     */
    public String value() {
        return null;    // TODO
    }

    /**
     * Returns the name of capturing group.
     *
     * @return the name of capturing group
     */
    public String name() {
        return null;    // TODO
    }

    /**
     * Returns the number of capturing groups in the input for this matcher's grammar.
     * <p>
     * Group zero denotes the entire pattern by convention. It is not included in this count.
     *
     * @return the number of capturing groups in the input for this matcher's grammar
     */
    public int groupCount() {
        return 0; // TODO
    }

    /**
     * Returns all the capturing groups in the input for this matcher's grammar.
     *
     * @return the capturing groups in the input for this matcher's grammar
     */
    public RecexpGroup[] groups() {
        return null; // TODO
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
        return null; // TODO
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
        return null; // TODO
    }
}
