package com.dooapp.gaedo.blueprints.transformers;

import com.dooapp.gaedo.blueprints.Kind;
import com.tinkerpop.blueprints.pgm.Vertex;


public class ClassLiteralTransformer extends AbstractSimpleLiteralTransformer<Class> implements LiteralTransformer<Class> {
	public static final String CLASS_URI_PREFIX = "java.lang.Class:";
	
	public ClassLiteralTransformer() {
		super(Class.class);
	}
	@Override
	protected Object getVertexValue(Class value) {
		return CLASS_URI_PREFIX+(value==null ? Object.class.getCanonicalName() : value.getCanonicalName());
	}

	@Override
	protected Class getValueClass(Class value) {
		return Class.class;
	}


	@Override
	protected String resolveType(String effectiveType) {
		return Class.class.getName();
	}
	
	@Override
	public Kind getKind() {
		return Kind.uri;
	}
	
	/**
	 * As class should be stored using URIs, their loading must revers the URI fragment into a meaningfull value
	 * @param valueClass
	 * @param key
	 * @param valueString
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractLiteralTransformer#loadObject(java.lang.Class, com.tinkerpop.blueprints.pgm.Vertex, java.lang.String)
	 */
	@Override
	protected Class loadObject(Class valueClass, Vertex key, String valueString) {
		return super.loadObject(valueClass, key, extractClassIn(valueString));
	}
	public String extractClassIn(String valueString) {
		if(valueString.startsWith(CLASS_URI_PREFIX))
			return valueString.substring(CLASS_URI_PREFIX.length());
		return valueString;
	}
}
