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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentResponse;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAAvailableTaskController extends FormBasicController {

	private FlexiTableElement tableEl;
	private AvailableTaskTableModel taskModel;
	
	private CloseableCalloutWindowController descriptionCalloutCtrl;
	
	/**
	 * True if it's a group task, false if it's an individual task.
	 */
	private final boolean businessGroupTask;
	private final GTACourseNode gtaNode;
	private final List<TaskDefinition> taskDefs;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private final TaskList taskList;
	private final Identity assessedIdentity;
	private final BusinessGroup assessedGroup;
	
	public GTAAvailableTaskController(UserRequest ureq, WindowControl wControl,
			List<TaskDefinition> taskDefs, TaskList taskList,
			BusinessGroup assessedGroup, Identity assessedIdentity,
			CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "available_tasks");
		this.gtaNode = gtaNode;
		this.taskDefs = taskDefs;
		this.taskList = taskList;
		this.assessedGroup = assessedGroup;
		this.assessedIdentity = assessedIdentity;
		this.courseEnv = courseEnv;
		businessGroupTask = GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.title.i18nKey(), ATDCols.title.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.description.i18nKey(), ATDCols.description.ordinal(),
				new DescriptionWithTooltipCellRenderer()));
		
		boolean preview = gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_PREVIEW);
		if(preview) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ATDCols.preview.i18nKey(), ATDCols.preview.ordinal()));
		}
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("select", -1, "select",
				new StaticFlexiCellRenderer(translate("select"), "select", "btn btn-primary", "o_icon o_icon_submit")));
		
		taskModel = new AvailableTaskTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", taskModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		formLayout.add("table", tableEl);
		
		loadModel();
	}
	
	private void loadModel() {
		File taskFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);

		List<AvailableTask> availableTasks = new ArrayList<>(taskDefs.size());
		List<String> usedSlotes;
		if(GTACourseNode.GTASK_SAMPLING_UNIQUE.equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SAMPLING))) {
			usedSlotes = gtaManager.getAssignedTasks(taskList);
		} else {
			usedSlotes = Collections.emptyList();
		}

		for(TaskDefinition taskDef:taskDefs) {
			String filename = taskDef.getFilename();
			if(usedSlotes.contains(filename)) {
				continue;
			}
			FormLink descriptionLink = null;
			if(StringHelper.containsNonWhitespace(taskDef.getDescription())) {
				descriptionLink = uifactory.addFormLink("preview-" + CodeHelper.getRAMUniqueID(), "description", "task.description", null, flc, Link.LINK);
				descriptionLink.setIconLeftCSS("o_icon o_icon_description");
			}
			
			File taskFile = new File(taskFolder, filename);
			DownloadLink download = uifactory.addDownloadLink("prev-" + CodeHelper.getRAMUniqueID(), filename, null, taskFile, tableEl);

			AvailableTask wrapper = new AvailableTask(taskDef, descriptionLink, download);
			availableTasks.add(wrapper);
			if(descriptionLink != null) {
				descriptionLink.setUserObject(wrapper);
			}
		}
		taskModel.setObjects(availableTasks);
		tableEl.reset();
		
		if(availableTasks.isEmpty()) {
			flc.contextPut("noMoreTasks", Boolean.TRUE);
		} else {
			flc.contextPut("noMoreTasks", Boolean.FALSE);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				AvailableTask row = taskModel.getObject(se.getIndex());
				if("select".equals(se.getCommand())) {
					doSelect(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("description".equals(link.getCmd())) {
				doDescription(ureq, (AvailableTask)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, AvailableTask row) {
		String taskName = row.getTaskDef().getFilename();
		File tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		File task = new File(tasksFolder, taskName);
		
		AssignmentResponse response;
		if(businessGroupTask) {
			response = gtaManager.selectTask(assessedGroup, taskList, gtaNode, task);
		} else {
			response = gtaManager.selectTask(assessedIdentity, taskList, gtaNode, task);
		}
		
		if(response == null || response.getStatus() == AssignmentResponse.Status.error) {
			showError("task.assignment.error");
		} else if(response.getStatus() == AssignmentResponse.Status.alreadyAssigned) {
			showWarning("task.alreadyChosen");
		} else if(response == null || response.getStatus() == AssignmentResponse.Status.ok) {
			showInfo("task.successfully.assigned");
			fireEvent(ureq, Event.DONE_EVENT);
			gtaManager.log("Assignment", "task assigned", response.getTask(), getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
			doSendConfirmationEmail();
		}
	}
	
	private void doSendConfirmationEmail() {
		MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
		
		MailBundle bundle = new MailBundle();
		bundle.setContext(context);
		ContactList contacts = new ContactList("participants");
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			List<Identity> participants = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
			contacts.addAllIdentites(participants);
			bundle.setMetaId(UUID.randomUUID().toString());
		} else {
			contacts.add(assessedIdentity);
		}
		bundle.setContactList(contacts);

		String subject = translate("mail.confirm.assignment.subject");
		String body = translate("mail.confirm.assignment.body");
		bundle.setContent(subject, body);

		mailManager.sendMessage(bundle);
	}
	
	private void doDescription(UserRequest ureq, AvailableTask row) {
		removeAsListenerAndDispose(descriptionCalloutCtrl);
		
		VelocityContainer descriptionVC = createVelocityContainer("description_callout");
		descriptionVC.contextPut("description", row.getTaskDef().getDescription());
		descriptionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				descriptionVC, row.getDescriptionLink().getFormDispatchId(), "", true, "");
		listenTo(descriptionCalloutCtrl);
		descriptionCalloutCtrl.activate();
	}
	
	public enum ATDCols {
		title("task.title"),
		description("task.description"),
		preview("preview");

		private final String i18nKey;
	
		private ATDCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private static class AvailableTask {

		private final TaskDefinition taskDef;
		private final FormLink descriptionLink;
		private final DownloadLink downloadLink;
		
		public AvailableTask(TaskDefinition taskDef, FormLink descriptionLink,  DownloadLink downloadLink) {
			this.taskDef = taskDef;
			this.downloadLink = downloadLink;
			this.descriptionLink = descriptionLink;
		}

		public TaskDefinition getTaskDef() {
			return taskDef;
		}

		public FormLink getDescriptionLink() {
			return descriptionLink;
		}

		public DownloadLink getDownloadLink() {
			return downloadLink;
		}
	}
	
	private static class AvailableTaskTableModel extends DefaultFlexiTableDataModel<AvailableTask> {
		
		public AvailableTaskTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public DefaultFlexiTableDataModel<AvailableTask> createCopyWithEmptyList() {
			return new AvailableTaskTableModel(getTableColumnModel());
		}

		@Override
		public Object getValueAt(int row, int col) {
			AvailableTask task = getObject(row);
			switch(ATDCols.values()[col]) {
				case title: return task.getTaskDef().getTitle();
				case description: return task.getTaskDef().getDescription();
				case preview: return task.getDownloadLink();
				default: return "ERROR";
			}
		}
	}
}
