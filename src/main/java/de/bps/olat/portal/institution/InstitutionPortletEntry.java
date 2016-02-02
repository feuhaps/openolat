package de.bps.olat.portal.institution;

import java.util.Collections;
import java.util.List;

/**
 * 
 * Description:<br>
 * This is one entry of the institution portlet.
 * 
 * <P>
 * Initial Date:  21.07.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
class InstitutionPortletEntry {

	public List<InstitutionPortletSupervisorEntry> supervisor;
	public List<PolymorphLink> polymorphlink;
	public Value logo;
	public Value name;
	public Value url;
	public String shortname;

	/**
	 * @param institutionName Name of the inst.
	 * @param institutionUrl URL of the inst.
	 * @param institutionLogo Logo file name of the inst.
	 * @param supervisors The supervisors. List of type InstitutionportletSupervisorEntry.
	 */
	public InstitutionPortletEntry() {
		//
	}

	/**
	 * @return Returns the institutionLogo.
	 */
	public String getInstitutionLogo() {
		return logo == null ? null : logo.value;
	}

	/**
	 * @return Returns the institutionName.
	 */
	public String getInstitutionName() {
		return name == null ? null : name.value;
	}

	/**
	 * @return Returns the institutionUrl.
	 */
	public String getInstitutionUrl() {
		return url == null ? null : url.value;
	}

	/**
	 * @return Returns the supervisors.
	 */
	public List<InstitutionPortletSupervisorEntry> getSupervisors() {
		if(supervisor == null) {
			return Collections.emptyList();
		}
		return supervisor;
	}

	public List<PolymorphLink> getPolymorphLinks() {
		if(polymorphlink == null) {
			return Collections.emptyList();
		}
		return polymorphlink;
	}
}