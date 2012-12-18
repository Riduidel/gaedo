package com.dooapp.gaedo.blueprints;

import static com.dooapp.gaedo.blueprints.TestUtils.*;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.fortytwo.sesametools.nquads.NQuadsWriter;

import static org.hamcrest.CoreMatchers.*;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import com.dooapp.gaedo.blueprints.finders.FindFirstUserByLogin;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.blueprints.finders.FindPostByText;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.SortingBuilder;
import com.dooapp.gaedo.finders.SortingExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Index;
import com.tinkerpop.blueprints.pgm.IndexableGraph;
import com.tinkerpop.blueprints.pgm.TransactionalGraph;

@RunWith(Parameterized.class)
public class GraphPostFinderServiceTest extends AbstractGraphTest {
	private static final Logger logger = Logger.getLogger(GraphPostFinderServiceTest.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}
	private User author;
	private Post post1;
	private Post post2;
	private Post post3;
	private Tag tag1;
	
	public GraphPostFinderServiceTest(AbstractGraphEnvironment<?> graph) {
		super(graph);
	}

	@Before
	public void loadService() throws Exception {
		super.loadService();
		// Now add some services

		// create some objects
		author = new User().withId(1).withLogin(USER_LOGIN).withPassword(USER_PASSWORD);
		author.about = new Post(ABOUT_ID, "a message about that user", 5, State.PUBLIC, author);
		author = getUserService().create(author);
		post1 = getPostService().create(new Post(ID_POST_1, "post text for 1", 1, State.PUBLIC, author, theseMappings("a", "b")));
		post2 = getPostService().create(new Post(2, "post text for 2", 2, State.PUBLIC, author));
		post3 = getPostService().create(new Post(3, "post text for 3", 3, State.PUBLIC, author));
		tag1 = getTagService().create(new Tag(1, TAG_TEXT));
		author.posts.add(post1);
		author.posts.add(post2);
		author.posts.add(post3);
		author = getUserService().update(author);
	}
	
	@Test
	public void makeSureListOrderIsPreserved() {
		User user = getUserService().find().matching(new QueryBuilder<UserInformer>() {

			@Override
			public QueryExpression createMatchingExpression(UserInformer informer) {
				return informer.getId().equalsTo(1);
			}
			
		}).getFirst();
		
		assertThat("Make sure we got the right number of posts", user.posts.size(), is(3));
		assertThat("First post is in correct position", user.posts.get(0), is(post1));
		assertThat("Second post is in correct position", user.posts.get(1), is(post2));
		assertThat("Third post is in correct position", user.posts.get(2), is(post3));
	}
	
	private Map<String, String> theseMappings(String...strings) {
		Map<String, String> returned = new TreeMap<String, String>();
		for (int i = 0; i < strings.length; i++) {
			if(i+1<strings.length) {
				returned.put(strings[i++], strings[i]);
			}
		}
		return returned;
	}

