package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CrfVersion;

public class CrfVersionDao extends AbstractDomainDao<CrfVersion> {

	@Override
	Class<CrfVersion> domainClass() {
		// TODO Auto-generated method stub
		return CrfVersion.class;
	}

}
