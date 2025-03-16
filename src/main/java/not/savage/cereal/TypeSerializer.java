package not.savage.cereal;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * TypeSerializer is a simple interface to provide a common interface for Gson TypeAdapters.
 * This allows modules implementing this interface to be used as a TypeAdapter for Gson serialization/deserialization.
 * @param <T> The type of object to serialize/deserialize
 */
public interface TypeSerializer<T> extends JsonSerializer<T>, JsonDeserializer<T> {
}
