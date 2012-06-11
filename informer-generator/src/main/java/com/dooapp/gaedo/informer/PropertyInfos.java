package com.dooapp.gaedo.informer;

import japa.parser.ast.type.Type;

public class PropertyInfos {
	/** Expected type */
	public Type type;
	
	public String name;
	/**
	 * property has declared storage
	 */
	public String field;
	/** property has getter */
	public String getter;
	/** property has setter */
	public String setter;
	
	public String generateJavadoc(InformerInfos informerInfos, String informerTypeFor) {
		StringBuilder sOut = new StringBuilder();
		sOut.append("\n\t").append("informer for property {@link ").append(informerInfos.className).append("#").append(name).append("}.");
		sOut.append("\n\t").append("can be removed by adding <propertiesExclude>").append(informerInfos.getQualifiedClassName()).append("#").
			append(name).append("</propertiesExclude> to container project pom.xml (in gaedo-informer-generator plugin configuration)");
		sOut.append("\n\t").append("@return an instance of {@link ").append(informerTypeFor).
			append("} configured for manipulation of {@link ").append(informerInfos.className).append("#").append(name).append("}");
		sOut.append("\n\t");
		return sOut.toString();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PropertyInfos [");
		if (type != null) {
			builder.append("type=");
			builder.append(type);
			builder.append(", ");
		}
		if (name != null) {
			builder.append("name=");
			builder.append(name);
		}
		builder.append("]");
		return builder.toString();
	}

}