package com.dooapp.gaedo.blueprints.annotations;

/**
 * Define a usable entity namespace by given both its prefix and its uri 
 * @author ndx
 *
 */
public @interface Namespace {
	public String prefix();
	
	public String uri();
}
