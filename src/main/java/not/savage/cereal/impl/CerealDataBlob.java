package not.savage.cereal.impl;

import lombok.Getter;
import not.savage.cereal.DataBlob;
import not.savage.cereal.impl.mode.object.CerealDataObject;

import java.util.UUID;

@Getter
public abstract class CerealDataBlob implements DataBlob<UUID> {

    protected UUID identifier;
    private transient CerealCache<?> cache;

    /**
     * When this object was originally loaded from the data source
     */
    private final long loadedAt;
    /**
     * The TTL of the current holder certificate within the database.
     */
    private transient long lastSaved;

    public <T extends CerealDataBlob> CerealDataBlob(CerealCache<T> cache) {
        this.cache = cache;
        this.loadedAt = System.currentTimeMillis();
        this.lastSaved = System.currentTimeMillis();
    }

    public CerealDataBlob() {
        this.loadedAt = System.currentTimeMillis();
        this.lastSaved = System.currentTimeMillis();
    }

    /**
     * This is called when the object is first initialized/created - not when it is loaded from the database.
     * This method will *not* call {@link CerealDataObject#load()}
     */
    public abstract void initialize();

    /**
     * This is called when the object is loaded from the database
     * This method will *not* call {@link CerealDataObject#initialize()}
     */
    public abstract void load();

    /**
     * Setting the identifier of a DataBlob is a one-time operation.
     * @throws IllegalStateException if the identifier is already set.
     */
    public void setIdentifier(UUID identifier) {
        if (this.identifier != null) {
            throw new IllegalStateException("Data blobs cant have their identifier changed once set.");
        }
        this.identifier = identifier;
    }

    /**
     * Update the last time a DataBlob was saved to the datastore.
     */
    public void lastSaved() {
        this.lastSaved = System.currentTimeMillis();
    }
}
