package com.dooapp.gaedo.test.beans;

import java.util.List;

import com.dooapp.gaedo.finders.dynamic.DynamicFinder;

public interface TagFinder extends DynamicFinder<Tag, TagInformer> {
	/**
	 * This one works !
	 * @param name
	 * @return
	 */
	public List<Tag> findAllByTextStartsWith(String name);
}
