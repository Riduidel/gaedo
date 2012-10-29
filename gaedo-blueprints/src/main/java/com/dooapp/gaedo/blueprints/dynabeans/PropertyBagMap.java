package com.dooapp.gaedo.blueprints.dynabeans;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.Id;

import com.dooapp.gaedo.blueprints.annotations.GraphProperty;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;
import com.dooapp.gaedo.properties.Property;

/**
 * Default implementation of a bean with dynamic properties.
 * Beware : these properties are defined by no dynamic class, and as a consequence are totally free
 * @author ndx
 *
 */
public class PropertyBagMap implements PropertyBag, PropertyMapPropertyAccess {
	/**
	 * URI of node in RDF graph representing this particular property bag
	 */
	@Id
	@GraphProperty(mapping=PropertyMappingStrategy.asIs)
	private String id;
	
	/**
	 * Map storing all data
	 */
	@BagProperty
	private Map<String, Object> data = new TreeMap<String, Object>();

	@Override
	public Object getFrom(Property graphProperty) {
		return data.get(graphProperty.getName());
}

	@Override
	public void setFrom(Property graphProperty, Object value) {
		data.put(graphProperty.getName(), value);
	}

	/**
	 * @return the id
	 * @category getter
	 * @category id
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#containsKey(java.lang.Object)
	 * @category delegate
	 */
	@Override
	public boolean contains(String key) {
		return data.containsKey(key);
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 * @category delegate
	 */
	@Override
	public Object get(String key) {
		return data.get(key);
	}

	/**
	 * @param key
	 * @param value
	 * @return
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 * @category delegate
	 */
	@Override
	public Object set(String key, Object value) {
		return data.put(key, value);
	}

	/**
	 * @return
	 * @see java.util.Map#keySet()
	 * @category delegate
	 */
	@Override
	public Set<String> properties() {
		return data.keySet();
	}
}
