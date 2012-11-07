package com.dooapp.gaedo.blueprints.strategies;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;

public abstract class AbstractJPAGraphImplementation {
	private static final CascadeType[] DEFAULT_CASCADE_TYPE = new CascadeType[] { CascadeType.PERSIST, CascadeType.MERGE };


	protected final Class targetEntity;
	protected final CascadeType[] cascadeType;

	public AbstractJPAGraphImplementation(Class targetEntity, CascadeType[] cascadeType) {
		super();
		this.targetEntity = targetEntity;
		this.cascadeType = cascadeType;
	}

	public AbstractJPAGraphImplementation(Class targetEntity) {
		super();
		this.targetEntity = targetEntity;
		cascadeType = DEFAULT_CASCADE_TYPE;
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
