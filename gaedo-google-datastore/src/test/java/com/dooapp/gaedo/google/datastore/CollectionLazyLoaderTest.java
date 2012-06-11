package com.dooapp.gaedo.google.datastore;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;

public class CollectionLazyLoaderTest {
	private ServiceRepository repository;

	@Before
	public void setUp() {
		AbstractDataStoreTest.setUp();
		repository = TestEnvironmentProvider.create();
	}

	@After
	public void tearDown() {
		AbstractDataStoreTest.tearDown();
	}

	@Test(expected=ImpossibleToSaveObjectWithNullParentException.class)
	public void testWithNoParentUser(){
		FinderCrudService<Post, Informer<Post>> service = repository
		.get(Post.class);
		Post p = new Post();
		p.text="TEXT";
		Assert.assertEquals(0, p.tags.size());
		p=service.create(p);
		
	}

	@Test
	public void test(){
		User user = new User().withLogin("toto").withPassword("dudu");
		FinderCrudService<Post, Informer<Post>> service = repository
		.get(Post.class);
		Post p = new Post();
		p.author = user;
		p.text="TEXT";
		Assert.assertEquals(0, p.tags.size());
		p=service.create(p);
		Assert.assertEquals(0, p.tags.size());
		p = service.findAll().iterator().next();
		Assert.assertEquals(0, p.tags.size());
		Tag t = new Tag();
		FinderCrudService<Tag, TagInformer> tagService = repository.get(Tag.class);
		t=tagService.create(t);
		p.tags.add(t);
		Assert.assertEquals(1, p.tags.size());
		p=service.update(p);
		Assert.assertEquals(1, p.tags.size());
		
	}
}
