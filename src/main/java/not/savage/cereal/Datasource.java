package not.savage.cereal;

import lombok.NonNull;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.sort.CerealFilterMode;
import not.savage.cereal.sort.CerealSortMode;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A datasource is a middleware between a database and an implementation of {@link Cache}
 * @param <T> The data blob type
 * @param <K> The identifier or key
 */
public interface Datasource<T extends DataBlob<K>, K> {

    /**
     * Start the datasource
     */
    void start() throws DatasourceException;

    /**
     * Get a value from the datasource, sync task.
     * @param key The key to get
     * @return The value
     */
    @NonNull Optional<T> get(@NonNull K key);

    /**
     * Get all values from the datasource, sync task.
     * @return All values
     */
    @NonNull Set<T> getAll();

    /**
     * Get a value from the datasource, async task.
     * @param key The key to get
     * @return The value
     */
    @NonNull
    CompletableFuture<Optional<T>> getAsync(@NonNull K key);

    /**
     * Get all values from the datasource, async task.
     * @return All values
     */
    @NonNull CompletableFuture<Set<T>> getAllAsync();

    /**
     * Get a value from the datasource by a field, sync task.
     * @param field The field to search by
     * @param value The value to search for
     * @return The value
     */
    @NonNull Optional<T> getByField(@NonNull String field, @NonNull Object value);

    /**
     * Get a value from the datasource by a field, async task.
     * @param field The field to search by
     * @param value The value to search for
     * @return The value
     */
    @NonNull CompletableFuture<Optional<T>> getByFieldAsync(@NonNull String field, @NonNull Object value);

    /**
     * Get all values from the datasource by a field, sync task.
     * @param field The field to search by
     * @param value The value to search for
     * @param limit The limit of values to return
     * @return A non-null set containing between 0 - limit values
     */
    @NonNull Set<T> getAllByField(String field, Object value, int limit);

    /**
     * Get all values from the datasource by a field, sync task.
     * @param field The field to search by
     * @param value The value to search for
     * @return A non-null set containing between 0 - n (all) values
     */
    default @NonNull Set<T> getAllByField(String field, Object value) {
        return getAllByField(field, value, -1);
    }

    /**
     * Get all values from the datasource by a field, async task.
     * @param field The field to search by
     * @param value The value to search for
     * @param limit The limit of values to return
     * @return A non-null set containing between 0 - limit values
     */
    @NonNull CompletableFuture<Set<T>> getAllByFieldAsync(String field, Object value, int limit);

    /**
     * Get all values from the datasource by a field, async task.
     * @param field The field to search by
     * @param value The value to search for
     * @return A non-null set containing between 0 - n (all) values
     */
    default @NonNull CompletableFuture<Set<T>> getAllByFieldAsync(String field, Object value) {
        return getAllByFieldAsync(field, value, -1);
    }

    /**
     * Get, Filter & sort values in the database rather than loading the entire set into memory.
     * Intended for use cases like Leaderboards where you want the entire set, but only users who qualify & in-order
     * so you can limit results.
     * @param filterMode The filter mode used to filter "valid" entries.
     * @param filterFor The filter value to filter by
     * @param filterByField The field to filter by (e.g. "score")
     * @param sortMode The sort mode to use when returning the values
     * @param sortByField The field to sort by (e.g. "score"), doesn't need to be the same as filterByField
     * @param limit The limit of values to return. This does not take or return a cursor index, so it's not paginated.
     * @return A non-null set containing between 0 - limit values
     */
    @NonNull Set<T> getAllByFieldFilteredAndOrdered(
            CerealFilterMode filterMode, Object filterFor,
            String filterByField, CerealSortMode sortMode, String sortByField,
            int limit
    );

    /**
     * Get, Filter & sort values in the database rather than loading the entire set into memory.
     * Intended for use cases like Leaderboards where you want the entire set, but only users who qualify & in-order
     * so you can limit results.
     * @param filterMode The filter mode used to filter "valid" entries.
     * @param filterFor The filter value to filter by
     * @param filterByField The field to filter by (e.g. "score")
     * @param sortMode The sort mode to use when returning the values
     * @param sortByField The field to sort by (e.g. "score"), doesn't need to be the same as filterByField
     * @param limit The limit of values to return. This does not take or return a cursor index, so it's not paginated.
     * @return A non-null set containing between 0 - limit values
     */
    @NonNull CompletableFuture<Set<T>> getAllByFieldFilteredAndOrderedAsync(
            CerealFilterMode filterMode, Object filterFor,
            String filterByField, CerealSortMode sortMode, String sortByField,
            int limit
    );

    /**
     * Delete a value from the datasource
     * @param value The value to delete
     */
    void delete(@NonNull T value);

    /**
     * Save a value to the datasource
     * @param value The value to save
     */
    void save(@NonNull T value);

    /**
     * Save all values to the datasource
     */
    void saveAll(@NonNull Set<T> values);

}
