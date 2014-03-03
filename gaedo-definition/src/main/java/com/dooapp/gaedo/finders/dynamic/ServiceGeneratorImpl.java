package com.dooapp.gaedo.finders.dynamic;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.utils.UnableToCreateInvocationHandlerException;

/**
 * Default implementation of service generator relying upon java proxies
 * @author ndx
 *
 */
public class ServiceGeneratorImpl implements ServiceGenerator {
	private static final Logger logger  = Logger.getLogger(ServiceGeneratorImpl.class.getName());

	/**
	 * Application-wide property provider
	 */
	protected final PropertyProvider provider;

	public ServiceGeneratorImpl(PropertyProvider provider) {
		this.provider = provider;
	}

	/**
	 * Creates a {@link DynamicFinderHandler} call handler to transform all calls to dynaamic interface into calls to effective back end
	 */
	@Override
	public <DataType, InformerType extends Informer<DataType>, Implementation extends DynamicFinder<DataType, InformerType>> Implementation generate(
			Class<Implementation> toImplement,
			FinderCrudService<DataType, InformerType> backEnd) {
		if (logger.isLoggable(Level.CONFIG)) {
			logger.config("creating a DynamicFinderHandler mapping "
					+ toImplement.getName() + " to "
					+ backEnd.getClass().getName());
		}
		try {
			Implementation proxy = (Implementation) Proxy.newProxyInstance(
					toImplement.getClassLoader(), new Class[] { toImplement },
					new DynamicFinderHandler(toImplement, backEnd, provider));
			return proxy;
		} catch(UnableToCreateInvocationHandlerException e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "Unable to create implementation of "+toImplement.getName()+" " +
						"using as back-end "+backEnd.getClass().getName()+
						"<"+backEnd.getContainedClass().getName()+", "+
						backEnd.getInformer().getClass().getName()+">");
			}
			throw e;
		}
	}

}
