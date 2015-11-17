package de.bps.olat.portal.links;

import java.util.UUID;

import org.olat.core.util.StringHelper;

/**
 * @author skoeber
 *
 */
class PortletLink {
	
	private String title, url, target, language, description;
	private String identifier;
	private transient PortletInstitution institution;
	
	public PortletLink(String title, String url, String target, String language, String description, String identifier) {
		setTitle(title);
		setUrl(url);
		setTarget(target);
		setLanguage(language);
		setDescription(description);
		setIdentifier(identifier);
	}

	public PortletInstitution getInstitution() {
		return institution;
	}

	public void setInstitution(PortletInstitution institution) {
		this.institution = institution;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setIdentifier(String identifier){
		if (identifier == null) {
			this.identifier = UUID.randomUUID().toString().replace("-", "");
		} else {
			this.identifier = identifier;
		}
	}
	
	public String getIdentifier(){
		if (!StringHelper.containsNonWhitespace(identifier)){
			setIdentifier(null);
		}
		return identifier;
	}
}