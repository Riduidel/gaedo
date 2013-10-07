package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.finders.FindPostById;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.blueprints.indexable.IndexNames;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.test.beans.Post;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

/**
 * What happens when an absolutely non standard gaedo vertex is put in index before the vertex we should use ?
 * Well, everything is transformed in flying shit.
 * @author ndx
 *
 */
@RunWith(Parameterized.class)
public class TestFor66_AKA_IndexMayLie extends AbstractGraphPostTest {
	private static final String VALUE = "some data, indeed";
	private static final String KEY = "data";
	private static String POLLUTION_KEY = "key:"+String.class.getName()+":"+KEY+"-value:"+String.class.getName()+":"+VALUE+"-";
	private static Level previousLevel;

	private static final Logger logger = Logger.getLogger(TestFor66_AKA_IndexMayLie.class.getName());

	/**
	 * to avoid log pollution, logging of IdnexableGraphBackedFinderService is totally disabled during that very test
	 */
	@BeforeClass
	public static void disableLoggingDuringThatTest() {
		Logger indexableLogger = Logger.getLogger(IndexableGraphBackedFinderService.class.getName());
		previousLevel = indexableLogger.getLevel();
		indexableLogger.setLevel(Level.OFF);
	}

	@AfterClass
	public static void enableLoggingDuringThatTest() {
		Logger indexableLogger = Logger.getLogger(IndexableGraphBackedFinderService.class.getName());
		indexableLogger.setLevel(previousLevel);
	}


	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor66_AKA_IndexMayLie(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	@Test
	public void ensurePollutionByEmptyVertexWork() {
		Graph g = environment.getGraph();
		if(g instanceof IndexableGraph) {
			IndexableGraph indexable = (IndexableGraph) g;
			// now corrupt that index
			Index<Vertex> vertices = indexable.getIndex(IndexNames.VERTICES.getIndexName(), Vertex.class);
			// pollution vertex has no property set ... on purpose or not
			Vertex pollution = indexable.addVertex(getClass().getName());
			vertices.put(Properties.value.name(), POLLUTION_KEY, pollution);
			assertThat(vertices.get(Properties.value.name(), POLLUTION_KEY).iterator().next(), Is.is(pollution));
			corruptPostByUpdatingIt(KEY, VALUE, vertices, pollution, POLLUTION_KEY);
		}
	}

	@Test
	public void ensurePollutionByVertexWithRandomKindWork() {
		Graph g = environment.getGraph();
		if(g instanceof IndexableGraph) {
			IndexableGraph indexable = (IndexableGraph) g;
			// now corrupt that index
			Index<Vertex> vertices = indexable.getIndex(IndexNames.VERTICES.getIndexName(), Vertex.class);
			// pollution vertex has no property set ... on purpose or not
			Vertex pollution = indexable.addVertex(getClass().getName());
			pollution.setProperty(Properties.kind.name(), Kind.uri.name());
			vertices.put(Properties.value.name(), POLLUTION_KEY, pollution);
			assertThat(vertices.get(Properties.value.name(), POLLUTION_KEY).iterator().next(), Is.is(pollution));
			corruptPostByUpdatingIt(KEY, VALUE, vertices, pollution, POLLUTION_KEY);
		}
	}

	private void corruptPostByUpdatingIt(String key, String value, Index<Vertex> vertices, Vertex pollution, String pollutionKey) {
		// and update post to use that corrupted value
		Post post = getPostService().find().matching(new FindPostByNote(2.0)).getFirst();
		post.annotations.put(key, value);
		final long postId = post.id;
		getPostService().update(post);
		assertThat(vertices.get(Properties.value.name(), pollutionKey).iterator().next(), Is.is(pollution));

		// now load it back
		post = getPostService().find().matching(new FindPostById(postId)).getFirst();
		assertThat(post.annotations.keySet(), IsCollectionContaining.hasItem(key));
		assertThat(post.annotations.values(), IsCollectionContaining.hasItem(value));
	}
}
