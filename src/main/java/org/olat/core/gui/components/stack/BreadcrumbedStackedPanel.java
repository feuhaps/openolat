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
package org.olat.core.gui.components.stack;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BreadcrumbedStackedPanel extends Panel implements StackedPanel, BreadcrumbPanel, ComponentEventListener {
	private static final OLog log = Tracing.createLoggerFor(BreadcrumbedStackedPanel.class);
	private static final ComponentRenderer RENDERER = new BreadcrumbedStackedPanelRenderer();
	
	protected final List<Link> stack = new ArrayList<>(3);
	
	protected final Link backLink;
	protected final Link closeLink;
	
	private int invisibleCrumb = 1;
	private String cssClass;
	private boolean showCloseLink = false;
	private boolean showCloseLinkForRootCrumb = false;
	
	public BreadcrumbedStackedPanel(String name, Translator translator, ComponentEventListener listener) {
		this(name, translator, listener, null);
	}
	
	public BreadcrumbedStackedPanel(String name, Translator translator, ComponentEventListener listener, String cssClass) {
		super(name);
		setTranslator(Util.createPackageTranslator(BreadcrumbedStackedPanel.class, translator.getLocale(), translator));
		addListener(listener);
		
		this.cssClass = cssClass;
		
		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		backLink = LinkFactory.createCustomLink("back", "back", null, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		backLink.setIconLeftCSS("o_icon o_icon_back");
		backLink.setTitle(translator.translate("back"));
		backLink.setAccessKey("b"); // allow navigation using keyboard

		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		closeLink = LinkFactory.createCustomLink("close", "close", null, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		closeLink.setIconLeftCSS("o_icon o_icon_close_tool");
		closeLink.setCustomDisplayText(translator.translate("close"));
		closeLink.setAccessKey("x"); // allow navigation using keyboard
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public int getInvisibleCrumb() {
		return invisibleCrumb;
	}

	public void setInvisibleCrumb(int invisibleCrumb) {
		this.invisibleCrumb = invisibleCrumb;
	}

	public Link getBackLink() {
		return backLink;
	}
	
	public Link getCloseLink() {
		return closeLink;
	}
	
	public boolean isShowCloseLink() {
		return showCloseLink;
	}

	public boolean isShowCloseLinkForRootCrumb() {
		return showCloseLinkForRootCrumb;
	}

	public void setShowCloseLink(boolean showCloseLinkForCrumbs, boolean showCloseLinkForRootCrumb) {
		this.showCloseLink = showCloseLinkForCrumbs;
		this.showCloseLinkForRootCrumb = showCloseLinkForRootCrumb;
	}
	
	public List<Link> getBreadCrumbs() {
		return stack;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>(3 + stack.size());
		cmps.add(backLink);
		cmps.add(closeLink);
		Component content = getContent();
		if(content != null && content != this) {
			cmps.add(getContent());
		}
		for(Link crumb:stack) {
			cmps.add(crumb);
		}
		return cmps;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if(cmd != null) {
			if(backLink.getCommand().equals(cmd)) {
				dispatchEvent(ureq, backLink, null);
			} else if(closeLink.getCommand().equals(cmd)) {
				dispatchEvent(ureq, closeLink, null);
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source.equals(backLink) || source.equals(closeLink)) {
			if (stack.size() > 1) {
				// back means to one level down, change source to the stack item one below current
				source = stack.get(stack.size()-2);
				// now continue as if user manually pressed a stack item in the list
			} else {
				// notify listeners that back or link beyond breadcrumb has been called
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		}
		
		if(stack.contains(source)) {
			Controller controllerToPop = getControllerToPop(source);
			//part of a hack for QTI editor
			if(controllerToPop instanceof VetoableCloseController
					&& !((VetoableCloseController)controllerToPop).requestForClose(ureq)) {
				// not my problem anymore, I have done what I can
				fireEvent(ureq, new VetoPopEvent());
				return;
			}
			Controller popedCtrl = popController(source);
			if(popedCtrl != null) {
				Controller last = getLastController();
				if(last != null) {
					addToHistory(ureq, last);
				}
				fireEvent(ureq, new PopEvent(popedCtrl));
			} else if(stack.indexOf(source) == 0) {
				fireEvent(ureq, new RootEvent());
				
			}
		}
	}
	
	private void addToHistory(UserRequest ureq, Controller controller) {
		WindowControl wControl = controller.getWindowControlForDebug();
		BusinessControlFactory.getInstance().addToHistory(ureq, wControl);
	}
	
	private Controller getLastController() {
		Controller controller = null;
		if(stack.size() > 0) {
			Link lastPath = stack.get(stack.size() - 1);
			BreadCrumb crumb = (BreadCrumb)lastPath.getUserObject();
			controller = crumb.getController();
		}
		return controller;
	}

	@Override
	public void popContent() {
		if(stack.size() > 1) {
			Link link = stack.remove(stack.size() - 1);
			BreadCrumb crumb = (BreadCrumb)link.getUserObject();
			crumb.dispose();
		}
	}

	@Override
	public void popUpToController(Controller controller) {
		int index = getIndex(controller);
		if(index > 0 && index < stack.size() - 1) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.getController().dispose();
			}

			Link currentLink = stack.get(index);
			BreadCrumb crumb  = (BreadCrumb)currentLink.getUserObject();
			setContent(crumb.getController());
			updateCloseLinkTitle();
		}
	}
	
	

	@Override
	public void popController(Controller controller) {
		int index = getIndex(controller);
		if(index > 0 && index < stack.size()) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i--> index; ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.getController().dispose();
			}

			Link currentLink = stack.get(index - 1);
			BreadCrumb crumb  = (BreadCrumb)currentLink.getUserObject();
			setContent(crumb.getController());
			updateCloseLinkTitle();
		}
	}

	@Override
	public void pushContent(Component newContent) {
		setContent(newContent);
	}
	
	private int getIndex(Controller controller) {
		int index = -1;
		for(int i=0; i<stack.size(); i++) {
			BreadCrumb crumb = (BreadCrumb)stack.get(i).getUserObject();
			if(crumb.getController() == controller) {
				index = i;
			}
		}
		return index;
	}
	
	private Controller getControllerToPop(Component source) {
		int index = stack.indexOf(source);
		if(index < (stack.size() - 1)) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.get(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
			}
			return popedCrumb.getController();
		}
		return null;
	}
	
	private Controller popController(Component source) {
		int index = stack.indexOf(source);
		if(index < (stack.size() - 1)) {
			
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.dispose();
			}

			Link currentLink = stack.get(index);
			BreadCrumb crumb  = (BreadCrumb)currentLink.getUserObject();
			setContent(crumb.getController());
			updateCloseLinkTitle();
			return popedCrumb.getController();
		}
		return null;
	}
	
	@Override
	public void rootController(String displayName, Controller controller) {
		if(stack.size() > 0) {
			for(int i=stack.size(); i-->0; ) {
				Link link = stack.remove(i);
				BreadCrumb crumb = (BreadCrumb)link.getUserObject();
				crumb.dispose();
			}
		}
		
		pushController(displayName, controller);
	}

	@Override
	public void popUpToRootController(UserRequest ureq) {
		if(stack.size() > 1) {
			for(int i=stack.size(); i-->1; ) {
				Link link = stack.remove(i);
				BreadCrumb crumb = (BreadCrumb)link.getUserObject();
				crumb.dispose();
			}
			
			//set the root controller
			Link rootLink = stack.get(0);
			BreadCrumb rootCrumb  = (BreadCrumb)rootLink.getUserObject();
			setContent(rootCrumb.getController()); 
			updateCloseLinkTitle();
			fireEvent(ureq, new PopEvent(rootCrumb.getController()));
		}
	}

	@Override
	public void pushController(String displayName, Controller controller) {
		Link link = LinkFactory.createLink("crumb_" + stack.size(), (Translator)null, this);
		link.setCustomDisplayText(StringHelper.escapeHtml(displayName));
		link.setDomReplacementWrapperRequired(false);
		link.setUserObject(createCrumb(controller));
		stack.add(link);
		setContent(controller);
		updateCloseLinkTitle();
	}
	
	protected BreadCrumb createCrumb(Controller controller) {
		return new BreadCrumb(controller);
	}
	
	private void setContent(Controller ctrl) {
		Component cmp = ctrl.getInitialComponent();
		if(cmp == this) {
			log.error("Set itself as content is forbidden");
			throw new AssertException("Set itself as content is forbidden");
		}
		super.setContent(cmp);
	}
	
	/**
	 * Update the close link title to match the name of the last visible item
	 */
	private void updateCloseLinkTitle() {
		String closeText;
		boolean showClose; 
		if(stack.size() < 2) { 
			// special case: root crumb
			Link link = stack.get(0);
			closeText = getTranslator().translate("doclose", new String[] { link.getCustomDisplayText() });
			showClose = isShowCloseLinkForRootCrumb();
			backLink.setTitle(closeText);
		} else {
			Link link = stack.get(stack.size()-1);
			closeText = getTranslator().translate("doclose", new String[] { link.getCustomDisplayText() });
			showClose = isShowCloseLink();
			backLink.setTitle(getTranslator().translate("back"));
		}
		closeLink.setCustomDisplayText(closeText);
		closeLink.setTitle(closeText);
		closeLink.setVisible(showClose);								
	}
	
	public static class BreadCrumb {
		private final Controller controller;
		
		public BreadCrumb(Controller controller) {
			this.controller = controller;
		}

		public Controller getController() {
			return controller;
		}

		public void dispose() {
			controller.dispose();
		}
	}
}