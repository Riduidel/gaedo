package com.dooapp.gaedo.blueprints;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;

import org.junit.After;
import org.neo4j.kernel.impl.util.FileUtils;
import org.openrdf.repository.sail.SailRepository;

import com.dooapp.gaedo.blueprints.beans.PostSubClass;
import com.dooapp.gaedo.blueprints.beans.PostSubClassInformer;
import com.dooapp.gaedo.blueprints.strategies.StrategyType;
import com.dooapp.gaedo.extensions.views.InViewService;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.repository.InheriterRepository;
import com.dooapp.gaedo.finders.repository.NoSuchServiceException;
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
import com.tinkerpop.blueprints.Graph;

/**
 * Class defining all graph elements and properties, used by tests.
 * @author ndx
 *
 * @param <GraphType>
 */
public abstract class AbstractGraphEnvironment<GraphType extends Graph> {
	/**
	 * component bag that is used through a {@link ThreadLocal} reference, allowing standard usage in monothread tests, but creating new objects
	 * (as a consequence avoiding most of synchronization bugs) when performing multithreaded tests
	 * @author ndx
	 *
	 */
	private class GaedoComponentBag {
		protected SimpleServiceRepository serviceRrepository;
		protected PropertyProvider provider;
		protected CumulativeFieldInformerLocator locator;
		protected ReflectionBackedInformerFactory reflectiveFactory;
		protected ProxyBackedInformerFactory proxyInformerFactory;
		private FinderCrudService<Tag, TagInformer> tagService;
		private FinderCrudService<Post, PostInformer> postService;
		private FinderCrudService<PostSubClass, PostSubClassInformer> postSubService;
		private FinderCrudService<User, UserInformer> userService;
		private FinderCrudService<Theme, ThemeInformer> themeService;
		private GraphType graph;

		public void load() {
			if(graph==null) {
				graph = createGraph(graphProvider);
				
				serviceRrepository = new InheriterRepository();
				provider = new FieldBackedPropertyProvider();
				locator = new CumulativeFieldInformerLocator();
				locator.add(new BasicFieldInformerLocator());
				locator.add(new ServiceBackedFieldLocator(serviceRrepository));
				locator.add(new LazyInterfaceInformerLocator());
				reflectiveFactory = new ReflectionBackedInformerFactory(
						locator, provider);
				proxyInformerFactory = new ProxyBackedInformerFactory(
						reflectiveFactory);
				
				// Now add some services
				tagService = createServiceFor(Tag.class, TagInformer.class, strategy );
				postService = createServiceFor(Post.class, PostInformer.class, strategy);
				postSubService= createServiceFor(PostSubClass.class, PostSubClassInformer.class, strategy);
				userService = createServiceFor(User.class, UserInformer.class, strategy);
				themeService = createServiceFor(Theme.class, ThemeInformer.class, strategy);
			}
		}

		public void unload() {
			if(graph!=null) {
				graph.shutdown();
				graph = null;
			}
		}
	}

	protected final String name;
	protected final GraphProvider graphProvider;
	protected ThreadLocal<GaedoComponentBag> bag = new ThreadLocal<GaedoComponentBag>();
	
	private StrategyType strategy = StrategyType.beanBased;

	public AbstractGraphEnvironment(GraphProvider graph) {
		this.name = getClass().getSimpleName()+" "+graph.getName();
		this.graphProvider = graph;
	}

	@After
	public void unload() throws Exception {
		synchronized(this) {
			getBag().unload();
			File f = new File(graphPath());
			try {
				FileUtils.deleteRecursively(f);
			} catch(IOException e) {
				System.out.println("there was a failure during graph deletion : "+e.getMessage());
			}
		}
	}

	public void loadService() throws Exception {
		synchronized(this) {
			getBag().load();
		}
	}

	protected abstract GraphType createGraph(GraphProvider graphProvider);

	public final <Type, InformerType extends Informer<Type>> InViewService<Type, InformerType, SortedSet<String>> createServiceFor(Class<Type> beanClass, Class<InformerType> informerClass, StrategyType strategy) {
		// It's an inheriterrepository ! so this call may return true even for a class which is not in repository (but which parent class is)
//		if(!getBag().serviceRrepository.containsKey(beanClass)) {
		
		boolean serviceExists = false;
		try {
			serviceExists = getBag().serviceRrepository.get(beanClass).getContainedClass().equals(beanClass);
		} catch(NoSuchServiceException e) {
			
		}
		if(!serviceExists) {
			FinderCrudService<Type, InformerType> created = doCreateServiceFor(beanClass, informerClass, strategy);
			getBag().serviceRrepository.add(created);
		}
		return (InViewService<Type, InformerType, SortedSet<String>>) getBag().serviceRrepository.get(beanClass);
	}

	protected abstract <Type, InformerType extends Informer<Type>> InViewService<Type, InformerType, SortedSet<String>> doCreateServiceFor(Class<Type> beanClass, Class<InformerType> informerClass, StrategyType strategy);

	/**
	 * @return the tagService
	 * @category getter
	 * @category tagService
	 */
	public FinderCrudService<Tag, TagInformer> getTagService() {
		return getBag().tagService;
	}

	/**
	 * @return the postService
	 * @category getter
	 * @category postService
	 */
	public FinderCrudService<Post, PostInformer> getPostService() {
		return getBag().postService;
	}

	/**
	 * @return the postSubService
	 * @category getter
	 * @category postSubService
	 */
	public FinderCrudService<PostSubClass, PostSubClassInformer> getPostSubService() {
		return getBag().postSubService;
	}

	/**
	 * @return the userService
	 * @category getter
	 * @category userService
	 */
	public FinderCrudService<User, UserInformer> getUserService() {
		return getBag().userService;
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
		return getBag().provider;
	}

	/**
	 * @return the locator
	 * @category getter
	 * @category locator
	 */
	public CumulativeFieldInformerLocator getLocator() {
		return getBag().locator;
	}

	/**
	 * @return the reflectiveFactory
	 * @category getter
	 * @category reflectiveFactory
	 */
	public ReflectionBackedInformerFactory getReflectiveFactory() {
		return getBag().reflectiveFactory;
	}

	/**
	 * @return the proxyInformerFactory
	 * @category getter
	 * @category proxyInformerFactory
	 */
	public InformerFactory getInformerFactory() {
		return getBag().proxyInformerFactory;
	}

	/**
	 * @return the serviceRrepository
	 * @category getter
	 * @category serviceRrepository
	 */
	public SimpleServiceRepository getServiceRrepository() {
		return getBag().serviceRrepository;
	}

	private GaedoComponentBag getBag() {
		if(bag.get()==null) {
			bag.set(new GaedoComponentBag());
			bag.get().load();
		}
		return bag.get();
	}

	/**
	 * @return the graph
	 * @category getter
	 * @category graph
	 */
	public GraphType getGraph() {
		while(getBag().graph==null) {
			synchronized(this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
                    // nothing to do but wait a little more
   				}
			}
		}
		return getBag().graph;
	}
}
