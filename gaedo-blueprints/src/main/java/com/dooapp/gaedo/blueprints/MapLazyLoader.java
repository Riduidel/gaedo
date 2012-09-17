package com.dooapp.gaedo.blueprints;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.patterns.WriteReplaceable;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class MapLazyLoader extends AbstractLazyLoader implements InvocationHandler, Serializable, WriteReplaceable {

	// Internal storage collection (not to be confused with external visible
	// collection)
	private Map map;

	/**
	 * Serialization constructor
	 */
	public MapLazyLoader() {

	}

	public MapLazyLoader(ClassLoader classLoader, ServiceRepository repository, Property p, Vertex objectVertex, Map<Object, Object> targetMap,
					Map<String, Object> objectsBeingAccessed) {
		super(p, objectVertex, repository, classLoader, objectsBeingAccessed);
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

	public void loadMap(Map map, Map<String, Object> objectsBeingAccessed) {
		try {
			// No need to lie, we know it's this one !
			for (Edge e : rootVertex.getOutEdges(edgeName)) {
				Vertex value = e.getInVertex();
				// Value is always a serialized map entry, so deserialize it
				// with magic !
				Map.Entry temporaryValue = (Entry) GraphUtils.createInstance(classLoader, value, repository, objectsBeingAccessed);
				map.put(temporaryValue.getKey(), temporaryValue.getValue());
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
