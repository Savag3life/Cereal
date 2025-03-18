package not.savage.cereal.internal.platform.sql;

import com.google.gson.Gson;
import lombok.NonNull;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.TypeSerializer;
import not.savage.cereal.exception.DataPersistenceException;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.internal.CerealDataBlob;
import not.savage.cereal.internal.CerealDatabase;
import not.savage.cereal.internal.CerealDatasource;
import not.savage.cereal.sort.CerealFilterMode;
import not.savage.cereal.sort.CerealSortMode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * A MySQL/MariaDB implementation of the {@link CerealDatasource} interface.
 * @param <T> The type of data blob to store in the datasource
 */
public class CerealSQLDatasource<T extends CerealDataBlob> extends CerealDatasource<T> implements CerealLogger {

    public static final String SAVE_QUERY = """
    INSERT INTO ? (id, data) VALUES (?, ?) ON DUPLICATE KEY UPDATE data = VALUES(data);
    """;
    public static final String GET_QUERY = """
    SELECT data FROM ? WHERE id = ?;
    """;
    public static final String GET_FIELD_QUERY = """
    SELECT data FROM ? WHERE JSON_EXTRACT(data, ?) = ? LIMIT ?;
    """;

    private final Class<T> clazz;
    private Gson gson;
    private final CerealSQLDatabase database;
    private final String tableName;


    public CerealSQLDatasource(Class<T> clazz, CerealDatabase database, String tableName, TypeSerializer<?>[] serializers) {
        super(database);
        if (!(database instanceof CerealSQLDatabase)) {
            throw new IllegalArgumentException("CerealSQLDatasource requires a CerealSQLDatabase");
        }
        this.database = (CerealSQLDatabase) database;
        this.clazz = clazz;
        this.tableName = tableName;
        debug("Registering %d type serializers", serializers.length);
        for (TypeSerializer<?> serializer : serializers) {
            this.builder.registerTypeAdapter(getTypeClass(serializer), serializer);
        }
    }

    public void start() throws DatasourceException {
        debug("Starting mongo datasource for %s", clazz.getName());
        try (Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS ? (id VARCHAR(36) PRIMARY KEY, data JSON)");
            stmt.setString(1, tableName);
            stmt.executeUpdate();
        } catch (SQLException er) {
            throw new DatasourceException("Failed to create SQL table for %s".formatted(clazz.getName()), er);
        }
        this.gson = builder.create();
    }

    @Override
    public void save(@NonNull T t) {
        debug("Saving object with key \"%s\" to file datasource", t.getIdentifier());
        t.lastSaved();
        String jsonData = this.gson.toJson(t);
        try (Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(SAVE_QUERY);
            stmt.setString(1, tableName);
            stmt.setString(1, t.getIdentifier().toString());
            stmt.setString(2, jsonData);
            stmt.executeUpdate();
        } catch (SQLException er) {
            error("Failed to save object with key \"%s\" to file datasource", er, t.getIdentifier());
        }
    }

