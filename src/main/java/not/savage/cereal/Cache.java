package not.savage.cereal;

import lombok.NonNull;
import not.savage.cereal.exception.InstantiationException;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Cache should be implemented as a class which holds/caches objects which are loaded from a {@link Datasource}
 * {@link Database} creates a {@link Datasource} which is then used to create a {@link Cache}
 * @param <T> The data blob type
 * @param <K> The identifier type
 */
public interface Cache<T extends DataBlob<K>, K> {

    /**
     * Save the object to the cache
     * @param t The object to save
     */
    void save(@NonNull T t);

    /**
     * Save all objects in the cache
     */
    void saveAll();

    /**
     * Remove the object from the cache, sync task
     * @param key The object to remove
     */
    @NonNull Optional<T> get(@NonNull K key);

    /**
     * Get & cache all objects from datastore, sync task
     * @return The set of objects
     */
    @NonNull Set<T> getAll();

    /**
     * Get all objects in the cache, sync task
     * @return The set of objects
     */
    @NonNull Set<T> getAllCached();

    /**
     * Get the object from the cache, async task
     * @param key The object to get
     * @return The object
     */
    @NonNull CompletableFuture<Optional<T>> getAsync(@NonNull K key);

    /**
     * Async retrieve all objects from the cache, async task
     * @return The object
     */

    @NonNull CompletableFuture<Set<T>> getAllAsync();

    /**
     * Get the object from the cache by a field, sync task
     * @param field The field to search
     * @param value The value to search
     * @return The object
     */
    @NonNull Optional<T> getByField(@NonNull String field, @NonNull Object value);

    /**
     * Get all objects by a field, sync task
     * @param field The field to search
     * @param value The value to search
     * @return Non-null set which can contain 0 - n objects
     */
    default @NonNull Set<T> getAllByField(@NonNull String field, @NonNull Object value) {
        return getAllByField(field, value, -1);
    }

    /**
     * Get all objects by a field, sync task
     * @param field The field to search
     * @param value The value to search
     * @param limit The maximum number of objects to return
     * @return Non-null set which can contain 0 - limit objects, -1 for no limit
     */
    @NonNull Set<T> getAllByField(@NonNull String field, @NonNull Object value, int limit);

    /**
     * Get the object from the cache by a field, async task
     * @param field The field to search
     * @param value The value to search
     * @return The object
     */
    @NonNull CompletableFuture<Optional<T>> getByFieldAsync(@NonNull String field, @NonNull Object value);

    /**
     * Get all objects by a field, async task
     * @param field The field to search
     * @param value The value to search
     * @return Non-null set which can contain 0 - n objects
     */
    default @NonNull CompletableFuture<Set<T>> getAllByFieldAsync(@NonNull String field, @NonNull Object value) {
        return getAllByFieldAsync(field, value, -1);
    }

    /**
     * Get all objects by a field, async task
     * @param field The field to search
     * @param value The value to search
     * @param limit The maximum number of objects to return
     * @return Non-null set which can contain 0 - limit objects, -1 for no limit
     */
    @NonNull CompletableFuture<Set<T>> getAllByFieldAsync(@NonNull String field, @NonNull Object value, int limit);

    /**
     * Returns the cached object or Optional.empty()
     * If the object is not cached, it will be loaded asynchronously and cached,
     * resulting in an {@link Optional#empty()} will be returned.
     * @param key The key of the object to get
     * @return {@link Optional} of the object or {@link Optional#empty()} if not cached
     */

    @NonNull Optional<T> getCachedOrAsyncLoad(@NonNull K key);

    /**
     * Remove the object from the cache
     * @param key The object to remove
     */
    @NonNull T create(@NonNull K key) throws InstantiationException;

    /**
     * Remove the object from the cache
     * @param t The object to remove
     */
    void delete(@NonNull T t);

}
