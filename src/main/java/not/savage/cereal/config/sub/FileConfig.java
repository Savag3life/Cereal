package not.savage.cereal.config.sub;

import not.savage.cereal.config.Mode;

/**
 * Represents the configuration for the file storage mode. {@link not.savage.cereal.internal.platform.file.CerealFileDatabase}
 * Has no properties. {@link Mode.FILE} requires
 * the developer to support the @Location annotation in their classes.
 */
public record FileConfig() {  }
