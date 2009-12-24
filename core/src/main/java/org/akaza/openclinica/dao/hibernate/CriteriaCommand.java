package org.akaza.openclinica.dao.hibernate;

import org.hibernate.Criteria;

public interface CriteriaCommand {

    public Criteria execute(Criteria criteria);

}
