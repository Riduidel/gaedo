package com.dooapp.gaedo.blueprints;

import java.io.File;

import org.junit.After;

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
import com.tinkerpop.blueprints.pgm.Graph;

public abstract class AbstractGraphTest<GraphType extends Graph> {

	protected String name;
	protected GraphType graph;
	protected SimpleServiceRepository repository;
	protected GraphProvider graphProvider;
	protected FieldBackedPropertyProvider provider;
	protected CumulativeFieldInformerLocator locator;
	protected ReflectionBackedInformerFactory reflectiveFactory;
	protected ProxyBackedInformerFactory proxyInformerFactory;

	public AbstractGraphTest(GraphProvider graph) {
		this.name = graph.getName();
		this.graphProvider = graph;
	}

	@After
	public void unload() throws Exception {
		graph.shutdown();
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
	}

	protected abstract GraphType createGraph(GraphProvider graphProvider);

	protected abstract <Type> FinderCrudService<Type, Informer<Type>> createServiceFor(Class<Type> beanClass, Class<? extends Informer<Type>> informerClass);
}
