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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.webservices.clients.onyxreporter;

/**
 * Description:<br>
 * TODO: thomasw Class Description for OnyxReporterException
 *
 * <P>
 * Initial Date:  09.09.2009 <br>
 * @author thomasw@bps-system.de
 */
public class OnyxReporterException extends Exception {

	/**
	 * 			//<ONYX-705>
	 */
	private static final long serialVersionUID = -3120820706663500150L;

	OnyxReporterException(String msg) {
		super(msg);
	}
	//<ONYX-705>
	OnyxReporterException(String msg, Throwable cause) {
		super(msg, cause);
	}
	//</ONYX-705>
}
