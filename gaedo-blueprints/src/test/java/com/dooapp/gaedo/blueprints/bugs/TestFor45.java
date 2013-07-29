package com.dooapp.gaedo.blueprints.bugs;

import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractBluePrintsBackedFinderService;
import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.finders.FindPostByNote;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.oupls.sail.GraphSail;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor45 extends AbstractGraphPostTest {
	private static final Logger logger = Logger.getLogger(TestFor45.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor45(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}

	/**
	 * This test ensure https://github.com/Riduidel/gaedo/issues/45 doesn't happen. For that, it creates a Tag and tries to link it to a Post in a different named graph from the default one.
	 * This should made that link invisible to gaedo (if it honors correctly the named graph mapping to lenses).
	 */
	@Test
	public void ensurePostDontLoadCollectionEdgesFromOtherNamedGraphs() {
		// Don't yet work in SailGraph
		String METHOD_NAME = "ensurePostDontLoadCollectionEdgesFromOtherNamedGraphs";
		Post third= getPostService().find().matching(new FindPostByNote(3.0f)).getFirst();
		// Find post vertex by forging its id
		Vertex thirdVertex = ((AbstractBluePrintsBackedFinderService<?, Post, PostInformer>) getPostService()).getIdVertexFor(third, false);

		Tag unConnected = new Tag(METHOD_NAME);
		unConnected = getTagService().create(unConnected);
		Vertex tagVertex = ((AbstractBluePrintsBackedFinderService<?, Tag, TagInformer>) getTagService()).getIdVertexFor(unConnected, false);

//		if(environment.getGraph() instanceof TransactionalGraph) {
//			((TransactionalGraph) environment.getGraph()).startTransaction();
//		}
		// Now forge an edge outside of standard named graph
		String edgeNameFor = Post.class.getName()+":"+"tags";
		String predicateProperty = METHOD_NAME+"#test Edge";
		Edge edge = environment.getGraph().addEdge(predicateProperty, thirdVertex, tagVertex, edgeNameFor);
		edge.setProperty(GraphUtils.PREDICATE_PROPERTY, predicateProperty);
		String contextProperty = GraphUtils.asSailProperty("https://github.com/Riduidel/gaedo/issues/45");
		edge.setProperty(GraphUtils.CONTEXT_PROPERTY, contextProperty);
		// Finally build the context-predicate property by concatenating both
		edge.setProperty(GraphUtils.CONTEXT_PREDICATE_PROPERTY, contextProperty + " " + predicateProperty);

		if(environment.getGraph() instanceof TransactionalGraph) {
			((TransactionalGraph) environment.getGraph()).stopTransaction(Conclusion.SUCCESS);
		}

		third= getPostService().find().matching(new FindPostByNote(3)).getFirst();
		assertThat(third, IsNull.notNullValue());
		assertThat(third.note, Is.is(3.0f));
		assertThat(third.text, Is.is("post text for 3"));
		// there should be no tags there
		assertThat(third.tags.size(), Is.is(0));
	}
}
