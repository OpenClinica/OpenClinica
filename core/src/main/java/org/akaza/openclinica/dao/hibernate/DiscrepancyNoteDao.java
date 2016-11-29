package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.hibernate.query.Query;

import java.util.List;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }
    static String findParentQueryByItemDataQuery = "select dn from DiscrepancyNote dn "
            + "join DnItemDataMap didm on didm.discrepancyNote.discrepancyNoteId = dn.discrepancyNoteId "
            + "join DiscrepancyNoteType dnt on dn.discrepancyNoteType.discrepancyNoteTypeId = dnt.discrepancyNoteTypeId "
            + "where dn.parentDiscrepancyNote is null "
            + "and didm.itemData.itemDataId = :itemDataId";

    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        String query = "select dn.* from discrepancy_note dn, dn_item_data_map didm where didm.item_data_id=" + itemDataId + " AND dn.parent_dn_id isnull " +
                "AND dn.discrepancy_note_id=didm.discrepancy_note_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(DiscrepancyNote.class);
        return ((List<DiscrepancyNote>) q.list());
    }

    public DiscrepancyNote findByDiscrepancyNoteId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.discrepancyNoteId = :discrepancynoteid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("discrepancynoteid", discrepancyNoteId);
        return (DiscrepancyNote) q.uniqueResult();
    }

    public DiscrepancyNote findParentQueryByItemData(Integer itemDataId) {
        Query q = getCurrentSession().createQuery(findParentQueryByItemDataQuery);
        q.setParameter("itemDataId", itemDataId);
        return (DiscrepancyNote) q.uniqueResult();
    }


}
