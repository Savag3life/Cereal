package not.savage.cereal;

import lombok.NonNull;

/**
 * DataObject is a simple interface to provide a common identifier for data objects.
 * @param <T> The type of the identifier
 */
public interface DataBlob<T> {

    /**
     * Get the identifier for the object
     * @return The identifier
     */
    @NonNull T getIdentifier();

}
