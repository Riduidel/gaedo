package com.dooapp.gaedo.blueprints.operations;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.AbstractPropertyAdapter;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.properties.UnusableTypeException;
import com.dooapp.gaedo.utils.Entry;

/**
 * Delegate property allowing creatin of a "sub" property on vertex.
 * To be more clear, when storing a collection of literals in a vertex, each literal need to have its own property (for loading)
 * and inverse property (for querying). As a consequence, this forged property allow those kinds of operations.
 * @author ndx
 *
 */
public abstract class LiteralInCollectionUpdaterProperty extends AbstractPropertyAdapter implements Property {
	private static final String SEPARATOR = ":";

	/**
	 * Get used property name
	 * @param p initial property
	 * @param key key of value in property (which is expected to contain a collection)
	 * @return full property name
	 */
	public static String getName(Property p, Object key) {
		LiteralTransformer transformer = Literals.get(key.getClass());
		return getName(p.getName(), transformer.toString(key));
	}

	/**
	 * Get used property name from parent property one and key for property value in parent property.
	 * @param propertyName parent property name
	 * @param propertyKey key for property value in parent. It MUST be obtained through a literal transformer.
	 * @return a property name
	 */
	public static String getName(String propertyName, String propertyKey) {
		return propertyName+SEPARATOR+propertyKey;
	}

	/**
	 * Extract property and key from given property name
	 * @param propertyName full property name
	 * @return a Map Entry containing as key the parent property and as value the property key
	 */
	public static Map.Entry<String, String> getKey(String propertyName, Property container) {
		String containerName = GraphUtils.getEdgeNameFor(container);
		int positionOfSeparator = propertyName.indexOf(SEPARATOR, containerName.length());
		if(positionOfSeparator<0)
			return null;
		String key = propertyName.substring(0, positionOfSeparator);
		String value = propertyName.substring(positionOfSeparator+SEPARATOR.length());
		return new Entry<String, String>(key, value);
	}

	private Object value;

	public LiteralInCollectionUpdaterProperty(Property p, Object key, Object value) {
		setDeclaringClass(p.getDeclaringClass());
		setGenericType(inferElementTypeIn(p.getGenericType()));
		copyAnnotationsFrom(p);
		String name = getName(p, key);
		setName(name);
		this.value = value;
	}

	@Override
	public String toGenericString() {
		return value.toString();
	}

	@Override
	public Object get(Object bean) {
		return value;
	}

	@Override
	public void set(Object bean, Object value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + LiteralInCollectionUpdaterProperty.class.getName() + "#set has not yet been implemented AT ALL");
	}

	@Override
	public Object fromString(String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + LiteralInCollectionUpdaterProperty.class.getName()
						+ "#fromString has not yet been implemented AT ALL");
	}

	/**
	 * Helper method allowing one to set property type for this object from a given collection type (for cases where direct obtaining is not possible).
	 * @param collectionType
	 */
	public void setGenericTypeFromCollection(Type collectionType) {
		setGenericType(inferElementTypeIn(collectionType));
	}

	/**
	 * Infer value type in one collection type
	 * @param genericType
	 * @return
	 */
	public static Type inferElementTypeIn(Type collectionType) {
		if(collectionType instanceof Class) {
			return Object.class;
		} else if(collectionType instanceof ParameterizedType) {
			ParameterizedType parameterizedCollectionType = (ParameterizedType) collectionType;
			Type collectionValueType = parameterizedCollectionType.getActualTypeArguments()[0];
			if(collectionValueType instanceof Class) {
				return (Class) collectionValueType;
			} else if(collectionValueType instanceof ParameterizedType) {
				return (ParameterizedType) collectionValueType;
			} else if(collectionValueType instanceof WildcardType) {
				throw new UnusableTypeException("we can't infer element type in widlcard type "+collectionType.toString());
			} else if(collectionValueType instanceof TypeVariable) {
				throw new UnusableTypeException("we can't infer element type in type variable "+collectionType.toString());
			}
		} else if(collectionType instanceof WildcardType) {
			throw new UnusableTypeException("we can't infer element type in widlcard type "+collectionType.toString());
		} else if(collectionType instanceof TypeVariable) {
			throw new UnusableTypeException("we can't infer element type in type variable "+collectionType.toString());
		}
		throw new UnusableTypeException("we can't infer element type in "+collectionType.toString());
	}

}
