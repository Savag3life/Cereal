package not.savage.cereal.impl.sort;

/**
 * Represents the sort mode for the Cereal database. Allows for platform-agnostic filtering of data.
 */
public enum CerealSortMode {

    /**
     * Sorts the data in ascending order.
     * MongoDB Equivalent: 1
     * SQL Equivalent: ASC
     */
    ASCENDING,
    /**
     * Sorts the data in descending order.
     * MongoDB Equivalent: -1
     * SQL Equivalent: DESC
     */
    DESCENDING
    ;

}
