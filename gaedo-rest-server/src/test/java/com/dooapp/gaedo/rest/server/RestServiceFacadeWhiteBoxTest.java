package com.dooapp.gaedo.rest.server;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.test.beans.Post;



public class RestServiceFacadeWhiteBoxTest {
	private RestServiceFacade tested;
	private ServiceRepository repository;

	@Before
	public void prepare() {
		tested = new RestServiceFacade();
		repository = TestEnvironmentProvider.create();
	}
	
	/**
	 * Test that finding works correctly with some input maps
	 */
	@Test
	public void testOpenFind() {
		Map<String, Object> filterData = new LinkedHashMap<String, Object>();
		filterData.put(RestServiceParams.FILTER.getPrefix()+"["+FilterAggregator.OR.getKey()+"][text][startsWith]", "first");
		filterData.put(RestServiceParams.FILTER.getPrefix()+"["+FilterAggregator.OR.getKey()+"][note][greaterThan]", "5");

		Map<String, Object> returnData = new LinkedHashMap<String, Object>();
		returnData.put(RestServiceParams.RETURN.getPrefix()+"[mode]", "findOneWith");

		Object returned = tested.find(repository.get(Post.class), filterData, Collections.EMPTY_MAP, returnData);
		
		Assert.assertThat(returned, Is.is(Post.class));
	}
	
	@Test
	public void testOpenCreate() throws Exception {
		Map<String, Object> objectData = new LinkedHashMap<String, Object>();
		String postText = "some not so long text";
		float postNote = 5.1f;
		objectData.put(RestServiceParams.OBJECT.getPrefix()+"[text]", postText);
		objectData.put(RestServiceParams.OBJECT.getPrefix()+"[note]", postNote+"");

		Object returned = tested.create(repository.get(Post.class), objectData);
		
		Assert.assertThat(returned, Is.is(Post.class));
		Post p = (Post) returned;
		Assert.assertThat(p.text, Is.is(postText));
		Assert.assertThat(p.note, Is.is(postNote));
	}
}
