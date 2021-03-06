package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.GraphQueryStatement;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.OptimizedGraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.VertexSet;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.State;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor88_DoNotRunTestTwice extends AbstractGraphPostSubClassTest {
    @Parameters
    public static Collection<Object[]> parameters() {
        return simpleTest();
    }

    public TestFor88_DoNotRunTestTwice(AbstractGraphEnvironment<?> environment) {
        super(environment);
    }

    @Before
    public void loadPost() {
    	getPostService().create(new PostSubClass(0, getClass().getName(), 1, State.PUBLIC, author));
    }

    @Test
    public void can_find_post_subclasses_in_post_service_without_looking_into_post_instances() {
    	GraphQueryStatement<Post, Post, PostInformer> query = (GraphQueryStatement<Post, Post, PostInformer>) getPostService().find().matching(new QueryBuilder<PostInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostInformer informer) {
				return informer.instanceOf(PostSubClass.class);
			}

    	});
    	assertThat(query.count(), is(1));
    	// Now for some officialy crazy whitebox testing ...
    	GraphExecutableQuery executable = query.prepareQuery();
    	assertThat(executable, instanceOf(OptimizedGraphExecutableQuery.class));
    	OptimizedGraphExecutableQuery optimized = (OptimizedGraphExecutableQuery) executable;
    	Map<VertexSet, VertexTest> roots = optimized.getPossibleRootsOf(optimized.getTest());
    	assertThat(roots.size(), is(2));
    	SortedSet<VertexSet> rootSet = optimized.sortedVertexSetsOf(roots);
    	VertexTest firstTest = roots.get(rootSet.first());
    	VertexTest lastTest = roots.get(rootSet.last());
    	assertThat(firstTest, IsNot.not(lastTest));
    }
}
