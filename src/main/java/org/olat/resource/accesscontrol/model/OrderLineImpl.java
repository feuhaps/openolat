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

package org.olat.resource.accesscontrol.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * Description:<br>
 * An implementation of the OrderLine interface
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity
public class OrderLineImpl extends PersistentObject implements OrderLine {

	private static final long serialVersionUID = -2630817206449967033L;
	
	@ManyToOne(targetEntity=OfferImpl.class)
	private Offer offer;
	@Embedded
	private Price unitPrice;
	@Embedded
	private Price totalPrice;
	
	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	@Override
	public Price getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Price unitPrice) {
		this.unitPrice = unitPrice;
	}

	@Override
	public Price getTotal() {
		return totalPrice;
	}

	public void setTotal(Price totalPrice) {
		this.totalPrice = totalPrice;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 27591 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrderLineImpl) {
			OrderLineImpl order = (OrderLineImpl)obj;
			return equalsByPersistableKey(order);
		}
		return false;
	}
}
