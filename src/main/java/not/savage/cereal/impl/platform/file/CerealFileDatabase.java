package not.savage.cereal.impl.platform.file;

import lombok.Getter;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.impl.CerealCache;
import not.savage.cereal.impl.CerealDataBlob;
import not.savage.cereal.impl.CerealDatabase;
import not.savage.cereal.impl.CerealDatasource;
import not.savage.cereal.impl.config.CerealConfig;
import not.savage.cereal.impl.exception.CacheInstantiationException;
import not.savage.cereal.impl.exception.DatasourceException;

import java.io.File;

/**
 * Flat file implementation of CerealDatabase for CerealDataObjects
 * Serializes data to a json string & saves it to a file.
 */
@Getter
public class CerealFileDatabase extends CerealDatabase implements CerealLogger {

    public CerealFileDatabase(CerealConfig config) {
        super(config);
    }

    @Override
    public boolean start() {
        // Files have no start up requirements
        return true;
    }

    @Override
    public void shutdown() {
        // Save all data
        debug("Shutting down database...");
        this.loadedCaches.values().forEach(CerealCache::saveAll);
    }

    @Override
    protected <T extends CerealCache<V>, V extends CerealDataBlob> CerealCache<V> prepareNewCache(Class<T> cacheClass,
                                                                                                  Class<V> dataObjectClass,
                                                                                                  boolean distinct)
    throws CacheInstantiationException, DatasourceException {
        // File to create/find
        File file = getFilePath(cacheClass);
        // The mapped data folder - depends on bukkit atm :fat:
        File mapped = new File(file.getName());
        CerealDatasource<V> source = new CerealFileDatasource<>(dataObjectClass, mapped, this, getSerializers(cacheClass));
        source.start();
        CerealCache<V> cache = createNewCacheInstance(cacheClass);
        cache.setDependencies(source, getConfig());
        return cache;
    }
}

