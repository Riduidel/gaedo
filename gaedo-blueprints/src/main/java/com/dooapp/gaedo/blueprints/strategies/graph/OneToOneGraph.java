package com.dooapp.gaedo.blueprints.strategies.graph;

import java.lang.annotation.Annotation;

import javax.persistence.CascadeType;
import javax.persistence.OneToOne;

import com.dooapp.gaedo.blueprints.strategies.AbstractJPAGraphImplementation;

public final class OneToOneGraph extends AbstractJPAGraphImplementation implements OneToOne {
	/**
	 * @param graphBasedPropertyBuilder
	 */
	OneToOneGraph(Class entity) {
		super(entity);
	}

	public OneToOneGraph(Class targetEntity, CascadeType[] cascadeType) {
		super(targetEntity, cascadeType);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return OneToOne.class;
	}

	@Override
	public CascadeType[] cascade() {
		return cascadeType;
	}
}