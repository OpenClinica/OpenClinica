package org.akaza.openclinica.domain;

import javax.persistence.Transient;
import java.io.Serializable;

public class DataMapDomainObject implements MutableDomainObject,Serializable {

	@Override
	public void setId(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transient
	public Integer getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVersion(Integer version) {
		// TODO Auto-generated method stub
		
	}

	@Override
	@Transient
	public Integer getId() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
