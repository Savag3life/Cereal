# Cereal Storage Library
A lightweight data storage library originally designed for Minecraft plugins. It provides a unified API for storing and retrieving data across multiple backend sources without requiring direct interaction with the storage implementation. While not a full-fledged database system, it prioritizes efficiency and ease of use. This library is still in early design & development stages, and as such, is not recommended for production use. We will try to keep methods, classes, and implementations signatures as stable as possible, but changes may occur.
## Core Concepts
- **Single-Instance Datasource Access** – A single connection or connection pool handles all requests.
- **Key-JSON Storage Format** – Data is stored in a JSON-based key-value format, leveraging underlying storage mechanisms for querying and retrieval.
- **Backend-Agnostic API** – Implementations do not interact directly with the storage backend/layer; they only communicate via the API.
## Supported Backends
- **Flat-File** – Data is stored in JSON files on disk. Mostly for development and testing purposes.
- **MySQL** (MariaDB) – Data is stored in a MySQL database, using JSON columns for storage.
- **MongoDB** – Data is stored in a MongoDB database, using BSON for storage.

*All backends rely on the underlying JSON column type for storage, allowing for efficient querying and retrieval of data. Cereal does not employ any ORM or query language, instead relying on the underlying database's capabilities. This is by design. Simplicity > Complexity*

## Usage
First we need an object worth sorting. This object extends `CerealDataObject` and implements the required methods.
Fields must not be final, and should be non-static. Fields marked as `transient` will not be serialized.
```java
public class Guild extends CerealDataObject {
    
    private String guildName;
    private UUID guildLeader;
    
    @Override
    public void initialize() {
        // Called when this object is created for the first time.
    }

    @Override
    public void load() {
        // Called when the object is loaded from a data source. (every time) 
    }
}
```
Next, we need a cache to store our objects. The cache acts as our single point of access to get or create objects. The cache extends `CerealObjectCache<Guild>` and implements the required methods.
All caches also require a `@Location` annotation to specify the file location if using flat-file persistence. As this is designed for an unknown end-user, all caches need to be ready for any backend to be used.
```java
@FileLocation("guilds.json") // File location if set to use flat-file persistence.
@Serializers({
        LocationSerializer.class // Custom Serializer
})
public class Guilds extends CerealObjectCache<Guild> {
    @Getter private static Guilds instance;
    public Guilds() {
        super(
                "guilds", // Collection|Table name.
                new CerealObjectFactory<Guild>() { // Object Factory for new Guilds.
                    @Override
                    protected Guild create(UUID uuid) {
                        return new Guild();
                    }
                }
        );
        instance = this;
    }
}
```
Once we get to the point of our loading function we need to check if Cereal has been initialized, if we're not alone in this environment, or initialize. We require a `File` which Cereal treats as the working directory. After Cereal is confirmed alive, we need to instantiate our cache.
```java
// Depending on implementation, find or create the API instance.
if (!CerealAPI.isInitialized()) {
    final File cerealWorkingDirectory = new File("datastore");
    new CerealAPI(cerealWorkingDirectory);
}

// Install our Module
CerealAPI.install(GuildsCache.class);
```
And once we've installed our cache into the API, we can now access it. This implementation uses a static instance, but it's not required, the `install` method returns the instantiated cache instance. Once objects are modified, you dont need to write them back to the cache, they will be automatically saved. Objects shouldn't be long-lived references. If you need to access an object, you should use the cache to get it, when you need it.
```java
Guilds.getInstance().getAsync(uuid).thenAccept(guild -> {
    guild.ifPresent(g -> {
        // Guild found with this id!    
    });
});

// Lookup by a given field name, and matching value.
String newGuildName = "new-name";
Guilds.getInstance().getByFieldAsync("guildName", "my-guild").thenAccept(guild -> {
   guild.ifPresent(g -> {
        g.setName(newGuildName);
   }); 
});
```
## Current Issues
This entire system was designed originally purely for us within the Minecraft/Paper ecosystem, which such functionality has been removed from this repository. As such some design patterns, or methods are missing & need a more fluid implementation.
1. **Proper Logging Solution** - Currently, the system uses a static logger, which is not ideal.
2. **File Structure** - Given the original design focused on JavaPlugin#getDataFolder, we need a more flexible way to handle file locations.
3. **API Origin Access (CerealAPI)** - This was the handle in the Minecraft implementation. It's unlikely to be needed in a standalone version.
## Credits, Inspiration, & Libraries
- [Payload](https://github.com/jonahseguin/Payload) - Inspiration for the design of the system.
- [Gson](https://github.com/google/gson) - Handles all serialization/deserialization of objects.
 