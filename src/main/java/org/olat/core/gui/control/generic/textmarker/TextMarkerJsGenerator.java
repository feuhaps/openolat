/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.core.gui.control.generic.textmarker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.commons.modules.glossary.GlossaryItem;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.util.vfs.VFSContainer;


/**
 * 
 * Generates Java-Script code for text-marker component.
 * 
 * @author Christian Guretzki
 * @author Roman Haag, frentix GmbH, frentix.com
 * 
 */
public class TextMarkerJsGenerator {

	
	public static String loadGlossaryItemListAsJSCommandsString(VFSContainer glossaryFolder, String domID) {
		ArrayList<GlossaryItem> glossaryItemArr = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);
		StringBuilder sb = new StringBuilder();		
		sb.append("o_info.glossaryTermArray_").append(domID).append(" = ").append(buildJSArrayString(glossaryItemArr));
		// start highlighting process with this array
		sb.append("jQuery(function() {o_tm_highlightFromArray(o_info.glossaryTermArray_").append(domID).append(", \"").append(domID).append("\")});");
		
		return sb.toString();
	}
	
	public static String loadGlossaryItemListAsJSArray(VFSContainer glossaryFolder) {
		List<GlossaryItem> glossaryItemArr = GlossaryItemManager.getInstance().getGlossaryItemListByVFSItem(glossaryFolder);

		StringBuilder sb = new StringBuilder();		
		sb.append(buildJSArrayString(glossaryItemArr));
		return sb.toString();		
	}
	
	/*
	 * build array of glossaryTerms containing array with term, flexion, synonym...
	 */
	public static StringBuilder buildJSArrayString(List<GlossaryItem> glossaryItemArr){
		StringBuilder sb = new StringBuilder();
		sb.append("new Array(");
		for (Iterator<GlossaryItem> iterator = glossaryItemArr.iterator(); iterator.hasNext();) {
			GlossaryItem glossaryItem = iterator.next();
			ArrayList<String> allHighlightStrings = glossaryItem.getAllStringsToMarkup();
			sb.append("new Array(\"");
			for (Iterator<String> iterator2 = allHighlightStrings.iterator(); iterator2.hasNext();) {
				String termFlexionSynonym = StringEscapeUtils.escapeJava(iterator2.next());
				sb.append(termFlexionSynonym);
				sb.append("\"");
				if (iterator2.hasNext()) {
					sb.append(",\"");
				}
			}
			sb.append(")");
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(");");
		return sb;
	}
}