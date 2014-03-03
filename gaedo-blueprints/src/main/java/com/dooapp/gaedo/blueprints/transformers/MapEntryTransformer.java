package com.dooapp.gaedo.blueprints.transformers;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.BluePrintsCrudServiceException;
import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.DescribedProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

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
	private Property KEY_PROPERTY;
	private Property VALUE_PROPERTY;

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
			// Adding the class collection property to this class to make sure we can get it back
			containedProperties.put(new ClassCollectionProperty(Map.Entry.class), cascade);
			containedProperties.put(new TypeProperty(), cascade);
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
	 * Before to persist, we make sure entry is a {@link WriteableKeyEntry} instance ... and that both key and values have id.
	 * Because if one of them don't, well, https://github.com/Riduidel/gaedo/issues/80 will come byte us.
	 * If needed, an id will be created.
	 * @param service
	 * @param cast
	 * @param objectsBeingUpdated
	 * @return
	 */
	@Override
	public <DataType> Vertex getVertexFor(AbstractBluePrintsBackedFinderService<? extends Graph, DataType, ?> service,
					GraphDatabaseDriver driver,
					Entry cast,
					CascadeType cascade,
					ObjectCache objectsBeingUpdated) {
		if(!(cast instanceof WriteableKeyEntry)) {
			cast = new WriteableKeyEntry(cast.getKey(), cast.getValue());
		}
		return super.getVertexFor(service, driver, cast, cascade, objectsBeingUpdated);
	}

	@Override
	public Entry instanciateTupleFor(ClassLoader classLoader, Vertex key) {
		try {
			return new WriteableKeyEntry();
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

	/**
	 * @param repository
	 * @param value
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractTupleTransformer#getIdOfTuple(com.dooapp.gaedo.finders.repository.ServiceRepository, java.lang.Object, CascadeType, ObjectCache)
	 */
	@Override
	public String getIdOfTuple(ServiceRepository repository, Entry value, CascadeType cascade, ObjectCache cache) {
		return getIdOfTuple(repository, value, Arrays.asList(KEY_PROPERTY, VALUE_PROPERTY), cascade, cache);
	}

}
