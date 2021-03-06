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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 01.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractFlexiTableRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		FlexiTableComponent ftC = (FlexiTableComponent) source;
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		String id = ftC.getFormDispatchId();

		renderHeaderButtons(renderer, sb, ftE, ubu, translator, renderResult, args);
		
		if(ftE.getTableDataModel().getRowCount() == 0 && StringHelper.containsNonWhitespace(ftE.getEmtpyTableMessageKey())) {
			String emptyMessageKey = ftE.getEmtpyTableMessageKey();
			sb.append("<div class='o_info'>")
			  .append(translator.translate(emptyMessageKey))
			  .append("</div>");
		} else {
			sb.append("<div class='o_table_wrapper o_table_flexi")
			  .append(" o_table_edit", ftE.isEditMode());
			String css = ftE.getElementCssClass();
			if (css != null) {
				sb.append(" ").append(css);
			}
			switch(ftE.getRendererType()) {
				case custom: sb.append(" o_rendertype_custom"); break;
				case classic: sb.append(" o_rendertype_classic"); break;
			}
			sb.append("'");
			String wrapperSelector = ftE.getWrapperSelector();
			if (wrapperSelector != null) {
				sb.append(" id='").append(wrapperSelector).append("'");
			}
			sb.append("><table id=\"").append(id).append("\" class=\"table table-condensed table-striped table-hover\">");
			
			//render headers
			renderHeaders(sb, ftC, translator);
			//render body
			sb.append("<tbody>");
			renderBody(renderer, sb, ftC, ubu, translator, renderResult);
			sb.append("</tbody></table>");
			renderFooterButtons(sb, ftC, translator);
			//draggable
			if(ftE.getColumnIndexForDragAndDropLabel() > 0) {
				sb.append("<script type='text/javascript'>")
				  .append("/* <![CDATA[ */ \n")
				  .append("jQuery(function() {\n")
				  .append(" jQuery('.o_table_flexi table tr').draggable({\n")
		          .append("  containment: '#o_main',\n")
		          .append("	 zIndex: 10000,\n")
		          .append("	 cursorAt: {left: 0, top: 0},\n")
		          .append("	 accept: function(event,ui){ return true; },\n")
		          .append("	 helper: function(event,ui,zt) {\n")
		          .append("    var helperText = jQuery(this).children('.o_dnd_label').text();\n")
		          .append("    return jQuery(\"<div class='ui-widget-header o_table_drag'>\" + helperText + \"</div>\").appendTo('body').css('zIndex',5).show();\n")
		          .append("  }\n")
		          .append("});\n")
		          .append("});\n")
		          .append("/* ]]> */\n")
				  .append("</script>\n");
			}
			
			sb.append("</div>");
		}
		
		//source
		if (source.isEnabled()) {
			FormJSHelper.appendFlexiFormDirty(sb, ftE.getRootForm(), id);
		}
	}
	
	protected void renderHeaderButtons(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		Component searchCmp = ftE.getExtendedSearchComponent();
		
		if(searchCmp == null && !ftE.isExtendedSearchExpanded() && !ftE.isNumOfRowsEnabled()
				&& !ftE.isFilterEnabled() && !ftE.isSortEnabled() && ! ftE.isExportEnabled()
				&& !ftE.isCustomizeColumns() && ftE.getAvailableRendererTypes().length  <= 1) {
			return;
		}
		
		if(searchCmp != null && ftE.isExtendedSearchExpanded()) {
			renderer.render(searchCmp, sb, args);
		}
		
		sb.append("<div class='row clearfix o_table_toolbar'>")
		  .append("<div class='col-sm-6 col-xs-12'>");
		if(searchCmp == null || !ftE.isExtendedSearchExpanded()) {
			renderHeaderSearch(renderer, sb, ftE, ubu, translator, renderResult, args);
		}
		sb.append("</div>");

		sb.append("<div class='col-sm-3 col-xs-4 o_table_row_count'>");
		if(ftE.isNumOfRowsEnabled()) {
			int rowCount = ftE.getTableDataModel().getRowCount();
			if(rowCount == 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entry"));
			} else if(rowCount > 1) {
				sb.append(rowCount).append(" ").append(ftE.getTranslator().translate("table.entries"));
			}
		}
		sb.append("</div><div class='col-sm-3 col-xs-8'><div class='pull-right'><div class='o_table_tools'>");
		
		boolean empty = ftE.getTableDataModel().getRowCount() == 0;

		String filterIndication = null;
		//filter
		if(ftE.isFilterEnabled()) {
			List<FlexiTableFilter> filters = ftE.getFilters();
			if(filters != null && filters.size() > 0) {
				filterIndication = renderFilterDropdown(sb, ftE, filters);
			}
		}
		
		//sort
		if(ftE.isSortEnabled()) {
			List<FlexiTableSort> sorts = ftE.getSorts();
			if(sorts != null && sorts.size() > 0) {
				renderSortDropdown(sb, ftE, sorts);
			}
		}
		
		if(ftE.getExportButton() != null && ftE.isExportEnabled()) {
			sb.append("<div class='btn-group'>");
			ftE.getExportButton().setEnabled(!empty);
			renderFormItem(renderer, sb, ftE.getExportButton(), ubu, translator, renderResult, args);
			sb.append("</div> ");
		}
		if(ftE.getCustomButton() != null && ftE.isCustomizeColumns()) {
			sb.append("<div class='btn-group'>");
			renderFormItem(renderer, sb, ftE.getCustomButton(), ubu, translator, renderResult, args);
			sb.append("</div> ");
		}
		
		//switch type of tables
		FlexiTableRendererType[] types = ftE.getAvailableRendererTypes();
		if(types.length > 1) {
			sb.append("<div class='btn-group'>");
			for(FlexiTableRendererType type:types) {
				renderHeaderSwitchType(type, renderer, sb, ftE, ubu, translator, renderResult, args);
			}
			sb.append("</div> ");
		}
		sb.append("</div>");
		if(StringHelper.containsNonWhitespace(filterIndication)) {
			sb.append("<div class='o_table_tools_indications'><i class='o_icon o_icon_filter o_icon-lg'> </i> ").append(filterIndication).append("</div>");
		}
		sb.append("</div>");
		
		
		sb.append("</div></div>");
	}
	
	protected void renderHeaderSearch(Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		if(ftE.isSearchEnabled()) {
			sb.append("<div class='o_table_search input-group'>");
			renderFormItem(renderer, sb, ftE.getSearchElement(), ubu, translator, renderResult, args);
			sb.append("<div class='input-group-btn'>");
			renderFormItem(renderer, sb, ftE.getSearchButton(), ubu, translator, renderResult, args);
			if(ftE.getExtendedSearchButton() != null) {
				renderFormItem(renderer, sb, ftE.getExtendedSearchButton(), ubu, translator, renderResult, args);
			}
			sb.append("</div></div>");
		} else if(ftE.getExtendedSearchButton() != null) {
			renderFormItem(renderer, sb, ftE.getExtendedSearchButton(), ubu, translator, renderResult, args);
		}
	}
	
	protected String renderFilterDropdown(StringOutput sb, FlexiTableElementImpl ftE, List<FlexiTableFilter> filters) {
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		String selected = null;
		
		sb.append("<div class='btn-group'>")
		  .append("<button id='table-button-filters-").append(dispatchId).append("' type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown'>")
		  .append("<i class='o_icon o_icon_filter o_icon-lg'> </i> <b class='caret'></b></button>")
		  .append("<div id='table-filters-").append(dispatchId).append("' class='hide'><ul class='o_dropdown list-unstyled' role='menu'>");
		
		for(FlexiTableFilter filter:filters) {
			if(FlexiTableFilter.SPACER.equals(filter)) {
				sb.append("<li class='divider'></li>");
			} else {
				sb.append("<li><a href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1, new NameValuePair("filter", filter.getFilter())))
				  .append("\">").append("<i class='o_icon o_icon_check o_icon-fw'> </i> ", filter.isSelected());
				if(filter.getIconLeftCSS() != null) {
					sb.append("<i class='o_icon ").append(filter.getIconLeftCSS()).append("'> </i> ");
				}
				sb.append(filter.getLabel()).append("</a></li>");
				if(filter.isSelected()) {
					selected = filter.getLabel();
				}
			}
		}
		sb.append("</ul></div></div> ")
		  .append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */\n")
		  .append("jQuery(function() { o_popover('table-button-filters-").append(dispatchId).append("','table-filters-").append(dispatchId).append("'); });\n")
		  .append("/* ]]> */\n")
		  .append("</script>");
		return selected;
	}
	
	protected void renderSortDropdown(StringOutput sb, FlexiTableElementImpl ftE, List<FlexiTableSort> sorts) {
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		
		sb.append("<div class='btn-group'>")
		  .append("<button id='table-button-sorters-").append(dispatchId).append("' type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown'>")
		  .append("<i class='o_icon o_icon_sort_menu o_icon-lg'> </i> <b class='caret'></b></button>")
		  .append("<div id='table-sorters-").append(dispatchId).append("' class='hide'><ul class='o_dropdown list-unstyled' role='menu'>");
		
		for(FlexiTableSort sort:sorts) {
			if(FlexiTableSort.SPACER.equals(sort)) {
				sb.append("<li class='divider'></li>");
			} else {
				sb.append("<li><a href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1,
						  new NameValuePair("sort", sort.getSortKey().getKey()),
						  new NameValuePair("asc",  sort.getSortKey().isAsc() ? "desc" : "asc")))
				  .append("\">");
				if(sort.isSelected()) {
					if(sort.getSortKey().isAsc()) {
						sb.append("<i class='o_icon o_icon_sort_desc o_icon-fw'> </i> ");
					} else {
						sb.append("<i class='o_icon o_icon_sort_asc o_icon-fw'> </i> ");
					}
				}
				sb.append(sort.getLabel()).append("</a></li>");
			}
		}
		sb.append("</ul></div></div> ")
		  .append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */\n")
		  .append("jQuery(function() { o_popover('table-button-sorters-").append(dispatchId).append("','table-sorters-").append(dispatchId).append("'); });\n")
		  .append("/* ]]> */\n")
		  .append("</script>");
	}
	
	protected void renderHeaderSwitchType(FlexiTableRendererType type, Renderer renderer, StringOutput sb, FlexiTableElementImpl ftE, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if(type != null) {
			switch(type) {
				case custom: {
					renderFormItem(renderer, sb, ftE.getCustomTypeButton(), ubu, translator, renderResult, args);
					break;
				}
				case classic: {
					renderFormItem(renderer, sb, ftE.getClassicTypeButton(), ubu, translator, renderResult, args);
					break;
				}
			}
		}
	}
	
	protected void renderFormItem(Renderer renderer, StringOutput sb, FormItem item, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		if(item != null) {
			Component cmp = item.getComponent();
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
		}
	}
	
	protected void renderFooterButtons(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		if(ftE.isSelectAllEnable()) {
			String formName = ftE.getRootForm().getFormName();
			String dispatchId = ftE.getFormDispatchId();

			sb.append("<div class='o_table_footer'><div class='o_table_checkall input-sm'>");

			sb.append("<label class='checkbox-inline'><a id='")
			  .append(dispatchId).append("_sa' href=\"javascript:o_table_toggleCheck('").append(formName).append("', true);")
			  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, new NameValuePair("select", "checkall")))
			  .append("\"><i class='o_icon o_icon-lg o_icon_check_on'> </i> <span>").append(translator.translate("form.checkall"))
			  .append("</span></a></label>");

			sb.append("<label class='checkbox-inline'><a id='")
			  .append(dispatchId).append("_dsa' href=\"javascript:o_table_toggleCheck('").append(formName).append("', false);")
			  .append(FormJSHelper.getXHRFnCallFor(ftE.getRootForm(), dispatchId, 1, new NameValuePair("select", "uncheckall")))
			  .append("\"><i class='o_icon o_icon-lg o_icon_check_off'> </i> <span>").append(translator.translate("form.uncheckall"))
			  .append("</span></a></label>");

			sb.append("</div></div>");
		}
		
		if(ftE.getDefaultPageSize() > 0) {
			renderPagesLinks(sb, ftC, translator);
		}
	}
	
	protected abstract void renderHeaders(StringOutput target, FlexiTableComponent ftC, Translator translator);
	
	protected void renderBody(Renderer renderer, StringOutput target, FlexiTableComponent ftC,
			URLBuilder ubu, Translator translator, RenderResult renderResult) {
		
		String id = ftC.getFormDispatchId();
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		
		// the really selected rowid (from the tabledatamodel)
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);

		String rowIdPrefix = "row_" + id + "-";
		for (int i = firstRow; i < lastRow; i++) {
			if(dataModel.isRowLoaded(i)) {
				renderRow(renderer, target, ftC, rowIdPrefix, i, ubu, translator, renderResult);
			}
		}				
		// end of table table
	}
	
	protected abstract void renderRow(Renderer renderer, StringOutput target, FlexiTableComponent ftC, String rowIdPrefix,
			int row, URLBuilder ubu, Translator translator, RenderResult renderResult);


	private void renderPagesLinks(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		int pageSize = ftE.getPageSize();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		int rows = dataModel.getRowCount();

		if (rows > 20) {
			renderPageSize(sb, ftC, translator);
		}

		sb.append("<ul class='pagination'>");
		if(pageSize > 0 && rows > pageSize) {
			int page = ftE.getPage();
			int maxPage = (int)Math.ceil(((double) rows / (double) pageSize));
			renderPageBackLink(sb, ftC, page);
			renderPageNumberLinks(sb, ftC, page, maxPage);
			renderPageNextLink(sb, ftC, page, maxPage);
		}
		sb.append("</ul>");
	}
	
	private void renderPageSize(StringOutput sb, FlexiTableComponent ftC, Translator translator) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
		
		Form theForm = ftE.getRootForm();
		String dispatchId = ftE.getFormDispatchId();
		
		int pageSize = ftE.getPageSize();
		int firstRow = ftE.getFirstRow();
		int maxRows = ftE.getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);
		
		sb.append("<div class='o_table_rows_infos'>");
		sb.append(translator.translate("page.size.a", new String[] {
				Integer.toString(firstRow + 1),//for humans
				Integer.toString(lastRow),
				Integer.toString(rows)
		  }))
		  .append(" ");
		
		sb.append("<div class='btn-group dropup'><button type='button' class='btn btn-default dropdown-toggle' data-toggle='dropdown' aria-expanded='false'>")
	      .append(" <span>");
		if(pageSize < 0) {
			sb.append(translator.translate("show.all"));
		} else {
			sb.append(Integer.toString(pageSize));
		}
		
		sb.append("</span> <span class='caret'></span></button>")
	      .append("<ul class='dropdown-menu' role='menu'>");
		
		int[] sizes = new int[]{ 20, 50, 100, 250 };
		for(int size:sizes) {
			sb.append("<li><a href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1,
					  new NameValuePair("pagesize", Integer.toString(size))))
			  .append("\"  onclick=\"return o2cl();\">").append(Integer.toString(size)).append("</a></li>");
		}
		
		if(ftE.isShowAllRowsEnabled()) {
			sb.append("<li><a href=\"javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, dispatchId, 1,
					  new NameValuePair("pagesize", "all")))
			  .append("\" onclick=\"return o2cl();\">").append(translator.translate("show.all")).append("</a></li>");
		}
		  
		sb.append("</ul></div>")
		  .append(" ").append(translator.translate("page.size.b"))
		  .append("</div> ");
	}
	
	private void renderPageBackLink(StringOutput sb, FlexiTableComponent ftC, int page) {
		boolean disabled = (page <= 0);
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<li").append(" class='disabled'", disabled).append("><a href=\"");
		if(disabled) {
			sb.append("#");
		} else {
			sb.append("javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("page", Integer.toString(page - 1))));
		}
		sb.append("\">").append("&laquo;").append("</a></li>");
	}
	
	private void renderPageNextLink(StringOutput sb, FlexiTableComponent ftC, int page, int maxPage) {
		boolean disabled = (page >= maxPage);
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<li ").append(" class='disabled'", disabled).append("><a href=\"");
		if(disabled) {
			sb.append("#");
		} else {
			sb.append("javascript:")
			  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("page", Integer.toString(page + 1)))); 
		}
		sb.append("\">").append("&raquo;").append("</li></a>");
	}
	
	private void renderPageNumberLinks(StringOutput sb, FlexiTableComponent ftC, int page, int maxPage) {
		if (maxPage < 12) {
			for (int i=0; i<maxPage; i++) {
				appendPagenNumberLink(sb, ftC, page, i);
			}
		} else {
			int powerOf10 = String.valueOf(maxPage).length() - 1;
			int maxStepSize = (int) Math.pow(10, powerOf10);
			int stepSize = (int) Math.pow(10, String.valueOf(page).length() - 1);
			boolean isStep = false;
			int useEveryStep = 3;
			int stepCnt = 0;
			boolean isNear = false;
			int nearleft = 5;
			int nearright = 5;
			if (page < nearleft) {
				nearleft = page;
				nearright += (nearright - nearleft);
			} else if (page > (maxPage - nearright)) {
				nearright = maxPage - page;
				nearleft += (nearleft - nearright);
			}
			for (int i = 0; i <= maxPage; i++) {
				// adapt stepsize if needed
				stepSize = adaptStepIfNeeded(page, maxStepSize, stepSize, i);
	
				isStep = ((i % stepSize) == 0);
				if (isStep) {
					stepCnt++;
					isStep = isStep && (stepCnt % useEveryStep == 0);
				}
				isNear = (i > (page - nearleft) && i < (page + nearright));
				if (i == 0 || i == maxPage || isStep || isNear) {
					appendPagenNumberLink(sb, ftC, page, i);
				}
			}
		}
	}
	
	private void appendPagenNumberLink(StringOutput sb, FlexiTableComponent ftC, int page, int i) {
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		Form theForm = ftE.getRootForm();
		sb.append("<li").append(" class='active'", (page == i)).append("><a href=\"javascript:")
		  .append(FormJSHelper.getXHRFnCallFor(theForm, ftC.getFormDispatchId(), 1, new NameValuePair("page", Integer.toString(i))))
		  .append("\">").append(i+1).append("</a></li>");
	}

	private int adaptStepIfNeeded(final int page, final int maxStepSize, final int stepSize, final int i) {
		int newStepSize = stepSize;
		if (i < page && stepSize > 1 && ((page - i) / stepSize == 0)) {
			newStepSize = stepSize / 10;
		} else if (i > page && stepSize < maxStepSize && ((i - page) / stepSize == 9)) {
			newStepSize = stepSize * 10;
		}
		return newStepSize;
	}
}
