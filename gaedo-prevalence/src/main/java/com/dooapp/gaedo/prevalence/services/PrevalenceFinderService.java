package com.dooapp.gaedo.prevalence.services;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.HashSet;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.collections.CollectionQueryStatement;
import com.dooapp.gaedo.finders.root.AbstractFinderService;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.prevalence.space.ExecutionSpace;
import com.dooapp.gaedo.prevalence.space.commands.Contains;
import com.dooapp.gaedo.prevalence.space.commands.Create;
import com.dooapp.gaedo.prevalence.space.commands.IterateOverCollection;
import com.dooapp.gaedo.prevalence.space.commands.PutInCollection;
import com.dooapp.gaedo.prevalence.space.commands.RemoveFromCollection;
import com.dooapp.gaedo.prevalence.space.commands.UpdateInCollection;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;
import com.dooapp.gaedo.utils.PropertyChangeEmitterImpl;

/**
 * A finder service backed by a prevalence layer
 * 
 * @author ndx
 * 
 * @param <DataType> notice that, as an added constraint to normal ones, here DataType MUST implement {@link Serializable}
 * @param <InformerType>
 */
public class PrevalenceFinderService<DataType extends Serializable, InformerType extends Informer<DataType>>
		extends AbstractFinderService<DataType, InformerType> implements
		FinderCrudService<DataType, InformerType> {

	/**
	 * Execution space used to store data
	 */
	private final ExecutionSpace<String> executionSpace;

	private final PropertyChangeEmitter support = new PropertyChangeEmitterImpl();

	public PrevalenceFinderService(Class<DataType> containedClass,
			Class<InformerType> informerClass,
			ProxyBackedInformerFactory factory,
			ExecutionSpace<String> executionSpace) {
		super(containedClass, informerClass, factory);
		// At construction, if none exists, we create a collection linked to class name
		this.executionSpace = executionSpace;
		if(!executionSpace.execute(new Contains<String>(getStorageName()))) {
			executionSpace.execute(new Create<String>(getStorageName(), createDataCollection()));
		}
	}

	/**
	 * Get name for storage container
	 * @return
	 */
	private String getStorageName() {
		return getContainedClass().getName();
	}

	/**
	 * Create data container (which fortunatly is a collection)
	 * @return a {@link HashSet} as it provides good unicity coercion
	 */
	private Serializable createDataCollection() {
		return new HashSet<DataType>();
	}

	@Override
	public DataType create(DataType toCreate) {
		executionSpace.execute(new PutInCollection<String>(getStorageName(), toCreate));
		return toCreate;
	}

	@Override
	public void delete(DataType toDelete) {
		executionSpace.execute(new RemoveFromCollection<String>(getStorageName(), toDelete));
	}

	@Override
	public DataType update(DataType toUpdate) {
		return executionSpace.execute(new UpdateInCollection<DataType, String>(getStorageName(), toUpdate));
	}

	@Override
	public Iterable<DataType> findAll() {
		return executionSpace.execute(new IterateOverCollection<DataType, String>(getStorageName()));
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

	@Override
	protected QueryStatement<DataType, InformerType> createQueryStatement(
			QueryBuilder<InformerType> query) {
		return new CollectionQueryStatement<DataType, InformerType>(query, getInformer(), findAll(), support);
	}
}
