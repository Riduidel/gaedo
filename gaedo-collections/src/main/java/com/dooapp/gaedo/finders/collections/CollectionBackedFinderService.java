package com.dooapp.gaedo.finders.collections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.dooapp.gaedo.CrudServiceException;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.root.AbstractFinderService;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;
import com.dooapp.gaedo.utils.PropertyChangeEmitterImpl;

/**
 * Base class for all class implementing {@link FinderCrudService} as
 * {@link Collection} data elements
 *
 * @author ndx
 *
 * @param <DataType>
 * @param <InformerType>
 */
public class CollectionBackedFinderService<DataType, InformerType extends Informer<DataType>>
		extends AbstractFinderService<DataType, InformerType> implements
		PropertyChangeEmitter {
	public static class NoSuchDataException extends CrudServiceException {

		private final Object unknownData;

		public Object getUnknownData() {
			return unknownData;
		}

		public NoSuchDataException(Object toUpdate) {
			super("data " + toUpdate.toString()
					+ " does not exists in this service");
			this.unknownData = toUpdate;
		}

	}

	private final Collection<DataType> data = new LinkedList<DataType>();

	private final PropertyChangeEmitter support = new PropertyChangeEmitterImpl();

	/**
	 * Build a collection backed finder
	 *
	 * @param containedClass
	 * @param informerClass
	 * @see AbstractFinderService
	 */
	public CollectionBackedFinderService(
			Class<DataType> containedClass, Class<InformerType> informerClass,
			InformerFactory factory) {
		super(containedClass, informerClass, factory);
	}

	public DataType create(DataType toCreate) {
		data.add(toCreate);
		return toCreate;
	}

	public void delete(DataType toDelete) {
		data.remove(toDelete);
	}

	public DataType update(DataType toUpdate) {
		if (!data.contains(toUpdate)) {
			throw new NoSuchDataException(toUpdate);
		}
		return toUpdate;
	}

	/**
	 * Creates the query statement used a copy of current data. As a
	 * consequence, long calls may result in inaccurate data set.
	 */
	@Override
	protected QueryStatement<DataType, DataType, InformerType> createQueryStatement(
			QueryBuilder<InformerType> query) {
		return new CollectionQueryStatement<DataType, DataType, InformerType>(query,
				getInformer(), new LinkedList<DataType>(data), support);
	}

	/**
	 * @return {@link Collections#unmodifiableCollection(Collection)} called
	 *         upon {@link #data}
	 */
	public Iterable<DataType> findAll() {
		return Collections.unmodifiableCollection(data);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		support.addPropertyChangeListener(propertyName, listener);
	}

	public void firePropertyChange(PropertyChangeEvent evt) {
		support.firePropertyChange(evt);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		support.removePropertyChangeListener(propertyName, listener);
	}
}
