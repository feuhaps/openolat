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
package org.olat.selenium;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.page.InitialPage;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.olat.selenium.page.LoginPage;
import org.olat.selenium.page.NavigationPage;
import org.olat.selenium.page.Participant;
import org.olat.selenium.page.Student;
import org.olat.selenium.page.User;
import org.olat.selenium.page.core.IMPage;
import org.olat.selenium.page.course.CourseEditorPageFragment;
import org.olat.selenium.page.course.CoursePageFragment;
import org.olat.selenium.page.course.EnrollmentConfigurationPage;
import org.olat.selenium.page.course.EnrollmentPage;
import org.olat.selenium.page.course.MembersPage;
import org.olat.selenium.page.course.PublisherPageFragment.Access;
import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.selenium.page.group.GroupPage;
import org.olat.selenium.page.group.MembersWizardPage;
import org.olat.selenium.page.user.UserToolsPage;
import org.olat.test.ArquillianDeployments;
import org.olat.test.rest.UserRestClient;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * 
 * Initial date: 03.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Arquillian.class)
public class BusinessGroupTest {
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		return ArquillianDeployments.createDeployment();
	}

	@Drone
	private WebDriver browser;
	@ArquillianResource
	private URL deploymentUrl;	

	@Page
	private UserToolsPage userTools;
	@Page
	private NavigationPage navBar;

	/**
	 * An author create a group, set the visibility to
	 * show owners and participants. Add a member to the
	 * group.
	 * 
	 * The participant log in, search the group and open it.
	 * 
	 * @param loginPage
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupMembersVisibility(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver participantBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Aoi");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group");
		
		MembersWizardPage members = group
			.openAdministration()
			.openAdminMembers()
			.setVisibility(true, true, false)
			.addMember();
		
		members.searchMember(participant, false)
			.next()
			.next()
			.next()
			.finish();
		
		LoginPage participantLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		
		NavigationPage participantNavBar = new NavigationPage(participantBrowser);
		participantNavBar
				.openGroups(participantBrowser)
				.selectGroup(groupName);
		
		WebElement contentEl = participantBrowser.findElement(By.id("o_main_center_content_inner"));
		String content = contentEl.getText();
		Assert.assertTrue(content.contains(groupName));
	}
	
	/**
	 * Configure group tools: create a group, go to administration > tools
	 * select the informations for members and write some message. Select
	 * all tools: contact, calendar, folder, forum, chat, wiki and portfolio.<br>
	 * 
	 * Check that all these functions are available.
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void collaborativeTools(@InitialPage LoginPage loginPage)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group");
		
		String news = "Welcome members ( " + UUID.randomUUID() + " )";
		group
			.openAdministration()
			.openAdminTools()
			.enableTools()
			.setMembersInfos(news);
		
		//check the news
		group
			.openNews()
			.assertNews(news);
		
		//check calendar
		group
			.openCalendar()
			.assertOnCalendar();
		
		//check members @see other selenium test dedicated to this one

		//check contact
		group
			.openContact()
			.assertOnContact();
		
		//check folder
		String directoryName = "New directory";
		group
			.openFolder()
			.assertOnFolderCmp()
			.createDirectory(directoryName)
			.assertOnDirectory(directoryName)
			.createHTMLFile("New file", "Some really cool content.")
			.assertOnFile("new file.html");
		
		//check forum
		String threadBodyMarker = UUID.randomUUID().toString();
		group
			.openForum()
			.createThread("New thread in a group", "Very interessant discussion in a group" + threadBodyMarker)
			.assertMessageBody(threadBodyMarker);
		
		//check chat @see other selenium test dedicated to this one
		
		//check wiki
		String wikiMarker = UUID.randomUUID().toString();
		group
			.openWiki()
			.createPage("Group page", "Content for the group's wiki " + wikiMarker)
			.assertOnContent(wikiMarker);
		
		//check portfolio
		String pageTitle = "Portfolio page " + UUID.randomUUID();
		String structureElementTitle = "Structure " + UUID.randomUUID();
		group
			.openPortfolio()
			.openEditor()
			.selectMapInEditor()
			.selectFirstPageInEditor()
			.setPage(pageTitle, "With a little description")
			.createStructureElement(structureElementTitle, "Structure description")
			.closeEditor()
			.assertStructure(structureElementTitle);
	}
	
	/**
	 * An author creates a group, it opens the tab groups and then "My groups". It
	 * creates a group, enters a number of participants "1", enable the waiting
	 * list. In members visibility, it see coaches, participants and waiting
	 * list visible to members.<br>
	 * A participant and than a student come, book the group. The first enters
	 * the group, the second the waiting list.<br>
	 * The author go in the members list to check if it's in the coach list,
	 * the participant in the participants list and the student in the waiting
	 * list.
	 * 
	 * Should show group starting page, with menu items Administration and Bookings visible
	 * 
	 * @param loginPage
	 * @param participantBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void createGroupWithWaitingList(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver participantBrowser,
			@Drone @Student WebDriver studentBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createRandomUser("Selena");
		UserVO participant = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		UserVO student = new UserRestClient(deploymentUrl).createRandomUser("Asuka");
	
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A group with a waiting list")
			.openAdministration()
			//set waiting list and 1 participant
			.openEditDetails()
			.setMaxNumberOfParticipants(1)
			.setWaitingList()
			.saveDetails();
		
		//add booking ( token one )
		String token = "secret";
		String description = "The password is secret";
		group.openBookingConfig()
			.openAddDropMenu()
			.addTokenMethod()
			.configureTokenMethod(token, description)
			.assertOnToken(token)
			.save();
		
		//members see members
		group = GroupPage.getGroup(browser)
			.openAdminMembers()
			.setVisibility(true, true, true)
			.openMembers();
		

		//participant search published groups
		LoginPage participantLoginPage = LoginPage.getLoginPage(participantBrowser, deploymentUrl);
		//tools
		participantLoginPage
			.loginAs(participant.getLogin(), participant.getPassword())
			.resume();
		//groups
		NavigationPage participantNavBar = new NavigationPage(participantBrowser);
		participantNavBar
				.openGroups(participantBrowser)
				.publishedGroups()
				.bookGroup(groupName)
				.bookToken(token);
		//are we that we are in the right group?
		GroupPage.getGroup(participantBrowser)
			.assertOnInfosPage(groupName);
		
		
		//student search published groups
		LoginPage studentLoginPage = LoginPage.getLoginPage(studentBrowser, deploymentUrl);
		//tools
		studentLoginPage
			.loginAs(student.getLogin(), student.getPassword())
			.resume();
		//groups
		NavigationPage studentNavBar = new NavigationPage(studentBrowser);
		studentNavBar
				.openGroups(studentBrowser)
				.publishedGroups()
				.bookGroup(groupName)
				.bookToken(token);
		//are we that we are in the right group?
		GroupPage.getGroup(studentBrowser)
			.assertOnWaitingList(groupName);
		
		group = GroupPage.getGroup(browser)
				.openMembers()
				.assertMembersInOwnerList(author)
				.assertMembersInParticipantList(participant)
				.assertMembersInWaitingList(student);
	}
	
	/**
	 * An author create a group, set the visibility to true for owners
	 * and participants, enable the tools and add 2 users to it. The 2
	 * users joins the chat. All three send some messages and read them.
	 * 
	 * @param loginPage
	 * @param kanuBrowser
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void groupChat(@InitialPage LoginPage loginPage,
			@Drone @Participant WebDriver kanuBrowser,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {

		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("Kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		loginPage
			.loginAs(author.getLogin(), author.getPassword())
			.resume();
		
		//go to groups
		String groupName = "Group-Chat-1-" + UUID.randomUUID();
		GroupPage group = navBar
			.openGroups(browser)
			.createGroup(groupName, "A very little group to chat");
		
		group
			.openAdministration()
			.openAdminTools()
			.enableTools()
			.openAdminMembers()
			.setVisibility(true, true, false);
		//add Kanu to the group
		group
			.openAdminMembers()
			.addMember()
			.searchMember(kanu, true)
			.next().next().next().finish();
		//add Ryomou
		group.addMember()
			.searchMember(ryomou, true)
			.next().next().next().finish();
		
		//Kanu open the group
		LoginPage kanuLoginPage = LoginPage.getLoginPage(kanuBrowser, deploymentUrl);
		kanuLoginPage
			.loginAs(kanu.getLogin(), kanu.getPassword())
			.resume();
		
		NavigationPage kanuNavBar = new NavigationPage(kanuBrowser);
		GroupPage kanuGroup = kanuNavBar
			.openGroups(kanuBrowser)
			.selectGroup(groupName);
		
		//Ryomou open the group
		LoginPage ryomouLoginPage = LoginPage.getLoginPage(ryomouBrowser, deploymentUrl);
		ryomouLoginPage
			.loginAs(ryomou.getLogin(), ryomou.getPassword())
			.resume();
		
		NavigationPage ryomouNavBar = new NavigationPage(ryomouBrowser);
		IMPage ryomouIM = ryomouNavBar
			.openGroups(ryomouBrowser)
			.selectGroup(groupName)
			.openChat()
			.openGroupChat();
		
		//Author send a message to Kanu
		String msg1 = "Hello Kanu " + UUID.randomUUID();
		IMPage authorIM = group
			.openChat()
			.openGroupChat()
			.sendMessage(msg1)
			.assertOnMessage(msg1);
		
		String msg2 = "Hello dear author " + UUID.randomUUID();
		//Kanu opens her chat window
		IMPage kanuIM = kanuGroup
			.openChat()
			.openGroupChat()
			.assertOnMessage(msg1)
			.sendMessage(msg2);
		
		String msg3 = "Hello Kanu and author " + UUID.randomUUID();
		//Ryomou reads her messages
		ryomouIM
			.sendMessage(msg3)
			.assertOnMessage(msg1)
			.assertOnMessage(msg2);
		//Kanu reads her message
		kanuIM
			.assertOnMessage(msg3);
		//Author reads too
		authorIM
			.assertOnMessage(msg2)
			.assertOnMessage(msg3);
	}
	
	/**
	 * An author create a course, with an enrollment course element. It
	 * configure it and create a group with max. participant set to 1 and
	 * enables the waiting list.<br>
	 * 
	 * Three users goes to the course and try to enroll. One will become
	 * a participant, the 2 others land in the waiting list.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollmentWithWaitingList(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//create a course
		String courseTitle = "Enrolment-1-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Enrolment
		String enNodeTitle = "Enrolment-1";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("en")
			.nodeTitle(enNodeTitle);
		//configure enrolment with a group that we create
		String groupName = "Enrolment group - 1 " + UUID.randomUUID();
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.createBusinessGroup(groupName, "-", 1, true, false);
		//publish the course
		courseEditor
			.publish()
			.quickPublish(Access.users);
		courseEditor.clickToolbarBack();
		
		GroupPage authorGroup = navBar
			.openGroups(browser)
			.selectGroup(groupName)
			.openAdministration()
			.openAdminMembers()
			.setVisibility(true, true, true)
			.openMembers()
			.assertParticipantList();
		
		//Rei open the course
		Enrollment[] participantDrivers = new Enrollment[]{
				new Enrollment(ryomou, ryomouBrowser),
				new Enrollment(rei, reiBrowser),
				new Enrollment(kanu, kanuBrowser)
		};
		for(Enrollment enrollment:participantDrivers) {
			WebDriver driver = enrollment.getDriver();
			LoginPage.getLoginPage(driver, deploymentUrl)
				.loginAs(enrollment.getUser())
				.resume();
			
			NavigationPage participantNavBar = new NavigationPage(driver);
			participantNavBar
				.openMyCourses()
				.openSearch()
				.extendedSearch(courseTitle)
				.select(courseTitle)
				.start();
			
			//go to the enrollment
			CoursePageFragment participantCourse = new CoursePageFragment(driver);
			participantCourse
				.clickTree()
				.selectWithTitle(enNodeTitle);
		
			EnrollmentPage enrollmentPage = new EnrollmentPage(driver);
			enrollmentPage
				.assertOnEnrolmentPage();
			enrollment.setEnrollmentPage(enrollmentPage);
		}
		
		//enroll
		for(Enrollment enrollment:participantDrivers) {
			enrollment.getEnrollmentPage().enrollNoWait();
		}
		//wait
		for(Enrollment enrollment:participantDrivers) {
			OOGraphene.waitBusy(enrollment.getDriver());
		}
		
		//author check the lists
		authorGroup.openMembers();
		//must a participant and 2 in waiting list
		int participants = 0;
		int waitingList = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(authorGroup.isInMembersParticipantList(enrollment.getUser()))  participants++;
			if(authorGroup.isInMembersInWaitingList(enrollment.getUser())) waitingList++;
		}
		Assert.assertEquals(1, participants);
		Assert.assertEquals(2, waitingList);
	}
	/**
	 * An author create a course, with an enrollment course element. It
	 * configure it and create 3 groups and set the maximum enrollment counter to 2<br>
	 * 
	 * One user goes to the course and enrolls in 2 of the groups. It shouldent be possible
	 * enroll in the third<br>
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */

	@Test
	@RunAsClient
	public void enrollmentWithMultiEnrollment(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		//create a course
		String courseTitle = "Enrolment-3-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();

		//create a course element of type Enrolment
		String enNodeTitle = "Enrolment-3";
		CourseEditorPageFragment courseEditor = CoursePageFragment.getCourse(browser)
			.edit();
		courseEditor
			.createNode("en")
			.nodeTitle(enNodeTitle);
		//configure enrolment with a group that we create
		List<String> groups = new ArrayList<String>();
		groups.add("Enrolment group - 3 " + UUID.randomUUID());
		groups.add("Enrolment group - 3 " + UUID.randomUUID());
		groups.add("Enrolment group - 3 " + UUID.randomUUID());
		
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.createBusinessGroup(groups.get(0), "-", 4, false, false)
			.createBusinessGroup(groups.get(1), "-", 4, false, false)
			.createBusinessGroup(groups.get(2), "-", 4, false, false)
			.selectMultipleEnrollments(2);
		//publish the course
		courseEditor
			.publish()
			.quickPublish(Access.users);
		courseEditor.clickToolbarBack();
		

		for(String groupName:groups){
				navBar
					.openGroups(browser)
					.selectGroup(groupName)
					.openAdministration()
					.openAdminMembers()
					.setVisibility(true, true, false)
					.openMembers();
		}
				
		//Ryomou open the course	
		LoginPage.getLoginPage(ryomouBrowser, deploymentUrl)
			.loginAs(ryomou)
			.resume();
		
		NavigationPage participantNavBar = new NavigationPage(ryomouBrowser);
		participantNavBar
			.openMyCourses()
			.openSearch()
			.extendedSearch(courseTitle)
			.select(courseTitle)
			.start();
		
		OOGraphene.waitBusy(ryomouBrowser);
		
		//go to the enrollment
		CoursePageFragment participantCourse = new CoursePageFragment(ryomouBrowser);
		participantCourse
			.clickTree()
			.selectWithTitle(enNodeTitle);
		
		EnrollmentPage enrollmentPage = new EnrollmentPage(ryomouBrowser);
		enrollmentPage
			.assertOnEnrolmentPage()
			.multiEnroll(2);

		//assert that that no more enrollment is allowed
		enrollmentPage
			.assertNoEnrollmentAllowed();
		
	}
	/**
	 * An author create a course and a business group in the members
	 * management. It has max. participants set to 1 and no waiting list.
	 * Than it returns in the course editor to create an enrollment
	 * course element. It configure it and select the group created before.<br>
	 * 
	 * Three users goes to the course and try to enroll. One will become
	 * a participant, the 2 others get an error message.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollment(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Enrollment-2-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a group in members management
		String groupName = "Enroll - " + UUID.randomUUID();
		CoursePageFragment authorCourse = CoursePageFragment.getCourse(browser);
		MembersPage membersPage = authorCourse
			.members()
			.selectBusinessGroups()
			.createBusinessGroup(groupName, "-", 1, false, false);
		//back to the members page
		navBar.openCourse(courseTitle);
		authorCourse = membersPage
			.clickToolbarBack();
		
		//create an enrollment course element
		String enNodeTitle = "Enroll - 2";
		CourseEditorPageFragment courseEditor = authorCourse
			.edit()
			.createNode("en")
			.nodeTitle(enNodeTitle);
		
		//select the group created above
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.selectBusinessGroups();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(Access.users);
		
		GroupPage authorGroup = navBar
			.openGroups(browser)
			.selectGroup(groupName)
			.openAdministration()
			.openAdminMembers()
			.setVisibility(false, true, false)
			.openMembers()
			.assertParticipantList();
		
		Enrollment[] participantDrivers = new Enrollment[]{
				new Enrollment(ryomou, ryomouBrowser),
				new Enrollment(rei, reiBrowser),
				new Enrollment(kanu, kanuBrowser)
		};
		for(Enrollment enrollment:participantDrivers) {
			WebDriver driver = enrollment.getDriver();
			LoginPage.getLoginPage(driver, deploymentUrl)
				.loginAs(enrollment.getUser())
				.resume();
			
			NavigationPage participantNavBar = new NavigationPage(driver);
			participantNavBar
				.openMyCourses()
				.openSearch()
				.extendedSearch(courseTitle)
				.select(courseTitle)
				.start();
			
			//go to the enrollment
			CoursePageFragment participantCourse = new CoursePageFragment(driver);
			participantCourse
				.clickTree()
				.selectWithTitle(enNodeTitle);
		
			EnrollmentPage enrollmentPage = new EnrollmentPage(driver);
			enrollmentPage
				.assertOnEnrolmentPage();
			enrollment.setEnrollmentPage(enrollmentPage);
		}
		
		//enroll
		for(Enrollment enrollment:participantDrivers) {
			enrollment.getEnrollmentPage().enrollNoWait();
		}
		//wait
		for(Enrollment enrollment:participantDrivers) {
			OOGraphene.waitBusy(enrollment.getDriver());
		}
		int errors = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(enrollment.getEnrollmentPage().hasError()) {
				errors++;
			}
		}
		
		//author check the lists
		authorGroup.openMembers();
		//must a participant and 2 in waiting list
		int participants = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(authorGroup.isInMembersParticipantList(enrollment.getUser())) {
				participants++;
			}
		}
		Assert.assertEquals(1, participants);
		Assert.assertEquals(participantDrivers.length - 1, errors);
	}
	
	/**
	 * Variant from the above test where the business group is not
	 * limited in size. This was a bug while development of the 10.3
	 * release.
	 * 
	 * @param authorLoginPage
	 * @param ryomouBrowser
	 * @param reiBrowser
	 * @param kanuBrowser
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	@RunAsClient
	public void enrollmentWithUnlimitedBusinessGroups(@InitialPage LoginPage authorLoginPage,
			@Drone @User WebDriver ryomouBrowser,
			@Drone @Participant WebDriver reiBrowser,
			@Drone @Student WebDriver kanuBrowser)
	throws IOException, URISyntaxException {
		
		UserVO author = new UserRestClient(deploymentUrl).createAuthor();
		UserVO rei = new UserRestClient(deploymentUrl).createRandomUser("Rei");
		UserVO kanu = new UserRestClient(deploymentUrl).createRandomUser("kanu");
		UserVO ryomou = new UserRestClient(deploymentUrl).createRandomUser("Ryomou");
		
		authorLoginPage.loginAs(author.getLogin(), author.getPassword());
		
		//create a course
		String courseTitle = "Enrollment-3-" + UUID.randomUUID();
		navBar
			.openAuthoringEnvironment()
			.createCourse(courseTitle)
			.clickToolbarBack();
		
		//create a group in members management
		String groupName = "Enroll - " + UUID.randomUUID();
		CoursePageFragment authorCourse = CoursePageFragment.getCourse(browser);
		MembersPage membersPage = authorCourse
			.members()
			.selectBusinessGroups()
			.createBusinessGroup(groupName, "-", -1, false, false);
		//back to the members page
		navBar.openCourse(courseTitle);
		authorCourse = membersPage
			.clickToolbarBack();
		
		//create an enrollment course element
		String enNodeTitle = "Enroll - 3";
		CourseEditorPageFragment courseEditor = authorCourse
			.edit()
			.createNode("en")
			.nodeTitle(enNodeTitle);
		
		//select the group created above
		EnrollmentConfigurationPage enrolmentConfig = new EnrollmentConfigurationPage(browser);
		enrolmentConfig
			.selectConfiguration()
			.selectBusinessGroups();
		
		//publish the course
		courseEditor
			.publish()
			.quickPublish(Access.users);
		
		GroupPage authorGroup = navBar
			.openGroups(browser)
			.selectGroup(groupName)
			.openAdministration()
			.openAdminMembers()
			.setVisibility(false, true, false)
			.openMembers()
			.assertParticipantList();
		
		Enrollment[] participantDrivers = new Enrollment[]{
				new Enrollment(ryomou, ryomouBrowser),
				new Enrollment(rei, reiBrowser),
				new Enrollment(kanu, kanuBrowser)
		};
		for(Enrollment enrollment:participantDrivers) {
			WebDriver driver = enrollment.getDriver();
			LoginPage.getLoginPage(driver, deploymentUrl)
				.loginAs(enrollment.getUser())
				.resume();
			
			NavigationPage participantNavBar = new NavigationPage(driver);
			participantNavBar
				.openMyCourses()
				.openSearch()
				.extendedSearch(courseTitle)
				.select(courseTitle)
				.start();
			
			//go to the enrollment
			CoursePageFragment participantCourse = new CoursePageFragment(driver);
			participantCourse
				.clickTree()
				.selectWithTitle(enNodeTitle);
		
			EnrollmentPage enrollmentPage = new EnrollmentPage(driver);
			enrollmentPage
				.assertOnEnrolmentPage();
			enrollment.setEnrollmentPage(enrollmentPage);
		}
		
		//enroll
		for(Enrollment enrollment:participantDrivers) {
			enrollment.getEnrollmentPage().enrollNoWait();
		}
		//wait
		int errors = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(enrollment.getEnrollmentPage().hasError()) {
				errors++;
			}
		}
		
		//author check the lists
		authorGroup.openMembers();
		//must a participant and 2 in waiting list
		int participants = 0;
		for(Enrollment enrollment:participantDrivers) {
			if(authorGroup.isInMembersParticipantList(enrollment.getUser())) {
				participants++;
			}
		}
		Assert.assertEquals(3, participants);
		Assert.assertEquals(0, errors);
	}
	
	public static class Enrollment {
		
		private final UserVO user;
		private final WebDriver driver;
		private EnrollmentPage enrollmentPage;
		
		public Enrollment(UserVO user, WebDriver driver) {
			this.user = user;
			this.driver = driver;
		}

		public UserVO getUser() {
			return user;
		}

		public WebDriver getDriver() {
			return driver;
		}

		public EnrollmentPage getEnrollmentPage() {
			return enrollmentPage;
		}

		public void setEnrollmentPage(EnrollmentPage enrollmentPage) {
			this.enrollmentPage = enrollmentPage;
		}
	}
}