    @Override
    public void saveAll(@NonNull Set<T> objects) {
        debug("Saving all objects... (%d objects)", objects.size());
        try (Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(SAVE_QUERY);
            for (T t : objects) {
                t.lastSaved();
                String jsonData = this.gson.toJson(t);
                stmt.setString(1, tableName);
                stmt.setString(1, t.getIdentifier().toString());
                stmt.setString(2, jsonData);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException er) {
            error("Failure to save data batch containing %d objects", er, objects.size());
            throw new DataPersistenceException("Critical failure in database.", er);
        }
    }

    @Override
    public @NonNull Optional<T> getByField(@NonNull String field, @NonNull Object value) {
        debug("Getting object with field \"%s\" matching \"%s\" from file datasource", field, value);
        try(Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(GET_FIELD_QUERY);
            
            stmt.setString(1, tableName);
            stmt.setString(2, field);
            stmt.setString(3, value.toString());
            stmt.setInt(3, 1);
            stmt.executeQuery();

            if (!stmt.getResultSet().next()) {
                return Optional.empty();
            }

            return Optional.ofNullable(gson.fromJson(stmt.getResultSet().getString("data"), clazz));
        } catch (Exception e) {
            error("Failed to get object with key \"%s\" from file datasource", e, value);
            throw new DataPersistenceException("Critical failure in database.", e);
        }
    }

    @Override
    public @NonNull Set<T> getAllByField(String field, Object value, int limit) {
        debug("Getting objects with key field \"%s\" matching \"%s\" from file datasource %s", field, value, limit > 1 ? "with limit " + limit : "");
        try(Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(GET_FIELD_QUERY);

            stmt.setString(1, tableName);
            stmt.setString(2, field);
            stmt.setString(3, value.toString());
            stmt.setInt(3, limit);
            stmt.executeQuery();

            return getResultSet(stmt);
        } catch (Exception e) {
            error("Failed to get object with key \"%s\" from file datasource", e, value);
            throw new RuntimeException("Exception in database search request.", e);
        }
    }

    @Override
    public @NonNull Set<T> getAllByFieldFilteredAndOrdered(
            CerealFilterMode filterMode, Object filterFor, String filterByField,
            CerealSortMode sortMode, String sortByField,
            int limit) {

        debug("Getting objects with filter mode \"%s\" matching \"%s\" from SQL datasource %s", filterMode, filterFor, limit > 1 ? "with limit " + limit : "");
        StringBuilder query = createQuery(filterMode, sortMode, limit);
        debug("Query: %s", query.toString());

        try(Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(query.toString());

            stmt.setString(1, tableName);
            stmt.setString(2, filterByField);
            stmt.setString(3, filterFor.toString());
            stmt.setString(4, sortByField);
            if (limit > 0) {
                stmt.setInt(5, limit);
            }

            return getResultSet(stmt);
        } catch (Exception e) {
            error("Failed to get object with key \"%s\" from file datasource", e, filterFor);
            throw new RuntimeException("Exception in database search request.", e);
        }
    }


    private @NonNull Set<T> getResultSet(@NonNull PreparedStatement stmt) throws SQLException {
        if (!stmt.getResultSet().next()) {
            return Collections.emptySet();
        }

        Set<T> results = new HashSet<>();
        while (stmt.getResultSet().next()) {
            results.add(gson.fromJson(stmt.getResultSet().getString("data"), clazz));
        }
        return results;
    }

    private @NonNull StringBuilder createQuery(CerealFilterMode filterMode, CerealSortMode sortMode, int limit) {
        StringBuilder query = new StringBuilder("SELECT data FROM ? WHERE JSON_EXTRACT(data, ?)");

        switch (filterMode) {
            case EQUAL -> query.append(" = ?");
            case GREATER_THAN -> query.append(" > ?");
            case LESS_THAN -> query.append(" < ?");
            case GREATER_THAN_OR_EQUAL_TO -> query.append(" >= ?");
            case LESS_THAN_OR_EQUAL_TO -> query.append(" <= ?");
            case NOT_EQUAL -> query.append(" != ?");
        }

        query.append(" ORDER BY JSON_EXTRACT(data, ?)");

        switch (sortMode) {
            case ASCENDING -> query.append(" ASC");
            case DESCENDING -> query.append(" DESC");
        }

        if (limit > 0) {
            query.append(" LIMIT ?;");
        } else {
            query.append(";");
        }
        return query;
    }

    @Override
    public @NonNull Optional<T> get(@NonNull UUID key) {
        debug("Getting object with key \"%s\" from file datasource", key);
        try (Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement(GET_QUERY);
            stmt.setString(1, tableName);
            stmt.setString(2, key.toString());
            stmt.executeQuery();
            if (!stmt.getResultSet().next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(gson.fromJson(stmt.getResultSet().getString("data"), clazz));
        } catch (Exception e) {
            error("Failed to get object with key \"%s\" from file datasource", e, key);
            throw new RuntimeException("Exception in database search request.", e);
        }
    }

    @Override
    public @NonNull Set<T> getAll() {
        debug("Getting all objects from file datasource");
        try (Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("SELECT data FROM ?");
            stmt.setString(1, tableName);
            stmt.executeQuery();
            Set<T> results = new HashSet<>();
            while (stmt.getResultSet().next()) {
                results.add(gson.fromJson(stmt.getResultSet().getString("data"), clazz));
            }
            return results;
        } catch (Exception e) {
            error("Failed to get all objects from file datasource", e);
            throw new RuntimeException("Exception in database search request.", e);
        }
    }

    @Override
    public void delete(@NonNull T t) {
        debug("Deleting object with key \"%s\" from file datasource", t.getIdentifier());
        try (Connection con = database.getDataSource().getConnection()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM ? WHERE id = ?");
            stmt.setString(1, tableName);
            stmt.setString(2, t.getIdentifier().toString());
            stmt.executeUpdate();
        } catch (SQLException er) {
            error("Failed to delete object with key \"%s\" from file datasource", er, t.getIdentifier());
        }
    }
}