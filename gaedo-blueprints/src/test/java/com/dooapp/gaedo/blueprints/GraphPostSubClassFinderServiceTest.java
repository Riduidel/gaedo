package com.dooapp.gaedo.blueprints;

import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.CascadeType;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.operations.Loader;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.id.IdBasedService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;


@RunWith(Parameterized.class)
public class GraphPostSubClassFinderServiceTest extends AbstractGraphPostSubClassTest{
	private static final Logger logger = Logger.getLogger(GraphPostSubClassFinderServiceTest.class.getName());

	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public GraphPostSubClassFinderServiceTest(AbstractGraphEnvironment<?> environment) {
		super(environment);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void ensurePostSubClassServiceWorksWell() {
		final String METHOD_NAME = "ensurePostSubClassServiceWorksWell";
		PostSubClass newOne = new PostSubClass(0, METHOD_NAME,1.0f, State.PUBLIC, author);
		Post saved = getPostSubService().create(newOne);
		final long postId = saved.id;
		assertThat(saved, IsInstanceOf.instanceOf(PostSubClass.class));
		Post loaded = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getText().equalsTo(METHOD_NAME);
			}
		}).getFirst();
		assertThat(loaded, IsInstanceOf.instanceOf(PostSubClass.class));
		if(getPostService() instanceof IdBasedService) {
			Post fromId = ((IdBasedService<Post>) getPostService()).findById(loaded.id);
			assertThat(fromId, IsInstanceOf.instanceOf(PostSubClass.class));
		}
		// Now make sure post can be updated and obtained
		loaded.text = "updated text : "+loaded.text;
		// exposition of https://github.com/Riduidel/gaedo/issues/23 here !
		// by updating object with basic post service, I create a second vertex in graph i can then retrieve in count.
		loaded = getPostService().update(loaded);
		assertThat(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getId().equalsTo(postId);
			}
		}).count(), Is.is(1));
	}


	@Test
	public void ensureBug23IsSolved() {
		final String METHOD_NAME = "ensureBug23IsSolved";
		PostSubClass newOne = new PostSubClass(0, METHOD_NAME,1.0f, State.PUBLIC, author);
		Post saved = getPostSubService().create(newOne);
		final long postId = saved.id;
		assertThat(saved, IsInstanceOf.instanceOf(PostSubClass.class));
		Post loaded = getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getText().equalsTo(METHOD_NAME);
			}
		}).getFirst();
		// Now make sure post can be updated and obtained
		loaded.text = "updated text : "+loaded.text;
		// exposition of https://github.com/Riduidel/gaedo/issues/23 here !
		// by updating object with basic post service, I create a second vertex in graph i can then retrieve in count.
		loaded = getPostService().update(loaded);
		assertThat(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getId().equalsTo(postId);
			}
		}).count(), Is.is(1));
	}


	@Test
	public void usingAnUntypedURIValueCouldWork_for_bug_32() {
		final String METHOD_NAME = "usingAnUntypedURIValueCouldWork_for_bug_32";
		PostSubClass newOne = new PostSubClass(0, METHOD_NAME,1.0f, State.PUBLIC, author);
		newOne.state = State.PUBLIC;
		newOne.anotherState = PostSubClass.AnotherStateForBug26.PUBLIC;
		Post saved = getPostSubService().create(newOne);
		assertThat(saved, IsInstanceOf.instanceOf(PostSubClass.class));

		// Directly manipulating vertex to link it to a raw uri node (used as ... text)
		IndexableGraphBackedFinderService<PostSubClass, PostSubClassInformer> postSubService = (IndexableGraphBackedFinderService<PostSubClass, PostSubClassInformer>) getPostSubService();
		Vertex postVertex = postSubService.getVertexFor(newOne, CascadeType.REFRESH, ObjectCache.create(CascadeType.REFRESH));

//		if(environment.getGraph() instanceof TransactionalGraph)
//			((TransactionalGraph) environment.getGraph()).startTransaction();
		Vertex valueVertex = environment.getGraph().addVertex("new-text-vertex");
		// using the service method, as it allows us to add properties to indices with ease
		GraphUtils.setIndexedProperty(environment.getGraph(), valueVertex, Properties.kind.name(), Kind.uri.name());
		String BUG_31_URI = "https://github.com/Riduidel/gaedo/issues/31";
		GraphUtils.setIndexedProperty(environment.getGraph(), valueVertex, Properties.value.name(), BUG_31_URI);

		// Have you notice this property is annotated ?
		String textPropertyName = "post:text";
		Iterable<Edge> previous = postVertex.getEdges(Direction.OUT, textPropertyName);
		for(Edge e : previous) {
			environment.getGraph().removeEdge(e);
		}

		Edge edgeToURI = environment.getGraph().addEdge(textPropertyName, postVertex, valueVertex, textPropertyName);


		Post loaded = postSubService.findById(newOne.id);

		assertThat(loaded.text, Is.is(BUG_31_URI));
	}

	@Test
	public void replacingACascadePersistValueWithNullShouldHaveNoEffect() {
		final String METHOD_NAME = "replacingACascadePersistValueWithNullShouldHaveNoEffect";
		PostSubClass newOne = new PostSubClass(0, METHOD_NAME,1.0f, State.PUBLIC, author);
		newOne.state = State.PUBLIC;
		newOne.anotherState = PostSubClass.AnotherStateForBug26.PUBLIC;
		newOne.creator = author;
		PostSubClass saved = getPostSubService().create(newOne);
		assertThat(saved, Is.is(PostSubClass.class));

		saved.creator = null;
		getPostSubService().update(saved);
		// little log manipulation added to make sure refresh WAS present on graph
		Logger.getLogger(Loader.LoadProperties.class.getName()).setLevel(Level.ALL);
		for(Handler handler : Logger.getLogger("").getHandlers()) {
			handler.setLevel(Level.FINE);
		}
		PostSubClass loaded = ((IdBasedService<PostSubClass>) getPostSubService()).findById(saved.id);

		assertThat(loaded.creator, Is.is(author));
	}

}
