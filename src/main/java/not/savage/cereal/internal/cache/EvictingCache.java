package not.savage.cereal.internal.cache;

import lombok.NonNull;
import not.savage.cereal.CerealLogger;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Simple Evicting Cache implementation.
 * Items are evicted based on expireAfterAccess and expireAfterWrite times.
 * Considered using Guava's CacheBuilder, could still be a better alternative.
 * @param <K> Key Type
 * @param <V> Value Type
 */
public abstract class EvictingCache<K, V> implements CerealLogger {

    private final ConcurrentHashMap<K, CacheEntry<V>> cache;

    private final long expireAfterAccess;
    private final long expireAfterWrite;

    private final ScheduledExecutorService executorService;

    /**
     * Creates a new Evicting Cache with the specified expireAfterAccess and expireAfterWrite times.
     * @param expireAfterAccessMinutes Time in minutes to expire an object after it was last accessed
     * @param expireAfterWriteMinutes Time in minutes to expire an object after it was created
     */
    public EvictingCache(long expireAfterAccessMinutes, long expireAfterWriteMinutes) {
        this.cache = new ConcurrentHashMap<>();
        this.expireAfterAccess = TimeUnit.MINUTES.toMillis(expireAfterAccessMinutes);
        this.expireAfterWrite = TimeUnit.MINUTES.toMillis(expireAfterWriteMinutes);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::maintenance, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * An implementation of this cache should implement this method to handle evictions.
     * @param value Value to expire
     * @param reason Reason for eviction
     */
    public abstract void expire(V value, EvictionReason reason);

    protected void remove(@NonNull K key) {
        cache.remove(key);
    }

    /**
     * Get a value from the cache.
     * @param key Key to lookup
     * @return Value if found (cached or created is undefined), empty if not found.
     */
    public Optional<V> get(@NonNull K key) {
        CacheEntry<V> entry = cache.get(key);

        if (entry != null) {
            entry.setLastAccessed(System.currentTimeMillis());
            return Optional.of(entry.getValue());
        }

        return Optional.empty();
    }

    /**
     * Get all values in the cache.
     * @return Set of all values in the cache.
     */
    public Set<V> getAll() {
        return cache.values().stream().map(CacheEntry::getValue).collect(Collectors.toSet());
    }

    /**
     * Cache a value.
     * @param key Key
     * @param value Value
     */
    public void cache(@NonNull K key, @NonNull V value) {
        // TODO - Don't overwrite existing values unless we are in network mode (Not implemented yet)
        if (cache.containsKey(key)) return;
        cache.put(key, new CacheEntry<>(value));
    }

    /**
     * Perform maintenance on the cache. (expirations)
     */
    public void maintenance() {
        for (Map.Entry<K, CacheEntry<V>> e : cache.entrySet()) {
            if (System.currentTimeMillis() - e.getValue().getLastAccessed() > expireAfterAccess && System.currentTimeMillis() - e.getValue().getCreated() > expireAfterWrite) {
                debug("[EvCache] DELETE " + e.getKey().toString());
                cache.remove(e.getKey());
                expire(e.getValue().getValue(), EvictionReason.EXPIRED);
            }
        }
    }

    /**
     * Evict all values from the cache.
     */
    public void evictAll() {
        cache.forEach((key, value) -> expire(value.getValue(), EvictionReason.SHUTDOWN));
    }
}
