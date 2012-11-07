package com.dooapp.gaedo.extensions.migrable;

import java.util.Collection;

import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.properties.Property;

/**
 * A migrator is used to transfer objects from one version to one other
 * 
 * <b>warning</b> in various locations in this document we use the <code>serialVersionUID</code> as an example of field storing version information.
 * This is true, as it is the role of this field. But this is also false as most of our persistence services implementations won't store a static field.
 * @author ndx
 *
 */
public interface Migrator {
	/**
	 * A default name for persisted version field name
	 */
	public static final String DEFAULT_PERSISTED_VERSION_FIELD_NAME = "objectVersionFieldName";
	/**
	 * Get current version, to only call migration call when needed
	 * @return a version value, that should not be null.
	 */
	public Object getCurrentVersion();

	/**
	 * Get name of field used in object to store version. As an example, were serialVersionUID to be persistable, this method could return serialVersionUID
	 * @return the name of an existing field
	 */
	public String getLiveVersionFieldName();

	/**
	 * Get the name of the "column" or equivalent concept using the given storage mechanism used to store the {@link #getLiveVersionFieldName()}. This method exists
	 * to allow one to easily refactor a class without having any fear of changing the live version field name
	 * <b>BEWARE</b>For optimal usage, the value returned by this method (the name of your DB column/GAE element/and so on) should NEVER change, elsewhere compatibility will be broken.
	 * This why we provide you a default value, that should theorically not conflict easily with your code.
	 * @return a good practice would be to return {@link #DEFAULT_PERSISTED_VERSION_FIELD_NAME}
	 */
	public String getPersistedVersionFieldName();
	
	/**
	 * Migrate data from a given finder service from initial version to final one
	 * @param <DataContent> type of content this service uses before creating objects
	 * @param service source service. This fild is given to easily allow different kind of behaviours when the same object is used in different contexts.
	 * @param nonMigratedContent content initially loaded from service before to be transformed into an object
	 * @param sourceVersion source version
	 * @param targetVersion target version
	 * @return a transformed content, suitable to be injected into an object of target version
	 */
	public <DataContent, ContainedClass> DataContent migrate(FinderCrudService<ContainedClass, ? extends Informer<ContainedClass>> service, DataContent nonMigratedContent, Object sourceVersion, Object targetVersion);

	/**
	 * Creates and return a property allowing simple access to property
	 * @return an emulated which will be named by {@link #getPersistedVersionFieldName()} and which will return value of {@link #getLiveVersionFieldName()}
	 */
	public Property getMigratorProperty(Property[] properties);

	/**
	 * Creates and return a property allowing simple access to property
	 * @return an emulated which will be named by {@link #getPersistedVersionFieldName()} and which will return value of {@link #getLiveVersionFieldName()}
	 */
	public Property getMigratorProperty(Collection<Property> properties);
}
