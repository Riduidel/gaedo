package com.dooapp.gaedo.blueprints.transformers;

import javax.persistence.CascadeType;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.properties.TypeProperty;
import com.tinkerpop.blueprints.Vertex;

/**
 * Allow transformation of a data type into a literal vertex
 * @author ndx
 *
 *@param Type type of transformed data (to avoid cast)
 */
public interface LiteralTransformer<Type> extends Transformer {

	/**
	 * Get value of given property from a string.
	 * This method is used to load literal values stored as vertex additional property
	 * @param valueClass property to read
	 * @param classloader classloader used to load that value. It should be of no use (excepted maybe for enums)
	 * @param objectCache cache from which value can be read to win some time
	 * @return the literal value. null is not possible.
	 */
	Type fromString(String propertyValue, Class valueClass, ClassLoader classloader, ObjectCache objectCache);

	/**
	 * Get value for object. It's this value that will be used in graph
	 *
	 * @param value
	 * @return a value for that vertex. Again, null is NOT allowed. Notice this value contains a class prefix allowing us to disambiguate cases where value
	 * may be refered from a more generic class (think about fields of types Object or Serializable)
	 */
	public String toString(Type value);

	/**
	 * Test method verifying that given object is equals to given graph value
	 * @param expected expected value
	 * @param effectiveGraphValue effective value
	 * @return true if both are equals according o transformation available with this transformer.
	 */
	boolean areEquals(Object expected, String effectiveGraphValue);
}
