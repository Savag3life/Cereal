package not.savage.cereal.exception;

/**
 * Exception thrown when an error occurs during the data persistence process during runtime.
 * If any data fails to save, this error is thrown & captured by {@link not.savage.cereal.CerealLogger}
 */
public class DataPersistenceException extends RuntimeException {

        public DataPersistenceException(String message) {
            super(message);
        }

        public DataPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }

}
