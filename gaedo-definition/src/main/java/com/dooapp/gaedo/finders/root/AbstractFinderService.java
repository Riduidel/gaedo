package com.dooapp.gaedo.finders.root;

import com.dooapp.gaedo.finders.Finder;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryStatement;

/**
 * Base class for finder service providing the required finder abstractioncs
 *
 * @author ndx
 *
 * @param <T>
 */
public abstract class AbstractFinderService<DataType, InformerType extends Informer<DataType>> implements FinderCrudService<DataType, InformerType>{
	/**
	 * Class of contained beans, used for massive introspection
	 */
	protected final Class<DataType> containedClass;

	/**
	 * Class of the informer interface. Knowing this class will help us creating
	 * a proxy for the reflective code
	 */
	protected final Class<InformerType> informerClass;

	/**
	 * Proxy backed informer factory
	 */
	private InformerFactory informerFactory;

	public AbstractFinderService(Class<DataType> containedClass,
			Class<InformerType> informerClass,
			InformerFactory factory) {
		this.containedClass = containedClass;
		this.informerClass = informerClass;
		this.informerFactory = factory;
	}

	protected abstract QueryStatement<DataType, DataType, InformerType> createQueryStatement(
			QueryBuilder<? super InformerType> query);

	/**
	 * Default find implementation
	 *
	 * @return a {@link SimpleFinder}
	 */
	public Finder<DataType, InformerType> find() {
		return new SimpleFinder<DataType, InformerType>(this);
	}

	/**
	 * Creates the informer object for the used data type
	 *
	 * @return
	 */
	public InformerType getInformer() {
		return informerFactory.get(informerClass, containedClass);
	}

	public Class<DataType> getContainedClass() {
		return containedClass;
	}

	@Override
	public String toString() {
		StringBuilder sOut = new StringBuilder();
		sOut.append(getClass().getName());
		sOut.append("(containdClass:").append(getContainedClass().getName());
		sOut.append("; informerClass:").append(
				getInformer().toString()).append(")");
		return sOut.toString();
	}

	/**
	 * @return the informerFactory
	 * @category getter
	 * @category informerFactory
	 */
	protected InformerFactory getInformerFactory() {
		return informerFactory;
	}

	/**
	 * @param informerFactory the informerFactory to set
	 * @category setter
	 * @category informerFactory
	 */
	protected void setInformerFactory(InformerFactory informerFactory) {
		this.informerFactory = informerFactory;
	}
}
