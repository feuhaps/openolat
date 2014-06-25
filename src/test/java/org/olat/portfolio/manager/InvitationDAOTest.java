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
package org.olat.portfolio.manager;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private EPPolicyManager policyManager;
	
	
	@Test
	public void createAndPersistInvitation() {
		Invitation invitation = invitationDao.createAndPersistInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commit();
		
		Assert.assertNotNull(invitation);
		Assert.assertNotNull(invitation.getKey());
		Assert.assertNotNull(invitation.getBaseGroup());
		Assert.assertNotNull(invitation.getToken());
	}
	
	@Test
	public void findInvitation_token() {
		Invitation invitation = invitationDao.createAndPersistInvitation();
		Assert.assertNotNull(invitation);
		dbInstance.commitAndCloseSession();
		
		Invitation reloadedInvitation = invitationDao.findInvitation(invitation.getToken());
		Assert.assertNotNull(reloadedInvitation);
		Assert.assertNotNull(reloadedInvitation.getKey());
		Assert.assertNotNull(reloadedInvitation.getBaseGroup());
		Assert.assertEquals(invitation, reloadedInvitation);
		Assert.assertEquals(invitation.getToken(), reloadedInvitation.getToken());
	}
	
	@Test
	public void hasInvitationPolicies_testHQL() {
		String token = UUID.randomUUID().toString();
		Date atDate = new Date();
		boolean hasInvitation = invitationDao.hasInvitations(token, atDate);
		Assert.assertFalse(hasInvitation);
	}
	
	@Test
	public void createAndUpdateInvitation() {
		Invitation invitation = invitationDao.createAndPersistInvitation();
		dbInstance.commit();
		
		invitation.setFirstName("Kanu");
		invitation.setLastName("Unchou");
		invitation.setMail("kanu.unchou@frentix.com");
		Invitation updatedInvitation = invitationDao.update(invitation);
		dbInstance.commit();
		
		Assert.assertEquals("Kanu", updatedInvitation.getFirstName());
		Assert.assertEquals("Unchou", updatedInvitation.getLastName());
		Assert.assertEquals("kanu.unchou@frentix.com", updatedInvitation.getMail());
		
		Invitation reloadedInvitation = invitationDao.findInvitation(invitation.getToken());
		Assert.assertEquals("Kanu", reloadedInvitation.getFirstName());
		Assert.assertEquals("Unchou", reloadedInvitation.getLastName());
		Assert.assertEquals("kanu.unchou@frentix.com", reloadedInvitation.getMail());
	}
	
	

}
