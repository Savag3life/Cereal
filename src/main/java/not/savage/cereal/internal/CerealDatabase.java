package not.savage.cereal.internal;

import lombok.Getter;
import not.savage.cereal.*;
import not.savage.cereal.annotation.FileLocation;
import not.savage.cereal.annotation.Serializers;
import not.savage.cereal.config.CerealConfig;
import not.savage.cereal.exception.CacheInstantiationException;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.exception.MissingLocationException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * "Cereal" Database abstraction layer. Implements most of the "Cereal Specific" logic
 * for loading & getting bits of information from the cache.
 * @see Serializers allows for custom {@link CerealDataBlob} type adatpers to be specified.
 * @see FileLocation Specifies the file path for {@link not.savage.cereal.internal.platform.file.CerealFileDatabase}
 */
public abstract class CerealDatabase extends Database implements CerealLogger {

    @Getter private final CerealConfig config;

    /**
     * Holds all loaded caches associated with this database layer {@link CerealDatabase} instance.
     */
    protected final HashMap<Class<? extends CerealCache<?>>, CerealCache<? extends CerealDataBlob>> loadedCaches = new HashMap<>();

    public CerealDatabase(CerealConfig config) {
        this.config = config;
    }

    /**
     * Attempt to load or create a new Datasource cache instance for the given class.
     * @param cacheClass Class of the cache
     * @param dataObjectClass Class of the data object
     * @param <T> CerealCache type
     * @param <V> CerealDataBlob type
     * @return CerealCache instance
     */
    public <T extends CerealCache<V>, V extends CerealDataBlob> CerealCache<V> loadOrCreateCache(
            Class<T> cacheClass,
            Class<V> dataObjectClass
    ) throws CacheInstantiationException, DatasourceException {
        CerealCache<V> cache = cacheClass.cast(loadedCaches.get(cacheClass));
        if (cache == null) {
            cache = prepareNewCache(cacheClass, dataObjectClass);


            this.loadedCaches.put(cacheClass, cache);
        }
        return cache;
    }

    @Override
    public <T extends Cache<K, V>, K extends DataBlob<V>, V> T getCache(Class<T> cache) {
        return cache.cast(loadedCaches.get(cache));
    }

    /**
     * Create a new cache instance.
     * @param cacheClass Class of the cache
     * @param dataObjectClass Class of the data
     */
    protected abstract <T extends CerealCache<V>, V extends CerealDataBlob> CerealCache<V> prepareNewCache(
            Class<T> cacheClass,
            Class<V> dataObjectClass
    ) throws CacheInstantiationException, DatasourceException;

    protected <T extends CerealCache<V>, V extends CerealDataBlob> CerealCache<V> createNewCacheInstance(Class<? extends CerealCache<V>> cacheClass) throws CacheInstantiationException {
        try {
            return cacheClass.getConstructor().newInstance();
        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException er) {
            throw new CacheInstantiationException("Failed to create new instance of cache class %s".formatted(cacheClass.getSimpleName()), er);
        } catch (NoSuchMethodException er) {
            throw new CacheInstantiationException("No no-args constructor found for cache %s".formatted(cacheClass.getSimpleName()), er);
        } catch (InvocationTargetException er) {
            throw new CacheInstantiationException("Exception in constructor for cache %s".formatted(cacheClass.getSimpleName()), er);
        }
    }

    /**
     * Extracts the file path for flat file storage data source.
     * @see FileLocation
     * @param clazz Class to extract file path from
     * @return File path for flat file storage
     */
    protected File getFilePath(Class<? extends CerealCache<?>> clazz) {
        if (!clazz.isAnnotationPresent(FileLocation.class)) {
            // Should only be thrown in the case the FILE database is used without a location annotation
            // This means someone wrote bad code & forgot the @Location annotation
            // or some spooky man is trying to use this code without the documentation
            throw new MissingLocationException("Location annotation not present on class " + this.getClass().getName());
        }
        return new File(clazz.getAnnotationsByType(FileLocation.class)[0].value());
    }

    /**
     * Extracts the serializers for the data object
     * @see Serializers
     * @param clazz Class to extract serializers from
     * @return Array of serializers which are used to serialize & deserialize the data object if present
     */
    protected TypeSerializer<?>[] getSerializers(Class<? extends CerealCache<?>> clazz) throws DatasourceException {
        if (!clazz.isAnnotationPresent(Serializers.class)) return new TypeSerializer[0]; // Optional
        Class<? extends TypeSerializer<?>>[] serializers = clazz.getAnnotationsByType(Serializers.class)[0].value();
        TypeSerializer<?>[] instances = new TypeSerializer[serializers.length];
        for (int i = 0; i < serializers.length; i++) {
            try {
                instances[i] = (TypeSerializer<?>) serializers[i].getDeclaredConstructors()[0].newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new DatasourceException("Failed to load serializer %s in %s".formatted(serializers[i].getName(), clazz.getName()), e);
            }
        }
        return instances;
    }
}
