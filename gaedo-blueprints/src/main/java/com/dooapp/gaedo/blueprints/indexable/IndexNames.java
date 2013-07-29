package com.dooapp.gaedo.blueprints.indexable;

import java.util.Collection;
import java.util.LinkedList;

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
	VERTICES("vertices", Vertex.class, true),
	/**
	 * Legacy edge index
	 */
	EDGES("edges", Edge.class, false /* see https://github.com/Riduidel/gaedo/issues/59 */);

	private final String indexName;
	private final Class<? extends Element> indexed;
	/**
	 * Indicates this index can be used. This is a side effect of https://github.com/Riduidel/gaedo/issues/59
	 */
	private final boolean usable;

	private IndexNames(String indexName, Class<? extends Element> indexed, boolean usable) {
		this.indexName = indexName;
		this.indexed = indexed;
		this.usable = usable;
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

	public static Collection<IndexNames> usableIndices() {
		Collection<IndexNames> returned = new LinkedList<IndexNames>();
		for(IndexNames index : values()) {
			if(index.usable)
				returned.add(index);
		}
		return returned;
	}

	/**
	 * @return the usable
	 * @category getter
	 * @category usable
	 */
	public boolean isUsable() {
		return usable;
	}
}
