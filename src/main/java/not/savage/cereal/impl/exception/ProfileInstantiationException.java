package not.savage.cereal.impl.exception;

import not.savage.cereal.impl.CerealObjectFactory;

import java.lang.InstantiationException;

/**
 * This exception is thrown when a profile cannot be instantiated by {@link CerealObjectFactory}
 */
public class ProfileInstantiationException extends InstantiationException {

    public ProfileInstantiationException(String message) {
        super(message);
    }

    public ProfileInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProfileInstantiationException(Throwable cause) {
        super(cause);
    }

}