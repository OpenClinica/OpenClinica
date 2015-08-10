package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.ResponseType;

public class ResponseTypeDao extends AbstractDomainDao<ResponseType> {

    @Override
    Class<ResponseType> domainClass() {
        // TODO Auto-generated method stub
        return ResponseType.class;
    }

    public ResponseType findByResponseTypeName(String name) {
        String query = "from " + getDomainClassName() + " response_type  where response_type.name = :name ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("name", name);
        return (ResponseType) q.uniqueResult();
    }

}
