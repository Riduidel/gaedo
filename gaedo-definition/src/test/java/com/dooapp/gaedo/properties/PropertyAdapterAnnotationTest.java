package com.dooapp.gaedo.properties;

import java.lang.annotation.Annotation;

import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class PropertyAdapterAnnotationTest {

	@Test
	public void test() {
		AdaptedProperty tested = new AdaptedProperty();
		tested.setAnnotation(new Deprecated() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return Deprecated.class;
			}
		});
		
		assertThat(tested.getAnnotations().size(), IsNot.not(0));
		assertThat(tested.getAnnotation(Deprecated.class), IsNull.notNullValue());
	}

}
