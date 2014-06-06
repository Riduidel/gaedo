package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.GraphQueryStatement;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.finders.FindPostByText;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.OptimizedGraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.VertexSet;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor89_CanSearchWithEqualsToWhenValueIsNull extends AbstractGraphPostSubClassTest {
	public static class For87 extends Post {

	}
    @Parameters
    public static Collection<Object[]> parameters() {
        return simpleTest();
    }

    public TestFor89_CanSearchWithEqualsToWhenValueIsNull(AbstractGraphEnvironment<?> environment) {
        super(environment);
    }

    @Test
    public void can_call_equals_to_on_null_text() {
    	final String METHOD_NAME = "#can_call_equals_to_on_null_text";
    	Post created = getPostService().create(new Post());
    	assertThat(created.text, nullValue());
    	assertThat(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getText().equalsTo(METHOD_NAME);
			}
		}).count(), is(0));
    }

    @Test
    public void can_call_equals_to_ignore_case_on_null_text() {
    	final String METHOD_NAME = "#can_call_equals_to_ignore_case_on_null_text";
    	Post created = getPostService().create(new Post());
    	assertThat(created.text, nullValue());
    	assertThat(getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.getText().equalsToIgnoreCase(METHOD_NAME);
			}
		}).count(), is(0));
    }
}
