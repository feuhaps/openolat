package de.bps.olat.portal.links;

import java.util.ArrayList;
import java.util.List;

/**
 * @author skoeber
 *
 */
class PortletInstitution {
	
	private String name; 
	private List<PortletLink> links;
	
	public PortletInstitution(String name) {
		this.name = name;
		this.links = new ArrayList<PortletLink>();
	}
	
	public PortletInstitution(String name, List<PortletLink> links) {
		this.name = name;
		this.links = links;
	}
	
	public void addLink(PortletLink link) {
		links.add(link);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<PortletLink> getLinks() {
		return links;
	}

	public void setLinks(List<PortletLink> links) {
		this.links = links;
	}
}