package not.savage.cereal.exception;

import not.savage.cereal.CerealObjectFactory;

/**
 * This exception is thrown when an object cannot be instantiated by {@link CerealObjectFactory}
 */
public class InstantiationException extends RuntimeException {

        public InstantiationException(String message) {
            super(message);
        }

        public InstantiationException(String message, Throwable cause) {
            super(message, cause);
        }

        public InstantiationException(Throwable cause) {
            super(cause);
        }
}
