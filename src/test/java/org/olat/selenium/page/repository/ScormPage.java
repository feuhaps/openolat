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

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Drive the SCORM page
 * 
 * Initial date: 04.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScormPage {
	
	private WebDriver browser;
	
	private ScormPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public static ScormPage getScormPage(WebDriver browser) {
		WebElement main = browser.findElement(By.id("o_main_wrapper"));
		Assert.assertTrue(main.isDisplayed());
		return new ScormPage(browser);
	}
	
	public ScormPage start() {
		By startBy = By.cssSelector("button.o_sel_start_scorm");
		WebElement startButton = browser.findElement(startBy);
		startButton.click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public ScormPage passVerySimpleScorm() {
		browser.switchTo().frame("scormContentFrame");
		
		By val0By = By.cssSelector("input[value='0']");
		browser.findElement(val0By).click();
		By val3By = By.cssSelector("input[value='3']");
		browser.findElement(val3By).click();
		
		By submitBy = By.id("submit_scorm_datas");
		browser.findElement(submitBy).click();
		OOGraphene.waitingALittleBit();
		
		browser.switchTo().defaultContent();
		
		for(int i=0; i<50; i++) {
			By rootNodeBy = By.cssSelector("span.o_tree_link.o_tree_l0 a");
			browser.findElement(rootNodeBy).click();
			
			By scormCompletedBadgeBy = By.cssSelector("span.badge.o_scorm_completed");
			List<WebElement> completedEls = browser.findElements(scormCompletedBadgeBy);
			if(completedEls.size() > 0) {
				break;
			}
			OOGraphene.waitingALittleLonger();
		}
		return this;
	}
	
	public ScormPage back() {
		By backBy = By.className("o_link_back");
		browser.findElement(backBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}

}
