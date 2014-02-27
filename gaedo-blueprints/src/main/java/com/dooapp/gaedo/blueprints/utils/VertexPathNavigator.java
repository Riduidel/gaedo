package com.dooapp.gaedo.blueprints.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralHelper;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Navigate from a source vertex to a target one by following a given apth
 * @author ndx
 *
 */
public class VertexPathNavigator {
	private static final Logger logger = Logger.getLogger(VertexPathNavigator.class.getName());
	/**
	 * Indicate from a source path a list of interesting elements :
	 * <ul>
	 * <li>navigated vertices</li>
	 * <li>navigated properties</li>
	 * <li>navigation was complete</li>
	 * </ul>
	 * @author ndx
	 *
	 */
	public static class VertexLocation {
		private boolean navigationSuccessfull = true;
		private Stack<Vertex> navigatedVertices = new Stack<Vertex>();
		private Stack<Property> navigatedProperties = new Stack<Property>();
		/**
		 * Navigated path that will be consumed
		 */
		private List<Property> initialPath = new ArrayList<Property>();

		/**
		 * Get final vertex on the navigation path
		 * @return the last vertex navigation send us to
		 */
		public Vertex vertex() {
			return navigatedVertices.peek();
		}

		/**
		 * Get final property on navigation path
		 * @return the last property the navigation send us to
		 */
		public Property property() {
			return navigatedProperties.peek();
		}

		public void push(Vertex source) {
			navigatedVertices.push(source);
		}

		public void push(Property currentProperty) {
			navigatedProperties .push(currentProperty);
		}

		void loadInitialPath(Iterable<Property> path) {
			initialPath.addAll(CollectionUtils.asList(path));
		}

		boolean hasPath() {
			return !initialPath.isEmpty() && isNavigationSuccessfull();
		}

		Property nextInPath() {
			Property firstProperty = initialPath.get(0);
			initialPath.remove(0);
			return firstProperty;
		}

		/**
		 * @return the navigationSuccessfull
		 * @category getter
		 * @category navigationSuccessfull
		 */
		public boolean isNavigationSuccessfull() {
			return navigationSuccessfull;
		}

		/**
		 * @param navigationSuccessfull the navigationSuccessfull to set
		 * @category setter
		 * @category navigationSuccessfull
		 */
		public void setNavigationSuccessfull(boolean navigationSuccessfull) {
			this.navigationSuccessfull = navigationSuccessfull;
		}
	}

	private Vertex source;
	private GraphMappingStrategy<?> strategy;
	private GraphDatabaseDriver driver;

	public VertexPathNavigator(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Vertex source) {
		this.strategy = strategy;
		this.driver = driver;
		this.source = source;
	}

	public VertexLocation navigateOn(Iterable<Property> path) {
		VertexLocation returned = new VertexLocation();
		returned.loadInitialPath(path);
		returned.push(source);

		while(returned.hasPath()) {
			Property currentProperty = returned.nextInPath();
			Vertex currentVertex = returned.vertex();
			Iterable<Edge> outEdges = strategy.getOutEdgesFor(currentVertex, currentProperty);
			Iterator<Edge> edges = outEdges.iterator();
			returned.push(currentProperty);
			if(edges.hasNext()) {
				returned.push(edges.next().getVertex(Direction.IN));
			} else {
				/*
				 * maybe that property can contain literal value.
				 * This literal value can be stored either as a direct literal (type is a literal one) or using the infamous
				 * TUples.serializables, in which case further analysis must be performed ...
				 */
				Class<?> propertyType = currentProperty.getType();
				if(driver.getRepository().containsKey(propertyType)) {
					returned.setNavigationSuccessfull(false);
				} else {
					Object value = currentVertex.getProperty(GraphUtils.getEdgeNameFor(currentProperty));
					if(value==null) {
						returned.setNavigationSuccessfull(true);
					} else {
						if(Literals.containsKey(propertyType)) {
							returned.setNavigationSuccessfull(true);
						} else if(Tuples.containsKey(propertyType)) {
							if(Tuples.serializables.getDataClass().isAssignableFrom(propertyType)) {
								// we must here unfrtonatly read value prefix to see if it is a literal
								String valueType = LiteralHelper.getTypePrefix(value.toString());
								try {
									ClassLoader classLoader = propertyType.getClassLoader();
									classLoader = (classLoader==null ? getClass().getClassLoader() : classLoader);
									Class valueClass = classLoader.loadClass(valueType);
									returned.setNavigationSuccessfull(Literals.containsKey(valueClass));
								} catch (ClassNotFoundException e) {
									if (logger.isLoggable(Level.WARNING)) {
										logger.log(Level.WARNING, String.format("Did you really decide to store an enum in a Serializable ?\n"
														+ "Gaedo is not strong enough for that, dude."
														+ "I would suggest to replace storage of %s"
														+ "from Serializable to ... somethign else.\n"
														+ "For additional reference, implied vertex is %s",
															currentProperty,
															GraphUtils.toString(currentVertex)), e);
									}
									returned.setNavigationSuccessfull(false);
								}
							} else {
								// Map entries are stored as effective objects in graph due to their multiple relationships
								returned.setNavigationSuccessfull(false);
							}
						}
					}
				}
			}
		}
		return returned;
	}

}
