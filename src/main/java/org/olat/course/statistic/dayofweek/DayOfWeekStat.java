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

package org.olat.course.statistic.dayofweek;

import javax.persistence.Entity;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * Hibernate object representing an entry in the o_stat_dayofweek table.
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
@Entity
public class DayOfWeekStat extends PersistentObject {

	private static final long serialVersionUID = -5632266056691684451L;
	private String businessPath;
	private int day;
	private int value;
	private long resId;
	
	public DayOfWeekStat(){
	// for hibernate	
	}
	
	public final long getResId() {
		return resId;
	}
	
	public void setResId(long resId) {
		this.resId = resId;
	}
	
	public final String getBusinessPath() {
		return businessPath;
	}

	public final void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public final int getDay() {
		return day;
	}
	
	public final void setDay(int day) {
		this.day = day;
	}
	
	public final int getValue() {
		return value;
	}
	
	public final void setValue(int value) {
		this.value = value;
	}
	
}
