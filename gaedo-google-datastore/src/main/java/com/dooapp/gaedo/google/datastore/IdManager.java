package com.dooapp.gaedo.google.datastore;

import java.lang.reflect.Field;

import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.Key;

public interface IdManager {
	/**
	 * Create a key for input object and stores it
	 * @param the kind as defined in DatastoreFinderService interface
	 * @param data input object
	 */
	public void createKey(String kind, Object data);

	/**
	 * Get key associated to given object.
	 * @param the kind as defined in DatastoreFinderService interface
	 * @param data the object for which we want a key
	 * @return the key identifying the object, as stored in datastore
	 */
	public Key getKey(String kind, Object data);

	/**
	 * Utility method checking if obejct already has a key
	 * @param the kind as defined in DatastoreFinderService interface
	 * @param data the object for which we check the key existence
	 * @return true if object already has a key, false elsewhere
	 */
	public boolean hasKey(String kind, Object data);
	
	/**
	 * Set the key for the given object (usually called when getting an object from the datastore)
	 * @param the kind as defined in DatastoreFinderService interface
	 * @param key the key to set
	 * @param returned the object receiving the new key
	 */
	public void setKey(String kind, Key key, Object returned);

	/**
	 * Return true if input field is the id field
	 * @param field
	 * @return
	 */
	public boolean isIdField(Property field);

	/**
	 * Create a key from a basic value (such as a long). This method will usually resolve in a call to KeyManager#createKey with the correct set of parameters
	 * @param kind
	 * @param value
	 * @return
	 */
	public Object buildKey(String kind, Object value);

	/**
	 * Get the id field used by this object
	 * @return
	 */
	public Property getIdField();
}
