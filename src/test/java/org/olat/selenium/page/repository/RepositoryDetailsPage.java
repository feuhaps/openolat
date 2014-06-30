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
package org.olat.selenium.page.repository;

import java.util.List;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jcodec.common.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Fragment to control the details view of a repository entry.
 * 
 * 
 * Initial date: 20.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryDetailsPage {
	
	public static final By launchBy = By.className("o_sel_author_launch");
	public static final By editBy = By.className("o_sel_author_edit_entry");
	

	@Drone
	private WebDriver browser;
	
	public RepositoryDetailsPage assertOnTitle(String displayName) {
		List<WebElement> titleList = browser.findElements(By.tagName("h1"));
		Assert.assertNotNull(titleList);
		Assert.assertEquals(1, titleList.size());
		
		WebElement title = titleList.get(0);
		Assert.assertTrue(title.isDisplayed());
		Assert.assertTrue(title.getText().contains(displayName));
		return this;
	}
	
	public void launch() {
		browser.findElement(launchBy).click();
		OOGraphene.waitBusy();
		OOGraphene.closeBlueMessageWindow(browser);
	}
	
	public void edit() {
		browser.findElement(editBy).click();
		OOGraphene.waitBusy();
		OOGraphene.closeBlueMessageWindow(browser);
	}

}