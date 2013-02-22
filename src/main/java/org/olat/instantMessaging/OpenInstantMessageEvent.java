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
package org.olat.instantMessaging;

import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.model.Buddy;

/**
 * 
 * Description:<br>
 * Message to open a new message window. Only use this event with
 * the Single VM message bus with the SingleUserEventCenter!!!
 * 
 * <P>
 * Initial Date:  2 mar. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenInstantMessageEvent extends MultiUserEvent {

	private static final long serialVersionUID = -7767366726634855700L;
	
	private final UserRequest ureq;
	private Buddy buddy;
	private boolean vip;
	private String roomName;
	private OLATResourceable ores;

	public OpenInstantMessageEvent(UserRequest ureq) {
		super("openim");
		this.ureq = ureq;
	}
	
	public OpenInstantMessageEvent(UserRequest ureq, Buddy buddy) {
		this(ureq);
		this.buddy = buddy;
	}
	
	public OpenInstantMessageEvent(UserRequest ureq, OLATResourceable ores, String roomName, boolean vip) {
		this(ureq);
		this.ores = OresHelper.clone(ores);
		this.roomName = roomName;
		this.vip = vip;
	}

	public Buddy getBuddy() {
		return buddy;
	}

	public UserRequest getUserRequest() {
		return ureq;
	}

	public OLATResourceable getOres() {
		return ores;
	}
	
	public String getRoomName() {
		return roomName;
	}
	
	public boolean isVip() {
		return vip;
	}
}
