package not.savage.cereal.internal.cache;

import lombok.Getter;
import lombok.Setter;


/**
 * Represents a cache entry in {@link EvictingCache}.
 * @param <T> the type of the value
 */
@Getter
public final class CacheEntry<T> {

    private final T value;
    private final long created;
    @Setter private long lastAccessed;

    public CacheEntry(T value) {
        this.value = value;
        this.created = System.currentTimeMillis();
        this.lastAccessed = System.currentTimeMillis();
    }
}