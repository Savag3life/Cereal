package not.savage.cereal.impl.exception;

import not.savage.cereal.impl.CerealObjectFactory;

/**
 * This exception is thrown when an object cannot be instantiated by {@link CerealObjectFactory}
 */
public class ObjectInstantiationException extends InstantiationException {
    public ObjectInstantiationException(String message) {
        super(message);
    }
}
