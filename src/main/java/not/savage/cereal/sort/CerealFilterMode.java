package not.savage.cereal.sort;

/**
 * Represents the mode of a filter. Allows for platform-agnostic filtering of data.
 */
public enum CerealFilterMode {

    /**
     * > (Greater than) mode, X > Y
     * MongoDB equivalent: $gt
     * SQL equivalent: >
     * Java equivalent: >
     */
    GREATER_THAN,
    /**
     * < (Less than) mode, X < Y
     * MongoDB equivalent: $lt
     * SQL equivalent: <
     * Java equivalent: <
     */
    LESS_THAN,
    /**
     * = (Equal to) mode, X = Y
     * MongoDB equivalent: $eq
     * SQL equivalent: =
     * Java equivalent: ==
     */
    EQUAL,
    /**
     * != (Not equal to) mode, X != Y
     * MongoDB equivalent: $ne
     * SQL equivalent: !=
     * Java equivalent: !=
     */
    NOT_EQUAL,
    /**
     * >= (Greater than or equal to) mode, X >= Y
     * MongoDB equivalent: $gte
     * SQL equivalent: >=
     * Java equivalent: >=
     */
    GREATER_THAN_OR_EQUAL_TO,
    /**
     * <= (Less than or equal to) mode, X <= Y
     * MongoDB equivalent: $lte
     * SQL equivalent: <=
     * Java equivalent: <=
     */
    LESS_THAN_OR_EQUAL_TO

    ;

}