	@Test 
	public void searchPostByNote() {
		Post noted2 = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getNote().equalsTo(2.0);
			}
		}).getFirst();
		
		assertThat(noted2.id, Is.is(post2.id));
	}

	@Test
	public void searchPostByAuthorLogin() {
		int postsOf  = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().getLogin().equalsTo(USER_LOGIN);
			}
		}).count();
		// All posts are from the same author
		// notice there are the 3 main posts, AND the about page (which is a post)
		assertThat(postsOf, Is.is(4));
	}

	/**
	 * Test for http://gaedo.origo.ethz.ch/issues/55
	 */
	@Test
	public void searchPostByAuthorObject() {
		int postsOf  = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().equalsTo(author);
			}
		}).count();
		// All posts are from the same author
		// notice there are the 3 main posts, AND the about page (which is a post)
		assertThat(postsOf, Is.is(4));
	}

	@Test
	public void updateAboutWithAuthor() {
		author.setLogin("new login");
		// here we modify about and let author update it for us
		String aboutText = "this is now user "+author.getLogin();
		((Post) author.about).text = aboutText;
		getUserService().update(author);
		Post about = ((IdBasedService<Post>) getPostService()).findById(ABOUT_ID);
		assertThat(about, IsNull.notNullValue());
		assertThat(about.text, Is.is(aboutText));
	}

	@Test
	public void ensureAuthorHasWrittenThosePosts() {
		assertThat(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().equalsTo(author);
			}
		}).count(), IsNot.not(0));
	}

	@Test
	public void ensureDeletCascadesWell() {
		User other = new User().withId(2).withLogin("other login").withPassword("other password");
		long id = 55;
		other.about = new Post(id, "a post about another user", 2, State.PUBLIC, other);
		getUserService().create(other);
		Post about = ((IdBasedService<Post>) getPostService()).findById(id);
		assertThat(about, IsNull.notNullValue());
		getUserService().delete(other);
		try {
			if (environment.graph instanceof TransactionalGraph) {
				TransactionalGraph transactionalGraph = (TransactionalGraph) environment.graph;
				transactionalGraph.startTransaction();
			}
			about = ((IdBasedService<Post>) getPostService()).findById(id);
			assertThat(about, IsNull.nullValue());
		} catch(NoReturnableVertexException e) {
			// that exception is one of possible correct behaviours
		}
	}

	@Test
	public void ensureUpdateCascadesWell() {
		User other = new User().withId(2).withLogin("other login").withPassword("other password");
		long id = 55;
		String POST_TEXT = "a post about another user";
		other.about = new Post(id, POST_TEXT, 2, State.PUBLIC, other);
		getUserService().create(other);
		Post about = ((IdBasedService<Post>) getPostService()).findById(id);
		assertThat(about, IsNull.notNullValue());
		assertThat(about.text, Is.is(POST_TEXT));
		about.text += POST_TEXT;
		getUserService().update(other);
		about = ((IdBasedService<Post>) getPostService()).findById(id);
		assertThat(about, IsNull.notNullValue());
		// we didn't change author's about Post, but the one we fetched separatly
		assertThat(about.text, Is.is(POST_TEXT));
		// Now we update the GOOD about message (yeah these cascade things are a nightmare)
		((Post) other.about).text += POST_TEXT;
		getUserService().update(other);
		about = ((IdBasedService<Post>) getPostService()).findById(id);
		assertThat(about, IsNull.notNullValue());
		assertThat(about.text, Is.is(POST_TEXT+POST_TEXT));
		
	}

	@Test
	public void ensureLoadCascadesWell() {
		User u1 = getUserService().find().matching(new FindFirstUserByLogin()).getFirst();
		assertThat(u1.about, IsNull.notNullValue());
		assertThat(u1.about, Is.is(Post.class));
		assertThat(((Post) u1.about).text, IsNull.notNullValue());
	}
	
	@Test
	public void serializeAuthorAndPosts() throws IOException, ClassNotFoundException {
		User u1 = getUserService().find().matching(new FindFirstUserByLogin()).getFirst();
		assertThat(u1, IsNull.notNullValue());
		assertThat(u1.posts.size(), IsNot.not(0));
		
		// Now serialize and deserialize a second version of author
		User u2 = getUserService().find().matching(new FindFirstUserByLogin()).getFirst();
		
		// Serialize it
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(bos);
		output.writeObject(u2);
		output.close();
		bos.close();
		// then deserialize
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream input = new ObjectInputStream(bis);
		User u3 = (User) input.readObject();
		input.close();
		bis.close();
		
		assertThat(u3, Is.is(u1));
		assertThat(u3.posts.size(), Is.is(u1.posts.size()));
		assertThat(Proxy.isProxyClass(u3.posts.getClass()), Is.is(false));
	}
	
	@Test
	public void ensureMapIsWellLoaded() throws IOException, ClassNotFoundException {
		Post first = getPostService().find().matching(new FindPostByNote(1)).getFirst();
		
		assertThat(first.id, Is.is(1L));
		assertThat(first.annotations.size(), IsNot.not(0));
		// Now serialize all to the second
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOutput = new ObjectOutputStream(outputStream);
		objectOutput.writeObject(first);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		ObjectInputStream objectInput = new ObjectInputStream(inputStream);
		Post second = (Post) objectInput.readObject();
		// Now perform some verifications
		assertThat(second, IsNull.notNullValue());
		assertThat(second.id, Is.is(1L));
		assertThat(second.annotations, IsNull.notNullValue());
		assertThat(second.annotations.size(), IsNot.not(0));
	}
	
	@Test
	public void ensureFindByIdWorksWell() {
		if (getPostService() instanceof IdBasedService) {
			IdBasedService idPosts = (IdBasedService) getPostService();
			Post first = (Post) idPosts.findById(ID_POST_1);
			assertThat(first, IsNull.notNullValue());
		} else { 
			Assert.fail("post service is an id based service, we all know that !");
		}
	}

	@Test
	public void ensureSerializableIsWellLoadedWithString() throws IOException, ClassNotFoundException {
		Post first = getPostService().find().matching(new FindPostByNote(1)).getFirst();
		
		String string = "a string";
		first.associatedData = string;
		first = getPostService().update(first);
		
		first = getPostService().find().matching(new FindPostByNote(1)).getFirst();;
		
		assertThat(first.associatedData, Is.is((Serializable) string));
	}

	@Test
	public void ensureSerializableIsWellLoadedWithPost() throws IOException, ClassNotFoundException {
		Post first = getPostService().find().matching(new FindPostByNote(1)).getFirst();
		User user = getUserService().find().matching(new FindFirstUserByLogin()).getFirst();
		
		first.associatedData = user;
		first = getPostService().update(first);
		
		first = getPostService().find().matching(new FindPostByNote(1)).getFirst();;
		
		assertThat(first.associatedData, Is.is((Serializable) user));
	}

	@Test
	public void ensureSerializableIsWellLoadedWithUnknownSerializable() throws IOException, ClassNotFoundException {
		Post first = getPostService().find().matching(new FindPostByNote(1)).getFirst();
		User user = getUserService().find().matching(new FindFirstUserByLogin()).getFirst();
		
		UnknownSerializable value = new UnknownSerializable().withText("a string");
		first.associatedData = value;
		first = getPostService().update(first);
		
		first = getPostService().find().matching(new FindPostByNote(1)).getFirst();;
		
		assertThat(first.associatedData, Is.is((Serializable) value));
	}

	@Test
	public void ensurePostIdCanBeGenerated() throws IOException, ClassNotFoundException {
		Post newOne = new Post().withText("some text");
		assertThat(newOne.id, Is.is(0l));
		newOne = getPostService().create(newOne);
		assertThat(newOne.id, IsNot.not(0l));
	}

	@Test
	public void ensureCreateOnUpdateWorks() throws IOException, ClassNotFoundException {
		// First find post with note "1"
		Post first = getPostService().find().matching(new FindPostByNote(1)).getFirst();
		// then remvoe its tags
		if(first.tags.size()>0) {
			first.tags.clear();
			first = getPostService().update(first);
		}
		assertThat(first.tags.size(), Is.is(0));
		Tag t = new Tag();
		t.setText(TEST_TAG_FOR_CREATE_ON_UPDATE);
		first.tags.add(t);
		// and now create that tag during the update operation
		first = getPostService().update(first);
		// if we could find it, it's a good sign !
		Tag inDB = getTagService().find().matching(new QueryBuilder<TagInformer>() {

			@Override
			public QueryExpression createMatchingExpression(TagInformer informer) {
				return informer.getText().equalsTo(TEST_TAG_FOR_CREATE_ON_UPDATE);
			}
		}).getFirst();
		assertThat(inDB.getText(), Is.is(t.getText()));
		assertThat(inDB.getId(), IsNot.not(0l));
	}

	@Test
	public void ensureUpdateOnCreateWorks() throws IOException, ClassNotFoundException {
		Post newxONe = new Post().withText(SOME_NEW_TEXT).withAuthor(author);
		author.setLogin(LOGIN_FOR_UPDATE_ON_CREATE);
		tag1.setText(TEST_TAG_FOR_CREATE_ON_UPDATE);
		newxONe.tags.add(tag1);
		newxONe = getPostService().create(newxONe);
		try {
			author = getUserService().find().matching(new QueryBuilder<UserInformer>() {
	
				@Override
				public QueryExpression createMatchingExpression(UserInformer informer) {
					return informer.getPassword().equalsTo(author.password);
				}
			}).getFirst();
			assertThat(author.getLogin(), Is.is(LOGIN_FOR_UPDATE_ON_CREATE));
			Tag official = getTagService().find().matching(new QueryBuilder<TagInformer>() {
	
				@Override
				public QueryExpression createMatchingExpression(TagInformer informer) {
					return informer.getId().equalsTo(tag1.getId());
				}
			}).getFirst();
			assertThat(official.getText(), Is.is(TEST_TAG_FOR_CREATE_ON_UPDATE));
		} finally {
			getPostService().delete(newxONe);
			tag1.setText(TAG_TEXT);
			getTagService().update(tag1);
		}
	}
	
	@Test
	public void ensureMapWorksInAllCases() throws Exception {
		Post newxONe = new Post().withText(SOME_NEW_TEXT).withAuthor(author);
		final long id = newxONe.id;
		newxONe = getPostService().create(newxONe);
		try {
			newxONe.annotations.put(A, null);
			getPostService().update(newxONe);
			newxONe = getPostService().find().matching(new FindPostByText(SOME_NEW_TEXT)).getFirst();
			assertThat(newxONe.annotations.size(), Is.is(1));
			assertThat(newxONe.annotations.containsKey(A), Is.is(true));
		} catch(Exception e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "unable to run test", e);
			}
			throw e;
		} finally {
			getPostService().delete(newxONe);
		}
	}
	
	@Test
	public void ensureMapCanBeEmptiedForIssue13() throws Exception {
		final String text = "#ensureMapCanBeEmptiedForIssue13";
		Post newxONe = new Post().withText(text).withAuthor(author);
		newxONe = getPostService().create(newxONe);
		try {
			newxONe.annotations.put(A, null);
			getPostService().update(newxONe);
			newxONe = getPostService().find().matching(new FindPostByText(text)).getFirst();
			assertThat(newxONe.annotations.size(), Is.is(1));
			assertThat(newxONe.annotations.containsKey(A), Is.is(true));
			newxONe.annotations.clear();
			getPostService().update(newxONe);
			newxONe = getPostService().find().matching(new FindPostByText(text)).getFirst();
			assertThat(newxONe.annotations.size(), Is.is(0));
		} catch(Exception e) {
			if (logger.isLoggable(Level.SEVERE)) {
				logger.log(Level.SEVERE, "unable to run test", e);
			}
			throw e;
		} finally {
			getPostService().delete(newxONe);
		}
	}


	@Test
	public void ensureAPostCanHaveAValueNulled() throws IOException, ClassNotFoundException {
		Post tested = new Post().withAuthor(author).withText("ensureAPostCanHaveAValueNulled");
		tested = getPostService().create(tested);
		assertThat(tested.text, IsNull.notNullValue());
		tested.text = null;
		tested = getPostService().update(tested);
		// force reload from graph
		tested = ((IdBasedService<Post>) getPostService()).findById(tested.id);
		assertThat(tested.text, IsNull.nullValue());
	}

	/**
	 * According to latest modifications, the both note and text will be linked to literal vertex containing value "3.0". How will it work ?
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void ensurePostCanHaveTextIdenticalToNote() throws IOException, ClassNotFoundException {
		Post third= getPostService().find().matching(new FindPostByNote(3.0f)).getFirst();
		// set value
		third.text = "3.0";
		third = getPostService().update(third);
		// get modified post
		third= getPostService().find().matching(new FindPostByNote(3)).getFirst();
		assertThat(third, IsNull.notNullValue());
		assertThat(third.note, Is.is(3.0f));
		assertThat(third.text, Is.is("3.0"));
	}

	@Test
	public void allowExport() throws FileNotFoundException, RepositoryException, RDFHandlerException {
		SailRepository repository = getRepository();
		repository.initialize();
		
		File output = new File(environment.graphPath()+"export.rdf");
		output.getParentFile().mkdirs();
		RDFHandler handler = new NQuadsWriter(new OutputStreamWriter(new FileOutputStream(output), Charset.forName("UTF-8")));

		repository.getConnection().export(handler, new URIImpl(GraphUtils.GAEDO_CONTEXT));
	}
	
	/**
	 * This test makes sure property rewriting works ok (by checking no {@link Post#text} property is written to graph.
	 * As one may has notice, posts are created during @Before method and as a consequence are available before each test (including this one).
	 */
	@Test 
	public void makeSureGraphDoesntContainAnyEdgeNamedText() {
		if (environment.graph instanceof IndexableGraph) {
			IndexableGraph indexableGraph = (IndexableGraph) environment.graph;
			
			Index<Edge> edgeIndex = indexableGraph.getIndex(Index.EDGES, Edge.class);
//			assertThat(edgeIndex.count("label", "Identified.id"), IsNot.not(Is.is(0l)));
			assertThat(edgeIndex.count("label", Post.POST_TEXT_PROPERTY), IsNot.not(Is.is(0l)));
			assertThat(edgeIndex.count("label", "Post.text"), Is.is(0l));
		}
	}

	/**
	 * According to latest modifications, the both note and text will be linked to literal vertex containing value "3.0". How will it work ?
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void ensurePostscanBeSortedByNoteDescending() throws IOException, ClassNotFoundException {
		Collection<Post> postsByAuthor= CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getAuthor().equalsTo(author);
			}
		}).sortBy(new SortingBuilder<PostInformer>() {
			
			@Override
			public SortingExpression createSortingExpression(PostInformer informer) {
				return SortingExpression.Build.sort().withDescending(informer.getNote());
			}
		}).getAll());
		float note = Float.MAX_VALUE;
		for(Post p : postsByAuthor) {
			assertThat(p.note<=note, Is.is(true));
			note = p.note;
		}
	}
	
	public void deleteAPostWithNoAuthorShouldWorkForIssue33() {
		Post toDelete = getPostService().create(new Post(0, "deleteAPostWithNoAuthorShouldWork", 10.5f, State.PUBLIC, (User) null));
		getPostService().delete(toDelete);
	}
}
