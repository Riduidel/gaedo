package com.dooapp.gaedo.blueprints;

import static com.dooapp.gaedo.blueprints.TestUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.CascadeType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.pgm.Vertex;

@RunWith(Parameterized.class)
public class OrderedCollectionTest extends AbstractGraphTest {

	private User author;
	private List<Post> posts = new LinkedList<Post>();

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public OrderedCollectionTest(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}
	
	@Before
	public void loadService() throws Exception {
		super.loadService();
		
		// Create some test data
		author = new User().withId(1).withLogin(USER_LOGIN).withPassword(USER_PASSWORD);
		author.about = new Post(ABOUT_ID, "a message about teh awfor", 5000, State.PUBLIC, author);
		author = getUserService().create(author);
		
		FinderCrudService<Post, PostInformer> postService = getPostService();
		posts.add(postService.create(new Post(1, "post text for 1", 1, State.PUBLIC, author)));
		posts.add(postService.create(new Post(2, "post text for 2", 2, State.PUBLIC, author)));
		posts.add(postService.create(new Post(3, "post text for 3", 3, State.PUBLIC, author)));
		posts.add(postService.create(new Post(4, "this is da forf", 4, State.PUBLIC, author)));
		posts.add(postService.create(new Post(69, "dis be da fif", 13513, State.PUBLIC, author)));
		posts.add(postService.create(new Post(800046, "post six, baby", -123, State.PUBLIC, author)));
	}

	
	@Test
	public void makeSureListOrderIsPreserved() {
		// Give the author some posts
		for(int i=posts.size()-1; i>=0; i--) {
			author.posts.add(posts.get(i));
		}
		getUserService().update(author);
		
		// Now, get a copy of the author from the DB...
		User user = getACopyOfTheAuthor();
		
		// ...and make sure that the posts are in the correct order
		assertThat("Make sure we got the right number of posts", user.posts.size(), is(6));
		assertThat("First post is in correct position", user.posts.get(0), is(posts.get(5)));
		assertThat("Second post is in correct position", user.posts.get(1), is(posts.get(4)));
		assertThat("Third post is in correct position", user.posts.get(2), is(posts.get(3)));
		assertThat("Fourth post is in correct position", user.posts.get(3), is(posts.get(2)));
		assertThat("Fifth post is in correct position", user.posts.get(4), is(posts.get(1)));
		assertThat("Sixth post is in correct position", user.posts.get(5), is(posts.get(0)));
	}
	
	@Test
	public void checkListOrderWithModifications() {
		// Add the posts, then remove some
		for(int i=posts.size()-1; i>=0; i--) {
			author.posts.add(posts.get(i));
		}
		getUserService().update(author);
		
		// order is now: 5 4 3 2 1 0
		author.posts.remove(3);
		// order is now: 5 4 3 1 0
		author.posts.remove(1);
		// order is now: 5 3 1 0
		getUserService().update(author);
		
		// Now, get a copy of the author from the DB...
		User user = getACopyOfTheAuthor();
		
		// ...and make sure that the posts are in the correct order
		assertThat("Make sure we got the right number of posts", user.posts.size(), is(posts.size() - 2));
		assertThat("First post is in correct position", user.posts.get(0), is(posts.get(5)));
		assertThat("Second post is in correct position", user.posts.get(1), is(posts.get(3)));
		assertThat("Third post is in correct position", user.posts.get(2), is(posts.get(1)));
		assertThat("fourth post is in correct position", user.posts.get(3), is(posts.get(0)));
	}
	
	private User getACopyOfTheAuthor() {
		User user = getUserService().find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer informer) {
				return informer.getId().equalsTo(author.id);
			}
			
		}).getFirst();
		
		return user;
	}
	
	@After
	public void deleteAllAuthorsPosts() {
		User u = getACopyOfTheAuthor();
		u.posts.clear();
		getUserService().update(u);
	}
	
	
	@Test
	public void checkWhatHappensWhenNotAllTheEdgesHaveACollectionIndex() {
		// Give the author some posts...
		author.posts.add(posts.get(0));
		author.posts.add(posts.get(1));
		author.posts.add(posts.get(2));
		getUserService().update(author);
		
		// Hack the DB to remove order information
		@SuppressWarnings("unchecked")
		AbstractBluePrintsBackedFinderService<?, User, ?> userService =
			(AbstractBluePrintsBackedFinderService<?, User, ?>) getUserService();
		Vertex authorVertex = userService.getVertexFor(author, CascadeType.REFRESH, new TreeMap<String, Object>());
		
		if(environment.getGraph() instanceof TransactionalGraph)
			((TransactionalGraph) environment.getGraph()).startTransaction();
		
		// This String should be the same thing as what GraphUtils.getDefaultEdgeNameFor
		// would return if we had the Property object
		Iterable<Edge> postEdges = authorVertex.getOutEdges("com.dooapp.gaedo.test.beans.User:posts");
		for(Edge e : postEdges) {
			e.removeProperty(Properties.collection_index.name());
		}
		
		if(environment.getGraph() instanceof TransactionalGraph)
			((TransactionalGraph) environment.getGraph()).stopTransaction(Conclusion.SUCCESS);
		
		// Now, add some more posts and make sure that it doesn't bomb.
		author.posts.add(posts.get(3));
		author.posts.add(posts.get(4));
		author.posts.add(posts.get(5));
		getUserService().update(author);
		
		User user = getACopyOfTheAuthor();
		assertThat("Make sure we got the right number of posts", user.posts.size(), is(6));
		
		// We don't really care where the first 3 posts are, as long as they're at the front.
		Set<Post> first3Posts = new HashSet<Post>();
		first3Posts.add(posts.get(0));
		first3Posts.add(posts.get(1));
		first3Posts.add(posts.get(2));
		for(int i=0; i<3; i++)
			assertTrue("Post " + i + " is at the front of the list", first3Posts.contains(user.posts.get(i)));
		
		// The rest of the posts should be in order.
		assertThat("Fourth post is in correct position", user.posts.get(3), is(posts.get(3)));
		assertThat("Fifth post is in correct position", user.posts.get(4), is(posts.get(4)));
		assertThat("Sixth post is in correct position", user.posts.get(5), is(posts.get(5)));
	}
}
