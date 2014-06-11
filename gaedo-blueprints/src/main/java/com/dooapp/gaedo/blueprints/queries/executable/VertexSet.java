package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Class expressing a way to find a vertices set from a list of starting
 * vertices and a path leading to these vertices. This class must provide the
 * mean to obtain the vertices count at any time.
 *
 * @author ndx
 *
 */
public class VertexSet {
	/**
	 * Faking the lazy laoder by giving an already loaded collection and masking
	 * it behind this lazy laoder interface
	 *
	 * @author ndx
	 *
	 */
	private static class EagerLoader implements LazyLoader, Comparable<LazyLoader> {

		private Collection<Vertex> loaded;
		private Collection<String> verticesIds;

		public EagerLoader(Collection<Vertex> loaded) {
			this.loaded = loaded;
		}

		@Override
		public Iterable<Vertex> get() {
			return loaded;
		}

		@Override
		public long size() {
			return loaded.size();
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("EagerLoader [");
			if (loaded != null) {
				builder.append("loaded=");
				builder.append(loaded);
			}
			builder.append("]");
			return builder.toString();
		}

		@Override
		public LazyLoader getSourceLoader() {
			return this;
		}

		@Override
		public int compareTo(LazyLoader o) {
			if (o instanceof EagerLoader) {
				EagerLoader loader = (EagerLoader) o;
				return CollectionUtils.compare(getVerticesIds(), loader.getVerticesIds());
			} else {
				return getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
			}
		}

		private Iterable<String> getVerticesIds() {
			if(verticesIds==null) {
				verticesIds = new TreeSet<String>();
				for(Vertex v : loaded) {
					verticesIds.add(v.getProperty(Properties.value.name()).toString());
				}
			}
			return verticesIds;
		}

	}

	/**
	 * Class dedicating to navigate the vertex set backwards.
	 *
	 * @author ndx
	 *
	 */
	private class PathNavigator {
		/**
		 * Load one level of property vertices. For that, previous lazy laoder
		 * is loaded
		 *
		 * @author ndx
		 *
		 */
		private class RunOneStepBack implements LazyLoader {

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

			public RunOneStepBack(Property next, LazyLoader vertices) {
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
			public LazyLoader getSourceLoader() {
				return vertices.getSourceLoader();
			}
		}

		/**
		 * Reverse iterator over property path. it only allows going from last
		 * property to first once. To restart iteration,
		 * {@link #initialize(Iterable)} method must be called.
		 */
		private Iterator<Property> pathIterator;

		/**
		 * Initializes the path navigator from the given property path. For
		 * that, an internal iterator will be created in which properties are
		 * navigated in reverse way. Notice
		 *
		 * @param propertyPath
		 */
		public void initialize(Iterable<Property> propertyPath) {
			List<Property> pathInDirectWay = CollectionUtils.asList(propertyPath);
			Collections.reverse(pathInDirectWay);
			// collection is now in reverse way, take care !
			pathIterator = pathInDirectWay.iterator();
		}

		public boolean canGoBack() {
			return pathIterator.hasNext();
		}

		/**
		 * So, how can we go back ? Well, by replacing current
		 * {@link VertexSet#vertices} lazy laoder by the one that will return
		 * effective vertices. This is in fact quite simple : we create a new
		 * lazy loader from initial one, where
		 */
		public void goBack() {
			vertices = new RunOneStepBack(pathIterator.next(), vertices);
		}

	}

	/**
	 * Current vertices lazy loader. The goal is to avoid as much as possible
	 * invoking the {@link LazyLoader#get()} method which would load the
	 * vertices.
	 */
	private LazyLoader vertices;

	private Iterable<Property> propertyPath;

	private PathNavigator navigator = new PathNavigator();

	/**
	 * @return the vertices
	 * @category getter
	 * @category vertices
	 */
	public LazyLoader getVertices() {
		return vertices;
	}

	/**
	 * @param vertices
	 *            the vertices to set
	 * @category setter
	 * @category vertices
	 */
	public void setVertices(LazyLoader vertices) {
		this.vertices = vertices;
	}

	/**
	 * @param vertices
	 *            new value for #vertices
	 * @category fluent
	 * @category setter
	 * @category vertices
	 * @return this object for chaining calls
	 */
	public VertexSet withVertices(LazyLoader vertices) {
		this.setVertices(vertices);
		return this;
	}

	public void setVertices(Collection<Vertex> loaded) {
		setVertices(new EagerLoader(loaded));
	}

	/**
	 * @param vertices
	 *            new value for #vertices
	 * @category fluent
	 * @category setter
	 * @category vertices
	 * @return this object for chaining calls
	 */
	public VertexSet withVertices(Collection<Vertex> vertices) {
		this.setVertices(vertices);
		return this;
	}

	/**
	 * @return the propertyPath
	 * @category getter
	 * @category propertyPath
	 */
	public Iterable<Property> getPropertyPath() {
		return propertyPath;
	}

	/**
	 * @param propertyPath
	 *            the propertyPath to set
	 * @category setter
	 * @category propertyPath
	 */
	public void setPropertyPath(Iterable<Property> propertyPath) {
		this.propertyPath = propertyPath;
		navigator.initialize(propertyPath);
	}

	/**
	 * @param propertyPath
	 *            new value for #propertyPath
	 * @category fluent
	 * @category setter
	 * @category propertyPath
	 * @return this object for chaining calls
	 */
	public VertexSet withPropertyPath(Iterable<Property> propertyPath) {
		this.setPropertyPath(propertyPath);
		return this;
	}

	/**
	 * Test if {@link #navigator} has work to do to fully develop the vertex set
	 *
	 * @return true if {@link #navigator} can continue navigation. false if full
	 *         vertex set has been reached.
	 */
	public boolean canGoBack() {
		return navigator.canGoBack();
	}

	/**
	 * Go back in path navigation
	 *
	 * @see PathNavigator#goBack()
	 */
	public void goBack() {
		navigator.goBack();
	}

	/**
	 * Obtain currently known size of this {@link VertexSet}.
	 * Notice this size can change according to calls made to {@link #goBack()}. As a consequence, please try to use it with care.
	 * @return value of current {@link LazyLoader#size()}
	 */
	public long size() {
		return vertices.size();
	}

	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("VertexSet [");
		if (vertices != null) {
			builder.append("vertices=");
			builder.append(vertices);
			builder.append(", ");
		}
		if (propertyPath != null) {
			builder.append("propertyPath=");
			builder.append(propertyPath);
		}
		builder.append("]");
		return builder.toString();
	}
}
