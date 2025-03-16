package not.savage.cereal.impl.exception;

/**
 * Thrown when a datasource exception occurs when starting
 */
public class DatasourceException extends Exception {

        public DatasourceException(String message) {
            super(message);
        }

        public DatasourceException(String message, Throwable cause) {
            super(message, cause);
        }
}
