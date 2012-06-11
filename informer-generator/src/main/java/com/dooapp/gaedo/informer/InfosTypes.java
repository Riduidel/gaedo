package com.dooapp.gaedo.informer;

enum InfosTypes {
	FIELD(""), GETTER("get"), SETTER("set");

	public String prefix;

	private InfosTypes(String prefix) {
		this.prefix = prefix;
	}

	public String getNameFor(String name) {
		switch (this) {
		case FIELD:
			return name;
		default:
			int prefixLength = prefix.length();
			return name.substring(prefixLength, prefixLength + 1).toLowerCase()
							+ (name.length() > prefixLength + 1 ? name.substring(prefixLength + 1) : "");
		}
	}
}