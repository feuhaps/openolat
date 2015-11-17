package de.bps.olat.portal.institution;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.UserConstants;

class PolymorphLink {
	public String defaultId;
	public String linkType;
	public String linkText;
	public List<PolymorphLinkElement> element;

	protected String getDefaultLink() {
		return this.defaultId;
	}

	protected String getLinkType() {
		return this.linkType;
	}

	public PolymorphLink() {
		//
	}

	/**
	 * used to check over the given rule set and find a matching rule for the user
	 * @param ureq ... we need to get the user from somewhere 
	 * @return Id from the first matching rule, otherwise <b>null</b>  
	 */
	protected String getResultIDForUser(UserRequest ureq) {
		if(element == null) return null;

		// first value --> orgUnit | second value --> studySubject must be equivalent with enumeration in PolymorphLinkElement

		String orgunit = ureq.getIdentity().getUser().getProperty(UserConstants.ORGUNIT, ureq.getLocale());
		String studysubject = ureq.getIdentity().getUser().getProperty(UserConstants.STUDYSUBJECT, ureq.getLocale());

		String[] userValues = {
						orgunit != null ? orgunit : "",
						studysubject != null ? studysubject : "" };
		
		for (PolymorphLinkElement elem : element) {
			switch (elem.getCondition()) {
				case 0:
					if (userValues[elem.getAttrib()].startsWith(elem.getValue())) return elem.getId(); break;
				case 1:
					if (userValues[elem.getAttrib()].equals(elem.getValue())) return elem.getId(); break;
				case 2:
					if (userValues[elem.getAttrib()].contains(elem.getValue())) return elem.getId(); break;
			}
		}
		return null;
	}
	
	protected boolean hasConditions() {
		return (element != null && element.size() > 0);
	}

	protected String getLinkText() {
		return linkText;
	}
}