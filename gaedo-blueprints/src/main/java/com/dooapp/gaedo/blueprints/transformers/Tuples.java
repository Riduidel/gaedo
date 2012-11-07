package com.dooapp.gaedo.blueprints.transformers;

import java.io.Serializable;
import java.util.Map;

/**
 * Enum listing the classes considered as tuples, sharing the same mechanism than {@link Literals}
 * @author ndx
 *
 */
public enum Tuples implements TransformerAssociation<TupleTransformer> {
	entries(Map.Entry.class, new MapEntryTransformer()),
	serializables(Serializable.class, new SerializableTransformer());
	
	/**
	 * Source dataclass
	 */
	private final Class dataClass;
	/**
	 * Associated transformer
	 */
	private final TupleTransformer transformer;
	
	Tuples(Class dataClass, TupleTransformer transformer) {
		this.dataClass = dataClass;
		this.transformer = transformer;
	}
	
	public static TupleTransformer get(Class dataClass) {
		return Transformers.get(Tuples.values(), dataClass);
	}
	
	public static TupleTransformer get(ClassLoader classLoader, String effectiveType) {
		return Transformers.get(Tuples.values(), classLoader, effectiveType);
	}

	public static boolean containsKey(Class<? extends Object> valueClass) {
		return Transformers.containsKey(Tuples.values(), valueClass);
	}
	
	public static boolean containsKey(ClassLoader classLoader, String effectiveType) {
		return Transformers.containsKey(Tuples.values(), classLoader, effectiveType);
	}

	@Override
	public Class<?> getDataClass() {
		return dataClass;
	}

	@Override
	public TupleTransformer getTransformer() {
		return transformer;
	}

	@Override
	public boolean canHandle(ClassLoader classLoader, String effectiveType) {
		return transformer.canHandle(classLoader, effectiveType);
	}
}
