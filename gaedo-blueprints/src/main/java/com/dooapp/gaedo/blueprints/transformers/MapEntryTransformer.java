package com.dooapp.gaedo.blueprints.transformers;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.DescribedProperty;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Graph;
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
	private final Map<Property, Collection<CascadeType>> containedProperties = new LinkedHashMap<Property, Collection<CascadeType>>();
	private DescribedProperty KEY_PROPERTY;
	private DescribedProperty VALUE_PROPERTY;
	
	public MapEntryTransformer() {
		Collection<CascadeType> cascade = GraphUtils.extractCascadeOf(new CascadeType[] {CascadeType.ALL});
		try {
			Class<Map.Entry> interfaceClass = Map.Entry.class; 
			Class<WriteableKeyEntry> implementationClass = WriteableKeyEntry.class;
			PropertyDescriptor keyProperty = new PropertyDescriptor("key", interfaceClass.getDeclaredMethod("getKey"), implementationClass.getDeclaredMethod("setKey", Object.class));
			PropertyDescriptor valueProperty = new PropertyDescriptor("value", interfaceClass.getDeclaredMethod("getValue"), interfaceClass.getDeclaredMethod("setValue", Object.class));
			
			KEY_PROPERTY = new DescribedProperty(keyProperty, Map.Entry.class);
			VALUE_PROPERTY = new DescribedProperty(valueProperty, Map.Entry.class); 
			
			containedProperties.put(
							KEY_PROPERTY, 
							cascade);
			containedProperties.put(
							VALUE_PROPERTY, 
							cascade);
		} catch(Exception e) {
			throw new UnableToGetKeyOrValueProperty(e);
		}
	}
	
	/**
	 * Should return one property for key, and one for value. ethod is made public for making sure https://github.com/Riduidel/gaedo/issues/5 is behind us
	 * @return
	 */
	public Map<Property, Collection<CascadeType>> getContainedProperties() {
		return containedProperties;
	}
	
	/**
	 * Before to persist, we make sure entry is a {@link WriteableKeyEntry} instance
	 * @param service
	 * @param cast
	 * @param objectsBeingUpdated
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractTupleTransformer#getVertexFor(com.dooapp.gaedo.blueprints.IndexableGraphBackedFinderService, java.lang.Object, java.util.Map)
	 */
	@Override
	public <DataType> Vertex getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service, Entry cast, Map<String, Object> objectsBeingUpdated) {
		if(!(cast instanceof WriteableKeyEntry)) {
			cast = new WriteableKeyEntry(cast.getKey(), cast.getValue());
		}
		return super.getVertexFor(service, cast, objectsBeingUpdated);
	}

	@Override
	public Entry instanciateTupleFor(ClassLoader classLoader, Vertex key) {
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

	/**
	 * Construct from the initial iterable an iterable allowing us to navigate to vertex key
	 * @param p
	 * @return
	 */
	public Iterable<Property> constructMapEntryKeyIterable(Iterable<Property> properties) {
		Collection<Property> returned = new LinkedList<Property>();
		for(Property p : properties) {
			returned.add(p);
		}
		returned.add(KEY_PROPERTY);
		return returned;
	}

	/**
	 * Construct from the initial iterable an iterable allowing us to navigate to vertex key
	 * @param p
	 * @return
	 */
	public Iterable<Property> constructMapEntryValueIterable(Iterable<Property> properties) {
		Collection<Property> returned = new LinkedList<Property>();
		for(Property p : properties) {
			returned.add(p);
		}
		returned.add(VALUE_PROPERTY);
		return returned;
	}
}
