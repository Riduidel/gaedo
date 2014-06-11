package com.dooapp.gaedo.blueprints.queries.executable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

/**
 * Faking the lazy laoder by giving an already loaded collection and masking
 * it behind this lazy laoder interface
 *
 * @author ndx
 *
 */
class EagerLoader implements LazyLoader {

	private Collection<Vertex> loaded;

	private WeakReference<Set<String>> verticesIds = new WeakReference<Set<String>>(null);

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
	public LazyLoader diveIntoLoadedSet() {
		return this;
	}

	@Override
	public int compareTo(LazyLoader o) {
		if (o instanceof EagerLoader) {
			EagerLoader eager = (EagerLoader) o;
			return CollectionUtils.compareCollections(getVerticesIds(), eager.getVerticesIds());
		} else {
            return getClass().getName().compareTo(o.getClass().getName());
		}
	}

	/**
	 * Cache of vertices ids to fasten thinkgs a little
	 * @return
	 */
	private Set<String> getVerticesIds() {
		Set<String> returned = null;
		while((returned = verticesIds.get())==null) {
			verticesIds = new WeakReference<Set<String>>(toId(loaded));
		}
		return returned;
	}

	Set<String> toId(Collection<Vertex> vertices) {
		SortedSet<String> returned = new TreeSet<String>();
		for(Vertex v : vertices) {
			returned.add(v.getProperty(Properties.value.name()).toString());
		}
		return returned;
	}
}