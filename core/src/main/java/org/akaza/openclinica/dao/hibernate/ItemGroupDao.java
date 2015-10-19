package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.hibernate.Criteria;

public class ItemGroupDao extends AbstractDomainDao<ItemGroup> {

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
        String query = "select distinct ig.* from item_group ig, item_group_metadata igm where igm.crf_version_id = " + crfVersionId
                + " and ig.item_group_id = igm.item_group_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemGroup.class);
        return (ArrayList<ItemGroup>) q.list();
    }
}
