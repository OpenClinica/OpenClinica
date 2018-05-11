package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ResponseSet;

import java.util.List;

public class ResponseSetDao extends AbstractDomainDao<ResponseSet> {

    @Override
    Class<ResponseSet> domainClass() {
        // TODO Auto-generated method stub
        return ResponseSet.class;
    }

    public List<ResponseSet> findAllByVersion(Integer version) {
        String query = "from " + getDomainClassName() + " response_set  where response_set.versionId = :version ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("version", version);
        return ((List<ResponseSet>) q.list());
    }

    public ResponseSet findByLabelVersion(String label, Integer version) {
        String query = "from " + getDomainClassName() + " response_set  where response_set.label = :label and response_set.versionId = :version ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("label", label);
        q.setInteger("version", version);
        return (ResponseSet) q.uniqueResult();
    }

    public List<ResponseSet> findAllByItemId(int itemId) {
        String query = "select rs.* from item_form_metadata ifm join response_set rs on ifm.response_set_id = rs.response_set_id " + "where ifm.item_id = "
                + itemId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ResponseSet.class);
        return ((List<ResponseSet>) q.list());
    }

}
