package not.savage.cereal.impl.platform.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.Getter;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.impl.*;
import not.savage.cereal.impl.config.CerealConfig;
import not.savage.cereal.impl.exception.CacheInstantiationException;
import not.savage.cereal.impl.exception.DatasourceException;

@Getter
public class CerealSQLDatabase extends CerealDatabase implements CerealLogger {

    private HikariDataSource dataSource;

    public CerealSQLDatabase(CerealConfig config) {
        super(config);
    }

    @Override
    public boolean start() {
        if (getConfig().getSqlConfig().getJdbcUrl().isEmpty()) {
            error("JDBC_URL must be set in cereal.json to use SQL as a database.");
            return false;
        }

        HikariConfig sqlConfig = new HikariConfig();
        sqlConfig.setJdbcUrl(getConfig().getSqlConfig().getJdbcUrl());
        sqlConfig.setUsername(getConfig().getSqlConfig().getUsername());
        sqlConfig.setPassword(getConfig().getSqlConfig().getPassword());
        sqlConfig.addDataSourceProperty("cachePrepStmts", "true");
        sqlConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        sqlConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            this.dataSource = new HikariDataSource(sqlConfig);
        } catch (Exception e) {
            if (e instanceof HikariPool.PoolInitializationException) {
                // Faster to check for this exception than to check the message
                // Then to deal with supporting people in tickets with long stack traces.
                if(e.getMessage().contains("Access denied for user")) {
                    error("Failed to connect to SQL database! Access denied for user. Please check your credentials.");
                    return false;
                }
            }

            error("Failed to connect to SQL Server: %s", e, e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void shutdown() {
        // Save all data
        debug("Saving all caches...");
        this.loadedCaches.values().forEach(CerealCache::saveAll);
        this.dataSource.close();
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

        CerealDatasource<V> source = new CerealSQLDatasource<>(dataObjectClass,this, classToCollectionName(cacheClass, distinct), getSerializers(cacheClass));
        source.start();
        CerealCache<V> cache = createNewCacheInstance(cacheClass);
        cache.setDependencies(source, getConfig());
        return cache;
    }
}
