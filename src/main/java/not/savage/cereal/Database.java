package not.savage.cereal;

/**
 * Represents a direct connection or implementation for interacting directly with a database or file system.
 */
public abstract class Database {

    /**
     * Start the database
     * @return boolean success
     */
    public abstract boolean start();

    /**
     * Shutdown the database
     */
    public abstract void shutdown();

}
