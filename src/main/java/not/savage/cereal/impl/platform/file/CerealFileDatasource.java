package not.savage.cereal.impl.platform.file;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.NonNull;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.TypeSerializer;
import not.savage.cereal.impl.*;
import not.savage.cereal.impl.exception.DatasourceException;
import not.savage.cereal.impl.sort.CerealFilterMode;
import not.savage.cereal.impl.sort.CerealSortMode;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

public class CerealFileDatasource<T extends CerealDataBlob> extends CerealDatasource<T> implements CerealLogger {

    // One file to store all data.
    private final File file;
    private final Class<T> clazz;

    // In memory cache of data from the file
    private final HashMap<UUID, T> datastore = new HashMap<>();
    private Gson gson;
    private final Type type;

    public CerealFileDatasource( Class<T> clazz, File file, CerealDatabase database, TypeSerializer<?>[] serializers) {
        super(database);
        this.file = file;
        this.database = database;
        this.clazz = clazz;
        this.type = TypeToken.getParameterized(Map.class, UUID.class, clazz).getType();

        for (TypeSerializer<?> serializer : serializers) {
            this.builder.registerTypeAdapter(getTypeClass(serializer), serializer);
        }
    }

    public void start() throws DatasourceException {
        debug("Starting file datasource for %s", file.getAbsolutePath());
        this.gson = builder.create();

        // try try try
        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    throw new DatasourceException("False state on file creation for %s".formatted(file.getAbsolutePath()));
                }
            } catch (IOException e) {
                throw new DatasourceException("Failed to create file %s".formatted(file.getAbsolutePath()), e);
            }
            try {
                Files.writeString(file.toPath(), "{}");
            } catch (IOException e) {
                throw new DatasourceException("Failed to write to file %s".formatted(file.getAbsolutePath()), e);
            }
        } else {

            // Load data from file
            String readData = null;
            try {
                readData = Files.readString(file.toPath());
            } catch (IOException e) {
                throw new DatasourceException("Failed to read data from file: %s".formatted(file.getName()), e);
            }

            if (readData == null || readData.isEmpty()) return;
            debug("Data Type Token: %s", type.getTypeName());
            HashMap<UUID, T> data = gson.fromJson(readData, type);
            if (data != null) {
                data.values().forEach(CerealDataBlob::load);
                datastore.putAll(data);
            }
        }
    }

    @Override
    public void save(@NonNull T t) {
        debug("Saving object with key \"%s\" to file datasource", t.getIdentifier());
        // Shouldn't do anything unless the object isn't already in the datastore as the dataobject is loaded into memory & passed around.
        if (datastore.containsKey(t.getIdentifier())) {
            t.lastSaved(); // Keep consistency with other datasources
            return;
        }
        datastore.put(t.getIdentifier(), t);
    }

    @Override
    public void saveAll(@NonNull Set<T> t) {
        try {
            debug("Saving all data to file: %s", file.getName());
            datastore.values().forEach(CerealDataBlob::lastSaved);
            Files.write(file.toPath(), gson.toJson(datastore).getBytes());
        } catch (Exception e) {

            error("Failed to save data to file: %s", e, file.getName());
        }
    }

    @Override
    public @NonNull Optional<T> get(@NonNull UUID key) {
        return Optional.ofNullable(datastore.get(key));
    }

    @Override
    public @NonNull Set<T> getAll() {
        return new HashSet<>(datastore.values());
    }

    @Override
    public @NonNull Optional<T> getByField(@NonNull String field, @NonNull Object value) {
        return this.datastore.values().stream().filter(getFieldAccessor(field, value)).findFirst();
    }

    @Override
    public @NonNull Set<T> getAllByField(String field, Object value, int limit) {
        return this.datastore.values().stream().filter(getFieldAccessor(field, value)).limit(limit == -1 ? Integer.MAX_VALUE : limit).collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    @Override
    public @NonNull Set<T> getAllByFieldFilteredAndOrdered(
            CerealFilterMode filterMode, Object filterFor, String filterByField,
            CerealSortMode sortMode, String sortByField,
            int limit) {

        return this.datastore.values().stream()
                .filter(getFieldAccessor(filterByField, filterFor))
                .limit(limit == -1 ? Integer.MAX_VALUE : limit)
                .sorted((o1, o2) -> {
                    try {
                        final Field f = o1.getClass().getDeclaredField(sortByField);
                        f.setAccessible(true);
                        final Object v1 = f.get(o1);
                        final Object v2 = f.get(o2);

                        if (v1 instanceof Comparable && v2 instanceof Comparable) {
                            return ((Comparable) v1).compareTo(v2);
                        }
                    } catch (NoSuchFieldException e) {
                        error("Field %s not found in class %s", e, sortByField, o1.getClass().getName());
                    } catch (IllegalAccessException e) {
                        error("Failed to access field %s in class %s", e, sortByField, o1.getClass().getName());
                    }
                    return 0;
                })
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    @Override
    public void delete(@NonNull T t) {
        debug("Deleting object with key \"%s\" from file datasource", t.getIdentifier());
        datastore.remove(t.getIdentifier(), t);
    }

    private Predicate<T> getFieldAccessor(String field, Object value) {
        return t -> {
            try {
                final Field f = t.getClass().getDeclaredField(field);
                f.setAccessible(true);
                return f.get(t).equals(value);
            } catch (NoSuchFieldException e) {
                error("Field %s not found in class %s", e, field, t.getClass().getName());
            } catch (IllegalAccessException e) {
                error("Failed to access field %s in class %s", e, field, t.getClass().getName());
            }
            return false;
        };
    }
}

