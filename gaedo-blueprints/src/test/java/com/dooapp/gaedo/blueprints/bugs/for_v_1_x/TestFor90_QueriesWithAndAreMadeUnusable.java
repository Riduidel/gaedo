package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;
import java.util.Map;
import java.util.SortedSet;

import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.GraphQueryStatement;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.blueprints.finders.FindPostByAuthor;
import com.dooapp.gaedo.blueprints.queries.executable.GraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.OptimizedGraphExecutableQuery;
import com.dooapp.gaedo.blueprints.queries.executable.VertexSet;
import com.dooapp.gaedo.blueprints.queries.executable.VertexSet.EagerLoader;
import com.dooapp.gaedo.blueprints.queries.tests.VertexTest;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor90_QueriesWithAndAreMadeUnusable extends AbstractGraphPostSubClassTest {
	public static class For87 extends Post {

	}
    @Parameters
    public static Collection<Object[]> parameters() {
        return simpleTest();
    }

    public TestFor90_QueriesWithAndAreMadeUnusable(AbstractGraphEnvironment<?> environment) {
        super(environment);
    }

    @Test
    public void can_do_a_query_with_and() {
    	final String METHOD_NAME = getClass().getSimpleName()+"#can_do_a_query_with_and";
    	final User creator = getUserService().create(new User().withLogin(METHOD_NAME));
    	// just create as much posts with same note than we have posts with same author
    	int postsByAuthor = 5;
    	// we remove "1" as we have one post by given author that have the 1 note
    	for (int i = 0; i < postsByAuthor; i++) {
    		PostSubClass toCreate = new PostSubClass(0, METHOD_NAME+"-"+i, 1, null, null);
    		// just the first is created with the good author !
    		if(i==0)
    			toCreate.author = creator;
    		toCreate.creator =creator;
    		getPostSubService().create(toCreate);
		}
    	// we have 2 posts with same note, 4 with same author, and 5 as total
    	GraphQueryStatement<PostSubClass, PostSubClass, PostSubClassInformer> matching = (GraphQueryStatement<PostSubClass, PostSubClass, PostSubClassInformer>)  getPostSubService().find().matching(new QueryBuilder<PostSubClassInformer>() {

			@Override
			public QueryExpression createMatchingExpression(PostSubClassInformer informer) {
				return Expressions.and(informer.getCreator().equalsTo(creator),
								informer.getAuthor().equalsTo(creator));
			}
		});
		assertThat(matching.count(), is(1));

    	// now go deeper and do some white-box testing
    	GraphExecutableQuery executable = matching.prepareQuery();
    	assertThat(executable, instanceOf(OptimizedGraphExecutableQuery.class));
    	OptimizedGraphExecutableQuery<?> optimized = (OptimizedGraphExecutableQuery<?>) executable;
    	Map<VertexSet, VertexTest> roots = optimized.getPossibleRootsOf(optimized.getTest());
    	/* we should have 3 query roots :
    	 * 1- class post
    	 * 2- author user
    	 * 3- creator user
    	 * Any other case indicates a confusion of roots collector, which may result in poorly formed queries.
    	*/
    	assertThat(roots.size(), is(3));
    	SortedSet<VertexSet> vertexSets = optimized.sortedVertexSetsOf(roots);
    	assertThat(vertexSets.size(), is(3));
    	// I guess best match should be the one related to creator (there is only one object matching)
    	VertexSet best = optimized.findBestRootIn(vertexSets);
    	assertThat(best.getVertices(), IsInstanceOf.instanceOf(EagerLoader.class));

    }
}
