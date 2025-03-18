package not.savage.cereal;

import not.savage.cereal.internal.CerealCache;

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

    /**
     * Get a cache from the database after it's been instantiated
     * @param cache The cache class
     * @param <T> Class type of the Cache we want.
     * @param <K> Class type of the DataBlob key
     * @param <V> Class type of the DataBlob value
     * @return The cache instance
     */
    public abstract <T extends Cache<K, V>, K extends DataBlob<V>, V> T getCache(Class<T> cache);

    /**
     * Uses {@link Class#getSimpleName()} to generate a standard naming convention for collections.
     * Each Cereal "instance" is assigned a UUID as a "server id" to ensure that each server has its own distinct cache.
     * We use this unique ID to suffix our collection names to ensure that each server has its own distinct cache.
     * @param clazz Class to generate
     * @return Collection name
     */
    public String classToCollectionName(Class<? extends CerealCache<?>> clazz) {
        return clazz.getSimpleName().replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

}
