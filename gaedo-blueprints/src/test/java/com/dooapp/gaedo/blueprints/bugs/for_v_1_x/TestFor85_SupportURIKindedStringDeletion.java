package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;

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
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.Kind;
import com.dooapp.gaedo.blueprints.NoReturnableVertexException;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.Vertex;

import static org.junit.Assert.assertThat;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

@RunWith(Parameterized.class)
public class TestFor85_SupportURIKindedStringDeletion extends AbstractGraphPostTest {
    @Parameters
    public static Collection<Object[]> parameters() {
        return simpleTest();
    }

    public TestFor85_SupportURIKindedStringDeletion(AbstractGraphEnvironment<?> environment) {
        super(environment);
    }


    /**
     * White-box testing : we <i>can</i> create a post which text is stored in a separate vertex having the URI kind.
     */
    @Test
    public void can_create_a_post_with_text_in_another_vertex() {
    	// given
        String METHOD_NAME = "#ensure_finding_first_post_will_end_with_index_closed";
        IndexableGraphBackedFinderService<Post, PostInformer> service = (IndexableGraphBackedFinderService<Post, PostInformer>) getPostService();
        Post toSet = service.create(new Post());
        createDetachedText(service, toSet, METHOD_NAME);
        // when
        toSet = service.findById(toSet.id);
        // then
        assertThat(toSet.text, is(METHOD_NAME));
    }

    /**
     * White-box testing : we <i>can</i> create a post which text is stored in a separate vertex having the URI kind.
     */
    @Test(expected=NoReturnableVertexException.class)
    public void can_delete_post_with_text_in_another_vertex() {
    	// given
        String METHOD_NAME = "#can_delete_post_with_text_in_another_vertex";
        IndexableGraphBackedFinderService<Post, PostInformer> service = (IndexableGraphBackedFinderService<Post, PostInformer>) getPostService();
        Post toSet = service.create(new Post());
        createDetachedText(service, toSet, METHOD_NAME);
        // when
        service.delete(service.findById(toSet.id));
        // then
        assertThat(service.findById(toSet.id), nullValue());
    }

	protected void createDetachedText(IndexableGraphBackedFinderService<Post, PostInformer> service, Post toSet, String text) {
		IndexableGraph graph = service.getDatabase();
		Vertex textVertex = graph.addVertex(text+"_text");
	        textVertex.setProperty(Properties.kind.name(), Kind.uri.name());
	        textVertex.setProperty(Properties.value.name(), text);
        ObjectCache cache = ObjectCache.create(CascadeType.REFRESH);
        Vertex postVertex = service.getVertexFor(toSet, CascadeType.REFRESH, cache);
        Collection<Property> properties = service.getStrategy().getContainedProperties(toSet, postVertex, CascadeType.REFRESH).keySet();
        Property textProperty = null;
        for(Property p : properties) {
        	if("text".equals(p.getName())) {
        		textProperty = p;
        	}
        }
        graph.addEdge(text, postVertex, textVertex, GraphUtils.getEdgeNameFor(textProperty));
	}
}
