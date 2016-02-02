package de.bps.olat.portal.institution;

/**
 * 
 * Description:<br>
 * One supervisor.
 * 
 * <P>
 * Initial Date:  21.07.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
class InstitutionPortletSupervisorEntry {
	public Value phone;
	public Value email;
	public Value person;
	public Value url;
	public Value blog;

	/**
	 * @param supervisorName The supervisors name. 
	 * @param supervisorSurname The supervisors surname.
	 * @param supervisorPhone The supervisors phone number.
	 * @param supervisorMail The supervisors mail.
	 * @param supervisorBlog The supervisor Blog 	 
	 */
	public InstitutionPortletSupervisorEntry() {
		//
	}
	
	public String getSupervisorBlog() {
		return blog == null ? null : blog.value;
	}

	/**
	 * @return Returns the supervisorMail.
	 */
	public String getSupervisorMail() {
		return email == null ? null : email.value;
	}

	/**
	 * @return Returns the supervisorPhone.
	 */
	public String getSupervisorPhone() {
		return phone == null ? null : phone.value;
	}

	public String getSupervisorPerson() {
		return person == null ? null : person.value;
	}

	public String getSupervisorURL() {
		return url == null ? null : url.value;
	}
}