package com.dooapp.gaedo.blueprints;

import java.io.File;

import org.junit.After;
import org.openrdf.repository.sail.SailRepository;

import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.LazyInterfaceInformerLocator;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.test.beans.Post;
import com.dooapp.gaedo.test.beans.PostInformer;
import com.dooapp.gaedo.test.beans.Tag;
import com.dooapp.gaedo.test.beans.TagInformer;
import com.dooapp.gaedo.test.beans.User;
import com.dooapp.gaedo.test.beans.UserInformer;
import com.dooapp.gaedo.test.beans.specific.Theme;
import com.dooapp.gaedo.test.beans.specific.ThemeInformer;
import com.tinkerpop.blueprints.pgm.Graph;

/**
 * Class defining all graph elements and properties, used by tests.
 * @author ndx
 *
 * @param <GraphType>
 */
public abstract class AbstractGraphEnvironment<GraphType extends Graph> {

	protected String name;
	protected GraphType graph;
	protected SimpleServiceRepository repository;
	protected GraphProvider graphProvider;
	protected FieldBackedPropertyProvider provider;
	protected CumulativeFieldInformerLocator locator;
	protected ReflectionBackedInformerFactory reflectiveFactory;
	protected ProxyBackedInformerFactory proxyInformerFactory;
	private FinderCrudService<Tag, TagInformer> tagService;
	private FinderCrudService<Post, PostInformer> postService;
	private FinderCrudService<PostSubClass, PostSubClassInformer> postSubService;
	private FinderCrudService<User, UserInformer> userService;
	private FinderCrudService<Theme, Informer<Theme>> themeService;

	public AbstractGraphEnvironment(GraphProvider graph) {
		this.name = getClass().getSimpleName()+" "+graph.getName();
		this.graphProvider = graph;
	}

	@After
	public void unload() throws Exception {
		if(graph!=null) {
			graph.shutdown();
		}
		File f = new File(GraphProvider.GRAPH_DIR);
		f.delete();
	}

	public void loadService() throws Exception {
		repository = new SimpleServiceRepository();
		provider = new FieldBackedPropertyProvider();
		locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		locator.add(new LazyInterfaceInformerLocator());
		reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);
		
		graph = createGraph(graphProvider);
		
		// Now add some services
		repository.add(createServiceFor(Tag.class, TagInformer.class));
		repository.add(createServiceFor(Post.class, PostInformer.class));
		repository.add(createServiceFor(PostSubClass.class, PostSubClassInformer.class));
		repository.add(createServiceFor(User.class, UserInformer.class));
		repository.add(createServiceFor(Theme.class, ThemeInformer.class));
		
		// and reference them
		tagService = repository.get(Tag.class);
		postService = repository.get(Post.class);
		postSubService= repository.get(PostSubClass.class);
		userService = repository.get(User.class);
		themeService = repository.get(Theme.class);
	}

	protected abstract GraphType createGraph(GraphProvider graphProvider);

	protected abstract <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> createServiceFor(Class<Type> beanClass, Class<InformerType> informerClass);

	/**
	 * @return the tagService
	 * @category getter
	 * @category tagService
	 */
	public FinderCrudService<Tag, TagInformer> getTagService() {
		return tagService;
	}

	/**
	 * @return the postService
	 * @category getter
	 * @category postService
	 */
	public FinderCrudService<Post, PostInformer> getPostService() {
		return postService;
	}

	/**
	 * @return the postSubService
	 * @category getter
	 * @category postSubService
	 */
	public FinderCrudService<PostSubClass, PostSubClassInformer> getPostSubService() {
		return postSubService;
	}

	/**
	 * @return the userService
	 * @category getter
	 * @category userService
	 */
	public FinderCrudService<User, UserInformer> getUserService() {
		return userService;
	}

	/**
	 * Create a sail repository out of given graph
	 * @return
	 */
	public abstract SailRepository getRepository();
	
	/**
	 * Get a path where test data can be stored
	 */
	public abstract String usablePath();

	public String graphPath() {
		return graphProvider.path(usablePath());
	}
}
