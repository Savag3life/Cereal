package not.savage.cereal.exception;

import not.savage.cereal.CerealAPI;
import not.savage.cereal.internal.CerealCache;

/**
 * This exception is thrown when a cache cannot be loaded or instantiated
 * by {@link CerealAPI} & {@link CerealCache}
 */
public class CacheInstantiationException extends Exception {

    public CacheInstantiationException(String message) {
        super(message);
    }

    public CacheInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
