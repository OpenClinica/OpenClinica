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

    public ResponseType findByItemFormMetaDataId(Integer itemFormMetadataId) {
        String query = "select rt.* from response_type rt, response_set rs, item_form_metadata ifm where ifm.response_set_id=rs.response_set_id"
                + " and rs.response_type_id=rt.response_type_id and ifm.item_form_metadata_id = " + String.valueOf(itemFormMetadataId);
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ResponseType.class);
        return (ResponseType) q.uniqueResult();
    }

}
