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

package org.olat.ldap;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.user.UserManager;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Description: 
 * This Module loads all needed configuration for the LDAP Login. 
 * All configuration is done in the spring olatextconfig.xml file.
 * <p>
 * LDAPLoginModule
 * <p>
 * 
 * @author maurus.rohrer@gmail.com
 */
@Service("org.olat.ldap.LDAPLoginModule")
public class LDAPLoginModule extends AbstractSpringModule {
	// Connection configuration
	
	@Value("${ldap.ldapUrl}")
	private String ldapUrl;
	@Value("${ldap.enable:false}")
	private boolean ldapEnabled;
	@Value("${ldap.activeDirectory:false}")
	private boolean activeDirectory;
	@Value("${ldap.dateFormat}")
	private String ldapDateFormat;
	
	//SSL configuration
	@Value("${ldap.sslEnabled}")
	private boolean sslEnabled;
	@Value("${ldap.trustStoreLocation}")
	private String trustStoreLoc;
	@Value("${ldap.trustStorePwd}")
	private String trustStorePass;
	@Value("${ldap.trustStoreType}")
	private String trustStoreTyp;
	
	// System user: used for getting all users and connection testing
	@Value("${ldap.ldapSystemDN}")
	private String systemDN;
	@Value("${ldap.ldapSystemPW}")
	private String systemPW;
	@Value("${ldap.connectionTimeout}")
	private Integer connectionTimeout;
	/**
	 * Create LDAP users on the fly when authenticated successfully
	 */
	@Value("${ldap.ldapCreateUsersOnLogin}")
	private boolean createUsersOnLogin;
	/**
	 * When users log in via LDAP, the system can keep a copy of the password as encrypted
	 * hash in the database. This makes OLAT more independent from an offline LDAP server 
	 * and users can use their LDAP password to use the WebDAV functionality.
	 * When setting to true (recommended), make sure you configured pwdchange=false in the
	 * org.olat.user.UserModule olat.propertes.
	 */
	@Value("${ldap.cacheLDAPPwdAsOLATPwdOnLogin}")
	private boolean cacheLDAPPwdAsOLATPwdOnLogin;
	/**
	 * When the system detects an LDAP user that does already exist in OLAT but is not marked
	 * as LDAP user, the OLAT user can be converted to an LDAP managed user. 
	 * When enabling this feature you should make sure that you don't have a user 'administrator'
	 * in your ldapBases (not a problem but not recommended)
	 */
	@Value("${ldap.convertExistingLocalUsersToLDAPUsers}")
	private boolean convertExistingLocalUsersToLDAPUsers;
	// 
	/**
	 * Users that have been created via LDAP sync but now can't be found on the LDAP anymore
	 * can be deleted automatically. If unsure, set to false and delete those users manually
	 * in the user management.
	 */
	@Value("${ldap.deleteRemovedLDAPUsersOnSync}")
	private boolean deleteRemovedLDAPUsersOnSync;
	/**
	 * Sanity check when deleteRemovedLDAPUsersOnSync is set to 'true': if more than the defined
	 * percentages of user accounts are not found on the LDAP server and thus recognized as to be
	 * deleted, the LDAP sync will not happen and require a manual triggering of the delete job
	 * from the admin interface. This should prevent accidential deletion of OLAT user because of
	 * temporary LDAP problems or user relocation on the LDAP side. 
	 * Value= 0 (never delete) to 100 (always delete). 
	 */
	@Value("${ldap.deleteRemovedLDAPUsersPercentage}")
	private int deleteRemovedLDAPUsersPercentage;
	// Propagate the password changes onto the LDAP server
	@Value("${ldap.propagatePasswordChangedOnLdapServer}")
	private boolean propagatePasswordChangedOnLdapServer;
	// Configuration for syncing user attributes

	
	// Should users be created and synchronized automatically? If you set this
	// configuration to false, the users will be generated on-the-fly when they
	// log in
	@Value("${ldap.ldapSyncOnStartup}")
	private boolean ldapSyncOnStartup;
	@Value("${ldap.ldapSyncCronSync}")
	private boolean ldapSyncCronSync;
	@Value("${ldap.ldapSyncCronSyncExpression}")
	private String ldapSyncCronSyncExpression;
	// User LDAP attributes to be synced and a map with the mandatory attributes


	private static final OLog log = Tracing.createLoggerFor(LDAPLoginModule.class);
	
