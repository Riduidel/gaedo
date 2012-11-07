package com.dooapp.gaedo.finders.sort;

import java.util.Comparator;
import java.util.Map.Entry;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.FieldInformer;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.SortingExpression.Direction;

/**
 * A generic comparator backed by a sorting expression
 * @author ndx
 *
 */
public class SortingBackedComparator<DataType> implements Comparator<DataType> {
	public static class UnableToCompareObjectsException extends CrudServiceException {

		public UnableToCompareObjectsException(Throwable cause) {
			super(cause);
		}
		
	}
	/**
	 * used sorting expression
	 */
	private SortingExpression sortingExpression;

	/**
	 * Build the comparator with the given expression
	 * @param sortingExpression
	 */
	public SortingBackedComparator(SortingExpression sortingExpression) {
		this.sortingExpression = sortingExpression;
	}

	@Override
	public int compare(DataType o1, DataType o2) {
		try {
			for(Entry<FieldInformer, Direction> entry : sortingExpression) {
				Comparable firstValue = (Comparable<?>) entry.getKey().getField().get(o1);
				Comparable secondValue = (Comparable<?>) entry.getKey().getField().get(o2);
				int value = firstValue.compareTo(secondValue);
				if(entry.getValue()==Direction.Descending)
					value *= -1;
				if(value!=0)
					return value;
			}
		} catch(Exception e) {
			throw new UnableToCompareObjectsException(e);
		}
		// According to this sorting comparator, both objects are equals
		return 0;
	}

}
