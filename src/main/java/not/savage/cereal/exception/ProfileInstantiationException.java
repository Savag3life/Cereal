package not.savage.cereal.exception;

import not.savage.cereal.CerealObjectFactory;

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