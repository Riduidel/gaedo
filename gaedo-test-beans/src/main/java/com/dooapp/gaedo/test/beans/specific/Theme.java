package com.dooapp.gaedo.test.beans.specific;

import java.io.Serializable;

import javax.persistence.Id;

public class Theme implements Serializable {
	@Id
	public long id;
	
	public String name;
}
