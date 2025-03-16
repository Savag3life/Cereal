package not.savage.cereal.impl.mode.object;

import lombok.Getter;
import lombok.NonNull;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.impl.CerealCache;
import not.savage.cereal.impl.CerealObjectFactory;
import not.savage.cereal.impl.cache.EvictingCache;
import not.savage.cereal.impl.cache.EvictionReason;
import not.savage.cereal.impl.exception.InstantiationException;
import not.savage.cereal.impl.exception.ObjectInstantiationException;
import not.savage.cereal.type.TypeComparator;

import java.util.*;

/**
 * Local on-server cache for database objects. Holds Database Reference, handles middleware between database get() and cache.get()
 * @param <V> DataObject
 */
@Getter
public class CerealObjectCache<V extends CerealDataObject> extends CerealCache<V> implements CerealLogger {

    public CerealObjectCache(@NonNull String id,
                             @NonNull CerealObjectFactory<V> instanceFactory) {
        super(id, instanceFactory, new HashSet<>(TypeComparator.DEFAULT_OPTS));
    }

    public CerealObjectCache(@NonNull String id,
                             @NonNull CerealObjectFactory<V> instanceFactory,
                             TypeComparator<?>... typeComparators) {
        super(
                id,
                instanceFactory,
                new HashSet<>(List.of(typeComparators)) {{
                    addAll(TypeComparator.DEFAULT_OPTS);
                }}
        );
    }

    @Override
    public void start() {
        super.start();
        debug("Starting Object Cache: %s", id);
        this.cache = new EvictingCache<>(this.config.cacheConfig().expireAfterAccessMinutes(), this.config.getCacheConfig().expireAfterWriteMinutes()) {
            @Override
            public void expire(V value, EvictionReason reason) {
                debug("Expiring object with key: %s, Reason: %s",value.getIdentifier(), reason.name());
                remove(value.getIdentifier());
                save(value);
            }
        };
    }

    @Override
    public @NonNull Optional<V> get(@NonNull UUID key) {
        // Check if value already in cache
        Optional<V> v = this.cache.get(key);

        if (v.isEmpty()) {
            // Not already in cache
            // Lets try and load it from the database
            v = this.datasource.get(key);

            if (v.isEmpty()) {
                // Objects don't create when not found.
                // They require explicit creation.
                return Optional.empty();
            } else {
                v.ifPresent(value -> {
                    value.load();
                    cache.cache(key, value);
                });
            }

        }

        return v;
    }

    @Override
    public @NonNull V create(@NonNull UUID key) throws InstantiationException {
        debug("Creating new object with key: %s", key);
        try {
            final V v = this.instanceFactory.instantiate(key);
            v.setIdentifier(key); // Set key before initialize() so its populated.
            v.initialize();
            return v;
        } catch (InstantiationException e) {
            throw new ObjectInstantiationException(e.getMessage());
        }
    }
}
