package com.dooapp.gaedo.blueprints.strategies.graph;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.FieldInformerLocator;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

public class GraphFieldLocator<DataType> implements FieldInformerLocator {

	private final Class<DataType> dataClass;
	private final GraphDatabaseDriver driver;
	private Graph graph;
	private Class<?> informerClass;
	private FieldInformerLocator delegate;

	public GraphFieldLocator(Class<DataType> serviceContainedClass, Class<?> serviceInformerClass, Graph graph, GraphDatabaseDriver driver, FieldInformerLocator informerLocator) {
		this.dataClass = serviceContainedClass;
		this.informerClass = serviceInformerClass;
		this.delegate = informerLocator;
		this.graph = graph;
		this.driver = driver;
	}

	/**
	 * This one should NEVER be implemented ... Or it should ? Don't really know, in fact
	 * @param field
	 * @return
	 * @see com.dooapp.gaedo.finders.root.FieldInformerLocator#getInformerFor(com.dooapp.gaedo.properties.Property)
	 */
	@Override
	public FieldInformer getInformerFor(Property field) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method " + GraphFieldLocator.class.getName() + "#getInformerFor has not yet been implemented AT ALL");
	}

	/**
	 * Only usage case is this one : creating a {@link GraphProperty} from the edge name and using it for an Informer
	 * @param informedClass
	 * @param fieldName
	 * @return
	 * @see com.dooapp.gaedo.finders.root.FieldInformerLocator#getInformerFor(java.lang.Class, java.lang.String)
	 */
	@Override
	public FieldInformer getInformerFor(Class informedClass, String fieldName) {
		if(informedClass.equals(dataClass)) {
			return getInformerFor(fieldName);
		} else {
			return null;
		}
	}

	/**
	 * Get a field informer by first checking if that edge exist, then, were it to exist somewhere, getting infos from the graph to build the best possible FieldInformer
	 * @param fieldName
	 * @return
	 */
	private FieldInformer getInformerFor(String fieldName) {
		if (graph instanceof IndexableGraph) {
			IndexableGraph indexable = (IndexableGraph) graph;
			Index<Edge> edges = indexable.getIndex(Index.EDGES, Edge.class);
			long count = edges.count(Properties.label.name(), fieldName);
			if(count>0) {
				GraphBasedPropertyBuilder<DataType> builder = new GraphBasedPropertyBuilder<DataType>(dataClass, driver);
				for(Edge e : edges.get(Properties.label.name(), fieldName)) {
					builder.add(e);
				}
				Property used = builder.build();
				return delegate.getInformerFor(used);
			}
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("GraphFieldLocator can't do a search for "+fieldName);
	}

}
