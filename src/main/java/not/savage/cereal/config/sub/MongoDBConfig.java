package not.savage.cereal.config.sub;

/**
 * MongoDB configuration object for the {@link not.savage.cereal.internal.platform.mongo.CerealMongoDatabase} implementation.
 * @param uri The MongoDB URI to connect to ie mongodb://localhost:27017
 * @param database The MongoDB database name to use
 */
public record MongoDBConfig(
        String uri,
        String database
) {

}
