package com.dooapp.gaedo.blueprints.queries.executable;

import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;

final class IndexLazyLoader implements LazyLoader {
    private final Index<Vertex> vertices;
    private final String propertyKeyInIndex;
    private final String propertyValueInIndex;

    public IndexLazyLoader(Index<Vertex> vertices, String propertyKeyInIndex, String propertyValueInIndex) {
        super();
        this.propertyValueInIndex = propertyValueInIndex;
        this.propertyKeyInIndex = propertyKeyInIndex;
        this.vertices = vertices;
    }

    @Override
    public Iterable<Vertex> get() {
        return vertices.get(propertyKeyInIndex, propertyValueInIndex);
    }

    @Override
    public long size() {
        return vertices.count(propertyKeyInIndex, propertyValueInIndex);
    }

    /**
     * @return
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IndexLazyLoader [");
        if (propertyKeyInIndex != null) {
            builder.append("key=");
            builder.append(propertyKeyInIndex);
            builder.append(", ");
        }
        if (propertyValueInIndex != null) {
            builder.append("value=");
            builder.append(propertyValueInIndex);
            builder.append(", ");
        }
        builder.append("size()=");
        builder.append(size());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public LazyLoader diveIntoLoadedSet() {
        return this;
    }

    @Override
    public int compareTo(LazyLoader o) {
        if (o instanceof IndexLazyLoader) {
            IndexLazyLoader index = (IndexLazyLoader) o;
            int returned = 0;
            if(returned==0) {
            	returned = propertyKeyInIndex.compareTo(index.propertyKeyInIndex);
            }
            if(returned==0) {
            	returned = propertyValueInIndex.compareTo(((IndexLazyLoader) o).propertyValueInIndex);
            }
            return returned;
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }
}
