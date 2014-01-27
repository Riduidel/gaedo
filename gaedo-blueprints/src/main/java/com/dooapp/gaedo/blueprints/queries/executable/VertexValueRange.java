package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Class linking a vertex, and a path to a global score describing how well it selects data in the DB
 * @author ndx
 *
 */
public class VertexValueRange {
	Entry<Iterable<Vertex>, Iterable<Property>> entry = null;
	private long edgesCount = Long.MAX_VALUE;

	public VertexValueRange() {

	}

	/**
	 * While the {@link #findBestMatch(Entry)} focus on checking if this entry has the smallest value range, this one simply return the while value range
	 * @return
	 */
	public List<Vertex> getValues() {
		Iterator<Property> pathIterator = entry.getValue().iterator();
		ArrayList<Vertex> arrayList = new ArrayList<Vertex>();
		getValues(entry, pathIterator, arrayList);
		return arrayList;
	}

	private void getValues(Entry<Iterable<Vertex>, Iterable<Property>> entry, Iterator<Property> pathIterator, List<Vertex> vertexAccumulator) {
		if(pathIterator.hasNext()) {
			Property current = pathIterator.next();
			getValues(entry, pathIterator, vertexAccumulator);
			List<Vertex> toScan = new LinkedList<Vertex>(vertexAccumulator);
			// clear parameter to let it be populated by result
			vertexAccumulator.clear();
			for(Vertex v : toScan) {
				Iterable<Edge> edges = v.getEdges(Direction.IN, GraphUtils.getEdgeNameFor(current));
				for(Edge e : edges) {
					vertexAccumulator.add(e.getVertex(Direction.OUT));
				}
			}
		} else {
			for(Vertex v : entry.getKey()) {
				vertexAccumulator.add(v);
			}
		}
	}

	/**
	 * Find best match between this entry score and the new one
	 * @param entry2
	 * @return
	 */
	public VertexValueRange findBestMatch(Entry<Iterable<Vertex>, Iterable<Property>> entry) {
		Iterator<Property> pathIterator = entry.getValue().iterator();
		return findBestMatch(entry, new AtomicLong(0), new ArrayList<Vertex>(), pathIterator);
	}

	/**
	 * Recursive method computing, using all the possible paths, the edge count linking
	 * @param entry evaluated entry
	 * @param edgesCount current value of edges count, an atomic long is used to not use primitive type (which is immutable) while guaranteeing that value will be safely changed.
	 * @param verticesToCountEdgesIn
	 * @param pathIterator that iterator browses the path and at each path element calls this very method to recurse over path
	 * @param usingProperty property that should be used to find the vertex in the graph (instead of simply relying upon valye type (which is especially useful for numbers). This property can be null
	 * @return
	 */
	private VertexValueRange findBestMatch(Entry<Iterable<Vertex>, Iterable<Property>> entry, AtomicLong edgesCount, ArrayList<Vertex> verticesToCountEdgesIn, Iterator<Property> pathIterator) {
		if(pathIterator.hasNext()) {
			Property current = pathIterator.next();
			VertexValueRange matching = findBestMatch(entry, edgesCount, verticesToCountEdgesIn, pathIterator);
			// If previous level already returned this entryscore, there is no chance for new one to have less edges ... or we guess so
			if(matching==this) {
				return matching;
			} else {
				long totalEdgeCount = 0;
				List<Vertex> toScan = new LinkedList<Vertex>(verticesToCountEdgesIn);
				// clear parameter to let it be populated by result
				verticesToCountEdgesIn.clear();
				for(Vertex v : toScan) {
					Iterable<Edge> edges = v.getEdges(Direction.IN, GraphUtils.getEdgeNameFor(current));
					for(Edge e : edges) {
						verticesToCountEdgesIn.add(e.getVertex(Direction.OUT));
						totalEdgeCount = edgesCount.addAndGet(1);
						if(totalEdgeCount>this.edgesCount)
							return this;
					}
				}
				totalEdgeCount = edgesCount.get();
				if(totalEdgeCount<this.edgesCount) {
					// Due to previous if, we can guarantee matching is not this object, but the new one (for which we need to update edgesCount)
					return matching.withEdgesCount(totalEdgeCount);
				} else {
					return this;
				}
			}
		} else {
			// the vertices to scan consist only in entry key
			verticesToCountEdgesIn.addAll(CollectionUtils.asList(entry.getKey()));
			edgesCount.set(verticesToCountEdgesIn.size());
			if(edgesCount.get()<this.edgesCount) {
				// End of the path, no ? then return supposed best match (which is, up to now, a new EntryScore)
				return new VertexValueRange().withEntry(entry).withEdgesCount(edgesCount.get());
			} else {
				return this;
			}
		}
	}

	/**
	 * @return the entry
	 * @category getter
	 * @category entry
	 */
	public Entry<Iterable<Vertex>, Iterable<Property>> getEntry() {
		return entry;
	}

	/**
	 * @param entry the entry to set
	 * @category setter
	 * @category entry
	 */
	public void setEntry(Entry<Iterable<Vertex>, Iterable<Property>> entry) {
		this.entry = entry;
	}

	/**
	 * @return the edgesCount
	 * @category getter
	 * @category edgesCount
	 */
	public long getEdgesCount() {
		return edgesCount;
	}

	/**
	 * @param edgesCount the edgesCount to set
	 * @category setter
	 * @category edgesCount
	 */
	public void setEdgesCount(long edgesCount) {
		this.edgesCount = edgesCount;
	}

	/**
	 * @param edgesCount new value for #edgesCount
	 * @category fluent
	 * @category setter
	 * @category edgesCount
	 * @return this object for chaining calls
	 */
	public VertexValueRange withEdgesCount(long edgesCount) {
		this.setEdgesCount(edgesCount);
		return this;
	}

	/**
	 * @param entry new value for #entry
	 * @category fluent
	 * @category setter
	 * @category entry
	 * @return this object for chaining calls
	 */
	public VertexValueRange withEntry(Entry<Iterable<Vertex>, Iterable<Property>> entry) {
		this.setEntry(entry);
		return this;
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VertexValueRange [");
		if (entry != null) {
			builder.append("entry=");
			builder.append(entry);
			builder.append(", ");
		}
		builder.append("edgesCount=");
		builder.append(edgesCount);
		builder.append("]");
		return builder.toString();
	}
}