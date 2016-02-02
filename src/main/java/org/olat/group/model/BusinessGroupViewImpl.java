/**
 * <a href=“http://www.openolat.org“>
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
 * 2011 by frentix GmbH, http://www.frentix.com
 * <p>
**/
package org.olat.group.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupView;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity
public class BusinessGroupViewImpl extends PersistentObject implements BusinessGroupView {

	private static final long serialVersionUID = -9042740930754224954L;
	
	@Transient 
	private Long identityKey;
	private String description;
	private String name;
	private String externalId;
	private Integer minParticipants;
	private Integer maxParticipants;
	@ManyToOne(targetEntity=OLATResourceImpl.class)
	private OLATResource resource;
	@ManyToOne (targetEntity=GroupImpl.class)
	private Group baseGroup;
	private Date lastUsage;
	private Boolean waitingListEnabled;
	private Boolean autoCloseRanksEnabled;
	private Date lastModified;
	private String managedFlagsString;

	private long numOfOwners;
	private long numOfParticipants;
	private long numOfPendings;
	@Transient 
	private long numWaiting;
	private long numOfRelations;
	private long numOfOffers;
	private long numOfValidOffers;

	@Override
	public long getNumOfRelations() {
		return numOfRelations;
	}

	public void setNumOfRelations(long numOfRelations) {
		this.numOfRelations = numOfRelations;
	}

	@Override
	public long getNumOfOwners() {
		return numOfOwners;
	}

	public void setNumOfOwners(long numOfOwners) {
		this.numOfOwners = numOfOwners;
	}

	@Override
	public long getNumOfParticipants() {
		return numOfParticipants;
	}

	public void setNumOfParticipants(long numOfParticipants) {
		this.numOfParticipants = numOfParticipants;
	}

	@Override
	public long getNumOfPendings() {
		return numOfPendings;
	}

	public void setNumOfPendings(long numOfPendings) {
		this.numOfPendings = numOfPendings;
	}

	@Override
	public long getNumWaiting() {
		return numWaiting;
	}

	public void setNumWaiting(long numWaiting) {
		this.numWaiting = numWaiting;
	}

	@Override
	public long getNumOfOffers() {
		return numOfOffers;
	}
	
	public void setNumOfOffers(long numOfOffers) {
		this.numOfOffers = numOfOffers;
	}

	@Override
	public long getNumOfValidOffers() {
		return numOfValidOffers;
	}

	public void setNumOfValidOffers(long numOfValidOffers) {
		this.numOfValidOffers = numOfValidOffers;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public Date getLastUsage() {
		return lastUsage;
	}

	public void setLastUsage(Date lastUsage) {
		this.lastUsage = lastUsage;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return BusinessGroupManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public OLATResource getResource() {
		return resource;
	}
	

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group group) {
		this.baseGroup = group;
	}

	@Override
	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	@Override
	public Integer getMinParticipants() {
		return minParticipants;
	}

	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}

	@Override
	public Boolean getAutoCloseRanksEnabled() {
		return autoCloseRanksEnabled;
	}

	public void setAutoCloseRanksEnabled(Boolean autoCloseRanksEnabled) {
		this.autoCloseRanksEnabled = autoCloseRanksEnabled;
	}

	@Override
	public Boolean getWaitingListEnabled() {
		return waitingListEnabled;
	}

	public void setWaitingListEnabled(Boolean waitingListEnabled) {
		this.waitingListEnabled = waitingListEnabled;
	}

	@Override
	public int hashCode() {
		return (getKey() == null ? 2634 : getKey().hashCode())
			+ (identityKey == null ? -24 :identityKey.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BusinessGroupViewImpl) {
			BusinessGroupViewImpl centric = (BusinessGroupViewImpl)obj;
			return getKey() != null && getKey().equals(centric.getKey())
					&& identityKey != null && identityKey.equals(centric.getKey());
		}
		return false;
	}
}