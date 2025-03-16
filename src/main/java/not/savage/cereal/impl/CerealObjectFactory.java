package not.savage.cereal.impl;

import not.savage.cereal.DataBlob;
import not.savage.cereal.ObjectFactory;

import java.util.UUID;

/**
 * A factory Cereal-Specific objects, pre-defining the UUID type for the DataBlob
 * @param <T> The data blob type
 */
public abstract class CerealObjectFactory<T extends DataBlob<UUID>> extends ObjectFactory<T, UUID> {

}
