package not.savage.cereal.impl.mode.object;

import not.savage.cereal.impl.CerealDataBlob;

/**
 * This class is for Player-Linked data like profiles.
 * Players are all given a UUID as an identifier by Minecraft, so we use it as a binder
 */
public abstract class CerealDataObject extends CerealDataBlob {

    public CerealDataObject() {
        super();
    }

    public <T extends CerealDataObject> CerealDataObject(CerealObjectCache<T> cache) {
        super(cache);
    }

}


