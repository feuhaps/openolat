/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.id.context;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.info.WindowControlInfo;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  14.06.2006 <br>
 *
 * @author Felix Jost
 */
public class StackedBusinessWindowControl implements WindowControl {
	private final WindowControl origWControl;
	private final BusinessControl businessControl;

	StackedBusinessWindowControl(WindowControl origWControl, BusinessControl businessControl) {
		this.origWControl = origWControl;
		this.businessControl = businessControl;
		
	}
	
	public BusinessControl getBusinessControl() {
		// inject the new business control here
		return businessControl;
	}

	public WindowControlInfo getWindowControlInfo() {
		return origWControl.getWindowControlInfo();
	}

	public void makeFlat() {
		origWControl.makeFlat();
	}

	public void pop() {
		origWControl.pop();
	}

	public void pushAsModalDialog(Component comp) {
		origWControl.pushAsModalDialog(comp);
	}

	@Override
	public void pushAsCallout(Component comp, String targetId) {
		origWControl.pushAsCallout(comp, targetId);
	}

	public void pushToMainArea(Component comp) {
		origWControl.pushToMainArea(comp);
	}

	public void setError(String string) {
		origWControl.setError(string);
	}

	public void setInfo(String string) {
		origWControl.setInfo(string);
	}

	public void setWarning(String string) {
		origWControl.setWarning(string);
	}

	public WindowBackOffice getWindowBackOffice() {
		return origWControl.getWindowBackOffice();
	}
	

}
