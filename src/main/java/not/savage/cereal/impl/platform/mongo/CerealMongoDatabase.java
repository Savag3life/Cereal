package not.savage.cereal.impl.platform.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.impl.*;
import not.savage.cereal.impl.config.CerealConfig;
import not.savage.cereal.impl.exception.CacheInstantiationException;
import not.savage.cereal.impl.exception.DatasourceException;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.util.HashMap;

public class CerealMongoDatabase extends CerealDatabase implements CerealLogger {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    // V is our data object, T is our cache
    private final HashMap<Class<? extends CerealCache<?>>, CerealCache<? extends CerealDataBlob>> loadedCaches = new HashMap<>();

    public CerealMongoDatabase(CerealConfig config) {
        super(config);
    }

    @Override
    public boolean start() {
        if (getConfig().getMongoDBConfig().uri().isEmpty()) {
            error("MongoDB URI must be set in cereal.json to use MongoDB as a database.");
            return false;
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(getConfig().getMongoDBConfig().uri()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applicationName("Cereal")
                .build();

        try {
            this.mongoClient = MongoClients.create(settings);
            this.mongoDatabase = mongoClient.getDatabase(getConfig().getMongoDBConfig().database());
        } catch (Exception e) {
            error("Failed to connect to MongoDB", e);
            return false;
        }

        return true;
    }

    @Override
    public void shutdown() {
        // Save all data
        debug("Saving all caches...");
        this.loadedCaches.values().forEach(CerealCache::saveAll);
        this.mongoClient.close();
    }

    @Override
    protected <T extends CerealCache<V>, V extends CerealDataBlob> CerealCache<V> prepareNewCache(Class<T> cacheClass,
                                                                                                  Class<V> dataObjectClass,
                                                                                                  boolean distinct)
    throws CacheInstantiationException, DatasourceException {
        // Overrides the code initializer of distinct to allow
        // Owners to edit the config to make specific datasource's distinct on their own usage.
        String name = classToCollectionName(cacheClass, distinct);
        if (getConfig().getServerConfig().containsDatasource(classToCollectionName(cacheClass, distinct))) {
            if (getConfig().getServerConfig().isDatasourceDistinct(classToCollectionName(cacheClass, distinct))) {
                if (!distinct) {
                    distinct = true;
                }
            } else {
                if (distinct) {
                    distinct = false;
                }
            }
        } else {
            getConfig().getServerConfig().getRegisteredDatasources().add(name);
            CerealAPI.getInstance().saveConfig();
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(classToCollectionName(cacheClass, distinct));
        CerealDatasource<V> source = new CerealMongoDatasource<>(dataObjectClass,  this, collection, getSerializers(cacheClass));
        source.start();
        CerealCache<V> cache = createNewCacheInstance(cacheClass);

        cache.setDependencies(source, getConfig());
        return cache;
    }
}
