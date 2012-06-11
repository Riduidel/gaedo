package com.dooapp.gaedo.tag;

import java.util.Arrays;

import org.junit.Ignore;

import com.dooapp.gaedo.finders.collections.CollectionBackedFinderService;
import com.dooapp.gaedo.finders.collections.IdSupportingCollectionBackedFinderService;
import com.dooapp.gaedo.finders.id.AnnotationUtils;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;

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
	private static InformerFactory instance;

	public static final ServiceRepository create() {
		ServiceRepository repository = new SimpleServiceRepository();
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		InformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);

		// Now add some services
		repository.add(new IdSupportingCollectionBackedFinderService(Tag.class, TagInformer.class,
				proxyInformerFactory, Arrays.asList(AnnotationUtils.locateIdField(provider, Tag.class, Long.TYPE, Long.class))));
		repository.add(new CollectionBackedFinderService(Post.class, PostInformer.class,
						proxyInformerFactory));

		return repository;
	}
}
