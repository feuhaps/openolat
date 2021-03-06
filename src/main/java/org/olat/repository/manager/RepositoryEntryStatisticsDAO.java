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

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.persistence.LockModeType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.UserCommentsDelegate;
import org.olat.core.commons.services.commentAndRating.UserRatingsDelegate;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryStatisticsDAO implements UserRatingsDelegate, UserCommentsDelegate {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	
	@PostConstruct
	public void init() {
		userRatingsDao.addDegelate(this);
		userCommentsDao.addDelegate(this);
	}
	
	/**
	 * Increment the launch counter.
	 * @param re
	 */
	public synchronized void incrementLaunchCounter(RepositoryEntry re) {
		RepositoryEntryStatistics stats = loadStatisticsForUpdate(re);
		if(stats != null) {
			stats.setLaunchCounter(stats.getLaunchCounter() + 1);
			stats.setLastUsage(new Date());
			dbInstance.getCurrentEntityManager().merge(stats);
		}
		dbInstance.commit();
	}

	/**
	 * Increment the download counter.
	 * @param re
	 */
	public void incrementDownloadCounter(RepositoryEntry re) {
		RepositoryEntryStatistics stats = loadStatisticsForUpdate(re);
		if(stats != null) {
			stats.setDownloadCounter(stats.getDownloadCounter() + 1);
			stats.setLastUsage(new Date());
			dbInstance.getCurrentEntityManager().merge(stats);
		}
		dbInstance.commit();
	}

	/**
	 * Set last-usage date to to now for certain repository-entry.
	 * @param 
	 */
	public void setLastUsageNowFor(RepositoryEntry re) {
		if (re == null) return;
		
		Date newUsage = new Date();
		RepositoryEntryStatistics stats = loadStatistics(re);
		Date lastUsage = stats.getLastUsage();
		//update every minute and not shorter
		if(lastUsage == null || (newUsage.getTime() - lastUsage.getTime()) > 60000) {
			stats.setLastUsage(newUsage);
			dbInstance.getCurrentEntityManager().merge(stats);
			dbInstance.commit();
		}
	}
	
	protected RepositoryEntryStatistics loadStatistics(OLATResourceable repositoryEntryRes) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v.statistics from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" where v.key=:key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryStatistics.class)
				.setParameter("key", repositoryEntryRes.getResourceableId())
				.getSingleResult();
	}
	
	private RepositoryEntryStatistics loadStatisticsForUpdate(OLATResourceable repositoryEntryRes) {
		if(repositoryEntryRes instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)repositoryEntryRes;
			dbInstance.getCurrentEntityManager().detach(re);
			dbInstance.getCurrentEntityManager().detach(re.getStatistics());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select stats from ").append(RepositoryEntryStatistics.class.getName()).append(" as stats")
		  .append(" where stats.key in (select v.statistics.key from ").append(RepositoryEntry.class.getName()).append(" as v where v.key=:key)");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryStatistics.class)
				.setParameter("key", repositoryEntryRes.getResourceableId())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getSingleResult();
	}

	@Override
	public boolean accept(OLATResourceable ores, String resSubPath) {
		if("RepositoryEntry".equals(ores.getResourceableTypeName()) && resSubPath == null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean update(OLATResourceable ores, String resSubPath, double newAverageRating, long numOfRatings) {
		RepositoryEntryStatistics statistics = loadStatisticsForUpdate(ores);
		if(statistics != null) {
			statistics.setRating(newAverageRating);
			statistics.setNumOfRatings(numOfRatings);
			statistics.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(statistics);
			return true;
		}
		return false;
	}

	@Override
	public boolean update(OLATResourceable ores, String resSubPath, int numOfComments) {
		RepositoryEntryStatistics statistics = loadStatisticsForUpdate(ores);
		if(statistics != null) {
			statistics.setNumOfComments(numOfComments);
			statistics.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(statistics);
			return true;
		}
		return false;
	}
}