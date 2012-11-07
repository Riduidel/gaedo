package com.dooapp.graphviz;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.collections.CollectionBackedFinderService;
import com.dooapp.gaedo.finders.root.ProxyBackedInformerFactory;

public class CollectionBackedTagFinderService extends
		CollectionBackedFinderService<DocumentedTag, DocumentedTagInformer> implements
		FinderCrudService<DocumentedTag, DocumentedTagInformer> {

	public CollectionBackedTagFinderService(ProxyBackedInformerFactory factory) {
		super(DocumentedTag.class, DocumentedTagInformer.class, factory);
	}

}
