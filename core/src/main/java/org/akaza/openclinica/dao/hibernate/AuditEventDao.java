/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.AuditEvent;


public class AuditEventDao extends AbstractDomainDao<AuditEvent> {

	 @Override
	    public Class<AuditEvent> domainClass() {
	        return AuditEvent.class;
	    }
}
