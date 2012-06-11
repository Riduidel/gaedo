package com.dooapp.gaedo.guice;

import com.dooapp.gaedo.finders.dynamic.ServiceGenerator;
import com.dooapp.gaedo.finders.dynamic.ServiceGeneratorImpl;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.FieldInformerLocator;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.CachingPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * Simple module providing all required configuration.
 * Using this module, one only have to bind its service finder interfaces to
 * their implementation.
 * As an example, suppose one have a user and userInformer in its application, the following code can be written with ease (provided one one uses gae)
 * <pre>
 * @Provides
 * public FinderCrudService<User, UserInformer> getUserService(ServiceRepository repository) {
 * 		// Yup, this part is not optimal
 * 		FinderCrudService returned = new DatastoreFinderServiceImpl<Post, PostInformer>(
				Post.class, PostInformer.class, proxyInformerFactory,
				repository);
		repository.add(returned);
		return returned;
 * }
 * 
 * @Provides
 * public DynamicUserService getUserService(ServiceGenerator generator, FinderCrudService<User, UserInformer> backend) {
 * 		return generator.generate(DynamicUserService.class, backend);
 * } 
 * </pre>
 * 
 * @author ndx
 *
 */
public class GaedoModule extends AbstractModule {

	/**
	 * No code in this method, since all is done through provider methods
	 */
	@Override
	protected void configure() {
		
	}
	/**
	 * Creates the property provider implementation
	 * @return
	 */
	@Provides
	public PropertyProvider getPropertyProvider() {
		return new CachingPropertyProvider(new FieldBackedPropertyProvider());
	}
	
	/**
	 * Creates the service generator implementation, used for dynamic finders
	 * @return
	 */
	@Provides
	public ServiceGenerator getServiceGenerator(PropertyProvider provider) {
		return new ServiceGeneratorImpl(provider);
	}
	
	/**
	 * Creates the service repository implementation, used for various purpose
	 * @return
	 */
	@Provides
	public ServiceRepository getServiceRepository() {
		return new SimpleServiceRepository();
	}
	
	/**
	 * get field locator, used for proxy factory
	 * @param repository service repository, used to locate services
	 * @return a {@link CumulativeFieldInformerLocator}
	 */
	@Provides
	public FieldInformerLocator getFieldLocator(ServiceRepository repository) {
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		return locator;
	}
	
	/**
	 * Directly provides the {@link ProxyBackedInformerFactory}. Notice that, although it uses
	 * a {@link ReflectionBackedInformerFactory}, this one is not made visible through guice, since
	 * it more and more appears to be an internal object
	 * @param locator
	 * @return
	 */
	@Provides
	public ProxyBackedInformerFactory getInformerFactory(FieldInformerLocator locator, PropertyProvider provider) {
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		ProxyBackedInformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);
		return proxyInformerFactory;
	}
}
