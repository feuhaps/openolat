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

import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.user.UserPropertiesRow;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachedIdentityRow {

	private final TaskLight task;
	private final UserPropertiesRow identity;
	
	public CoachedIdentityRow(UserPropertiesRow identity, TaskLight task) {
		this.identity = identity;
		this.task = task;
	}
	
	public TaskProcess getTaskStatus() {
		return task == null ? null : task.getTaskStatus();
	}
	
	public UserPropertiesRow getIdentity() {
		return identity;
	}
}