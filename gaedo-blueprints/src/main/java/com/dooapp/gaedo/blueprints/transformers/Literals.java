package com.dooapp.gaedo.blueprints.transformers;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Date;

import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

/**
 * An helper class allowing easy literal transformer lookup
 * @author ndx
 *
 */
public enum Literals implements TransformerAssociation<LiteralTransformer> {
	uris(URI.class, new URILiteralTransformer()),
	strings(String.class, new StringLiteralTransformer()),
	numbers(Number.class, new NumberLiteralTransformer()),
	booleans(Boolean.class, new BooleanLiteralTransformer()),
	enums(Enum.class, new EnumLiteralTransformer()),
	dates(Date.class, new DateLiteralTransformer()),
	classes(Class.class, new ClassLiteralTransformer());

	public static final char CLASS_VALUE_SEPARATOR = ':';
	/**
	 * Source dataclass
	 */
	private final Class dataClass;
	/**
	 * Associated transformer
	 */
	private LiteralTransformer transformer;

	Literals(Class dataClass, LiteralTransformer transformer) {
		this.dataClass = dataClass;
		this.transformer = transformer;
	}

	public static LiteralTransformer get(ClassLoader classLoader, String effectiveType) {
		return Transformers.get(Literals.values(), classLoader, effectiveType);
	}

	public static LiteralTransformer get(Class dataClass) {
		return Transformers.get(Literals.values(), dataClass);
	}

	public static LiteralTransformer get(Type genericType) {
		return Transformers.get(Literals.values(), genericType);
	}

	public static boolean containsKey(ClassLoader classLoader, String effectiveType) {
		return Transformers.containsKey(Literals.values(), classLoader, effectiveType);
	}

	public static boolean containsKey(Class<? extends Object> valueClass) {
		return Transformers.containsKey(Literals.values(), valueClass);
	}

	@Override
	public Class<?> getDataClass() {
		return dataClass;
	}

	@Override
	public LiteralTransformer getTransformer() {
		return transformer;
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return transformer.canHandle(classLoader, effectiveType);
	}

	/**
	 * Get type prefix of the given literal transformer for later loading it
	 * @param propertyValue a value in which we want to extract type prefix
	 * @return the value before ":"
	 */
	public static String getTypePrefix(String propertyValue) {
		return propertyValue.substring(0, propertyValue.indexOf(CLASS_VALUE_SEPARATOR));
	}

	public static String getValueIn(String propertyValue) {
		return propertyValue.substring(propertyValue.indexOf(CLASS_VALUE_SEPARATOR)+1);
	}
}
