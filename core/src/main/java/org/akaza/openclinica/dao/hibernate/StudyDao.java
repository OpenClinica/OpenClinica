package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Study;
import org.hibernate.query.Query;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
@Transactional
public class StudyDao extends AbstractDomainDao<Study> {
	
    @Override
    public Class<Study> domainClass() {
        return Study.class;
    }

    public boolean doesProtocolExist(String uniqueId, String schemaName) {
        Query q = getCurrentSession().createNativeQuery("select count(*) from " + schemaName + ".study where unique_identifier='" + uniqueId + "'");
        BigInteger count = (BigInteger) q.getSingleResult();
        return (count.intValue() == 1) ? true:false;
    }
}
