package not.savage.cereal.impl.config.sub;

/**
 * MongoDB configuration object for the {@link not.savage.cereal.impl.platform.mongo.CerealMongoDatabase} implementation.
 * @param uri The MongoDB URI to connect to ie mongodb://localhost:27017
 * @param database The MongoDB database name to use
 */
public record MongoDBConfig(
        String uri,
        String database
) {

}
