package com.dooapp.gaedo.finders;

import java.util.Map.Entry;

import com.dooapp.gaedo.finders.sort.SortingExpressionVisitor;
import com.dooapp.gaedo.patterns.Visitable;

/**
 * Define a sorting mode
 * @author ndx
 *
 */
public interface SortingExpression extends Iterable<Entry<FieldInformer, SortingExpression.Direction>>, Visitable<SortingExpressionVisitor>{
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
	}

	/**
	 * Adds a sorting directive for a given field
	 * @param informer informer for field
	 * @param direction direction for field
	 */
	public SortingExpression add(FieldInformer informer, Direction direction);
}
