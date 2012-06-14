package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.UnableToCreateException;
import com.dooapp.gaedo.utils.Utils;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * Helper class for literal transformer
 * @author ndx
 *
 */
public abstract class AbstractLiteralTransformer<Type> {

	public Vertex getVertexFor(IndexableGraph database, Type value) {
		Object vertexId = getVertexId(database, value);
		// First try direct vertexId access
		if(database.getVertex(vertexId)!=null) {
			return database.getVertex(vertexId);
		}
		// Then indexed vertex id (for neo4j, typically)
		Vertex returned = GraphUtils.locateVertex(database, Properties.vertexId.name(), vertexId);
		// Finally create vertex
		if(returned==null) {
			returned = database.addVertex(vertexId);
			returned.setProperty(Properties.value.name(), getVertexValue(value));
			returned.setProperty(Properties.vertexId.name(), vertexId);
			returned.setProperty(Properties.kind.name(), Kind.literal.name());
			returned.setProperty(Properties.type.name(), value.getClass().getCanonicalName());
		}
		return returned;
	}

	public Type loadObject(Vertex key) {
		String effectiveType = key.getProperty(Properties.type.name()).toString();
		try {
			Class valueClass = Class.forName(effectiveType);
			return loadObject(valueClass, key);
		} catch (ClassNotFoundException e) {
			throw new UnableToCreateException(effectiveType, e);
		}
	}

	public Type loadObject(Class valueClass, Vertex key) {
		String valueString = key.getProperty(Properties.value.name()).toString();
		return loadObject(valueClass, key, valueString);
	}

	protected Type loadObject(Class valueClass, Vertex key, String valueString) {
		return (Type) Utils.fromString(valueString, valueClass);
	}

	/**
	 * Get vertex value for object. notice it is better to get it as a string than anything else
	 * @param value
	 * @return a value for that vertex. Again, null is NOT allowed.
	 */
	protected abstract Object getVertexValue(Type value);

	/**
	 * Creates an id out of an object
	 * @param value
	 * @return
	 */
	public String getVertexId(Graph database, Type value) {
		String idString = getValueClass(value).getCanonicalName()+":"+getVertexValue(value).toString();
		return idString;
	}

	/**
	 * Get value class name for this literal
	 * @param value
	 * @return usually should return value.getClass(). The main thing to understand is that null is NOT allowed
	 */
	protected abstract Class getValueClass(Type value);

}
