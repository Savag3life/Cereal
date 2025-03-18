package not.savage.cereal;

import not.savage.cereal.exception.DataPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO - This is not a "Good" logger - it was a quick and dirty solution to get logging in place.
 * This will be replaced with a proper logger in the future.
 */
public interface CerealLogger {

    String LOG_PREFIX = "[Database]";
    String DEBUG_PREFIX = "[Database-Debug]";
    String CATASTROPHIC_FAILURE = """
            This is a critical error & its important you do not ignore this message! One or more
            critical errors have occurred and the database may be in an inconsistent state. To prevent
            further issues, it is recommended to restart the server immediately. Please report this error to
            the developers immediately. This is a fatal error and may result in data loss, corruption, or worse.
            """;
    boolean DEBUG_MODE = System.getProperty("cereal.debug") != null; // -Dcereal.debug to enable debug mode
    Logger logger = LoggerFactory.getLogger("Cereal");

    /**
     * Log a normal message to console.
     * @param message The message to log
     */
    default void log(String message) {
        logger.info("{} {}", LOG_PREFIX, message);
    }

    /**
     * Log a formatted message to console.
     * @param message The message to log
     * @param args The arguments to format the message with
     */
    default void log(String message, Object... args) {
        logger.info("{} {}", LOG_PREFIX, message.formatted(args));
    }

    /**
     * Log a debug message to console.
     * @param message The message to log
     * @see #DEBUG_MODE
     */
    default void debug(String message) {
        if (!DEBUG_MODE) return;
        logger.info("{} {}", DEBUG_PREFIX, message);
    }

    /**
     * Log a formatted debug message to console.
     * @param message The message to log
     * @param args The arguments to format the message with
     * @see #DEBUG_MODE
     */
    default void debug(String message, Object... args) {
        if (!DEBUG_MODE) return;
        logger.info("{} {}", DEBUG_PREFIX, message.formatted(args));
    }

    /**
     * Log an error message to console.
     * @param message The message to log
     * @param throwable The exception to log
     */
    default void error(String message, Throwable throwable) {
        if (throwable instanceof DataPersistenceException er) {
            logger.error(CATASTROPHIC_FAILURE);
        }
        logger.error(message, throwable);
    }

    /**
     * Log an error message to console.
     * @param message The message to log
     */
    default void error(String message) {
        logger.error(message);
    }

    /**
     * Log a formatted error message to console.
     * @param message The message to log
     * @param exception The exception thrown
     * @param args The arguments to format the message with
     */
    default void error(String message, Throwable exception, Object... args) {
        if (exception instanceof DataPersistenceException er) {
            logger.error(CATASTROPHIC_FAILURE);
        }
        logger.error(message.formatted(args), exception);
    }
}
