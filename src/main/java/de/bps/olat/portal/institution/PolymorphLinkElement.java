package de.bps.olat.portal.institution;

class PolymorphLinkElement {
	protected static final String EQUALS = "equals";
	protected static final String STARTS_WITH = "starts_with";
	protected static final String CONTAINS = "contains";

	public String id;
	public String cond;
	public String value;
	public String attribute;

	public PolymorphLinkElement() {
		//
	}

	protected int getAttrib() {
		if ("orgunit".equals(attribute)) {
			return 0;
		} else if ("studysubject".equals(attribute)) {
			return 1;
		}
		return -1;
	}

	protected String getValue() {
		return value;
	}

	protected int getCondition() {
		if (STARTS_WITH.equals(cond)) {
			return 0;
		} else if (EQUALS.equals(cond)) {
			return 1;
		} else if (CONTAINS.equals(cond)) {
			return 2;
		}
		return -1;
	}

	protected String getId() {
		return id;
	}
}