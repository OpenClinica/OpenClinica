/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.VersioningMap;

public class VersioningMapDao extends AbstractDomainDao<VersioningMap> {

    @Override
    Class<VersioningMap> domainClass() {
        // TODO Auto-generated method stub
        return VersioningMap.class;
    }

}
