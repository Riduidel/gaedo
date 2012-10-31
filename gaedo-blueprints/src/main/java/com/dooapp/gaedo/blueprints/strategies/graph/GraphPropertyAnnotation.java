package com.dooapp.gaedo.blueprints.strategies.graph;

import java.lang.annotation.Annotation;

import com.dooapp.gaedo.blueprints.annotations.GraphProperty;
import com.dooapp.gaedo.blueprints.strategies.PropertyMappingStrategy;

public class GraphPropertyAnnotation implements GraphProperty {

	private final String name;
	
	private final PropertyMappingStrategy strategy;
	
	private final String[] contexts;

	public GraphPropertyAnnotation(String name, PropertyMappingStrategy strategy, String[] contexts) {
		super();
		this.name = name;
		this.strategy = strategy;
		this.contexts = contexts;
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return GraphProperty.class;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public PropertyMappingStrategy mapping() {
		return strategy;
	}

	@Override
	public String[] contexts() {
		return contexts;
	}

}
