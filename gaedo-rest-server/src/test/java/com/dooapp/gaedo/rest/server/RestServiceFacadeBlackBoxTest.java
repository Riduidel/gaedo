package com.dooapp.gaedo.rest.server;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.User;

/**
 * Black box testing of {@link RestServiceFacade} : it uses REST to get/put/... data and check that all is correct.
 * @author ndx
 *
 */
public class RestServiceFacadeBlackBoxTest extends AbstractGaedoResourceApplicationTest {
	/**
	 * Test that the all get method is working correctly in most of the well-known cases for the User class
	 */
	@Test
	public void testGetUsers() throws Exception {
		ClientResource userServiceEndPoint = new ClientResource("http://localhost:"+TEST_PORT+TEST_DIR+GaedoResourceApplication.SERVICE_LIST_ROUTE+"/"+User.class.getName()+"/");
		Representation representation = userServiceEndPoint.get(MediaType.TEXT_XML);
		Assert.assertThat(representation.getMediaType(), Is.is(MediaType.APPLICATION_JAVA_OBJECT));
		ObjectRepresentation<Serializable> client = new ObjectRepresentation<Serializable>(representation);
		Serializable content = client.getObject();
		Assert.assertThat(content, Is.is(Collection.class));
		Collection contentCollection = (Collection) content;
		Assert.assertThat(contentCollection.size(), Is.is(2));
		Iterator iterator = contentCollection.iterator();
		Object first = iterator.next();
		Assert.assertThat(first, Is.is(User.class));
		// Which is first user
		User firstUser = (User) first;
		Assert.assertThat(firstUser.getLogin(), Is.is("first"));
		Object second = iterator.next();
		Assert.assertThat(second, Is.is(User.class));
		// Which is first user
		User secondUser = (User) second;
		Assert.assertThat(secondUser.getLogin(), Is.is("second"));
		// Happy ? Not yet. Are posts and tags loaded ?
		Assert.assertThat(firstUser.posts.size(), Is.is(1));
		Post firstPost = firstUser.posts.iterator().next();
		Assert.assertThat(firstPost.text, Is.is("first post"));
	}
}
