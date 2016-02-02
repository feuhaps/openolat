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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util.mail.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;

class EMailIdentity implements Identity {

	private static final long serialVersionUID = -2899896628137672419L;
	private final String email;
	private final User user;
	private final Locale locale;

	public EMailIdentity(String email, Locale locale) {
		this.email = email;
		user = new EMailUser(email);
		this.locale = locale;
	}

	@Override
	public Long getKey() {
		return null;
	}
	
	@Override
	public String getExternalId() {
		return null;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return this == persistable;
	}

	@Override
	public Date getCreationDate() {
		return null;
	}

	@Override
	public String getName() {
		return email;
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public Date getLastLogin() {
		return null;
	}

	@Override
	public void setLastLogin(Date loginDate) {/**/
	}

	@Override
	public Integer getStatus() {
		return null;
	}

	@Override
	public void setStatus(Integer newStatus) {/**/
	}

	@Override
	public void setName(String name) {/**/
	}
	

	private class EMailUser implements User, ModifiedInfo {

		private static final long serialVersionUID = 7260225880639460228L;
		private final EMailPreferences prefs = new EMailPreferences();
		private Map<String, String> data = new HashMap<String, String>();

		public EMailUser(String email) {
			data.put(UserConstants.FIRSTNAME, "");
			data.put(UserConstants.LASTNAME, "");
			data.put(UserConstants.EMAIL, email);
		}

		public Long getKey() {
			return null;
		}

		public boolean equalsByPersistableKey(Persistable persistable) {
			return this == persistable;
		}

		public Date getLastModified() {
			return null;
		}

		@Override
		public void setLastModified(Date date) {
			//
		}

		public Date getCreationDate() {
			return null;
		}

		public void setProperty(String propertyName, String propertyValue) {
			//
		}

		public void setPreferences(Preferences prefs) {
			//
		}

		public String getProperty(String propertyName, Locale locale) {
			return data.get(propertyName);
		}

		public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {/**/
		}

		public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
			return data.get(propertyName);
		}

		public Preferences getPreferences() {
			return prefs;
		}
	}

	private class EMailPreferences extends Preferences {
		private static final long serialVersionUID = 7039109437910126584L;

		@Override
		public String getLanguage() {
			return locale.getLanguage();
		}

		@Override
		public void setLanguage(String l) {
			//
		}

		@Override
		public String getFontsize() {
			return null;
		}

		@Override
		public void setFontsize(String l) {
			//
		}

		@Override
		public String getNotificationInterval() {
			return null;
		}

		@Override
		public void setNotificationInterval(String notificationInterval) {/* */
		}

		@Override
		public String getReceiveRealMail() {
			return "true";
		}

		@Override
		public void setReceiveRealMail(String receiveRealMail) {
			//
		}

		@Override
		public boolean getInformSessionTimeout() {
			return false;
		}

		@Override
		public void setInformSessionTimeout(boolean b) {/* */
		}

		@Override
		public boolean getPresenceMessagesPublic() {
			return false;
		}

		@Override
		public void setPresenceMessagesPublic(boolean b) {/* */
		}
	}
}