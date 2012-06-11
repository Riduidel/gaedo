package com.dooapp.gaedo.test.beans.specific;

import java.io.Serializable;

public class Theme implements Serializable {
	public String name;
	
	public Class<? extends User> allowedUser;
}
