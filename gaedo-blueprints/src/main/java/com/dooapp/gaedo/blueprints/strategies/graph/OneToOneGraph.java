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

	@Override
	public Class<? extends Annotation> annotationType() {
		return OneToOne.class;
	}

	@Override
	public CascadeType[] cascade() {
		return new CascadeType[] { CascadeType.PERSIST, CascadeType.MERGE };
	}
}