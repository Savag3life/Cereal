package not.savage.cereal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.NonNull;
import not.savage.cereal.config.CerealConfig;
import not.savage.cereal.config.Mode;
import not.savage.cereal.config.sub.CacheConfig;
import not.savage.cereal.exception.CacheInstantiationException;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.internal.CerealCache;
import not.savage.cereal.internal.CerealDataBlob;
import not.savage.cereal.internal.CerealDatabase;
import not.savage.cereal.internal.platform.file.CerealFileDatabase;
import not.savage.cereal.internal.platform.mongo.CerealMongoDatabase;
import not.savage.cereal.internal.platform.sql.CerealSQLDatabase;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;

/**
 * CerealAPI create a single point of access to a database connection provided and wrapped by Cereal
 */
public class CerealAPI implements CerealLogger {

    @Getter private static CerealAPI instance;

    @Getter private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    @Getter private CerealConfig serverConfig;
    @Getter private final File workingDirectory;

    @Getter private CerealDatabase sharedDatabaseInstance;
    // plugins need to create a CerealAPI instance or Create and share one for each table.


    /**
     * Initialize the CerealAPI so that it can begin creating Datasources and Caches
     */
    public CerealAPI(@NonNull File workingDirectory) {
        instance = this;
        this.workingDirectory = workingDirectory;
        File configFile = new File(this.workingDirectory, "cereal.json");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                error("Failed to create Cereal config file", e);
                throw new IllegalStateException("Failed to create Cereal config file");
            }

            this.serverConfig = CerealConfig.defaults();
            String defaultConfig = this.gson.toJson(this.serverConfig);
            try {
                Files.writeString(configFile.toPath(), defaultConfig);
            } catch (Exception e) {
                error("Failed to write default Cereal config", e);
                throw new IllegalStateException("Failed to write default Cereal config");
            }

        } else {
            try {
                String config = Files.readString(configFile.toPath());
                this.serverConfig = this.gson.fromJson(config, CerealConfig.class);
            } catch (Exception e) {
                error("Failed to load Cereal config", e);
                throw new IllegalStateException("Failed to load Cereal config");
            }
        }

        if (this.serverConfig == null) {
            System.out.println("Failed to load Cereal Config");
            // Plugins should be checking {@link CerealAPI#isInitialized()} before using the API
            instance = null;
        }

        CacheConfig caching = this.serverConfig.getCacheConfig();

        if ((caching.expireAfterAccessMinutes() < 5 || caching.expireAfterWriteMinutes() < 5) && serverConfig.mode() != Mode.FILE) {
            log("** Data caching may be inefficient with current configuration.");
            log("You should consider upping the time cached to between 5m & 120m to prevent");
            log("excessive database calls / overloading the database. Caching values also results");
            log("a more responsive experience for players. Open a ticket for more info.");
        }
    }

    /**
     * Write the current in-memory config to the file system
     */
    public void saveConfig() {
        File configFile = new File(this.workingDirectory, "cereal.json");
        try {
            Files.writeString(configFile.toPath(), this.gson.toJson(this.serverConfig));
        } catch (Exception e) {
            error("Failed to save Cereal config");
        }
    }

    /**
     * Initialize the database connection based on the mode set in the config.
     * Internal use only.
     */
    private void initialize() {
        switch (this.serverConfig.mode()) {
            case FILE:
                this.sharedDatabaseInstance = new CerealFileDatabase(serverConfig);
                break;
            case MONGO:
                this.sharedDatabaseInstance = new CerealMongoDatabase(serverConfig);
                break;
            case SQL:
                this.sharedDatabaseInstance = new CerealSQLDatabase(serverConfig);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported Cereal mode: " + this.serverConfig.mode());
        }
        this.sharedDatabaseInstance.start();
    }

    /**
     * Should always be checked before attempting to install/initialize any caches
     * @return boolean - true if the API is initialized and ready to use
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    public <T extends CerealCache<V>, V extends CerealDataBlob> T install(Class<T> cache) {
        return install(cache, false);
    }

    @SuppressWarnings("unchecked")
    public <T extends CerealCache<V>, V extends CerealDataBlob> T install(Class<T> cache, boolean distinct) {
        if (!isInitialized()) {
            throw new IllegalStateException("CerealAPI is not initialized");
        }

        if (this.sharedDatabaseInstance == null) {
            initialize();
        }

        Class<V> child = null;
        Type superClass = cache.getGenericSuperclass();
        if (superClass instanceof ParameterizedType type) {
            try {
                Type[] types = type.getActualTypeArguments();
                if (types.length > 0 && types[0] instanceof Class<?> &&
                        CerealDataBlob.class.isAssignableFrom((Class<?>) types[0])) {
                    child = (Class<V>) types[0];
                }
            } catch (Exception e) {
                error("Failed to extract data object class from cache %s", e, cache.getName());
            }
        } else {
            error("Cache class does not contain a parameterized type %s".formatted(cache.getName()));
        }

        try {
            this.sharedDatabaseInstance.loadOrCreateCache(cache, child);
        } catch (CacheInstantiationException e) {
            error("Failed to create ", e, cache.getName());
        } catch (DatasourceException e) {
            error("Failed to install cache %s", e, cache.getName());
        }

        return sharedDatabaseInstance.getCache(cache);
    }
}
