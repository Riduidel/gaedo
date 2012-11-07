package com.dooapp.gaedo.blueprints.transformers;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * An helper class allowing easy literal transformer lookup
 * @author ndx
 *
 */
public enum Literals implements TransformerAssociation<LiteralTransformer> {
	strings(String.class, new StringLiteralTransformer()),
	numbers(Number.class, new NumberLiteralTransformer()),
	booleans(Boolean.class, new BooleanLiteralTransformer()),
	enums(Enum.class, new EnumLiteralTransformer()),
	dates(Date.class, new DateLiteralTransformer()),
	classes(Class.class, new ClassLiteralTransformer());
	
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
}
