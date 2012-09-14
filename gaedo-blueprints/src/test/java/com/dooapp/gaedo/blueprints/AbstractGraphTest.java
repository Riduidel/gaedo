package com.dooapp.gaedo.blueprints;

import org.junit.After;
import org.junit.Before;

import com.dooapp.gaedo.AbstractCrudService;
import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.blueprints.indexable.IndexableGraphBackedFinderService;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.tinkerpop.blueprints.pgm.IndexableGraph;

public class AbstractGraphTest  {
	
	private AbstractGraphEnvironment<?> environment;

	public AbstractGraphTest(AbstractGraphEnvironment<?> environment) {
		this.environment = environment;
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
