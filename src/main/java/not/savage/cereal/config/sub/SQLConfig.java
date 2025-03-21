package not.savage.cereal.config.sub;

import lombok.Getter;

/**
 * SQL Configuration object for {@link not.savage.cereal.internal.platform.sql.CerealSQLDatabase}
 * @param jdbcUrl The JDBC URL
 * @param username The username
 * @param password The password
 */
public record SQLConfig(
    @Getter String jdbcUrl,
    @Getter String username,
    @Getter String password
) { }
