/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.portal.institution;

import java.io.File;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.logging.StartupException;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.naming.NoNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;


public class InstitutionPortlet extends AbstractPortlet {
	private String cssWrapperClass = "o_portlet_institutions";

	private static final String CONFIG_FILE = "/WEB-INF/olat_portals_institution.xml";
	private static FastHashMap institutions = null;

	public static final String TYPE_COURSE = "course";
	public static final String TYPE_CATALOG = "catalog";
	
	public static String HTTP_REQUEST_ATTRIBUT="catalog_node_id";
	private Controller runCtr;

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		String title = getConfiguration().get("title_" + getTranslator().getLocale().toString());
		if (title == null) {
			title = getTranslator().translate("institution.title");
		}
		return title;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		String desc = getConfiguration().get("description_" + getTranslator().getLocale().toString());
		if (desc == null) {
			desc = getTranslator().translate("institution.description");
		}
		return desc;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		if (institutions == null) init();
		InstitutionPortlet p = new InstitutionPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(InstitutionPortlet.class, ureq.getLocale()));
		// override css class if configured
		String cssClass = configuration.get("cssWrapperClass");
		if (cssClass != null) p.setCssWrapperClass(cssClass);
		return p;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr =  new InstitutionPortletRunController(ureq, wControl);
		return runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		disposeRunComponent();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		return cssWrapperClass;
	}

	/**
	 * Helper used to overwrite the default css class with the configured class
	 * @param cssWrapperClass
	 */
	void setCssWrapperClass(String cssWrapperClass) {
		this.cssWrapperClass = cssWrapperClass;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		if (runCtr != null) {
			runCtr.dispose();
			runCtr = null;
		}
	}

	/**
	 * initializes the institution portlet config
	 */
	public void init() {

		institutions = new FastHashMap();
		
		File configurationFile = new File(WebappHelper.getContextRealPath(CONFIG_FILE));
		XStream xstream = getInstitutionConfigXStream();
		InstitutionConfiguration configuration = (InstitutionConfiguration)xstream.fromXML(configurationFile);
		
		for(InstitutionPortletEntry institution: configuration.getInstitution()) {
			String shortName = institution.shortname;
			if (shortName == null) { 
				throw new StartupException("Institution portlet startup: No shortname given for one entry!");
			}
			institutions.put(shortName.toLowerCase(), institution);
		}

		// from now on optimize for non-synchronized read access
		institutions.setFast(true);
	}

	/**
	 * 
	 * @param institution
	 * @return The entry, or null if not found
	 */
	public static InstitutionPortletEntry getInstitutionPortletEntry(String institution) {
		return (InstitutionPortletEntry) institutions.get(institution);
	}
		
	public static XStream getInstitutionConfigXStream() {
		XStream xstream = new XStream(new XppDriver(new NoNameCoder()));
		xstream.alias("configuration", InstitutionConfiguration.class);
		xstream.addImplicitCollection(InstitutionConfiguration.class, "institution", "institution", InstitutionPortletEntry.class);
		xstream.alias("institution", InstitutionPortletEntry.class);
		xstream.addImplicitCollection(InstitutionPortletEntry.class, "polymorphlink", "polymorphlink", PolymorphLink.class);
		xstream.aliasAttribute(InstitutionPortletEntry.class, "shortname", "shortname");
		xstream.alias("logo", Value.class);
		xstream.alias("name", Value.class);
		xstream.alias("url", Value.class);
		
		xstream.alias("supervisor", InstitutionPortletSupervisorEntry.class);
		xstream.addImplicitCollection(InstitutionPortletEntry.class, "supervisor", "supervisor", InstitutionPortletSupervisorEntry.class);
		xstream.alias("person", Value.class);
		xstream.alias("phone", Value.class);
		xstream.alias("email", Value.class);
		xstream.alias("blog", Value.class);
		//polymorph link
		xstream.alias("polymorphlink", PolymorphLink.class);
		xstream.aliasAttribute(PolymorphLink.class, "defaultId", "default_targetid");
		xstream.aliasAttribute(PolymorphLink.class, "linkType", "type");
		xstream.aliasAttribute(PolymorphLink.class, "linkText", "text");
		//polymorph link element
		xstream.alias("element", PolymorphLinkElement.class);
		xstream.addImplicitCollection(PolymorphLink.class, "element", "element", PolymorphLinkElement.class);
		xstream.aliasAttribute(PolymorphLinkElement.class, "attribute", "attribute");
		xstream.aliasAttribute(PolymorphLinkElement.class, "value", "value");
		xstream.aliasAttribute(PolymorphLinkElement.class, "cond", "condition");
		xstream.aliasAttribute(PolymorphLinkElement.class, "id", "targetid");

		xstream.aliasAttribute(Value.class, "value", "value");
		return xstream;
	}
}
