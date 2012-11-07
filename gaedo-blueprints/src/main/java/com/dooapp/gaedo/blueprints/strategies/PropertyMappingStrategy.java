package com.dooapp.gaedo.blueprints.strategies;

import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.properties.Property;

public enum PropertyMappingStrategy {
	/**
	 * When using this property mapping strategy, no literal is read or written to graph without being prefixed with literal class.
	 */
	prefixed,
	/**
	 * When using this strategy, value is directly copied in graph without adding/removing any prefix
	 */
	asIs;

	public String literalToId(Class<?> declaredClass, Property idProperty, Object objectId) {
		StringBuilder sOut = new StringBuilder();
		if(this==prefixed)
			sOut.append(declaredClass.getCanonicalName()).append(":");
		if(idProperty==null) {
			sOut.append(Literals.get(declaredClass).getVertexId(objectId));
		} else {
			sOut.append(Literals.get(idProperty.getType()).getVertexId(objectId));
		}
		return sOut.toString();
	}

}
