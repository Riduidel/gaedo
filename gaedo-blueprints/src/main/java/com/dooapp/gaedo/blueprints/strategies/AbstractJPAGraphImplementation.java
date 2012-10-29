package com.dooapp.gaedo.blueprints.strategies;

import javax.persistence.FetchType;

public abstract class AbstractJPAGraphImplementation {

	protected final Class targetEntity;

	public AbstractJPAGraphImplementation(Class targetEntity) {
		super();
		this.targetEntity = targetEntity;
	}

	public Class targetEntity() {
		return targetEntity;
	}

	public boolean optional() {
		return false;
	}

	public String mappedBy() {
		return "";
	}

	public FetchType fetch() {
		return FetchType.EAGER;
	}

}
