package com.dooapp.gaedo.blueprints.strategies;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.UnsupportedIdException;
import com.dooapp.gaedo.blueprints.UnsupportedIdTypeException;
import com.dooapp.gaedo.extensions.id.IdGenerator;
import com.dooapp.gaedo.extensions.id.IntegerGenerator;
import com.dooapp.gaedo.extensions.id.LongGenerator;
import com.dooapp.gaedo.extensions.id.StringGenerator;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.PropertyProviderUtils;
import com.dooapp.gaedo.properties.TypeProperty;
import com.dooapp.gaedo.utils.Utils;

public class BeanBasedMappingStrategy<DataType> implements GraphMappingStrategy<DataType> {
	private static final Logger logger = Logger.getLogger(BeanBasedMappingStrategy.class.getName());

	/**
	 * Cach linking classes to their property maps
	 */
	private Map<Class<?>, Map<Property, Collection<CascadeType>>> classes = new HashMap<Class<?>, Map<Property,Collection<CascadeType>>>();
	
	private final PropertyProvider propertyProvider;

	private final Class<DataType> serviceContainedClass;

	private final Migrator migrator;
	/**
	 * Property used to store id
	 */
	private Property idProperty;

	private boolean idGenerationRequired;

	public BeanBasedMappingStrategy(Class<DataType> serviceContainedClass, PropertyProvider propertyProvider, Migrator migrator) {
		super();
		this.serviceContainedClass = serviceContainedClass;
		this.propertyProvider = propertyProvider;
		this.migrator = migrator;
		this.idProperty = AnnotationUtils.locateIdField(propertyProvider, serviceContainedClass, Long.TYPE, Long.class, String.class);
		this.idGenerationRequired = idProperty.getAnnotation(GeneratedValue.class) != null;
	}
	
	@Override
	public Map<Property, Collection<CascadeType>> getContainedProperties(DataType object) {
		Class<? extends Object> objectClass = object.getClass();
		return getContainedProperties(objectClass);
	}

	public Map<Property, Collection<CascadeType>> getContainedProperties(Class<? extends Object> objectClass) {
		if (!classes.containsKey(objectClass)) {
			classes .put(objectClass, getPropertiesFor(propertyProvider, objectClass));
		}
		return classes.get(objectClass);
	}

	/**
	 * Get map linking properties to their respective cascading informations
	 * 
	 * @param provider
	 *            used provider
	 * @param searchedClass
	 *            searched class
	 * @return a map linking each property to all its cascading informations
	 */
	public Map<Property, Collection<CascadeType>> getPropertiesFor(PropertyProvider provider, Class<?> searchedClass) {
		Map<Property, Collection<CascadeType>> returned = new HashMap<Property, Collection<CascadeType>>();
		Property[] properties = PropertyProviderUtils.getAllProperties(provider, searchedClass);
		for (Property p : properties) {
			if (p.getAnnotation(OneToOne.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(OneToOne.class).cascade()));
			} else if (p.getAnnotation(OneToMany.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(OneToMany.class).cascade()));
			} else if (p.getAnnotation(ManyToMany.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(ManyToMany.class).cascade()));
			} else if (p.getAnnotation(ManyToOne.class) != null) {
				returned.put(p, GraphUtils.extractCascadeOf(p.getAnnotation(ManyToOne.class).cascade()));
			} else {
				returned.put(p, new LinkedList<CascadeType>());
			}
		}
		// And, if class is the contained one, add the (potential) Migrator
		// property
		if (this.migrator != null) {
			// Migrator has no cascade to be done on
			returned.put(migrator.getMigratorProperty(returned.keySet()), new LinkedList<CascadeType>());
		}
		// Finally, create a fake "classesCollection" property and add it to
		// property
		try {
			returned.put(new ClassCollectionProperty(serviceContainedClass), new LinkedList<CascadeType>());
			returned.put(new TypeProperty(serviceContainedClass), new LinkedList<CascadeType>());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "what ? a class without a \"class\" field ? WTF", e);
		}
		return returned;
	}

	/**
	 * This implementation only supports single valued id
	 * @param value
	 * @param id
	 * @see com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy#assignId(java.lang.Object, java.lang.Object[])
	 */
	@Override
	public void assignId(DataType value, Object... id) {
		idProperty.set(value, id[0]);
	}

	public void generateIdFor(AbstractBluePrintsBackedFinderService<?, DataType, ?> service, DataType toCreate) {
		IdGenerator generator = null;
		Class<?> objectType = Utils.maybeObjectify(idProperty.getType());
		if (Long.class.isAssignableFrom(objectType)) {
			generator = new LongGenerator(service, idProperty);
		} else if (Integer.class.isAssignableFrom(objectType)) {
			generator = new IntegerGenerator(service, idProperty);
		} else if (String.class.isAssignableFrom(objectType)) {
			generator = new StringGenerator(service, idProperty);
		} else {
			throw new UnsupportedIdTypeException(objectType + " can't be used as id : we don't know how to generate its values !");
		}
		generator.generateIdFor(toCreate);
	}

	@Override
	public void generateValidIdFor(AbstractBluePrintsBackedFinderService<?, DataType, ?> service, DataType toCreate) {
		// Check value of idProperty
		Object value = idProperty.get(toCreate);
		if (value == null) {
			generateIdFor(service, toCreate);
		} else if (Number.class.isAssignableFrom(Utils.maybeObjectify(idProperty.getType()))) {
			Number n = (Number) value;
			if (n.equals(0) || n.equals(0l)) {
				generateIdFor(service, toCreate);
			}
		}
	}

	@Override
	public String getIdString(DataType object) {
		Object objectId = idProperty.get(object);
		return GraphUtils.getIdOfLiteral(serviceContainedClass, idProperty, objectId);
	}

	@Override
	public String getAsId(Object object) {
		if (Utils.maybeObjectify(idProperty.getType()).isAssignableFrom(Utils.maybeObjectify(object.getClass()))) {
			return GraphUtils.getIdOfLiteral(serviceContainedClass, idProperty, object);
		} else {
			throw new UnsupportedIdException(object.getClass(), idProperty.getType());
		}
	}

	@Override
	public Collection<Property> getIdProperties() {
		return Arrays.asList(idProperty);
	}

	@Override
	public boolean isIdGenerationRequired() {
		return idGenerationRequired;
	}

}
