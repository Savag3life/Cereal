package not.savage.cereal.config.sub;

/**
 * Configuration object for the {@link not.savage.cereal.internal.cache.EvictingCache}
 * @param expireAfterAccessMinutes How long before the cache entry is evicted after it was last accessed
 * @param expireAfterWriteMinutes How long before the cache entry is evicted after it was created
 */
public record CacheConfig(
        int expireAfterAccessMinutes,
        int expireAfterWriteMinutes
) {
}
