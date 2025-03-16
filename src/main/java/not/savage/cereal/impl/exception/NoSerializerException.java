package not.savage.cereal.impl.exception;

/**
 * Exception thrown when a serializer is not found for a given type at runtime.
 */
public class NoSerializerException extends IllegalArgumentException {
    public NoSerializerException(String message) {
        super(message);
    }
}
