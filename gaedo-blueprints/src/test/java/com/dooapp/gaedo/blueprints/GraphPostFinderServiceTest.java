package com.dooapp.gaedo.blueprints;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.finders.FindFirstUserByLogin;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.blueprints.finders.FindPostByText;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.tinkerpop.blueprints.TransactionalGraph;

import static com.dooapp.gaedo.blueprints.TestUtils.A;
import static com.dooapp.gaedo.blueprints.TestUtils.ABOUT_ID;
import static com.dooapp.gaedo.blueprints.TestUtils.ID_POST_1;
import static com.dooapp.gaedo.blueprints.TestUtils.LOGIN_FOR_UPDATE_ON_CREATE;
import static com.dooapp.gaedo.blueprints.TestUtils.SOME_NEW_TEXT;
import static com.dooapp.gaedo.blueprints.TestUtils.TEST_TAG_FOR_CREATE_ON_UPDATE;
import static com.dooapp.gaedo.blueprints.TestUtils.USER_LOGIN;
import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class GraphPostFinderServiceTest extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(GraphPostFinderServiceTest.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}
	public GraphPostFinderServiceTest(AbstractGraphEnvironment<?> graph) {
		super(graph);
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
			if (environment.getGraph() instanceof TransactionalGraph) {
				TransactionalGraph transactionalGraph = (TransactionalGraph) environment.getGraph();
//				transactionalGraph.startTransaction();
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
	public void ensureCascadedCreateWorks() throws IOException, ClassNotFoundException {
		String METHOD_NAME = "ensureCascadedCreateWorks";
		Post toCreate = new Post();
		toCreate.text = METHOD_NAME;
		User author = new User();
		author.password = METHOD_NAME;
		author.setLogin(METHOD_NAME);
		toCreate.author = author;
		getPostService().create(toCreate);
		// Find it back
		Post found = getPostService().find().matching(new FindPostByText(METHOD_NAME)).getFirst();
		assertThat(found, IsNull.notNullValue());
		assertThat(found.author, IsNull.notNullValue());
		assertThat(found.author.getLogin(), IsNull.notNullValue());
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

	/**
	 * As a consequence of https://github.com/Riduidel/gaedo/issues/41
	 * this test has been changed to make sure update on create has not the slightest influence
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void ensureUpdateOnCreateDoNotWork() throws IOException, ClassNotFoundException {
		Post newxONe = new Post().withText(SOME_NEW_TEXT).withAuthor(author);
		String initialLogin = author.getLogin();
		author.setLogin(LOGIN_FOR_UPDATE_ON_CREATE);
		String initialText = tag1.getText();
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
			assertThat(author.getLogin(), Is.is(initialLogin));
			Tag official = getTagService().find().matching(new QueryBuilder<TagInformer>() {

				@Override
				public QueryExpression createMatchingExpression(TagInformer informer) {
					return informer.getId().equalsTo(tag1.getId());
				}
			}).getFirst();
			assertThat(official.getText(), Is.is(initialText));
		} finally {
			getPostService().delete(newxONe);
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

	/**
	 * This test makes sure property rewriting works ok (by checking no {@link Post#text} property is written to graph.
	 * As one may has notice, posts are created during @Before method and as a consequence are available before each test (including this one).
	 *
	 * Notice that, as edge index is now disabled, this test is of no use now.
	 */
	@Test @Ignore
	public void makeSureGraphDoesntContainAnyEdgeNamedText() {
	}
}
