package com.dooapp.gaedo.blueprints.dynabeans;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
	private Map<Property, Object> data = new HashMap<Property, Object>();
	
	@Override
	public Object getFrom(Property graphProperty) {
		return data.get(graphProperty);
}

	@Override
	public void setFrom(Property graphProperty, Object value) {
		data.put(graphProperty, value);
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
		return data.get(findProperty(key));
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
		try {
			return data.put(findProperty(key), value);
		} catch(NoSuchPropertyException e) {
			com.dooapp.gaedo.blueprints.strategies.graph.GraphProperty used = new com.dooapp.gaedo.blueprints.strategies.graph.GraphProperty();
			used.setName(key);
			used.setType(value.getClass());
			return data.put(used, value);
		}
	}

	private Property findProperty(String key) {
		for(Property p : data.keySet()) {
			if(p.getName().equals(key))
				return p;
		}
		throw new NoSuchPropertyException(key, properties());
	}

	/**
	 * @return
	 * @see java.util.Map#keySet()
	 * @category delegate
	 */
	@Override
	public Set<String> properties() {
		Set<String> returned = new TreeSet<String>();
		for(Property p : data.keySet()) {
			returned.add(p.getName());
		}
		return returned;
	}
	
	/**
	 * @param id new value for #id
	 * @category fluent
	 * @category setter
	 * @category id
	 * @return this object for chaining calls
	 */
	public PropertyBagMap withId(String id) {
		this.setId(id);
		return this;
	}

	/**
	 * @param id the id to set
	 * @category setter
	 * @category id
	 */
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Iterable<Property> propertyUris() {
		return data.keySet();
	}

	@Override
	public void dont_use_this_interface_which_is_gaedo_specific_customers_should_use_property_bag_interface() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method "+PropertyMapPropertyAccess.class.getName()+"#dont_use_this_interface_which_is_gaedo_specific_customers_should_use_property_bag_interface has not yet been implemented AT ALL");
	}

	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertyBagMap other = (PropertyBagMap) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PropertyBagMap [");
		if (getId() != null) {
			builder.append("getId()=");
			builder.append(getId());
		}
		builder.append("]");
		return builder.toString();
	}
}
