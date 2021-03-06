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
package org.olat.repository.manager;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryServiceImplTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryServiceImpl repositoryService;
	
	@Test
	public void createRepositoryEntry() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-1");
		
		String displayName = "ServiceTest";
		String resourceName = "ServiceTest";
		String description = "Test the brand new service";
		RepositoryEntry re = repositoryService.create(initialAuthor, null, resourceName, displayName, description, null, 0);
		dbInstance.commit();
		
		Assert.assertNotNull(re);
		Assert.assertNotNull(re.getCreationDate());
		Assert.assertNotNull(re.getLastModified());
		Assert.assertNotNull(re.getOlatResource());
	}
	
	@Test
	public void createAndLoadRepositoryEntry() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-1");
		
		String displayName = "Service test 2";
		String resourceName = "ServiceTest";
		String description = "Test the brand new service";
		RepositoryEntry re = repositoryService.create(initialAuthor, null, resourceName, displayName, description, null, 0);
		dbInstance.commitAndCloseSession();
		Assert.assertNotNull(re);
		
		RepositoryEntry loadedEntry = repositoryService.loadByKey(re.getKey());
		Assert.assertNotNull(loadedEntry);
		Assert.assertNotNull(re.getCreationDate());
		Assert.assertNotNull(re.getLastModified());
		Assert.assertNotNull(re.getOlatResource());
		Assert.assertNotNull(loadedEntry.getGroups());
		Assert.assertEquals(1, loadedEntry.getGroups().size());
		//saved?
		Assert.assertEquals(displayName, re.getDisplayname());
		Assert.assertEquals(resourceName, re.getResourcename());
		Assert.assertEquals(description, re.getDescription());
		//default value
		Assert.assertFalse(re.getCanCopy());
		Assert.assertFalse(re.getCanDownload());
		Assert.assertFalse(re.getCanReference());
		Assert.assertEquals(0, re.getAccess());
	}
	
	@Test
	public void deleteCourse() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("auth-del-1");
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(initialAuthor);
		dbInstance.commitAndCloseSession();
		
		Roles roles = new Roles(false, false, false, true, false, false, false);
		ErrorList errors = repositoryService.delete(re, initialAuthor, roles, Locale.ENGLISH);
		Assert.assertNotNull(errors);
		Assert.assertFalse(errors.hasErrors());
	}
	
	

}
