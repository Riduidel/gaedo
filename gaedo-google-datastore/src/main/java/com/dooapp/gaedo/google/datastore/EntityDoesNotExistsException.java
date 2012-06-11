/**
 * 
 */
package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.CrudServiceException;
import com.google.appengine.api.datastore.Key;

public class EntityDoesNotExistsException extends GAECrudServiceException {
	public EntityDoesNotExistsException(Exception e, Key k) {
		super("Entity associated to key (kind : "+k.getKind()+", name : "+k.getName()+", id "+k.getId()+") does not seems to exist", e);
	}
}