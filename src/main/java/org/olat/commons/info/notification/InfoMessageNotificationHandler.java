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

package org.olat.commons.info.notification;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.commons.info.manager.InfoMessageManager;
import org.olat.commons.info.model.InfoMessage;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessageNotificationHandler extends LogDelegator implements NotificationsHandler {

	private static final String CSS_CLASS_ICON = "o_infomsg_icon";
	
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
		
			try {
				final Long resId = subscriber.getPublisher().getResId();
				final String resName = subscriber.getPublisher().getResName();
				String resSubPath = subscriber.getPublisher().getSubidentifier();
				String title = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resId);
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(title, CSS_CLASS_ICON), null);
				
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
				List<InfoMessage> infos = InfoMessageManager.getInstance().loadInfoMessageByResource(ores, resSubPath, null, compareDate, null, 0, 0);
				for(InfoMessage info:infos) {
					String desc = info.getTitle();
					String tooltip = info.getMessage();
					String infoBusinessPath = info.getBusinessPath() + "[InfoMessage:" + info.getKey() + "]";
					String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(infoBusinessPath);
					Date dateInfo = info.getModificationDate() == null ? info.getCreationDate() : info.getModificationDate();
					SubscriptionListItem subListItem = new SubscriptionListItem(desc, tooltip, urlToSend, infoBusinessPath, dateInfo, CSS_CLASS_ICON);
					si.addSubscriptionListItem(subListItem);
				}
			} catch (Exception e) {
				logError("Unexpected exception", e);
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
		} else {
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		return si;
	}
	
	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(this.getClass(), locale);
		String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(subscriber.getPublisher().getResId());
		return translator.translate("notification.title", new String[]{displayName});
	}
	
	@Override
	public String getType() {
		return "InfoMessage";
	}
}
