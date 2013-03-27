package com.dooapp.gaedo.blueprints.providers;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.event.ErrorState;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import com.dooapp.gaedo.blueprints.AbstractGraphProvider;
import com.dooapp.gaedo.blueprints.GraphProvider;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

public class Neo4j extends AbstractGraphProvider implements GraphProvider {
	private Map<String, GraphDatabaseService> services = Collections.synchronizedMap(new TreeMap<String, GraphDatabaseService>());
	
	@Override
	public IndexableGraph get(String path) {
		final String graphPath = path(path);
		GraphDatabaseService service;
		synchronized(services) {
			if(!services.containsKey(graphPath)) {
				service = new EmbeddedGraphDatabase(graphPath);
				services.put(graphPath, service);
				service.registerKernelEventHandler(new KernelEventHandler() {
	
					@Override
					public void beforeShutdown() {
						services.remove(graphPath);
					}
	
					@Override
					public void kernelPanic(ErrorState error) {
						services.remove(graphPath);
					}
	
					@Override
					public Object getResource() {
						return graphPath;
					}
	
					@Override
					public ExecutionOrder orderComparedTo(KernelEventHandler other) {
						return ExecutionOrder.DOESNT_MATTER;
					}
					
				});
			} else {
				service = services.get(graphPath);
			}
		}
		Neo4jGraph neo4jgraph = new Neo4jGraph(service);
		neo4jgraph.setCheckElementsInTransaction(true);
		return neo4jgraph;
	}

	@Override
	public String path(String usablePath) {
		return usablePath+"/neo4j/";
	}
}