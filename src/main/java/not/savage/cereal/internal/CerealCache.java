package not.savage.cereal.internal;

import lombok.Getter;
import lombok.NonNull;
import not.savage.cereal.Cache;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.CerealObjectFactory;
import not.savage.cereal.config.CerealConfig;
import not.savage.cereal.internal.cache.EvictingCache;
import not.savage.cereal.internal.cache.EvictionReason;
import not.savage.cereal.type.TypeComparator;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Implementation of a {@link Cache} & defines generic standards for a cache.
 * @param <T> The data blob type
 */
public abstract class CerealCache<T extends CerealDataBlob> implements Cache<T, UUID>, CerealLogger {

    @Getter protected final String id;
    @Getter protected final CerealObjectFactory<T> instanceFactory;

    protected CerealDatasource<T> datasource;
    protected EvictingCache<UUID, T> cache;
    protected Set<TypeComparator<?>> typeComparators;

    @Getter protected CerealConfig config;

    protected CerealCache(String id, CerealObjectFactory<T> instanceFactory, Set<TypeComparator<?>> typeComparators) {
        this.id = id;
        this.instanceFactory = instanceFactory;
        this.typeComparators = typeComparators;
        debug("Creating Cereal cache instance with id: " + id + " (Factory: " + instanceFactory.getClass().getSimpleName() + ", TypeComparators: " + typeComparators.size() + ")");
    }

    public void start() {
        if (this.config == null) {
            throw new IllegalStateException("Cache config missing! This is likely because the author didn't register this Cache with CerealAPI.");
        }
    }

    public void setDependencies(@NonNull CerealDatasource<T> datasource,
                                @NonNull CerealConfig config) {
        this.datasource = datasource;
        this.config = config;
    }

    @Override
    public void save(@NonNull T v) {
        this.datasource.save(v);
    }

    @Override
    public void saveAll() {
        this.datasource.saveAll(getAllCached());
    }

    public void delete(@NonNull T t) {
        this.cache.expire(t, EvictionReason.EXPLICIT);
        this.datasource.delete(t);
    }

    @Override
    public @NonNull Optional<T> getCachedOrAsyncLoad(@NonNull UUID key) {
        Optional<T> v = this.cache.get(key);
        if (v.isEmpty()) {
            getAsync(key); // Blind call to load as we return empty to act as a "defer" to the caller
            return Optional.empty();
        } else {
            return v;
        }
    }

    @Override
    public @NonNull Optional<T> getByField(@NonNull String field, @NonNull Object value) {
        //System.out.println("Getting by field: " + field + " with value: " + value + " class: " + getId());
        Predicate<T> predicate = v -> {
            try {
                //System.out.println(v.getClass().getSimpleName());
                Field[] fields = v.getClass().getDeclaredFields();
                Field[] superFields = v.getClass().getSuperclass().getDeclaredFields();
                Field[] allFields = new Field[fields.length + superFields.length];
                System.arraycopy(fields, 0, allFields, 0, fields.length);
                System.arraycopy(superFields, 0, allFields, fields.length, superFields.length);
                //System.out.println(Arrays.stream(allFields).map(Field::getName).reduce((a, b) -> a + ", " + b).orElse("None"));
                Field f = Arrays.stream(allFields).filter(field1 -> field1.getName().equals(field)).findFirst().orElseThrow(NoSuchFieldException::new);
                f.setAccessible(true);
                for (TypeComparator<?> type : typeComparators) {
                    if (type.isType(f.getType())) {
                        return type.compare(f.get(v), value);
                    }
                }
                return f.get(v).equals(value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return false;
            }
        };

        Optional<T> v = this.cache.getAll().stream().filter(predicate).findFirst();

        if (v.isEmpty()) {
            v = this.datasource.getByField(field, value);
            v.ifPresent(value1 -> cache.cache(value1.getIdentifier(), value1));
        }

        return v;
    }

    @Override
    public @NonNull Set<T> getAll() {
        Set<T> all = this.datasource.getAll();
        Set<T> cacheMapped = new HashSet<>();

        // Take the value from cache over the value from the database
        // as the server data is older/more recent age.
        for (T t : all) {
            if (this.cache.get(t.getIdentifier()).isEmpty()) {
                cacheMapped.add(t);
                cache.cache(t.getIdentifier(), t);
            } else {
                cache.get(t.getIdentifier()).ifPresent(cacheMapped::add);
            }
        }

        return cacheMapped;
    }

    @Override
    public @NonNull Set<T> getAllByField(@NonNull String field, @NonNull Object value, int limit) {
        Set<T> all = this.datasource.getAllByField(field, value);
        Set<T> cacheMapped = new HashSet<>();

        // Take the value from cache over the value from the database
        // as the server data is older/more recent age.
        for (T t : all) {
            if (this.cache.get(t.getIdentifier()).isEmpty()) {
                cacheMapped.add(t);
                cache.cache(t.getIdentifier(), t);
            } else {
                cache.get(t.getIdentifier()).ifPresent(cacheMapped::add);
            }
        }

        return cacheMapped;
    }

    @Override
    public @NonNull CompletableFuture<Set<T>> getAllAsync() {
        return CompletableFuture.supplyAsync(this::getAll);
    }

    @Override
    public @NonNull CompletableFuture<Optional<T>> getAsync(@NonNull UUID key) {
        return CompletableFuture.supplyAsync(() -> get(key));
    }

    @Override
    public @NonNull Set<T> getAllCached() {
        return this.cache.getAll();
    }

    @Override
    public @NonNull CompletableFuture<Optional<T>> getByFieldAsync(@NonNull String field, @NonNull Object value) {
        return CompletableFuture.supplyAsync(() -> getByField(field, value));
    }

    @Override
    public @NonNull CompletableFuture<Set<T>> getAllByFieldAsync(@NonNull String field, @NonNull Object value, int limit) {
        return CompletableFuture.supplyAsync(() -> getAllByField(field, value, limit));
    }
}
