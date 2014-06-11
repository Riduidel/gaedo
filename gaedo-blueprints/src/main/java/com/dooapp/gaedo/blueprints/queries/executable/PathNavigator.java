package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;

/**
 * Class dedicating to navigate the vertex set backwards.
 *
 * @author ndx
 *
 */
class PathNavigator {
	/**
	 *
	 */
	private final VertexSet vertexSet;

	/**
	 * @param vertexSet
	 */
	PathNavigator(VertexSet vertexSet) {
		this.vertexSet = vertexSet;
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
		this.vertexSet.vertices = new RunOneStepBackLoader(pathIterator.next(), this.vertexSet.vertices);
	}

}