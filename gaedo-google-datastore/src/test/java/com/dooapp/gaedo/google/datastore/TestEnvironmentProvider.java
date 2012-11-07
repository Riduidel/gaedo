package com.dooapp.gaedo.google.datastore;

import org.junit.Ignore;

import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.LazyInterfaceInformerLocator;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.dooapp.gaedo.test.beans.specific.Theme;
import com.dooapp.gaedo.test.beans.specific.ThemeInformer;

/**
 * Test provider for {@link ProxyBackedInformerFactory} (used before IoC is
 * introduced)
 * 
 * @author Nicolas
 * 
 */
@Deprecated
@Ignore
public class TestEnvironmentProvider {
	private static ProxyBackedInformerFactory instance;

	public static final ServiceRepository create() {
		ServiceRepository repository = new SimpleServiceRepository();
		repository.getSupport().addPropertyChangeListener(new QueryQuotaMonitorer());
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		locator.add(new LazyInterfaceInformerLocator());
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		ProxyBackedInformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);

		// Now, add real services
		repository.add(new DatastoreFinderServiceImpl<Post, PostInformer>(
				Post.class, PostInformer.class, proxyInformerFactory,
				repository, provider));
		repository.add(new DatastoreFinderServiceImpl<User, UserInformer>(
				User.class, UserInformer.class, proxyInformerFactory,
				repository, provider));
		repository.add(new DatastoreFinderServiceImpl<Tag, TagInformer>(
				Tag.class, TagInformer.class, proxyInformerFactory,
				repository, provider));
		repository.add(new DatastoreFinderServiceImpl<Theme, ThemeInformer>(
						Theme.class, ThemeInformer.class, proxyInformerFactory,
						repository, provider));

		repository.add(new DatastoreFinderServiceImpl<MigrableObject, MigrableObjectInformer>(
				MigrableObject.class, MigrableObjectInformer.class, proxyInformerFactory,
				repository, provider));
		return repository;
	}
}
