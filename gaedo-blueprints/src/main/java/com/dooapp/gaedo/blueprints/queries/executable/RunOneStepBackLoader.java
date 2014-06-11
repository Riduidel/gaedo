package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.ArrayList;
import java.util.List;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Load one level of property vertices. For that, previous lazy laoder
 * is loaded
 *
 * @author ndx
 *
 */
class RunOneStepBackLoader implements LazyLoader {

	private Property property;
	private LazyLoader vertices;

	/**
	 * Number of vertices this loader should return. Lazy loaded by {@link #size()} method.
	 */
	private long size = -1;

	/**
	 * Collection of vertices returned here. Lazy loaded by {@link #get()} method.
	 */
	private List<Vertex> returned;

	public RunOneStepBackLoader(Property next, LazyLoader vertices) {
		this.property = next;
		this.vertices = vertices;
	}

	@Override
	public Iterable<Vertex> get() {
		if(returned==null) {
			returned = new ArrayList<Vertex>();
			String edgeNameFor = GraphUtils.getEdgeNameFor(property);
			for (Vertex v : vertices.get()) {
				Iterable<Edge> edges = v.getEdges(Direction.IN, edgeNameFor);
				for (Edge e : edges) {
					returned.add(e.getVertex(Direction.OUT));
				}
			}
		}
		return returned;
	}

	@Override
	public long size() {
		if(size<0) {
			size = 0;
			String edgeNameFor = GraphUtils.getEdgeNameFor(property);
			for (Vertex v : vertices.get()) {
				Iterable<Edge> edges = v.getEdges(Direction.IN, edgeNameFor);
				for(Edge e : edges) {
					size++;
				}
			}
		}
		return size;
	}

	/**
	 * @return the property
	 * @category getter
	 * @category property
	 */
	public Property getProperty() {
		return property;
	}

	/**
	 * @param property the property to set
	 * @category setter
	 * @category property
	 */
	public void setProperty(Property property) {
		this.property = property;
	}

	/**
	 * @return the vertices
	 * @category getter
	 * @category vertices
	 */
	public LazyLoader getVertices() {
		return vertices;
	}

	/**
	 * @param vertices the vertices to set
	 * @category setter
	 * @category vertices
	 */
	public void setVertices(LazyLoader vertices) {
		this.vertices = vertices;
	}

	@Override
	public LazyLoader diveIntoLoadedSet() {
		return vertices.diveIntoLoadedSet();
	}

	@Override
	public int compareTo(LazyLoader o) {
		return diveIntoLoadedSet().compareTo(o.diveIntoLoadedSet());
	}
}