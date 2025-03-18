package not.savage.cereal.internal.platform.mongo;

import com.google.gson.Gson;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.WriteModel;
import lombok.NonNull;
import not.savage.cereal.CerealLogger;
import not.savage.cereal.TypeSerializer;
import not.savage.cereal.exception.DatasourceException;
import not.savage.cereal.internal.CerealDataBlob;
import not.savage.cereal.internal.CerealDatabase;
import not.savage.cereal.internal.CerealDatasource;
import not.savage.cereal.sort.CerealFilterMode;
import not.savage.cereal.sort.CerealSortMode;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * A MongoDB implementation of the {@link CerealDatasource} interface.
 * @param <T> The type of data blob to store in the datasource
 */
public class CerealMongoDatasource<T extends CerealDataBlob> extends CerealDatasource<T> implements CerealLogger {

    private final Class<T> clazz;
    private Gson gson;

    private final MongoCollection<Document> datastore;

    public CerealMongoDatasource(Class<T> clazz, CerealDatabase database, MongoCollection<Document> document, TypeSerializer<?>[] serializers) {
        super(database);
        this.database = database;
        this.clazz = clazz;
        this.datastore = document;

        for (TypeSerializer<?> serializer : serializers) {
            this.builder.registerTypeAdapter(getTypeClass(serializer), serializer);
        }
    }

    public void start() throws DatasourceException {
        debug("Starting mongo datasource for %s", clazz.getName());
        this.gson = builder.create();
    }

    @Override
    public void save(@NonNull T t) {
        debug("Saving object with key \"%s\" to file datasource", t.getIdentifier());
        t.lastSaved();
        Document doc = Document.parse(this.gson.toJson(t));
        doc.put("_id", t.getIdentifier());
        UpdateOptions opts = new UpdateOptions().upsert(true);
        datastore.updateOne(new Document("_id", t.getIdentifier()), new Document("$set", doc), opts);
    }

    @Override
    public void saveAll(@NonNull Set<T> all) {
        List<WriteModel<Document>> writeModels = new ArrayList<>();
        for (T t : all) {
            t.lastSaved();
            Document doc = Document.parse(this.gson.toJson(t));
            doc.put("_id", t.getIdentifier());
            writeModels.add(new UpdateOneModel<>(
                    new Document("_id", t.getIdentifier()),
                    new Document("$set", doc),
                    new UpdateOptions().upsert(true)
            ));
        }

        BulkWriteResult result = datastore.bulkWrite(writeModels);
        debug("Saved %d objects to MongoDB datasource", result.getModifiedCount());
    }

    @Override
    public @NonNull Optional<T> getByField(@NonNull String field, @NonNull Object value) {
        Document document = datastore.find(new Document(field, value)).first();
        if (document == null || document.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(gson.fromJson(document.toJson(), clazz));
    }

    @Override
    public @NonNull Set<T> getAllByField(String field, Object value, int limit) {
        if (limit == -1) {
            return datastore.find(eq(field, value)).map(document -> gson.fromJson(document.toJson(), clazz)).into(new HashSet<>());
        }
        return datastore.find(eq(field, value)).limit(limit).map(document -> gson.fromJson(document.toJson(), clazz)).into(new HashSet<>());
    }

    @Override
    public @NonNull Optional<T> get(@NonNull UUID key) {
        Document document = datastore.find(new Document("_id", key)).first();
        if (document == null || document.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(gson.fromJson(document.toJson(), clazz));
    }

    @Override
    public @NonNull Set<T> getAll() {
        return new HashSet<>(datastore.find().map(document -> gson.fromJson(document.toJson(), clazz)).into(new ArrayList<>()));
    }

    @Override
    public void delete(@NonNull T t) {
        debug("Deleting object with key \"%s\" from file datasource", t.getIdentifier());
        datastore.deleteOne(new Document("_id", t.getIdentifier()));
    }

    @Override
    public @NonNull Set<T> getAllByFieldFilteredAndOrdered(
            CerealFilterMode filterMode, Object filterFor,
            String filterByField, CerealSortMode sortMode, String sortByField,
            int limit
    ) {

        Document sortDoc = new Document(sortByField, sortMode == CerealSortMode.ASCENDING ? 1 : -1);
        Document filterDoc = switch (filterMode) {
            case CerealFilterMode.EQUAL -> new Document(filterByField, filterFor);
            case CerealFilterMode.NOT_EQUAL -> new Document(filterByField, new Document("$ne", filterFor));
            case CerealFilterMode.GREATER_THAN -> new Document(filterByField, new Document("$gt", filterFor));
            case CerealFilterMode.GREATER_THAN_OR_EQUAL_TO -> new Document(filterByField, new Document("$gte", filterFor));
            case CerealFilterMode.LESS_THAN -> new Document(filterByField, new Document("$lt", filterFor));
            case CerealFilterMode.LESS_THAN_OR_EQUAL_TO -> new Document(filterByField, new Document("$lte", filterFor));
            default -> new Document();
        };

        if (limit == -1) {
            return datastore.find(filterDoc)
                    .sort(sortDoc)
                    .map(document -> gson.fromJson(document.toJson(), clazz))
                    .into(new HashSet<>());
        }

        return
                datastore.find(filterDoc)
                        .sort(sortDoc)
                        .limit(limit)
                        .map(document -> gson.fromJson(document.toJson(), clazz))
                        .into(new HashSet<>());
    }
}
