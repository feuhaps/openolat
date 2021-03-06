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
package org.olat.modules.qpool.ui.datasource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.qpool.ui.QuestionItemsSource;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectionOfItemsSource implements QuestionItemsSource {
	
	private final Roles roles;
	private final Identity identity;
	private final QPoolService qpoolService;
	private final QuestionItemCollection collection;
	
	public CollectionOfItemsSource(QuestionItemCollection collection, Identity identity, Roles roles) {
		this.roles = roles;
		this.identity = identity;
		this.collection = collection;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
	}

	@Override
	public String getName() {
		return collection.getName();
	}

	public QuestionItemCollection getCollection() {
		return collection;
	}

	@Override
	public Controller getSourceController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public boolean isRemoveEnabled() {
		return true;
	}

	@Override
	public boolean isDeleteEnabled() {
		return false;
	}

	@Override
	public boolean askEditable() {
		return false;
	}

	@Override
	public int postImport(List<QuestionItem> items, boolean editable) {
		if(items == null || items.isEmpty()) return 0;
		qpoolService.addItemToCollection(items, Collections.singletonList(collection));
		return items.size();
	}

	@Override
	public void removeFromSource(List<QuestionItemShort> items) {
		qpoolService.removeItemsFromCollection(items, collection);
	}

	@Override
	public int getNumOfItems() {
		return qpoolService.countItemsOfCollection(collection);
	}

	@Override
	public List<QuestionItemView> getItems(Collection<Long> key) {
		SearchQuestionItemParams params = new SearchQuestionItemParams(identity, roles);
		params.setItemKeys(key);
		ResultInfos<QuestionItemView> items = qpoolService.getItemsOfCollection(collection, params, 0, -1);
		return items.getObjects();
	}

	@Override
	public ResultInfos<QuestionItemView> getItems(String query, List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		SearchQuestionItemParams params = new SearchQuestionItemParams(identity, roles);
		params.setSearchString(query);
		params.setCondQueries(condQueries);
		return qpoolService.getItemsOfCollection(collection, params, firstResult, maxResults, orderBy);
	}
}
