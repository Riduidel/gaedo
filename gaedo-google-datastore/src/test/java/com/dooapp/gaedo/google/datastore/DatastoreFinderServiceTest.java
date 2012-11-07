package com.dooapp.gaedo.google.datastore;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.hamcrest.core.AllOf;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.IsSame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.matchers.IsCollectionContaining;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.sort.SortingExpressionImpl;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;

public class DatastoreFinderServiceTest {
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

	/**
	 * Testing that basic fields of a Post are correctly written
	 */
	@Test
	public void soloTest() {
		User user = new User().withLogin("toto").withPassword("dudu");
		FinderCrudService<Post, Informer<Post>> service = repository
				.get(Post.class);
		Post b = new Post();
		b.author = user;
		b.text = "a";
		b.note = (float) 2.1;
		b = service.create(b);
		Assert.assertThat(b.id, IsNot.not(0l));
		b.text = "b";
		b = service.update(b);
		Assert.assertThat(b.text, Is.is("b"));
		Assert.assertThat(b.note, Is.is(2.1f));
		service.delete(b);
	}

	/**
	 * Test that creating a post with a user give both of them an id
	 */
	@Test
	public void postWithUserTest() {
		FinderCrudService<Post, Informer<Post>> service = repository
				.get(Post.class);
		Post p = new Post();
		User u = new User();
		u.setLogin("toto");
		p.text = "Some text";
		p.author = u;
		p = service.create(p);
		Assert.assertThat(p.id, IsNot.not(0l));
		Assert.assertThat(p.author.id, IsNot.not(0l));
		p.text = "no, change that text !";
		p = service.update(p);
		Assert.assertThat(p.text, Is.is("no, change that text !"));
		service.delete(p);
		// repository.get(User.class)
	}

