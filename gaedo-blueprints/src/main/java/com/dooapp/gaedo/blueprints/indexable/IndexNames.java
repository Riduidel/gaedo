package com.dooapp.gaedo.blueprints.indexable;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

/**
 * Interface storing the names of some legacy index names
 * @author ndx
 *
 */
public enum IndexNames {

	/**
	 * Legacy vertex index
	 */
	VERTICES("vertices", Vertex.class),
	/**
	 * Legacy edge index
	 */
	EDGES("edges", Edge.class);

	private final String indexName;
	private final Class<? extends Element> indexed;

	private IndexNames(String indexName, Class<? extends Element> indexed) {
		this.indexName = indexName;
		this.indexed = indexed;
	}

	/**
	 * @return the indexName
	 * @category getter
	 * @category indexName
	 */
	public String getIndexName() {
		return indexName;
	}

	/**
	 * @return the indexed
	 * @category getter
	 * @category indexed
	 */
	public Class<? extends Element> getIndexed() {
		return indexed;
	}
	
	public String describe() {
		StringBuilder sOut = new StringBuilder();
		sOut.append(indexName).append(" indexing ").append(indexed.getName());
		return sOut.toString();
	}
}
