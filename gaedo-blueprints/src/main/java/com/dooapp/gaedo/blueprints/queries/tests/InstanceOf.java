package com.dooapp.gaedo.blueprints.queries.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.GraphUtils;
import com.dooapp.gaedo.blueprints.operations.LiteralInCollectionUpdaterProperty;
import com.dooapp.gaedo.blueprints.strategies.GraphMappingStrategy;
import com.dooapp.gaedo.blueprints.transformers.LiteralHelper;
import com.dooapp.gaedo.properties.ClassCollectionProperty;
import com.dooapp.gaedo.properties.Property;
import com.dooapp.gaedo.utils.CollectionUtils;
import com.tinkerpop.blueprints.Vertex;

public class InstanceOf extends MonovaluedValuedVertexTest<Object, Class<?>> implements VertexTest {
	/**
	 * Improve provided path by automagically adding a classes collection property as end property when end property is either
	 * <ul>
	 * <li>null in which case the full path is null</li>
	 * <li>a property for which declared type, or type in collection, can be cast to one of types managed by service repository</li>
	 * </ul>
	 * In other words all the cases where destination property denotes an object managed by service.
	 * @param driver
	 * @param path
	 * @return
	 */
	private static Iterable<Property> improvePath(GraphDatabaseDriver driver, Iterable<Property> path) {
		/* we declare the class collection property as member of this type, as we won't use it as storage mechanism, but rather as adressing one
		 * in which case the given class is not meaningful
		 */
		Property classCollection = new ClassCollectionProperty(InstanceOf.class);

		List<Property> list = new ArrayList<Property>(CollectionUtils.asList(path));
		if(list.size()==0) {
			return Arrays.asList(classCollection);
		} else {
			Property last = list.get(list.size()-1);
			Class containedValueType = (Class) LiteralInCollectionUpdaterProperty.inferElementTypeIn(last.getGenericType());
			if(driver.getRepository().containsKey(containedValueType)) {
				list.add(classCollection);
			}
			return list;
		}
	}

	public InstanceOf(GraphMappingStrategy<?> strategy, GraphDatabaseDriver driver, Iterable<Property> path, Class<?> type) {
		super(strategy, driver, improvePath(driver, path), type);
	}

	@Override
	public void accept(VertexTestVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean callMatchManaged(Vertex currentVertex, Property finalProperty) {
		List<Property> localPath = Arrays.asList(finalProperty);
		CollectionContains classesContains = new CollectionContains(strategy, driver, localPath, expected);
		/*
		 * Now classes are stored as literal values, this distinction is quite important
		 */
		return classesContains.callMatchLiteral(currentVertex, finalProperty);
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
		if(finalProperty instanceof ClassCollectionProperty) {
			return callMatchManaged(currentVertex, finalProperty);
		}
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
