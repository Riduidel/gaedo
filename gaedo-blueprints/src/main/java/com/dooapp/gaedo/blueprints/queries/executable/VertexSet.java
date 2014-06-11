package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Collection;

import com.dooapp.gaedo.properties.ComparePropertiesOnGenericName;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

import static com.dooapp.gaedo.utils.CollectionUtils.asList;
import static com.dooapp.gaedo.utils.CollectionUtils.compareCollections;

/**
 * Class expressing a way to find a vertices set from a list of starting
 * vertices and a path leading to these vertices. This class must provide the
 * mean to obtain the vertices count at any time.
 *
 * @author ndx
 *
 */
public class VertexSet implements Comparable<VertexSet> {
    /**
     * Current vertices lazy loader. The goal is to avoid as much as possible
     * invoking the {@link LazyLoader#get()} method which would load the
     * vertices.
     */
    LazyLoader vertices;

    private Iterable<Property> propertyPath;

    private PathNavigator navigator = new PathNavigator(this);

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

    @Override
    public int compareTo(VertexSet o) {
        int returned = (int) Math.signum(size()-o.size());
        if(equals(o))
        	return 0;
        // compare property paths
        if(returned==0) {
        	returned = compareCollections(asList(propertyPath), asList(o.propertyPath), new ComparePropertiesOnGenericName());
        }
        if(returned==0) {
        	// paths are identical ..; well, w'll have to dive to the first loaded loader
        	returned = vertices.compareTo(o.vertices);
        }
        return returned;
    }
}
