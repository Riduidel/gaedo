package com.dooapp.gaedo.blueprints.queries.executable;

import java.util.Comparator;

import com.dooapp.gaedo.properties.ComparePropertyByNameAndDeclaringClass;
import com.dooapp.gaedo.utils.CollectionUtils;

/**
 * Compare vertex set first by set size, then by property path depth, then finally by loaded vertices list
 * @author ndx
 *
 */
class VertexSetSizeComparator implements Comparator<VertexSet> {
	public static class LazyLoaderComparator implements Comparator<LazyLoader> {

		@Override
		public int compare(LazyLoader o1, LazyLoader o2) {
			LazyLoader firstSource = o1.getSourceLoader();
			LazyLoader secondSource = o2.getSourceLoader();
			if (firstSource instanceof Comparable) {
				Comparable<LazyLoader> firstComparable= (Comparable<LazyLoader>) firstSource;
				return firstComparable.compareTo(secondSource);
			} else if (secondSource instanceof Comparable) {
				Comparable<LazyLoader> secondComparable = (Comparable<LazyLoader>) secondSource;
				return -1*secondComparable.compareTo(firstSource);
			}
			throw new UnsupportedOperationException(String.format("I'm really sorry, but this should just not happen : you compared\n"
							+ " 1 - %s which has as source %s to\n"
							+ " 2 - %ss which has as source %s",
							o1, firstSource,
							o2, firstSource));
		}

	}

	private LazyLoaderComparator lazyLoaderComparator = new LazyLoaderComparator();

	@Override
	public int compare(VertexSet o1, VertexSet o2) {
		int returned = (int) Math.signum(o1.size()-o2.size());
		if(returned==0) {
			// compare them on their inner size
			returned = CollectionUtils.compare(o1.getPropertyPath(), o2.getPropertyPath(), new ComparePropertyByNameAndDeclaringClass());
		}
		if(returned==0) {
			returned = lazyLoaderComparator.compare(o1.getVertices(), o2.getVertices());
		}
		return returned;
	}

}