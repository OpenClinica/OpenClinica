package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemDataType;

public class ItemDataTypeDao extends AbstractDomainDao<ItemDataType> {

    @Override
    Class<ItemDataType> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataType.class;
    }

    public ItemDataType findByItemDataTypeCode(String item_data_type_code) {
        String query = "from " + getDomainClassName() + " item_data_type  where item_data_type.code = :itemdatatypecode ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("itemdatatypecode", item_data_type_code);
        return (ItemDataType) q.uniqueResult();
    }

    public ItemDataType findByItemDataTypeId(int item_data_type_id) {
        String query = "from " + getDomainClassName() + " item_data_type  where item_data_type.itemDataTypeId = :item_data_type_id ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("item_data_type_id", item_data_type_id);
        ItemDataType result = (ItemDataType) q.uniqueResult();
        return result;
    }

    public ItemDataType findByItemId(int item_id) {
        String query = "select idt.* from item_data_type idt join item i on idt.item_data_type_id=i.item_data_type_id where i.item_id = " + item_id;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemDataType.class);
        ItemDataType result = (ItemDataType) q.uniqueResult();
        return result;
    }
}
