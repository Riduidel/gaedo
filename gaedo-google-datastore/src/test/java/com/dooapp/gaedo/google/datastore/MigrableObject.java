package com.dooapp.gaedo.google.datastore;

import javax.persistence.Id;

import com.dooapp.gaedo.extensions.migrable.Migrable;

/**
 * An object implementing migrable ina  totally WRONG but totally testable fashion
 * @author ndx
 *
 */
@Migrable(migratorClass=MigrableMigrator.class)
public class MigrableObject {
	public static Integer storageVersion = 1;

	@Id
	public long id;
	
	public int version = storageVersion;
	
	public String text = "initial";
}
