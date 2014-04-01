package com.dooapp.gaedo.blueprints.bugs.for_v_0_x;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsSame;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.User;
import com.tinkerpop.blueprints.Vertex;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor72_AKA_GremlinQueryIntegration extends AbstractGraphPostSubClassTest {
	private static final Logger logger = Logger.getLogger(TestFor72_AKA_GremlinQueryIntegration.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor72_AKA_GremlinQueryIntegration(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void loadAllPostsAfterHavingFoundAllTheirVertices() {
		testSynchronisationBetweenVerticesAndObjects((IndexableGraphBackedFinderService<Post, ?>) getPostService());
	}

	@Test
	public void loadAllUsersAfterHavingFoundAllTheirVertices() {
		testSynchronisationBetweenVerticesAndObjects((IndexableGraphBackedFinderService<User, ?>) getUserService());
	}


	@Test
	public void loadAllTagsAfterHavingFoundAllTheirVertices() {
		testSynchronisationBetweenVerticesAndObjects((IndexableGraphBackedFinderService<Tag, ?>) getTagService());
	}

	private <DataType> void testSynchronisationBetweenVerticesAndObjects(IndexableGraphBackedFinderService<DataType, ?> indexablePostService) {
		List<Vertex> postVertices = new ArrayList<Vertex>();
		ObjectCache cache = new ObjectCache();
		for(DataType p : indexablePostService.findAll()) {
			postVertices.add(indexablePostService.getVertexFor(p, CascadeType.REFRESH, cache ));
		}
		// now these vertices are loaded, well, just load associated objects over an iterable
		Iterable<DataType> loadedPosts = indexablePostService.loadObjects(postVertices);
		int index = 0;
		for(DataType p : loadedPosts) {
			Vertex source = postVertices.get(index);
			assertThat(indexablePostService.getVertexFor(p, CascadeType.REFRESH, cache), Is.is(source));
			index++;
		}
	}
}
