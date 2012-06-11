package com.dooapp.gaedo.tapestry;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.apache.tapestry5.ioc.Configuration;

import com.dooapp.gaedo.finders.FinderCrudService;
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
import com.dooapp.gaedo.properties.CachingPropertyProvider;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;

/**
 * The module providing everything necessary to use gaedo with taspestry IoC
 * mechanisms.
 * 
 * @author antoine
 * 
 */
public final class GaedoModule {

	private static final Logger log = Logger.getLogger(GaedoModule.class);

	public static PropertyProvider buildPropertyProvider() {
		return new CachingPropertyProvider(new FieldBackedPropertyProvider());
	}

	public static ServiceGenerator buildServiceGenerator(PropertyProvider provider) {
		return new ServiceGeneratorImpl(provider);
	}

	public static ServiceRepository build(
			Collection<FinderCrudService> configuration) {
		ServiceRepository serviceRepository = new SimpleServiceRepository();
		for (FinderCrudService finderCrudService : configuration) {
			log.info("adding service " + finderCrudService);
			serviceRepository.add(finderCrudService);
		}
		return serviceRepository;
	}

	public static FieldInformerLocator build(
			Collection<FieldInformerLocator> configuration,
			ServiceRepository serviceRepository) {
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		for (FieldInformerLocator fieldInformerLocator : configuration) {
			locator.add(fieldInformerLocator);
		}
		return locator;
	}

	public static void contributeFieldInformerLocator(
			Configuration<FieldInformerLocator> configuration,
			ServiceRepository serviceRepository) {
		configuration.add(new BasicFieldInformerLocator());
		configuration.add(new ServiceBackedFieldLocator(serviceRepository));
	}

	public static ProxyBackedInformerFactory build(FieldInformerLocator locator, PropertyProvider provider) {
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		return new ProxyBackedInformerFactory(reflectiveFactory);
	}
}
