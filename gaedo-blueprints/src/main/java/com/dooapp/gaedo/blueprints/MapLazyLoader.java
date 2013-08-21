package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

public class MapLazyLoader extends AbstractLazyLoader implements InvocationHandler, Serializable, WriteReplaceable {
	private static final Logger logger = Logger.getLogger(MapLazyLoader.class.getName());

	// Internal storage collection (not to be confused with external visible
	// collection)
	private Map map;

	/**
	 * Serialization constructor
	 */
	public MapLazyLoader() {

	}

	public MapLazyLoader(GraphDatabaseDriver driver, GraphMappingStrategy strategy, ClassLoader classLoader, ServiceRepository repository, Property p, Vertex objectVertex,
					Map<Object, Object> targetMap, ObjectCache objectsBeingAccessed) {
		super(driver, strategy, p, objectVertex, repository, classLoader, objectsBeingAccessed);
		this.map = targetMap;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (!loaded) {
			loadMap(map, objectsBeingAccessed);
		}
		if (method.getDeclaringClass().equals(WriteReplaceable.class)) {
			// there is only writeReplace there, so writeReplace !
			return map;
		}
		return method.invoke(map, args);
	}

	public void loadMap(Map map, ObjectCache objectsBeingAccessed) {
		try {
			for (Edge e : strategy.getOutEdgesFor(rootVertex, property)) {
				Vertex value = e.getVertex(Direction.IN);
				// Value is always a serialized map entry, so deserialize it
				// with magic !
				try {
					Map.Entry temporaryValue = (Entry) GraphUtils.createInstance(driver, strategy, classLoader, value, property.getType(), repository, objectsBeingAccessed);
					map.put(temporaryValue.getKey(), temporaryValue.getValue());
				} catch(UnableToCreateException ex) {
					if (logger.isLoggable(Level.WARNING)) {
						logger.log(Level.WARNING, "We failed to load entry associated to vertex "+GraphUtils.toString(value), ex);
					}
				}
			}
		} finally {
			loaded = true;
		}
	}

	@Override
	public Object writeReplace() throws ObjectStreamException {
		if (!loaded) {
			loadMap(map, objectsBeingAccessed);
		}
		return map;
	}
}
