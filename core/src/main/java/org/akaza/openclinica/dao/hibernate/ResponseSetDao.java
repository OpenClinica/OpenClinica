package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ResponseSet;

public class ResponseSetDao extends AbstractDomainDao<ResponseSet> {

    @Override
    Class<ResponseSet> domainClass() {
        // TODO Auto-generated method stub
        return ResponseSet.class;
    }

    public ResponseSet findByLabelVersion(String label, Integer version) {
        String query = "from " + getDomainClassName() + " response_set  where response_set.label = :label and response_set.versionId = :version ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("label", label);
        q.setInteger("version", version);
        return (ResponseSet) q.uniqueResult();
    }

}
