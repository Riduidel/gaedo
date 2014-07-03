package com.dooapp.gaedo.blueprints.bugs.for_v_1_x;

import java.util.Collection;

import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.dooapp.gaedo.blueprints.AbstractGraphEnvironment;
import com.dooapp.gaedo.blueprints.AbstractGraphPostSubClassTest;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.finders.FindPostSubclassById;

import static com.dooapp.gaedo.blueprints.TestUtils.simpleTest;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestFor91_DeletingDontGoThroughMapEntries extends AbstractGraphPostSubClassTest {
    @Parameters
    public static Collection<Object[]> parameters() {
        return simpleTest();
    }

    public TestFor91_DeletingDontGoThroughMapEntries(AbstractGraphEnvironment<?> environment) {
        super(environment);
    }

    @Test
    public void can_delete_a_post_pages() {
    	final String METHOD_NAME = getClass().getSimpleName()+"#can_delete_a_post_pages";
    	PostSubClass longOne = new PostSubClass();
    	longOne.author = author;
    	longOne.text = METHOD_NAME+" long text beginning";
    	PostSubClass secondPart = new PostSubClass();
    	secondPart.author = author;
    	secondPart.text = METHOD_NAME+" second part";
    	longOne.getPostPages().put(1, secondPart);
    	longOne = getPostSubService().create(longOne);
    	assertThat(longOne.id, not(0l));
    	secondPart = longOne.getPostPages().get(1);
		assertThat(secondPart.id, not(0l));
		// Now delete long post and see what remains
		getPostSubService().delete(longOne);
		assertThat(getPostSubService().find().matching(new FindPostSubclassById(secondPart.id)).count(), is(0));
    }
}
