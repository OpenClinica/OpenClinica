package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.hibernate.query.Query;

public class DiscrepancyNoteDao extends AbstractDomainDao<DiscrepancyNote> {

    @Override
    Class<DiscrepancyNote> domainClass() {
        return DiscrepancyNote.class;
    }

    static String findParentQueryByItemDataQuery = "select dn from DiscrepancyNote dn "
            + "join DnItemDataMap didm on didm.discrepancyNote.discrepancyNoteId = dn.discrepancyNoteId "
            + "join DiscrepancyNoteType dnt on dn.discrepancyNoteType.discrepancyNoteTypeId = dnt.discrepancyNoteTypeId "
            + "where dn.parentDiscrepancyNote is null "
            + "and didm.itemData.itemDataId = :itemDataId and dn.discrepancyNoteType.discrepancyNoteTypeId= :noteTypeId";

    static String findChildQueriesByItemData = "select dn from DiscrepancyNote dn "
            + "join DnItemDataMap didm on didm.discrepancyNote.discrepancyNoteId = dn.discrepancyNoteId "
            + "join DiscrepancyNoteType dnt on dn.discrepancyNoteType.discrepancyNoteTypeId = dnt.discrepancyNoteTypeId "
            + "where dn.parentDiscrepancyNote is not null " + "and didm.itemData.itemDataId = :itemDataId order by dn.dateCreated desc";

    public List<DiscrepancyNote> findParentNotesByItemData(Integer itemDataId) {
        String query = "select dn.* from discrepancy_note dn, dn_item_data_map didm where didm.item_data_id=" + itemDataId + " AND dn.parent_dn_id isnull "
                + "AND dn.discrepancy_note_id=didm.discrepancy_note_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(DiscrepancyNote.class);
        return ((List<DiscrepancyNote>) q.list());
    }

    public DiscrepancyNote findByDiscrepancyNoteId(int discrepancyNoteId) {
        String query = "from " + getDomainClassName() + " do where do.discrepancyNoteId = :discrepancynoteid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("discrepancynoteid", discrepancyNoteId);
        return (DiscrepancyNote) q.uniqueResult();
    }

    public DiscrepancyNote findParentQueryByItemData(Integer itemDataId, Integer noteTypeId) {
        Query q = getCurrentSession().createQuery(findParentQueryByItemDataQuery);
        q.setParameter("itemDataId", itemDataId);
        q.setParameter("noteTypeId", noteTypeId);
        return (DiscrepancyNote) q.uniqueResult();
    }

    public List<DiscrepancyNote> findChildQueriesByItemData(Integer itemDataId) {
        Query q = getCurrentSession().createQuery(findChildQueriesByItemData);
        q.setParameter("itemDataId", itemDataId);
        return ((List<DiscrepancyNote>) q.list());
    }

}
