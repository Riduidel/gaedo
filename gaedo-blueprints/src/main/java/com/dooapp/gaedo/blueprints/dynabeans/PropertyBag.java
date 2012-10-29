package com.dooapp.gaedo.blueprints.dynabeans;

import java.util.Set;

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
	public Object get(String property);

	/**
	 * Set given dynamic property to have the given value. Typeckech should be made - at will - by implementors.
	 * @param key
	 * @param value
	 * @return previous property value
	 */
	public Object set(String key, Object value);

	/**
	 * @return all available dynamic properties names
	 */
	public Set<String> properties();}
