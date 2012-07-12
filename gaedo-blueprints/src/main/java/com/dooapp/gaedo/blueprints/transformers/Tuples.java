package com.dooapp.gaedo.blueprints.transformers;

import java.io.Serializable;
import java.util.Map;

import com.dooapp.gaedo.utils.Utils;

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
	
	public static TupleTransformer get(String effectiveType) {
		return Transformers.get(Tuples.values(), effectiveType);
	}

	public static boolean containsKey(Class<? extends Object> valueClass) {
		return Transformers.containsKey(Tuples.values(), valueClass);
	}
	
	public static boolean containsKey(String effectiveType) {
		return Transformers.containsKey(Tuples.values(), effectiveType);
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
	public boolean canHandle(String effectiveType) {
		return transformer.canHandle(effectiveType);
	}
}