	@Autowired
	private LDAPSyncConfiguration syncConfiguration;
	@Autowired
	private Scheduler scheduler;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LDAPLoginManager ldapManager;
	@Autowired
	private UserManager userManager;
	
	@Autowired
	public LDAPLoginModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	/**
	 * @see org.olat.core.configuration.Initializable#init()
	 */
	@Override
	public void init() {
		// Check if LDAP is enabled
		if (!isLDAPEnabled()) {
			log.info("LDAP login is disabled");
			return;
		}
		log.info("Starting LDAP module");
		
		// Create LDAP Security Group if not existing. Used to identify users that
		// have to be synced with LDAP
		SecurityGroup ldapGroup = securityManager.findSecurityGroupByName(LDAPConstants.SECURITY_GROUP_LDAP);
		if (ldapGroup == null) {
			ldapGroup = securityManager.createAndPersistNamedSecurityGroup(LDAPConstants.SECURITY_GROUP_LDAP);
		}
		// check for valid configuration
		if (!checkConfigParameterIsNotEmpty(ldapUrl)) return;
		if (!checkConfigParameterIsNotEmpty(systemDN)) return;
		if (!checkConfigParameterIsNotEmpty(systemPW)) return;
		if (syncConfiguration.getLdapBases() == null || syncConfiguration.getLdapBases().isEmpty()) {
			log.error("Missing configuration 'ldapBases'. Add at least one LDAP Base to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if (syncConfiguration.getLdapUserFilter() != null) {
			if (!syncConfiguration.getLdapUserFilter().startsWith("(") || !syncConfiguration.getLdapUserFilter().endsWith(")")) {
				log.error("Wrong configuration 'ldapUserFilter'. Set filter to emtpy value or enclose filter in brackets like '(objectClass=person)'. Disabling LDAP");
				setEnableLDAPLogins(false);
				return;
			}
		}
		
		if (!checkConfigParameterIsNotEmpty(syncConfiguration.getLdapUserCreatedTimestampAttribute())) {
			return;
		}
		if (!checkConfigParameterIsNotEmpty(syncConfiguration.getLdapUserLastModifiedTimestampAttribute())) {
			return;
		}
		if (syncConfiguration.getUserAttributeMap() == null || syncConfiguration.getUserAttributeMap().isEmpty()) {
			log.error("Missing configuration 'userAttrMap'. Add at least the email propery to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if (syncConfiguration.getRequestAttributes() == null || syncConfiguration.getRequestAttributes().isEmpty()) {
			log.error("Missing configuration 'reqAttr'. Add at least the email propery to the this configuration in olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		// check if OLAT user properties is defined in olat_userconfig.xml, if not disable the LDAP module
		if(!syncConfiguration.checkIfOlatPropertiesExists(syncConfiguration.getUserAttributeMap())){
			log.error("Invalid LDAP OLAT properties mapping configuration (userAttrMap). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(!syncConfiguration.checkIfOlatPropertiesExists(syncConfiguration.getRequestAttributes())){
			log.error("Invalid LDAP OLAT properties mapping configuration (reqAttr). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(syncConfiguration.getSyncOnlyOnCreateProperties() != null
				&& !syncConfiguration.checkIfStaticOlatPropertiesExists(syncConfiguration.getSyncOnlyOnCreateProperties())){
			log.error("Invalid LDAP OLAT syncOnlyOnCreateProperties configuration. Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		if(syncConfiguration.getStaticUserProperties() != null
				&& !syncConfiguration.checkIfStaticOlatPropertiesExists(syncConfiguration.getStaticUserProperties().keySet())){
			log.error("Invalid static OLAT properties configuration (staticUserProperties). Disabling LDAP");
			setEnableLDAPLogins(false);
			return;
		}
		
		// check SSL certifications, throws Startup Exception if certificate is not found
		if(isSslEnabled()){
			if (!checkServerCertValidity(0)) {
				log.error("LDAP enabled but no valid server certificate found. Please fix!");
			} else if (!checkServerCertValidity(30)) {
				log.warn("Server Certificate will expire in less than 30 days.");
			}
		}
		
		// Check ldap connection
		if (ldapManager.bindSystem() == null) {
			// don't disable ldap, maybe just a temporary problem, but still report
			// problem in logfile
			log.error("LDAP connection test failed during module initialization, edit config or contact network administrator");
		} else {
			log.info("LDAP login is enabled");
		}
		
		// Sync LDAP Users on Startup
		if (isLdapSyncOnStartup()) {
			initStartSyncJob();
		} else {
			log.info("LDAP start sync is disabled");
		}

		// Start LDAP cron sync job
		if (isLdapSyncCronSync()) {
			initCronSyncJob();
		} else {
			log.info("LDAP cron sync is disabled");
		}
		
		// OK, everything finished checkes passed
		log.info("LDAP login is enabled");
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	/**
	 * Internal helper to sync users right away
	 * @param ldapManager
	 */
	private void initStartSyncJob() {
		LDAPError errors = new LDAPError();
		if (ldapManager.doBatchSync(errors, true)) {
			log.info("LDAP start sync: users synced");
		} else {
			log.warn("LDAP start sync error: " + errors.get());
		}
	}

	/**
	 * Internal helper to initialize the cron syncer job
	 */
	private void initCronSyncJob() {
		try {
			// Create job with cron trigger configuration
			JobDetail jobDetail = new JobDetail("LDAP_Cron_Syncer_Job", Scheduler.DEFAULT_GROUP, LDAPUserSynchronizerJob.class);
			CronTrigger trigger = new CronTrigger();
			trigger.setName("LDAP_Cron_Syncer_Trigger");
			trigger.setCronExpression(ldapSyncCronSyncExpression);
			// Schedule job now
			scheduler.scheduleJob(jobDetail, trigger);
			log.info("LDAP cron syncer is enabled with expression::" + ldapSyncCronSyncExpression);
		} catch (ParseException e) {
			setLdapSyncCronSync(false);
			log.error("LDAP configuration in attribute 'ldapSyncCronSyncExpression' is not valid ("
				+ ldapSyncCronSyncExpression
				+ "). See http://quartz.sourceforge.net/javadoc/org/quartz/CronTrigger.html to learn more about the cron syntax. Disabling LDAP cron syncing",
				e);
		} catch (SchedulerException e) {
			log.error("Error while scheduling LDAP cron sync job. Disabling LDAP cron syncing", e);
		}
	}

	
	/**
	 * Checks if SSL certification is know and accepted by Java JRE.
	 * 
	 * 
	 * @param dayFromNow Checks expiration 
	 * @return true Certification accepted, false No valid certification
	 * 
	 * @throws Exception
	 * 
	 */
	private boolean checkServerCertValidity(int daysFromNow) {
		KeyStore keyStore;
		try {
			keyStore = KeyStore.getInstance(getTrustStoreType());
			keyStore.load(new FileInputStream(getTrustStoreLocation()), (getTrustStorePwd() != null) ? getTrustStorePwd().toCharArray() : null);
			Enumeration<String> aliases = keyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();
				Certificate cert = keyStore.getCertificate(alias);
				if (cert instanceof X509Certificate) {
					return isCertificateValid((X509Certificate)cert, daysFromNow);
				}
			}
		}	catch (Exception e) {
			return false;
		}
		return false;
	}
	
	private boolean isCertificateValid(X509Certificate x509Cert, int daysFromNow) {
		try {
			x509Cert.checkValidity();
			if (daysFromNow > 0) {
				Date nowPlusDays = new Date(System.currentTimeMillis() + (new Long(daysFromNow).longValue() * 24l * 60l * 60l * 1000l));
				x509Cert.checkValidity(nowPlusDays);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Internal helper to check for emtpy config variables
	 * 
	 * @param param
	 * @return true: not empty; false: empty or null
	 */
	private boolean checkConfigParameterIsNotEmpty(String param) {
		if (StringHelper.containsNonWhitespace(param)) {
			return true;
		} else {
			log.error("Missing configuration '" + param + "'. Add this configuration to olatextconfig.xml first. Disabling LDAP");
			setEnableLDAPLogins(false);
			return false;
		}
	}

	/*
	 * Spring setter methods - don't use them to modify values at runtime!
	 */
	public void setEnableLDAPLogins(boolean enableLDAPLogins) {
		ldapEnabled = enableLDAPLogins;
	}

	public void setSslEnabled(boolean sslEnabl) {
		sslEnabled = sslEnabl;
	}
	
	public void setActiveDirectory(boolean aDirectory) {
		activeDirectory = aDirectory;
	}
	
	public void setLdapDateFormat(String dateFormat) {
		ldapDateFormat = dateFormat;
	}
	
	public void setTrustStoreLocation(String trustStoreLocation){
		trustStoreLoc=trustStoreLocation.trim();
	}
	public void setTrustStorePwd(String trustStorePwd){
		trustStorePass=trustStorePwd.trim();
	}
	
	public void setTrustStoreType(String trustStoreType){
		trustStoreTyp= trustStoreType.trim();
	}

	public void setLdapSyncOnStartup(boolean ldapStartSyncs) {
		ldapSyncOnStartup = ldapStartSyncs;
	}

	public String getLdapSystemDN() {
		return systemDN;
	}

	public void setLdapSystemDN(String ldapSystemDN) {
		systemDN = ldapSystemDN.trim();
	}
	
	public String getLdapSystemPW() {
		return systemPW;
	}

	public void setLdapSystemPW(String ldapSystemPW) {
		systemPW = ldapSystemPW.trim();
	}
	
	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrlConfig) {
		ldapUrl = ldapUrlConfig.trim();
	}
	

	
	
	public Integer getLdapConnectionTimeout() {
		return connectionTimeout;
	}
	
	public void setLdapConnectionTimeout(Integer timeout) {
		connectionTimeout = timeout;
	}

	public void setLdapSyncCronSync(boolean ldapSyncCronSync) {
		this.ldapSyncCronSync = ldapSyncCronSync;
	}

	public void setLdapSyncCronSyncExpression(String ldapSyncCronSyncExpression) {
		this.ldapSyncCronSyncExpression = ldapSyncCronSyncExpression.trim();
	}
	
	public void setCacheLDAPPwdAsOLATPwdOnLogin(boolean cacheLDAPPwdAsOLATPwdOnLogin) {
		this.cacheLDAPPwdAsOLATPwdOnLogin = cacheLDAPPwdAsOLATPwdOnLogin;
	}

	public void setCreateUsersOnLogin(boolean createUsersOnLogin) {
		this.createUsersOnLogin = createUsersOnLogin;
	}

	public void setConvertExistingLocalUsersToLDAPUsers(boolean convertExistingLocalUsersToLDAPUsers) {
		this.convertExistingLocalUsersToLDAPUsers = convertExistingLocalUsersToLDAPUsers;
	}

	public void setDeleteRemovedLDAPUsersOnSync(boolean deleteRemovedLDAPUsersOnSync) {
		this.deleteRemovedLDAPUsersOnSync = deleteRemovedLDAPUsersOnSync;
	}
	
	public void setDeleteRemovedLDAPUsersPercentage(int deleteRemovedLDAPUsersPercentage){
		this.deleteRemovedLDAPUsersPercentage = deleteRemovedLDAPUsersPercentage;
	}

	public void setPropagatePasswordChangedOnLdapServer(boolean propagatePasswordChangedOnServer) {
		this.propagatePasswordChangedOnLdapServer = propagatePasswordChangedOnServer;
	}

	public boolean isLDAPEnabled() {
		return ldapEnabled;
	}

	public boolean isSslEnabled() {
		return sslEnabled;
	}
	
	public boolean isActiveDirectory() {
		return activeDirectory;
	}
	
	public String getLdapDateFormat() {
		if(StringHelper.containsNonWhitespace(ldapDateFormat)) {
			return ldapDateFormat;
		}
		return "yyyyMMddHHmmss'Z'";//default
	}
	
	public String getTrustStoreLocation(){
		return trustStoreLoc;
	}
	
	public String getTrustStorePwd(){
		return trustStorePass;
	}
	
	public String getTrustStoreType(){
		return trustStoreTyp;
	}

	public boolean isLdapSyncOnStartup() {
		return ldapSyncOnStartup;
	}

	public boolean isLdapSyncCronSync() {
		return ldapSyncCronSync;
	}

	public String getLdapSyncCronSyncExpression() {
		return ldapSyncCronSyncExpression;
	}
	
	public boolean isCreateUsersOnLogin() {
		return createUsersOnLogin;
	}

	public boolean isCacheLDAPPwdAsOLATPwdOnLogin() {
		return cacheLDAPPwdAsOLATPwdOnLogin;
	}

	public boolean isConvertExistingLocalUsersToLDAPUsers() {
		return convertExistingLocalUsersToLDAPUsers;
	}

	public boolean isDeleteRemovedLDAPUsersOnSync() {
		return deleteRemovedLDAPUsersOnSync;
	}
	
	public int getDeleteRemovedLDAPUsersPercentage(){
		return deleteRemovedLDAPUsersPercentage;
	}

	public boolean isPropagatePasswordChangedOnLdapServer(){
		return propagatePasswordChangedOnLdapServer;
	}
}
