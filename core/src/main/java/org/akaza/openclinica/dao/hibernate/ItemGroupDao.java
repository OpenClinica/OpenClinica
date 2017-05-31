package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.oid.ItemGroupOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.hibernate.query.Query;

import java.util.ArrayList;

public class ItemGroupDao extends AbstractDomainDao<ItemGroup> {

    String findByCrfVersionIdQuery = "select distinct ig from ItemGroup ig "
            + "join fetch ig.itemGroupMetadatas igm "
            + "join fetch igm.crfVersion crf "
            + "join fetch igm.item as i "
            + "where crf.crfVersionId = :crfVersionId "
            + "order by i.itemId";

    @Override
    Class<ItemGroup> domainClass() {
        return ItemGroup.class;
    }

    public ItemGroup findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("OCOID", OCOID);
        return (ItemGroup) q.uniqueResult();
    }

    public ItemGroup findByNameCrfId(String groupName, CrfBean crf) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.name = :groupName and do.crf = :crf";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("groupName", groupName);
        q.setEntity("crf", crf);
        return (ItemGroup) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemGroup> findByCrfVersionId(Integer crfVersionId) {
        //String query = "select distinct ig.* from item_group ig, item_group_metadata igm where igm.crf_version_id = " + crfVersionId
        //        + " and ig.item_group_id = igm.item_group_id";

        Query q = getCurrentSession().createQuery(findByCrfVersionIdQuery);
        q.setParameter("crfVersionId", crfVersionId);
        return (ArrayList<ItemGroup>) q.list();
    }

    public String getValidOid(ItemGroup itemGroup, String crfName, String itemGroupLabel, ArrayList<String> oidList) {
    OidGenerator oidGenerator = new ItemGroupOidGenerator();
        String oid = getOid(itemGroup, crfName, itemGroupLabel);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null || oidList.contains(oid)) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    private String getOid(ItemGroup itemGroup, String crfName, String itemGroupLabel) {
        OidGenerator oidGenerator = new ItemGroupOidGenerator();
        String oid;
        try {
            oid = itemGroup.getOcOid() != null ? itemGroup.getOcOid() : oidGenerator.generateOid(crfName, itemGroupLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }
}
