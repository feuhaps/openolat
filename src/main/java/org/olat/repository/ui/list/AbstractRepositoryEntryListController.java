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
package org.olat.repository.ui.list;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.impl.UserRatingsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.list.RepositoryEntryDataModel.Cols;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractRepositoryEntryListController extends FormBasicController implements RepositoryEntryDataSourceUIFactory {
	
	private FormLink listLink, tableLink;
	private FlexiTableElement tableEl;
	private RepositoryEntryDataModel model;
	private FlexiTableDataSourceDelegate<RepositoryEntryRow> dataSource;
	
	private final String mapperThumbnailUrl;
	private final MarkManager markManager;
	private final UserRatingsDAO userRatingsDao;
	
	public AbstractRepositoryEntryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "repoentry_table");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		userRatingsDao = CoreSpringFactory.getImpl(UserRatingsDAO.class);
		mapperThumbnailUrl = registerCacheableMapper(ureq, "repositoryentryImage", new RepositoryEntryImageMapper());
	}
	
	public FlexiTableDataSourceDelegate<RepositoryEntryRow> getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(FlexiTableDataSourceDelegate<RepositoryEntryRow> dataSource) {
		this.dataSource = dataSource;
		if(tableEl != null) {
			model.setSource(dataSource);
			tableEl.reset();
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		listLink = uifactory.addFormLink("switchLayoutList", "list", "table.switch.list", null, formLayout, Link.BUTTON);
		tableLink = uifactory.addFormLink("switchLayoutTable", "table", "table.switch.table", null, formLayout, Link.BUTTON);
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.key.i18nKey(), Cols.key.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName.i18nKey(), Cols.displayName.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleLabel.i18nKey(), Cols.lifecycleLabel.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleSoftkey.i18nKey(), Cols.lifecycleSoftkey.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleStart.i18nKey(), Cols.lifecycleStart.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lifecycleEnd.i18nKey(), Cols.lifecycleEnd.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.select.i18nKey(), Cols.select.ordinal(), false, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.details.i18nKey(), Cols.details.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.start.i18nKey(), Cols.start.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.ratings.i18nKey(), Cols.ratings.ordinal()));

		model = new RepositoryEntryDataModel(dataSource, columnsModel);
		tableEl = uifactory.addTableElement(ureq, getWindowControl(), "table", model, 20, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		
		VelocityContainer row = createVelocityContainer("row_1");
		tableEl.setRowRenderer(row);
	}

	@Override
	public String getMapperThumbnailUrl() {
		return mapperThumbnailUrl;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == listLink) {
			tableEl.setRendererType(FlexiTableRendererType.custom);
		} else if(source == tableLink) {
			tableEl.setRendererType(FlexiTableRendererType.classic);
		} else if(source instanceof RatingWithAverageFormItem && event instanceof RatingFormEvent) {
			RatingFormEvent ratingEvent = (RatingFormEvent)event;
			RatingWithAverageFormItem ratingItem = (RatingWithAverageFormItem)source;
			RepositoryEntryRow row = (RepositoryEntryRow)ratingItem.getUserObject();
			doRating(row, ratingEvent.getRating());
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			
			if("mark".equals(cmd)) {
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				if(doMark(row)) {
					link.setCustomEnabledLinkCSS("b_mark_set");
				} else {
					link.setCustomEnabledLinkCSS("b_mark_not_set");
				}
				link.getComponent().setDirty(true);
			} else if ("select".equals(cmd) || "start".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpen(ureq, row);
			} else if ("details".equals(cmd)){
				RepositoryEntryRow row = (RepositoryEntryRow)link.getUserObject();
				doOpenDetails(ureq, row);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void doRating(RepositoryEntryRow row, float rating) {
		OLATResourceable ores = row.getOLATResourceable();
		userRatingsDao.updateRating(getIdentity(), ores, null, Math.round(rating));
	}
	
	protected void doOpen(UserRequest ureq, RepositoryEntryRow row) {
		String businessPath = "[RepositoryEntry:" + row.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenDetails(UserRequest ureq, RepositoryEntryRow row) {
		String businessPath = "[RepositoryEntry:" + row.getKey() + "][details:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected boolean doMark(RepositoryEntryRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		} else {
			String businessPath = "[QuestionItem:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}

	@Override
	public void forgeMarkLink(RepositoryEntryRow row) {
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "&nbsp;&nbsp;&nbsp;&nbsp;", null, null, Link.NONTRANSLATED);
		markLink.setCustomEnabledLinkCSS(row.isMarked() ? "b_mark_set" : "b_mark_not_set");
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
	}
	
	@Override
	public void forgeSelectLink(RepositoryEntryRow row) {
		String name = row.getDisplayName();
		FormLink selectLink = uifactory.addFormLink("select_" + row.getKey(), "select", name, null, null, Link.NONTRANSLATED);
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	@Override
	public void forgeStartLink(RepositoryEntryRow row) {
		FormLink startLink = uifactory.addFormLink("start_" + row.getKey(), "start", "start", null, null, Link.LINK);
		startLink.setUserObject(row);
		row.setStartLink(startLink);
	}
	
	@Override
	public void forgeDetailsLink(RepositoryEntryRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + row.getKey(), "details", "details", null, null, Link.LINK);
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
	}

	@Override
	public void forgeRatings(RepositoryEntryRow row, RepositoryEntry entry) {
		CommentAndRatingService commentAndRatingService = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
		commentAndRatingService.init(getIdentity(), entry.getOlatResource(), null, false, false);
		UserRating rating = userRatingsDao.getRating(getIdentity(), row.getOLATResourceable(), null);
		float ratingValue = rating != null ? rating.getRating() : 0.0f;
		RatingWithAverageFormItem ratingCmp = new RatingWithAverageFormItem("rat_" + row.getKey(), ratingValue, 4, 5, 32);
		row.setRatingFormItem(ratingCmp);
		ratingCmp.setUserObject(row);
	}
}