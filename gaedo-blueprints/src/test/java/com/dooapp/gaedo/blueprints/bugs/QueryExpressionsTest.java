package com.dooapp.gaedo.blueprints.bugs;

import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostTest;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.finders.FindPostById;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.expressions.Expressions;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.utils.CollectionUtils;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class QueryExpressionsTest extends AbstractGraphPostTest {
    private static final Logger logger = Logger.getLogger(QueryExpressionsTest.class.getName());

    @Parameters
    public static Collection<Object[]> parameters() {
        return simpleTest();
    }

    public QueryExpressionsTest(AbstractGraphEnvironment<?> environment) {
        super(environment);
    }

    @Test
    public void can_find_start_with() {
            Collection<Post>  found = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

                @Override
                public QueryExpression createMatchingExpression(PostInformer informer) {
                    return informer.getText().startsWith("post");
                }
            }).getAll());
            assertThat(found, IsCollectionContaining.hasItems(post1, post2, post3));
    }

    @Test
    public void can_compare_ignore_case() {
        Post tested = getPostService().find().matching(new QueryBuilder<PostInformer>() {

            @Override
            public QueryExpression createMatchingExpression(PostInformer informer) {
                return informer.getText().equalsToIgnoreCase("Post TExt for 1");
            }
        }).getFirst();
        assertThat(tested, Is.is(post1));
    }

    @Test
    public void can_compare_using_regexp() {
        Collection<Post>  found = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

            @Override
            public QueryExpression createMatchingExpression(PostInformer informer) {
                return informer.getText().matches("post.*");
            }
        }).getAll());
        assertThat(found, IsCollectionContaining.hasItems(post1, post2, post3));
    }

    @Test
    public void can_find_using_instanceof_on_managed_vertex() {
    	PostSubClass newPost = new PostSubClass(0, post1.text, post1.note, post1.state, post1.author);
    	newPost = (PostSubClass) getPostService().create(newPost);
        Collection<Post>  found = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

            @Override
            public QueryExpression createMatchingExpression(PostInformer informer) {
            	return Expressions.and(informer.getNote().equalsTo(post1.note), informer.instanceOf(PostSubClass.class));

            }
        }).getAll());
        assertThat(found, IsCollectionContaining.hasItems((Post) newPost));
    }

    @Test
    public void can_find_using_instanceof_on_literal_value() {
    	post1.associatedData = "a";
    	post1 = getPostService().update(post1);
        Collection<Post>  found = CollectionUtils.asList(getPostService().find().matching(new QueryBuilder<PostInformer>() {

            @Override
            public QueryExpression createMatchingExpression(PostInformer informer) {
            	return informer.getAssociatedData().instanceOf(String.class);

            }
        }).getAll());
        assertThat(found, IsCollectionContaining.hasItems(post1));
    }
}
