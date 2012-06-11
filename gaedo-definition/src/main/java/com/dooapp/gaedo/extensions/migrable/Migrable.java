package com.dooapp.gaedo.extensions.migrable;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A migrable object supports association to a Migration class, that will allow data service to provide migration code, when stored object is not of the same version
 * (as would have given the serialVersionUID field, as an example) of actually loaded class.
 * 
 *  Usage depends upon the FinderCrudService implementation but, in a general fashion, it should work as follow :
 *  
 *  at class laoding, this annotation is read, the storageVersion field is located, and the migratorClass is located.
 *  When an object is read from storage, if the value of its storageVersion field differs from the version given by migratorClass, the migrate method
 *  is called on data coming from storage (theorically before the object is loaded, but it is storage-dependant).
 *  
 * @author ndx
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Migrable {
	/**
	 * Class used for migration.
	 * @return
	 */
	Class<? extends Migrator> migratorClass();
}
