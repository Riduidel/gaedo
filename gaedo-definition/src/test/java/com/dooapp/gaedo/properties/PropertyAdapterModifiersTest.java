package com.dooapp.gaedo.properties;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class PropertyAdapterModifiersTest {
	public static final int[] ALL_MODIFIERS = new int[] { Modifier.PUBLIC, Modifier.PROTECTED, Modifier.PRIVATE,
					Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL, Modifier.TRANSIENT, Modifier.VOLATILE, 
					Modifier.SYNCHRONIZED, Modifier.NATIVE, Modifier.STRICT,
					Modifier.INTERFACE, };
	
	
	private AdaptedProperty tested;


	@Before
	public void createTested() {
		tested = new AdaptedProperty();
	}
	
	@Test
	public void testWithPublic() {
		validate(Arrays.asList(Modifier.PUBLIC));
	}

	@Test
	public void testWithPublicNegated() {
		validate(Arrays.asList(Modifier.PUBLIC));
		tested.setModifier(Modifier.PUBLIC, false);
		// after negating, no more flag is set. Set one and test
		validate(Arrays.asList(Modifier.PRIVATE));
	}

	@Test
	public void testWithPrivateFinalTransient() {
		validate(Arrays.asList(Modifier.PRIVATE, Modifier.FINAL, Modifier.TRANSIENT));
	}
	
	@Test
	public void testWithProtectedStrictSynchronized() {
		validate(Arrays.asList(Modifier.PROTECTED, Modifier.STRICT, Modifier.SYNCHRONIZED));
	}

	public void validate(Collection<Integer> modifiers) {
		for (int m : modifiers) {
			tested.setModifier(m, true);
		}
		for(int m : ALL_MODIFIERS) {
			if(modifiers.contains(m))
				assertThat(tested.hasModifier(m), Is.is(true));
			else
				assertThat(tested.hasModifier(m), Is.is(false));
		}
	}
}
