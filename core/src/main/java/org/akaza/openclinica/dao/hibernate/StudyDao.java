/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Study;


public class StudyDao extends AbstractDomainDao<Study> {
	
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }
}
