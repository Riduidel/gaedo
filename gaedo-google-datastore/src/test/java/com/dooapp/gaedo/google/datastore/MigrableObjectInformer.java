package com.dooapp.gaedo.google.datastore;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.informers.DoubleFieldInformer;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;

public interface MigrableObjectInformer extends
		Informer<MigrableObject> {
	public DoubleFieldInformer getId();
	public DoubleFieldInformer getVersion();
	public StringFieldInformer getText();
}
