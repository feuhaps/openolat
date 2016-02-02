package de.bps.olat.portal.institution;

class Value {
	public String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value == null ? "null" : value;
	}
}