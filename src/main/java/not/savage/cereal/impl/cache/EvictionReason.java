package not.savage.cereal.impl.cache;

/**
 * Represents the reason an entry was evicted from the cache.
 */
public enum EvictionReason {
    /**
     * The entry was evicted because it expired.
     */
    EXPIRED,
    /**
     * The entry was evicted because the cache reached its maximum size.
     */
    MAX_SIZE,
    /**
     * The entry was evicted because the cache was shutdown.
     * This is also the eviction reason when {@link EvictingCache#writeAll()} is called.
     */
    SHUTDOWN,
    /**
     * The entry was evicted because it was explicitly removed via {@link EvictingCache#remove(Object)}.
     */
    EXPLICIT
    ;
}