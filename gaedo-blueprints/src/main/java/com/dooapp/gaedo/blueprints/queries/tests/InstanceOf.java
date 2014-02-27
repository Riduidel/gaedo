package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.Arrays;
import java.util.List;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralHelper;
import com.dooapp.gaedo.blueprints.transformers.LiteralTransformer;
import com.dooapp.gaedo.blueprints.transformers.Literals;
import com.dooapp.gaedo.blueprints.transformers.TupleTransformer;
import com.dooapp.gaedo.blueprints.transformers.Tuples;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.tinkerpop.blueprints.Vertex;

public class InstanceOf extends MonovaluedValuedVertexTest<Object, Class<?>> implements VertexTest {

	public InstanceOf(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> path, Class<?> type) {
		super(strategy, driver, path, type);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		/* we declare the class collection property as member of this type, as we won't use it as storage mechanism, but rather as adressing one
		 * in which case the given class is not meaningful
		 */
		Property classCollection = new ClassCollectionProperty(this.getClass());
		List<Property> localPath = Arrays.asList(classCollection);
		CollectionContains classesContains = new CollectionContains(strategy, driver, localPath, expected);
		/*
		 * Now classes are stored as literal values, this distinction is quite important
		 */
		return classesContains.callMatchLiteral(currentVertex, classCollection);
	}

	/**
	 * We test type of a literal value ... this is not so cool as we have to load property to check its type
	 * @param currentVertex
	 * @param finalProperty
	 * @return
	 * @see com.dooapp.gaedo.blueprints.queries.tests.MonovaluedValuedVertexTest#callMatchLiteral(com.tinkerpop.blueprints.Vertex, com.dooapp.gaedo.properties.Property)
	 */
	@Override
	protected boolean callMatchLiteral(Vertex currentVertex, Property finalProperty) {
		Class<?> type = finalProperty.getType();
		String effectiveGraphValue = currentVertex.getProperty(GraphUtils.getEdgeNameFor(finalProperty));
		if(effectiveGraphValue!=null) {
			Object effectiveObjectValue = LiteralHelper.getLiteralFromText(expected.getClassLoader(), objectsBeingAccessed, finalProperty, effectiveGraphValue);
			if(effectiveObjectValue!=null) {
				return expected.isInstance(effectiveObjectValue);
			}
		}
		return false;
	}

}
