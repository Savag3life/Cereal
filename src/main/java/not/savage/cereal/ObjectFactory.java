package not.savage.cereal;

import lombok.NonNull;
import not.savage.cereal.exception.InstantiationException;

/**
 * Object factory for creating objects when they are first instantiated.
 * @param <T> The object type
 * @param <K> The identifier type
 */
public abstract class ObjectFactory<T extends DataBlob<K>, K> {

    /**
     * Create a new instance of the object - Implementation should create new instance of the object
     * @param key The cache to create the object for
     * @return The new instance of the object
     */
    protected abstract T create(K key);

    /**
     * Called to create a new object when required in a Cache see
     * @param key The identifier for the object
     * @return The new instance of the object
     * @throws InstantiationException If the object could not be created/throws an exception during creation
     */
    public @NonNull T instantiate(K key) throws InstantiationException {
        try {
            return create(key);
        } catch (Exception e) {
            throw new InstantiationException(e);
        }
    }
}
