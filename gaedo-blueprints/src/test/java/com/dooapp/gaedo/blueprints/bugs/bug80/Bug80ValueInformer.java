package com.dooapp.gaedo.blueprints.bugs.bug80;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.finders.informers.StringFieldInformer;

public interface Bug80ValueInformer extends Informer<Bug80Value> {
	public StringFieldInformer getId();
	public StringFieldInformer getText();
}
