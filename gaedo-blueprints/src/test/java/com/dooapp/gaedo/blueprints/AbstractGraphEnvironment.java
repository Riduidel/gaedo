package com.dooapp.gaedo.blueprints;

import java.io.File;

import org.junit.After;
import org.neo4j.kernel.impl.util.FileUtils;
import org.openrdf.repository.sail.SailRepository;

import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.InformerFactory;
import com.dooapp.gaedo.finders.root.LazyInterfaceInformerLocator;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
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
	protected SimpleServiceRepository serviceRrepository;
	protected GraphProvider graphProvider;
	protected PropertyProvider provider;
	protected CumulativeFieldInformerLocator locator;
	protected ReflectionBackedInformerFactory reflectiveFactory;
	protected ProxyBackedInformerFactory proxyInformerFactory;
	private FinderCrudService<Tag, TagInformer> tagService;
	private FinderCrudService<Post, PostInformer> postService;
	private FinderCrudService<PostSubClass, PostSubClassInformer> postSubService;
	private FinderCrudService<User, UserInformer> userService;
	private FinderCrudService<Theme, ThemeInformer> themeService;
	private StrategyType strategy = StrategyType.beanBased;

	public AbstractGraphEnvironment(GraphProvider graph) {
		this.name = getClass().getSimpleName()+" "+graph.getName();
		this.graphProvider = graph;
	}

	@After
	public void unload() throws Exception {
		if(graph!=null) {
			graph.shutdown();
			graph = null;
		}
		File f = new File(graphPath());
		FileUtils.deleteRecursively(f);
	}

	public void loadService() throws Exception {
		serviceRrepository = new SimpleServiceRepository();
		provider = new FieldBackedPropertyProvider();
		locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(serviceRrepository));
		locator.add(new LazyInterfaceInformerLocator());
		reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);
		
		graph = createGraph(graphProvider);
		
		// Now add some services
		tagService = createServiceFor(Tag.class, TagInformer.class, strategy );
		postService = createServiceFor(Post.class, PostInformer.class, strategy);
		postSubService= createServiceFor(PostSubClass.class, PostSubClassInformer.class, strategy);
		userService = createServiceFor(User.class, UserInformer.class, strategy);
		themeService = createServiceFor(Theme.class, ThemeInformer.class, strategy);
	}

	protected abstract GraphType createGraph(GraphProvider graphProvider);

	public final <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> createServiceFor(Class<Type> beanClass, Class<InformerType> informerClass, StrategyType strategy) {
		if(!serviceRrepository.containsKey(beanClass)) {
			FinderCrudService<Type, InformerType> created = doCreateServiceFor(beanClass, informerClass, strategy);
			serviceRrepository.add(created);
		}
		return serviceRrepository.get(beanClass);
	}

	protected abstract <Type, InformerType extends Informer<Type>> FinderCrudService<Type, InformerType> doCreateServiceFor(Class<Type> beanClass, Class<InformerType> informerClass, StrategyType strategy);

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
	public abstract SailRepository getSailRepository();
	
	/**
	 * Get a path where test data can be stored
	 */
	public abstract String usablePath();

	public String graphPath() {
		return graphProvider.path(usablePath());
	}

	/**
	 * @return the provider
	 * @category getter
	 * @category provider
	 */
	public PropertyProvider getProvider() {
		return provider;
	}

	/**
	 * @param provider the provider to set
	 * @category setter
	 * @category provider
	 */
	public void setProvider(PropertyProvider provider) {
		this.provider = provider;
	}

	/**
	 * @return the locator
	 * @category getter
	 * @category locator
	 */
	public CumulativeFieldInformerLocator getLocator() {
		return locator;
	}

	/**
	 * @param locator the locator to set
	 * @category setter
	 * @category locator
	 */
	public void setLocator(CumulativeFieldInformerLocator locator) {
		this.locator = locator;
	}

	/**
	 * @return the reflectiveFactory
	 * @category getter
	 * @category reflectiveFactory
	 */
	public ReflectionBackedInformerFactory getReflectiveFactory() {
		return reflectiveFactory;
	}

	/**
	 * @return the proxyInformerFactory
	 * @category getter
	 * @category proxyInformerFactory
	 */
	public InformerFactory getInformerFactory() {
		return proxyInformerFactory;
	}

	/**
	 * @return the serviceRrepository
	 * @category getter
	 * @category serviceRrepository
	 */
	public SimpleServiceRepository getServiceRrepository() {
		return serviceRrepository;
	}

	/**
	 * @param serviceRrepository the serviceRrepository to set
	 * @category setter
	 * @category serviceRrepository
	 */
	public void setServiceRrepository(SimpleServiceRepository serviceRrepository) {
		this.serviceRrepository = serviceRrepository;
	}
}
