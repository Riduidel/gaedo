package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.Iterator;

import javax.persistence.CascadeType;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.indexable.IndexBrowser;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import static org.junit.Assert.assertThat;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

@RunWith(Parameterized.class)
public class TestFor84_CloseableIterableMustBeClosed extends AbstractGraphPostTest {
	public class RecordingIndexBrowser extends IndexBrowser {
		private boolean closeCalled;
		private class RecordingCloseableIterable implements CloseableIterable<Vertex> {

			private CloseableIterable<Vertex> delegate;

			public RecordingCloseableIterable(CloseableIterable<Vertex> verticesWithId) {
				this.delegate = verticesWithId;
			}

			/**
			 *
			 * @see com.tinkerpop.blueprints.CloseableIterable#close()
			 * @category delegate
			 */
			public void close() {
				closeCalled = true;
				delegate.close();
			}

			/**
			 * @return
			 * @see java.lang.Iterable#iterator()
			 * @category delegate
			 */
			public Iterator<Vertex> iterator() {
				return delegate.iterator();
			}

		}

		/**
		 * @param database
		 * @param objectVertexId
		 * @return
		 * @see com.dooapp.gaedo.blueprints.indexable.IndexBrowser#getVerticesWithId(com.tinkerpop.blueprints.IndexableGraph, java.lang.String)
		 */
		@Override
		protected CloseableIterable<Vertex> getVerticesWithId(IndexableGraph database, String objectVertexId) {
			return new RecordingCloseableIterable(super.getVerticesWithId(database, objectVertexId));
		}

	}
	@Parameters
	public static Collection<Object[]> parameters() {
		return simpleTest();
	}

	public TestFor84_CloseableIterableMustBeClosed(AbstractGraphEnvironment<?> environment) {
		super(environment);
	}


	/**
	 * White-box testing : we obtain id from indexable graph service, then both this id and the service VertexMatcher to load
	 * correct vertex
	 */
	@Test
	public void ensure_finding_first_post_will_end_with_index_closed() {
		IndexableGraphBackedFinderService<Post, PostInformer> service = (IndexableGraphBackedFinderService<Post, PostInformer>) getPostService();
		String id = (String) service.getIdOf(post1);
		// now we first load vertex
		Vertex vertex = service.getVertexFor(post1, CascadeType.REFRESH, new ObjectCache());
		assertThat(vertex, notNullValue());
		assertThat(vertex.getProperty(Properties.value.name()).toString(), is(id));
		// now we know the vertex is correct (not a total surprise, mind you), let's use our custom IndexBrowser to find it again
		RecordingIndexBrowser recorder = new RecordingIndexBrowser();
		Vertex navigated = recorder.browseFor(service.getDatabase(),
						id, service.getContainedClass().getName(), service.vertexMatcher());
		// basic validation (after all, we just did an extracted call to exactly the same method)
		assertThat(navigated, notNullValue());
		assertThat(navigated, is(vertex));
		assertThat(recorder.closeCalled, is(true));
	}
}
