package com.dooapp.gaedo.blueprints;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AbstractGraphTest  {

	protected final AbstractGraphEnvironment<?> environment;

	public AbstractGraphTest(AbstractGraphEnvironment<?> environment) {
		this.environment = environment;
	}

	/**
	 * Perform that test using an IndexableGraph, as given by {@link TestUtils#indexable(GraphProvider)}
	 * @param graphProvider
	 */
	public AbstractGraphTest(GraphProvider graphProvider) {
		this(TestUtils.indexable(graphProvider));
	}

	@Before
	public void loadService() throws Exception {
		environment.loadService();
	}

	/**
	 * @throws Exception
	 * @see com.dooapp.gaedo.blueprints.AbstractGraphEnvironment#unload()
	 * @category delegate
	 */
	@After
	public void unload() throws Exception {
		environment.unload();
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractGraphEnvironment#getTagService()
	 * @category delegate
	 */
	public FinderCrudService<Tag, TagInformer> getTagService() {
		return environment.getTagService();
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractGraphEnvironment#getPostService()
	 * @category delegate
	 */
	public FinderCrudService<Post, PostInformer> getPostService() {
		return environment.getPostService();
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractGraphEnvironment#getPostSubService()
	 * @category delegate
	 */
	public FinderCrudService<PostSubClass, PostSubClassInformer> getPostSubService() {
		return environment.getPostSubService();
	}

	/**
	 * @return
	 * @see com.dooapp.gaedo.blueprints.AbstractGraphEnvironment#getUserService()
	 * @category delegate
	 */
	public FinderCrudService<User, UserInformer> getUserService() {
		return environment.getUserService();
	}

}
