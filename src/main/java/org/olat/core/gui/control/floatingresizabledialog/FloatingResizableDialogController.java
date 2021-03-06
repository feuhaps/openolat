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
*/ 
package org.olat.core.gui.control.floatingresizabledialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * Controller for the floating resizable inline panels which are javascript based and
 * done by the extjs library see webapp/static/js/ext*
 * 
 * <P>
 * Initial Date:  05.06.2007 <br>
 * @author guido
 */
public class FloatingResizableDialogController extends BasicController {
	
	private VelocityContainer wrapper;
	private String panelName;
	
	private int offsetX = -1;
	private int offsetY = -1;
	private int width   = -1;
	private int height  = -1;
	
	/**
	 * create a panel with default sizes and position
	 * @param ureq
	 * @param wControl
	 * @param content
	 * @param title sets a panel title
	 */
	public FloatingResizableDialogController( UserRequest ureq, WindowControl wControl, Component content, String title) {
		this(ureq, wControl, content, title, 200, 150, 300, 300, null, "", false, false);
	}
	
	/**
	 * creates a panel with a single content component
	 * @param ureq
	 * @param wControl
	 * @param content
	 * @param title
	 * @param initialWidth
	 * @param initialHeight
	 * @param offsetX
	 * @param offsetY
	 * @param resizable
	 */
	public FloatingResizableDialogController( UserRequest ureq, WindowControl wControl, Component content, String title, int initialWidth, int initialHeight,
			int offsetX, int offsetY, boolean resizable, boolean autoScroll) {
		this(ureq, wControl, content, title, initialWidth, initialHeight, offsetX, offsetY, null, "", resizable, autoScroll);
	}

	/**
	 * create a panel with an optional additions visual component which is collabsible
	 * @param ureq
	 * @param wControl
	 * @param content
	 * @param title
	 * @param initialWidth
	 * @param initialHeight
	 * @param offsetX
	 * @param offsetY
	 * @param collabsibleContent
	 * @param collabsibleContentPanelTitel
	 * @param autoScroll
	 * @param resizable
	 */
	public FloatingResizableDialogController(UserRequest ureq, WindowControl wControl,
			Component content, String title,
			int initialWidth, int initialHeight,
			int offsetX, int offsetY, Component collabsibleContent, String collabsibleContentPanelTitel, boolean resizable, boolean autoScroll) {
		this(ureq, wControl, content, title, initialWidth, initialHeight, offsetX, offsetY, collabsibleContent, collabsibleContentPanelTitel, resizable, autoScroll, false, null);
	}
		
	public FloatingResizableDialogController(UserRequest ureq, WindowControl wControl,
			Component content, String title, int initialWidth, int initialHeight, int offsetX, int offsetY,
			Component collabsibleContent, String collabsibleContentPanelTitel, boolean resizable, boolean autoScroll, boolean constrain, String uniquePanelName) {
		
		super(ureq, wControl);
		
		this.width   = initialWidth;
		this.height  = initialHeight;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		
		wrapper = createVelocityContainer("index");
		wrapper.put("panelContent", content);
		if (collabsibleContent != null) {
			wrapper.put("collapsibleContent", collabsibleContent);
		}
		
		String escapedTitle = StringHelper.escapeHtml(title);
		escapedTitle = StringHelper.escapeJavaScript(title);
		
		panelName = "o_extjsPanel_" + (uniquePanelName == null ? hashCode() : uniquePanelName);
		wrapper.contextPut("panelName", panelName);
		wrapper.contextPut("width", this.width);
		wrapper.contextPut("height", this.height);
		wrapper.contextPut("offsetX", this.offsetX);
		wrapper.contextPut("offsetY", this.offsetY);
		wrapper.contextPut("title", escapedTitle);
		wrapper.contextPut("collabsibleContentPanelTitel", StringEscapeUtils.escapeHtml(collabsibleContentPanelTitel));
		wrapper.contextPut("resizable", resizable);
		wrapper.contextPut("constrain", constrain);
		wrapper.contextPut("scroll", Boolean.valueOf(autoScroll).toString());
		if (wControl.getWindowBackOffice().getGlobalSettings().getAjaxFlags().isIframePostEnabled()) {
			wrapper.contextPut("renderOnce", new ConsumableBoolean(true));//panels should only be rendered once in ajax mode, otherwise they get doubled (e.g. switching tabs between open courses)
			wrapper.contextPut("renderAlways", Boolean.FALSE);
		} else {
			wrapper.contextPut("renderAlways", Boolean.TRUE);//render each time in non ajax mode
			wrapper.contextPut("renderOnce", new ConsumableBoolean(true));
		}
		
		wrapper.contextPut("ajaxFlags", wControl.getWindowBackOffice().getGlobalSettings().getAjaxFlags());
		
		//add the dialog javascript
		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/jquery/ui/jquery-ui-1.11.4.custom.dialog.min.js" }, null);
		wrapper.put("jsAdder", js);
		
		putInitialPanel(wrapper);
	}
	
	private static final Pattern allGeometry = Pattern.compile("^(\\d+),(\\d+):(\\d+),(\\d+)$");
	private static final Pattern posGeometry = Pattern.compile("^(\\d+),(\\d+)$");
	private static final Pattern parseGeometry = Pattern.compile("(\\d+)");
	
	public void setElementCSSClass(String cssClass) {
		if(StringHelper.containsNonWhitespace(cssClass)) {
			wrapper.contextPut("cssClass", cssClass);
		} else {
			wrapper.contextRemove("cssClass");
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals("geometry")) {
			String p = ureq.getHttpReq().getParameter("p");	
			if (p != null) {	
				if(allGeometry.matcher(p).matches()) {
					Matcher m = parseGeometry.matcher(p);
					
					if (m.find()) offsetX = Integer.parseInt(m.group());
					if (m.find()) offsetY = Integer.parseInt(m.group());
					if (m.find()) width   = Integer.parseInt(m.group());
					if (m.find()) height  = Integer.parseInt(m.group());
					
					boolean dirt = wrapper.isDirty();
					wrapper.contextPut("width"  , width);
					wrapper.contextPut("height" , height);
					wrapper.contextPut("offsetX", offsetX);
					wrapper.contextPut("offsetY", offsetY);
					wrapper.setDirty(dirt);
				} else if(posGeometry.matcher(p).matches()) {
					Matcher m = parseGeometry.matcher(p);
					
					if (m.find()) offsetX = Integer.parseInt(m.group());
					if (m.find()) offsetY = Integer.parseInt(m.group());
					
					boolean dirt = wrapper.isDirty();
					wrapper.contextPut("offsetX", offsetX);
					wrapper.contextPut("offsetY", offsetY);
					wrapper.setDirty(dirt);
				}
			}		
		} else if (source == wrapper) {
			if (event.getCommand().equals("close")) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
		//nothing to dispose
	}

	public String getPanelName() {
		return panelName;
	}
	
	public JSCommand getCloseCommand () {
		StringBuilder sb = new StringBuilder();
		sb.append("try{")
		  .append(" jQuery('#").append(getPanelName()).append("').dialog('destroy').remove();")
		  .append("}catch(e){}");
		return new JSCommand(sb.toString());
	}
	
	public void executeCloseCommand() {
		getWindowControl().getWindowBackOffice().sendCommandTo(getCloseCommand());
	}
}
