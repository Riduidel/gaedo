package com.dooapp.gaedo.extensions.migrable;




public class VersionMigratorFactory {
	/**
	 * Create a version migrator from a given contained class
	 * @param containedClass
	 * @return null if none found. This is one of the few special cases where gaedo do not use the null-fail pattern
	 */
	public static Migrator create(Class<?> containedClass) {
		if(containedClass.isAnnotationPresent(Migrable.class)) {
			try {
				return containedClass.getAnnotation(Migrable.class).migratorClass().newInstance();
			} catch (Exception e) {
				throw new UnableToCreateMigratorException(e);
			}
		}
		return null;
	}
}
