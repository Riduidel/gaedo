package com.dooapp.gaedo;

/**
 * Base for various implementation of crudity providers, it defines the common
 * behaviours.
 * 
 * @author ndx
 * 
 * @param <DataType>
 */
public interface AbstractCrudService<DataType> {
	/**
	 * Create a new object of the given type and returns it
	 * 
	 * @param toCreate
	 *            object to save
	 * @return returned object
	 */
	public DataType create(DataType toCreate);

	/**
	 * Delete an object of the given type
	 * 
	 * @param toDelete
	 *            object to delete
	 */
	public void delete(DataType toDelete);

	/**
	 * Update an object of the given type
	 * 
	 * @param toUpdate
	 *            object to update
	 * @return updated object
	 */
	public DataType update(DataType toUpdate);

}
