package com.dooapp.gaedo.blueprints.dynabeans;

import java.util.List;
import java.util.Set;

/**
 * Notice all properties are considered multi-valued.
 * @author ndx
 *
 */
public interface PropertyBag  {

	/**
	 * @return the id
	 * @category getter
	 * @category id
	 */
	public String getId();

	/**
	 * @param key
	 * @return
	 */
	public boolean contains(String property);

	/**
	 * Get given dynamic property value
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public List<Object> get(String property);
	
	/**
	 * Get size of given property.
	 * @param property
	 * @return 0 if property is not present in this bag
	 */
	public int getSize(String property);

	/**
	 * Set given dynamic property to have the given value. Typeckech should be made - at will - by implementors.
	 * @param key
	 * @param value the value to set. if not a list, this value will be transformed into one.
	 * @return previous property value
	 */
	public List<Object> set(String key, Object value);

	/**
	 * @return all available dynamic properties names
	 */
	public Set<String> properties();
}
