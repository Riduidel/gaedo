package com.dooapp.gaedo.blueprints.transformers;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.BluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.DescribedProperty;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Vertex;

public class MapEntryTransformer extends AbstractTupleTransformer<Map.Entry> implements TupleTransformer<Map.Entry> {
	private static class UnableToGetKeyOrValueProperty extends BluePrintsCrudServiceException {
		public UnableToGetKeyOrValueProperty(Exception e) {
			super(e);
		}
	}
	
	/**
	 * Beware : order of elements is signifiant here (as it is used to build id), as a consequence we use a LinkedHashMap 
	 */
	private static final Map<Property, Collection<CascadeType>> ENTRY_PROPERTIES = new LinkedHashMap<Property, Collection<CascadeType>>();
	
	static {
		Collection<CascadeType> cascade = GraphUtils.extractCascadeOf(new CascadeType[] {CascadeType.ALL});
		try {
			Class<Map.Entry> interfaceClass = Map.Entry.class; 
			Class<WriteableKeyEntry> implementationClass = WriteableKeyEntry.class;
			PropertyDescriptor keyProperty = new PropertyDescriptor("key", interfaceClass.getDeclaredMethod("getKey"), implementationClass.getDeclaredMethod("setKey", Object.class));
			PropertyDescriptor valueProperty = new PropertyDescriptor("value", interfaceClass.getDeclaredMethod("getValue"), interfaceClass.getDeclaredMethod("setValue", Object.class));
			
			
			ENTRY_PROPERTIES.put(
							new DescribedProperty(keyProperty, Map.Entry.class), 
							cascade);
			ENTRY_PROPERTIES.put(
							new DescribedProperty(valueProperty,	Map.Entry.class), 
							cascade);
		} catch(Exception e) {
			throw new UnableToGetKeyOrValueProperty(e);
		}
	}
	
	/**
	 * Should return one property for key, and one for value
	 * @return
	 */
	protected Map<Property, Collection<CascadeType>> getContainedProperties() {
		return ENTRY_PROPERTIES;
	}
	
	/**
	 * Before to persist, we make sure entry is a {@link WriteableKeyEntry} instance
	 * @param service
	 * @param cast
	 * @param objectsBeingUpdated
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractTupleTransformer#getVertexFor(com.dooapp.gaedo.blueprints.BluePrintsBackedFinderService, java.lang.Object, java.util.Map)
	 */
	@Override
	public <DataType> Vertex getVertexFor(BluePrintsBackedFinderService<DataType, ?> service, Entry cast, Map<String, Object> objectsBeingUpdated) {
		if(!(cast instanceof WriteableKeyEntry)) {
			cast = new WriteableKeyEntry(cast.getKey(), cast.getValue());
		}
		return super.getVertexFor(service, cast, objectsBeingUpdated);
	}

	@Override
	protected Entry instanciateTupleFor(ClassLoader classLoader, Vertex key) {
		try {
			return new WriteableKeyEntry();
//			return (Entry) classLoader.loadClass(WriteableKeyEntry.class.getCanonicalName()).newInstance();
		} catch (Exception e) {
			throw new UnableToInstanciateWriteableEntryException(e);
		}
	}

	/**
	 * 
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractTupleTransformer#getContainedClass()
	 */
	@Override
	protected Class<?> getContainedClass() {
		return WriteableKeyEntry.class;
	}
}
