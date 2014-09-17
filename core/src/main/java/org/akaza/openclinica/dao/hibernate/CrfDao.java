package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CrfBean;

public class CrfDao  extends AbstractDomainDao<CrfBean>{

	@Override
	Class<CrfBean> domainClass() {
		// TODO Auto-generated method stub
		return CrfBean.class;
	}
	

}
