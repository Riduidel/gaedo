package com.dooapp.gaedo.blueprints.strategies.graph;

import java.lang.annotation.Annotation;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

import com.dooapp.gaedo.blueprints.strategies.AbstractJPAGraphImplementation;

public final class OneToManyGraph extends AbstractJPAGraphImplementation implements OneToMany {

	public OneToManyGraph(Class targetEntity) {
		super(targetEntity);
	}


	@Override
	public Class<? extends Annotation> annotationType() {
		return OneToMany.class;
	}

	@Override
	public CascadeType[] cascade() {
		return new CascadeType[] { CascadeType.PERSIST, CascadeType.MERGE };
	}
}
