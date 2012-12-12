package com.dooapp.gaedo.blueprints.strategies;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.properties.PropertyProviderUtils;
import com.dooapp.gaedo.properties.TypeProperty;

public class StrategyUtils {
	private static final Logger logger = Logger.getLogger(StrategyUtils.class.getName());

	/**
	 * Load strategy correpsonding to the given strategy type
	 * @param strategy strategy type to load a strategy for
	 * @param containedClass associated class
	 * @param propertyProvider property provider
	 * @param migrator value migrator (of no use in graph, but well ...)
	 * @return a 
	 */
	public static <DataType> GraphMappingStrategy<DataType> loadStrategyFor(StrategyType strategy, Class<DataType> containedClass, PropertyProvider propertyProvider, Migrator migrator) {
		switch(strategy) {
		case beanBased:
			return new BeanBasedMappingStrategy<DataType>(containedClass, propertyProvider, migrator);
		case graphBased:
			return new GraphBasedMappingStrategy<DataType>(containedClass, propertyProvider, migrator);
		default:
			throw new UnsupportedOperationException("the StrategyType "+strategy.name()+" is not yet supported");
		}
	}

	/**
	 * Extract cascade mode of available JPA annotations
	 * @param p property to extract cascade annotations from
	 * @return the list of cascaded operations
	 */
	public static Collection<CascadeType> extractCascadeOfJPAAnnotations(Property p) {
		Collection<CascadeType> mapping = null;
		if (p.getAnnotation(OneToOne.class) != null) {
			mapping = GraphUtils.extractCascadeOf(p.getAnnotation(OneToOne.class).cascade());
		} else if (p.getAnnotation(OneToMany.class) != null) {
			mapping = GraphUtils.extractCascadeOf(p.getAnnotation(OneToMany.class).cascade());
		} else if (p.getAnnotation(ManyToMany.class) != null) {
			mapping = GraphUtils.extractCascadeOf(p.getAnnotation(ManyToMany.class).cascade());
		} else if (p.getAnnotation(ManyToOne.class) != null) {
			mapping = GraphUtils.extractCascadeOf(p.getAnnotation(ManyToOne.class).cascade());
		} else {
			mapping = GraphUtils.extractCascadeOf(new CascadeType[] {CascadeType.ALL});
		}
		return mapping;
	}

	/**
	 * Get map linking properties to their respective cascading informations
	 * 
	 * @param provider
	 *            used provider
	 * @param searchedClass
	 *            searched class
	 * @param serviceContainedClass TODO
	 * @param migrator TODO
	 * @return a map linking each property to all its cascading informations
	 */
	public static Map<Property, Collection<CascadeType>> getBeanPropertiesFor(PropertyProvider provider, Class<?> searchedClass, Migrator migrator) {
		Map<Property, Collection<CascadeType>> returned = new HashMap<Property, Collection<CascadeType>>();
		Property[] properties = PropertyProviderUtils.getAllProperties(provider, searchedClass);
		for (Property p : properties) {
			returned.put(p, extractCascadeOfJPAAnnotations(p));
		}
		// And, if class is the contained one, add the (potential) Migrator
		// property
		if (migrator != null) {
			// Migrator has no cascade to be done on
			returned.put(migrator.getMigratorProperty(returned.keySet()), new LinkedList<CascadeType>());
		}
		return returned;
	}

}
