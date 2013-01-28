package com.dooapp.gaedo.finders;

import java.util.Map.Entry;

import com.dooapp.gaedo.exceptions.UncomparableObjectsInSortingException;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.dooapp.gaedo.finders.sort.SortingExpressionVisitor;
import com.dooapp.gaedo.patterns.Visitable;

/**
 * Define a sorting mode
 * @author ndx
 *
 */
public interface SortingExpression extends Iterable<Entry<FieldInformer, SortingExpression.Direction>>, Visitable<SortingExpressionVisitor>{
	/**
	 * An easy builder pattern for sorting expressions
	 * @author ndx
	 *
	 */
	public static class Build {
		public static SortingExpression sort() {
			return new SortingExpressionImpl();
		}
	}
	/**
	 * The sorting direction for a given field
	 * @author ndx
	 *
	 */
	public static enum Direction {
		Ascending("Ascending"),
		Descending("Descending");
		
		/**
		 * Text associated to direction. This text can be used in dynamic finders code
		 */
		private final String text;

		public String getText() {
			return text;
		}

		private Direction(String name) {
			this.text = name; 
		}

		/**
		 * Compare objects and return a result according to direction
		 * @param firstValue first compared value
		 * @param secondValue second compared value
		 * @return comparison result according to this ordering, or an exception if anything failed (a null somewhere, or a non comparable object)
		 */
		public int compareTo(Object firstValue, Object secondValue) {
			switch(this) {
			case Ascending:
				if(firstValue instanceof Comparable)
					return ((Comparable) firstValue).compareTo(secondValue);
				else if(secondValue instanceof Comparable)
					return -1*((Comparable) secondValue).compareTo(firstValue);
				throw new UncomparableObjectsInSortingException("nor firstValue "+firstValue+" neither secondValue "+secondValue+" is Comparable !");
			case Descending:
				if(firstValue instanceof Comparable)
					return -1*((Comparable) firstValue).compareTo(secondValue);
				else if(secondValue instanceof Comparable)
					return ((Comparable) secondValue).compareTo(firstValue);
				throw new UncomparableObjectsInSortingException("nor firstValue "+firstValue+" neither secondValue "+secondValue+" is Comparable !");
			}
			throw new UnsupportedOperationException("this cas is unknown to Direction ("+this+")");
		}
	}

	/**
	 * Adds a sorting directive for a given field
	 * @param informer informer for field
	 * @param direction direction for field
	 */
	public SortingExpression add(FieldInformer informer, Direction direction);
	
	/**
	 * Adds an ascending sorting directive for the given informer
	 * @param informer
	 * @return
	 */
	public SortingExpression withAscending(FieldInformer informer);
	
	/**
	 * Adds a descending sorting directive for the given informer
	 * @param informer
	 * @return
	 */
	public SortingExpression withDescending(FieldInformer informer);
}
