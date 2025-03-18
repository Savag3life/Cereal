package not.savage.cereal.exception;

import not.savage.cereal.annotation.FileLocation;
import not.savage.cereal.config.Mode;

/**
 * This exception is thrown when a cache is missing the {@link FileLocation} annotation
 * to determine filename and location of the cache. Should only be thrown when
 * mode is set to {@link Mode.FILE} and the cache
 * which is attempting to load is missing the {@link FileLocation} annotation.
 */
public class MissingLocationException extends RuntimeException {
        public MissingLocationException(String message) {
            super(message);
        }
}
