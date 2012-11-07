package com.dooapp.graphviz;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.QueryBuilder;
import com.dooapp.gaedo.finders.QueryExpression;
import com.dooapp.gaedo.finders.QueryStatement;
import com.dooapp.gaedo.finders.dynamic.ServiceGenerator;
import com.dooapp.gaedo.finders.dynamic.ServiceGeneratorImpl;
import com.dooapp.gaedo.finders.repository.ServiceBackedFieldLocator;
import com.dooapp.gaedo.finders.repository.ServiceRepository;
import com.dooapp.gaedo.finders.repository.SimpleServiceRepository;
import com.dooapp.gaedo.finders.root.BasicFieldInformerLocator;
import com.dooapp.gaedo.finders.root.CumulativeFieldInformerLocator;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;
import com.dooapp.gaedo.finders.root.ReflectionBackedInformerFactory;
import com.dooapp.gaedo.properties.FieldBackedPropertyProvider;
import com.dooapp.gaedo.properties.PropertyProvider;
import com.dooapp.gaedo.utils.PropertyChangeEmitter;

public class Simpletest {
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";

	@Test
	public void loadService() {
		Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
		ServiceRepository repository = new SimpleServiceRepository();
		PropertyProvider provider = new FieldBackedPropertyProvider();
		CumulativeFieldInformerLocator locator = new CumulativeFieldInformerLocator();
		locator.add(new BasicFieldInformerLocator());
		locator.add(new ServiceBackedFieldLocator(repository));
		ReflectionBackedInformerFactory reflectiveFactory = new ReflectionBackedInformerFactory(
				locator, provider);
		ProxyBackedInformerFactory proxyInformerFactory = new ProxyBackedInformerFactory(
				reflectiveFactory);

		// Now add some services
		repository.add(new CollectionBackedTagFinderService(
				proxyInformerFactory));
		ServiceGenerator generator = new ServiceGeneratorImpl(provider);
//		generator.addDynamicHandlerListener(new QueryStatementDotGeneratorListener());
		FinderCrudService<DocumentedTag, DocumentedTagInformer> backend = repository.get(DocumentedTag.class);
		((PropertyChangeEmitter) backend).addPropertyChangeListener(QueryStatement.STATE_PROPERTY, new DotGeneratorGrapherChangeListener());
		DynamicDocumentedTagService service = generator.generate(DynamicDocumentedTagService.class, backend);
		service.findAllByTextStartsWithOrInterestGreaterThanSortByTextAscending(A, 0);
		Logger.getLogger("").getHandlers()[0].flush();
	}
}
