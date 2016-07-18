package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemDataFlag;

import java.util.List;

public class ItemDataFlagDao extends AbstractDomainDao<ItemDataFlag> {

    @Override
    Class<ItemDataFlag> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataFlag.class;
    }


    
    public List<ItemDataFlag> findAllByEventCrfPath(int tag_id , String eventCrfPath ) {

        String query = " from " + getDomainClassName() + "  where "
                + " tag_id= " + tag_id +  " and path LIKE '" + eventCrfPath +".%'"  ;
        
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (List<ItemDataFlag>) q.list();
    }

    public ItemDataFlag findByItemDataPath(int tag_id ,  String itemDataPath ) {

        String query = " from " + getDomainClassName() + "  where "
                + " tag_id= " + tag_id  + " and path= '" + itemDataPath +"'"   ;
        
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (ItemDataFlag) q.uniqueResult();
    }


    
}
