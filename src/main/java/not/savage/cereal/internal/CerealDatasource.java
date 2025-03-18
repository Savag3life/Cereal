package not.savage.cereal.internal;

import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.NonNull;
import not.savage.cereal.Datasource;
import not.savage.cereal.TypeSerializer;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.exception.NoSerializerException;
import not.savage.cereal.sort.CerealFilterMode;
import not.savage.cereal.sort.CerealSortMode;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public abstract class CerealDatasource<T extends CerealDataBlob> implements Datasource<T, UUID> {

    protected final GsonBuilder builder;
    protected CerealDatabase database;

    public CerealDatasource(CerealDatabase database) {
        this.database = database;
        this.builder = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .serializeNulls()
                .enableComplexMapKeySerialization()
                .disableInnerClassSerialization()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE);
    }

    /**
     * Used to start and initialize the datasource.
     * Should always be called directly after constructor but not within to give time for dependencies to be injected.
     */
    public abstract void start() throws DatasourceException;

    @Override
    public @NonNull CompletableFuture<Optional<T>> getAsync(@NonNull UUID key) {
        return CompletableFuture.supplyAsync(() -> get(key));
    }

    @Override
    public @NonNull CompletableFuture<Set<T>> getAllAsync() {
        return CompletableFuture.supplyAsync(this::getAll);
    }

    @Override
    public @NonNull CompletableFuture<Optional<T>> getByFieldAsync(@NonNull String field, @NonNull Object value) {
        return CompletableFuture.supplyAsync(() -> getByField(field, value));
    }

    @Override
    public @NonNull CompletableFuture<Set<T>> getAllByFieldAsync(String field, Object value, int limit) {
        return CompletableFuture.supplyAsync(() -> getAllByField(field, value, limit));
    }

    @Override
    public @NonNull CompletableFuture<Set<T>> getAllByFieldFilteredAndOrderedAsync(CerealFilterMode filterMode, Object filterFor,
                                                                                   String filterByField, CerealSortMode sortMode, String sortByField,
                                                                                   int limit) {
        return CompletableFuture.supplyAsync(() ->
                getAllByFieldFilteredAndOrdered(filterMode, filterFor, filterByField, sortMode, sortByField, limit));
    }

    /**
     * Resolve the generic type of the serializer. Used to extract a type <?> from a given Serializer Class.
     * @param serializer TypeSerializer to resolve
     * @return Class of the type the serializer is for
     */
    protected static @NonNull Class<?> getTypeClass(@NonNull TypeSerializer<?> serializer) {
        Type[] interfaces = serializer.getClass().getGenericInterfaces();
        for (Type interface0 : interfaces) {
            if (interface0 instanceof ParameterizedType parameterizedType) {
                Type rawType = parameterizedType.getRawType();
                if (rawType.equals(TypeSerializer.class)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class<?> clazz) {
                        return clazz;
                    }
                }
            }
        }
        throw new NoSerializerException("Unable to determine type class for " + serializer.getClass());
    }
}
