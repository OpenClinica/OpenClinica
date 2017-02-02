package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.bean.oid.ItemOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.Item;
import org.hibernate.query.Query;

public class ItemDao extends AbstractDomainDao<Item> {

    static String findByItemGroupCrfVersionOrderedQuery = "select i from Item i " + "join i.itemGroupMetadatas igm " + "join igm.itemGroup ig "
            + "left join fetch i.itemFormMetadatas ifm " + "where ig.itemGroupId= :itemGroupId " + "and igm.crfVersion.crfVersionId= :crfVersionId "
            + "order by i.itemId";

    static String findAllByCrfVersion = "select i from Item i " + " join fetch i.itemFormMetadatas ifm where ifm.crfVersionId= :crfVersionId "
            + " order by i.itemId ";

    @Override
    Class<Item> domainClass() {
        // TODO Auto-generated method stub
        return Item.class;
    }

    public Item findByOcOID(String OCOID) {
        String query = "from " + getDomainClassName() + " item  where item.ocOid = :ocoid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("ocoid", OCOID);
        return (Item) q.uniqueResult();
    }

    public Item findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct i.* from item i, item_form_metadata ifm,crf_version cv " + "where i.name= '" + name + "' and i.item_id= ifm.item_id "
                + "and ifm.crf_version_id=cv.crf_version_id " + "and cv.crf_id=" + crfId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(Item.class);
        return ((Item) q.uniqueResult());
    }

    public int getItemDataTypeId(Item item) {
        String query = "select item_data_type_id from item where item_id = " + item.getItemId();
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);
        return ((Number) q.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Item> findByItemGroupCrfVersionOrdered(Integer itemGroupId, Integer crfVersionId) {

        Query q = getCurrentSession().createQuery(findByItemGroupCrfVersionOrderedQuery);
        q.setParameter("itemGroupId", itemGroupId);
        q.setParameter("crfVersionId", crfVersionId);
        return (ArrayList<Item>) q.list();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Item> findAllByCrfVersion(Integer crfVersionId) {
        Query q = getCurrentSession().createQuery(findAllByCrfVersion);
        q.setParameter("crfVersionId", crfVersionId);
        return (ArrayList<Item>) q.list();
    }

    public String getValidOid(Item item, String crfName, String itemLabel, ArrayList<String> oidList) {
        OidGenerator oidGenerator = new ItemOidGenerator();
        String oid = getOid(item, crfName, itemLabel);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(Item item, String crfName, String itemLabel) {
        OidGenerator oidGenerator = new ItemOidGenerator();
        String oid;
        try {
            oid = item.getOcOid() != null ? item.getOcOid() : oidGenerator.generateOid(crfName, itemLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

}