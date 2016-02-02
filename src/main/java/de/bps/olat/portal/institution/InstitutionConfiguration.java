package de.bps.olat.portal.institution;

import java.util.Collections;
import java.util.List;

class InstitutionConfiguration {
	public List<InstitutionPortletEntry> institution;
	
	public List<InstitutionPortletEntry> getInstitution() {
		if(institution == null) {
			return Collections.emptyList();
		}
		return institution;
	}
}