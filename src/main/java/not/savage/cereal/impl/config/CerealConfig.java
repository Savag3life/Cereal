package not.savage.cereal.impl.config;

import lombok.Getter;
import lombok.NonNull;
import not.savage.cereal.impl.config.sub.*;

import java.util.ArrayList;
import java.util.UUID;

/**
 * CerealConfig is a record class that holds all the configuration values for Cereal.
 * @param mode The {@link Mode} of the configuration.
 * @param nodeId The unique identifier for this node.
 * @param sqlConfig The SQL configuration {@link SQLConfig}
 * @param mongoDBConfig The MongoDB configuration {@link MongoDBConfig}
 * @param fileConfig The File configuration {@link FileConfig} empty by default.
 * @param cacheConfig The Cache configuration {@link CacheConfig}
 * @param serverConfig The Server configuration {@link ServerConfig}
 * @param debug Whether debug mode is enabled. Can also be enabled with `-Dcereal.debug=true` in the JVM args.
 */
public record CerealConfig(
        @Getter @NonNull Mode mode,
        @Getter @NonNull String nodeId,
        @Getter @NonNull SQLConfig sqlConfig,
        @Getter @NonNull MongoDBConfig mongoDBConfig,
        @Getter @NonNull FileConfig fileConfig,
        @Getter @NonNull CacheConfig cacheConfig,
        @Getter @NonNull ServerConfig serverConfig,
        @Getter boolean debug
        ){

        /**
         * Returns the default configuration for Cereal.
         * @return The default configuration.
         */
        public static CerealConfig defaults() {
                return new CerealConfig(
                        Mode.FILE,
                        UUID.randomUUID().toString(),
                        new SQLConfig("jdbc:mariadb://HOST/DATABASE?permitMysqlScheme", "username", "password"),
                        new MongoDBConfig("mongodb://localhost:27017", "database"),
                        new FileConfig(),
                        new CacheConfig(120, 120),
                        new ServerConfig(new ArrayList<>()),
                        true
                );
        }
}
