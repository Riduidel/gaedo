package com.dooapp.gaedo.rest.server;

import org.junit.Ignore;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.collections.DefaultCollectionBackedFinderService;
import com.dooapp.gaedo.finders.dynamic.ServiceGenerator;
import com.dooapp.gaedo.finders.dynamic.ServiceGeneratorImpl;
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
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagFinder;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.dooapp.gaedo.test.beans.specific.Theme;
import com.dooapp.gaedo.test.beans.specific.ThemeInformer;

@Ignore
public class TestEnvironmentProvider {

	private static ProxyBackedInformerFactory instance;

	public static final ServiceRepository create() {
		ServiceRepository repository = new SimpleServiceRepository();
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		locator.add(new LazyInterfaceInformerLocator());
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(locator, provider);
		ProxyBackedInformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(reflectiveFactory);

		// Now, add real services
		repository.add(DefaultCollectionBackedFinderService.create(Post.class, PostInformer.class, proxyInformerFactory));
		repository.add(DefaultCollectionBackedFinderService.create(User.class, UserInformer.class, proxyInformerFactory));
		repository.add(DefaultCollectionBackedFinderService.create(Theme.class, ThemeInformer.class, proxyInformerFactory));
		FinderCrudService<Tag, TagInformer> tagService = DefaultCollectionBackedFinderService.create(Tag.class, TagInformer.class, proxyInformerFactory);
		repository.add(tagService);
		// TODO separate that layer !
		ServiceGenerator generator = new ServiceGeneratorImpl(provider);
		repository.add(generator.generate(TagFinder.class, tagService));

		// And put some data in services

		User first = repository.get(User.class).create(new User().withId(1).withLogin("first").withPassword("paaaaswrd"));
		User second = repository.get(User.class).create(new User().withId(2).withLogin("second").withPassword("pswooord"));
		Post firstPost = repository.get(Post.class).create(new Post(1, "first post", 2, State.PUBLIC, first));
		first.posts.add(firstPost);
		Tag t = repository.get(Tag.class).create(new Tag("first"));
		firstPost.tags.add(t);

		return repository;
	}
}
