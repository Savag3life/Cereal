package not.savage.cereal.internal.platform.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.config.CerealConfig;
import not.savage.cereal.exception.CacheInstantiationException;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.internal.CerealCache;
import not.savage.cereal.internal.CerealDataBlob;
import not.savage.cereal.internal.CerealDatabase;
import not.savage.cereal.internal.CerealDatasource;
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
                                                                                                  Class<V> dataObjectClass)
    throws CacheInstantiationException, DatasourceException {
        // Overrides the code initializer of distinct to allow
        // Owners to edit the config to make specific datasource's distinct on their own usage.
        String name = classToCollectionName(cacheClass);
        MongoCollection<Document> collection = mongoDatabase.getCollection(name);
        CerealDatasource<V> source = new CerealMongoDatasource<>(dataObjectClass,  this, collection, getSerializers(cacheClass));
        source.start();
        CerealCache<V> cache = createNewCacheInstance(cacheClass);
        cache.setDependencies(source, getConfig());
        return cache;
    }
}