	/**
	 * Full test for a user with three associated posts
	 * 
	 * @throws IllegalAccessException
	 */
	@Test
	public void userWithPosts() throws IllegalAccessException {
		Post a = new Post();
		Post b = new Post();
		b.annotations.put("c", "d");
		User u = new User();
		u.setLogin("toto");
		a.text = "A";
		a.author = u;
		b.text = "B";
		b.author = u;
		u.posts.add(a);
		u.posts.add(b);
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		DatastoreFinderService<User, UserInformer> userService2 = (DatastoreFinderService<User, UserInformer>) userService;
		u = userService.create(u);
		Assert.assertThat(u.getLogin(), Is.is("toto"));
		u.setLogin("tata");
		// Due to special optimization, returned u is the input one.
		// To have a different one, go down in the death valley
		User result = userService.update(u);
		Assert.assertThat(result, IsSame.sameInstance(u));
		// Deep diving in internals of service (not very respectfull, but damn
		// efficient !
		User real = userService2.find().matching(
				new QueryBuilder<UserInformer>() {

					@Override
					public QueryExpression createMatchingExpression(
							UserInformer object) {
						return object.getLogin().equalsTo("tata");
					}
				}).getFirst();
		Assert.assertThat(real.getLogin(), Is.is("tata"));
		Assert.assertThat(real.posts.size(), Is.is(2));
		Assert.assertThat(real.posts, IsCollectionContaining.hasItem(a));
		Assert.assertThat(real.posts, IsCollectionContaining.hasItem(b));
		Iterator<Post> iterator = real.posts.iterator();
		Assert.assertThat(iterator.next(), Is.is(a));
		Post otherB = iterator.next();
		Assert.assertThat(otherB, Is.is(b));
		Assert.assertThat(otherB.annotations.size(), Is.is(1));
		Assert.assertThat(otherB.annotations.containsKey("c"), Is.is(true));
		Assert.assertThat(otherB.annotations.get("c"), Is.is("d"));
		Post c = new Post();
		c.text = "c";
		c.author = u;
		u.posts.add(c);
		userService.update(u);
		real = userService2.find().matching(new QueryBuilder<UserInformer>() {
			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().equalsTo("tata");
			}
		}).getFirst();
		Assert.assertThat(real.posts.size(), Is.is(3));
		Assert.assertThat(real.posts, IsCollectionContaining.hasItem(c));
		userService.delete(u);
		FinderCrudService<Post, PostInformer> postService = repository
				.get(Post.class);
		Post p = postService.find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer object) {
				return object.getText().equalsTo("A");
			}
		}).getFirst();
		Assert.assertThat(p, Is.is(a));

		// Finally, get all results
		Iterable<Post> posts = postService.findAll();
		Assert.assertThat(posts, IsNull.notNullValue());
	}

	/**
	 * Test user with a null psots collection
	 */
	@Test
	public void testUserWithNullPosts() {
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		User u = new User();
		u.setLogin("toto");
		u.posts = null;
		// Due to implementation optimization, returned u is the input one
		u = userService.create(u);
		Assert.assertThat(u, IsNull.notNullValue());
		// As a consequence, posts is null
		Assert.assertThat(u.posts, IsNull.nullValue());
		// But if we get it back again, collection won't be null
		u = userService.find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.getLogin().equalsTo("toto");
			}
		}).getFirst();
		Assert.assertThat(u, IsNull.notNullValue());
		// This time posts is not null, but an empty collection
		Assert.assertThat(u.posts, IsNull.notNullValue());
		Assert.assertThat(u.posts.size(), Is.is(0));
	}

	/**
	 * Test that, when querying an object on its id, it works (in this case, we
	 * will make use of so-called id resolution, done by service implementation)
	 */
	@Test
	public void testQueryOnIdResolution() {
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		User u = new User();
		u.setLogin("toto");
		final User grabbed = userService.create(u);
		User o = userService.find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.get("id").equalsTo(grabbed.id);
			}
		}).getFirst();
		Assert.assertThat(o, IsNull.notNullValue());

	}

	/**
	 * Test that getting a User when service has no one throws an exception
	 * (NoReturnableEntity).
	 */
	@Test(expected = NoReturnableEntity.class)
	public void testWithNoUser() {
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		User o = userService.find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer object) {
				return object.get("login").equalsTo("toto");
			}
		}).getFirst();
		// This code will never be reached, since an exception must have been
		// thrown before
		Assert.assertThat(o, IsNull.notNullValue());
	}

	/**
	 * Full test for a user with three associated posts. Test created to ensure
	 * bug #23 is correctly corrected
	 * 
	 * @throws IllegalAccessException
	 */
	@Test
	public void postByUser() throws IllegalAccessException {
		Post a = new Post();
		Post b = new Post();
		b.annotations.put("c", "d");
		User u = new User();
		u.setLogin("toto");
		a.text = "A";
		a.author = u;
		b.text = "B";
		b.author = u;
		u.posts.add(a);
		u.posts.add(b);
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		final User saved = userService.create(u);
		// Now, everything should be saved
		FinderCrudService<Post, PostInformer> postService = repository
				.get(Post.class);
		Collection<Post> all = new LinkedList<Post>();
		Iterable<Post> iterable = postService.findAll();
		for (Post data : iterable) {
			all.add(data);
		}
		Assert.assertThat(all.size(), Is.is(2));
		Iterable<Post> matching = postService.find().matching(
				new QueryBuilder<PostInformer>() {

					@Override
					public QueryExpression createMatchingExpression(
							PostInformer object) {
						return object.getAuthor().equalsTo(saved);
					}
				}).getAll();
		Collection<Post> result = new LinkedList<Post>();
		for (Post post : matching) {
			result.add(post);
		}
		Assert.assertThat(result.size(), Is.is(2));
		Assert.assertThat(result, AllOf.allOf(
				IsCollectionContaining.hasItem(a), IsCollectionContaining
						.hasItem(b)));
	}

	/**
	 * Full test for a user with three associated posts. Test created to ensure
	 * bug #23 is correctly corrected
	 * 
	 * @throws IllegalAccessException
	 */
	@Test
	public void postSortedByText() throws IllegalAccessException {
		Post a = new Post();
		Post b = new Post();
		b.annotations.put("c", "d");
		User u = new User();
		u.setLogin("toto");
		a.text = "AAAAB";
		a.author = u;
		b.text = "AAAAA";
		b.author = u;
		u.posts.add(a);
		u.posts.add(b);
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		final User saved = userService.create(u);
		// Now, everything should be saved
		FinderCrudService<Post, PostInformer> postService = repository
				.get(Post.class);
		Iterable<Post> posts = postService.find().matching(
				new QueryBuilder<PostInformer>() {

					@Override
					public QueryExpression createMatchingExpression(
							PostInformer informer) {
						// This query returns both posts
						return informer.getText().startsWith("A");
					}
				}).sortBy(new SortingBuilder<PostInformer>() {

			@Override
			public SortingExpression createSortingExpression(
					PostInformer informer) {
				return new SortingExpressionImpl().add(informer.getText(),
						SortingExpression.Direction.Ascending);
			}
		}).getAll();
		Iterator<Post> p = posts.iterator();
		Assert.assertThat(p.next(), Is.is(b));
		Assert.assertThat(p.next(), Is.is(a));
		Assert.assertThat(p.hasNext(), Is.is(false));
	}

	/**
	 * Test how to have working queries in collections
	 */
	@Test
	public void queryOnCollection() throws IllegalAccessException {
		final Post a = new Post();
		Post b = new Post();
		b.annotations.put("c", "d");
		User u = new User();
		u.setLogin("toto");
		a.text = "A";
		a.author = u;
		b.text = "B";
		b.author = u;
		u.posts.add(a);
		u.posts.add(b);
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		final User saved = userService.create(u);
		User found = userService.find().matching(
				new QueryBuilder<UserInformer>() {

					@Override
					public QueryExpression createMatchingExpression(
							UserInformer informer) {
						return informer.getPosts().containing(a);
					}
				}).getFirst();
		Assert.assertThat(found, IsNull.notNullValue());
		Assert.assertThat(found.id, Is.is(saved.id));
	}

	/**
	 * Test how to have working queries in collections
	 */
	@Test @Ignore /* seems to only work on real GAE server */
	public void cascadeDeletion() throws IllegalAccessException {
		FinderCrudService<User, UserInformer> userService = repository
				.get(User.class);
		FinderCrudService<Post, PostInformer> postService = repository
				.get(Post.class);

		User first = new User().withLogin("first").withPassword("dudu");
		final Post a = new Post().withAnnotation("c", "d").withText("A")
				.withAuthor(first);
		Post b = new Post().withText("B").withAuthor(first);
		first.addPosts(a, b);

		User second = new User().withLogin("second")
				.withPassword("another one");
		second.addPosts(new Post().withText("C").withAuthor(second), new Post()
				.withText("D").withAuthor(second), new Post().withText("E")
				.withAuthor(second));

		final User savedFirst = userService.create(first);
		final User savedSecond = userService.create(second);
		Assert.assertThat(postService.find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(
					PostInformer informer) {
				return informer.getText().isAnything();
			}
		}).count(), Is.is(5));
		userService.delete(savedFirst);
		
		
		Assert.assertThat(userService.find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(
					UserInformer informer) {
				return informer.getLogin().isAnything();
			}
		}).count(), Is.is(1));
		
		Assert.assertThat(postService.find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(
					PostInformer informer) {
				return informer.getText().isAnything();
			}
		}).count(), Is.is(3));
	}
}
