package com.dooapp.gaedo.extensions.migrable;

import com.dooapp.gaedo.properties.Property;

public class MigratorUtils {

	/**
	 * Helper method for easier writing of {@link Migrator#getMigratorProperty(Property[])}
	 * @param liveVersionFieldName
	 * @param persistentVersionFieldName
	 * @param declaring
	 * @param properties
	 * @return
	 */
	public static Property getMigratorProperty(String liveVersionFieldName, String persistentVersionFieldName, Class declaring, Property[] properties) {
		Property liveVersion = null;
		for(Property p : properties) {
			if(p.getName().equals(liveVersionFieldName)) {
				liveVersion = p;
			}
		}
		return new DelegateProperty(persistentVersionFieldName, liveVersion, declaring); 
	}

}
