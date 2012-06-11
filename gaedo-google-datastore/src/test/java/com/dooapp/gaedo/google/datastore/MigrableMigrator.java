package com.dooapp.gaedo.google.datastore;

import java.util.Collection;

import com.dooapp.gaedo.extensions.migrable.Migrator;
import com.dooapp.gaedo.extensions.migrable.MigratorUtils;
import com.dooapp.gaedo.finders.FinderCrudService;
import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.properties.Property;
import com.google.appengine.api.datastore.Entity;

public class MigrableMigrator implements Migrator {

	public static final String TEXT_UPDATED_BY_MIGRATOR = "text updated by migrator";

	@Override
	public Object getCurrentVersion() {
		return MigrableObject.storageVersion;
	}

	@Override
	public String getLiveVersionFieldName() {
		return "version";
	}

	@Override
	public String getPersistedVersionFieldName() {
		return Migrator.DEFAULT_PERSISTED_VERSION_FIELD_NAME;
	}

	/**
	 * Not usual implementation : when given a MigrableObject, it updates the version to an higher one and totally replace text
	 */
	@Override
	public <DataContent, ContainedClass> DataContent migrate(
			FinderCrudService<ContainedClass, ? extends Informer<ContainedClass>> service,
			DataContent nonMigratedContent, Object sourceVersion,
			Object targetVersion) {
		if(nonMigratedContent instanceof Entity) {
			Entity toMigrate = (Entity) nonMigratedContent;
			toMigrate.setProperty(Utils.getDatastoreFieldName(service.getInformer().get(getLiveVersionFieldName()).getField()), MigrableObject.storageVersion+1);
			toMigrate.setProperty(Utils.getDatastoreFieldName(service.getInformer().get("text").getField()), TEXT_UPDATED_BY_MIGRATOR);
			return (DataContent) toMigrate;
		}
		return null;
	}

	@Override
	public Property getMigratorProperty(Property[] properties) {
		return MigratorUtils.getMigratorProperty(getLiveVersionFieldName(), 
						getPersistedVersionFieldName(), getClass(), properties);
	}

	@Override
	public Property getMigratorProperty(Collection<Property> properties) {
		return getMigratorProperty(properties.toArray(new Property[properties.size()]));
	}
}
