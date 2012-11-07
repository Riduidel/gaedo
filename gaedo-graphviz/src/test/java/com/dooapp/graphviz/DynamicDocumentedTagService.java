package com.dooapp.graphviz;

import java.util.Collection;

import com.dooapp.gaedo.finders.dynamic.DynamicFinder;

public interface DynamicDocumentedTagService extends DynamicFinder<DocumentedTag, DocumentedTagInformer> {
	public Collection<DocumentedTag> findAllByTextStartsWithOrInterestGreaterThanSortByTextAscending(String t, double v);
}
