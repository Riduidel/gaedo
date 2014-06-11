package com.dooapp.gaedo.blueprints.beans;

import com.dooapp.gaedo.finders.Informer;
import com.dooapp.gaedo.test.beans.InternalGaedoPostInformer;
import com.dooapp.gaedo.test.beans.UserInformer;

public interface PostSubClassInformer extends Informer<PostSubClass>, InternalGaedoPostInformer {
    public UserInformer getCreator();
}