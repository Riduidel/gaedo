package com.dooapp.gaedo.finders.root;

/**
 * Validable objects allows validation, which can send runtime exception to inform us objects aren't valid
 * @author ndx
 *
 */
public interface Validable {
	/**
	 * Validate object to make sure it is internally consistent
	 */
	void validate();

}
