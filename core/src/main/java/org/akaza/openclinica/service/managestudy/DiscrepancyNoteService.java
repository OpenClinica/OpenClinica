/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.service.managestudy;

import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import javax.sql.DataSource;

public class DiscrepancyNoteService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    private DiscrepancyNoteDAO discrepancyNoteDao;

    public DiscrepancyNoteService(DataSource ds) {
        this.ds = ds;
    }

    public void saveFieldNotes(String description, int entityId, String entityType, StudyBean sb, UserAccountBean ub) {

        // Create a new thread each time
        DiscrepancyNoteBean parent = createDiscrepancyNoteBean(description, entityId, entityType, sb, ub, null);
        createDiscrepancyNoteBean(description, entityId, entityType, sb, ub, parent.getId());

    }

    private DiscrepancyNoteBean createDiscrepancyNoteBean(String description, int entityId, String entityType, StudyBean sb, UserAccountBean ub,
            Integer parentId) {
        DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
        dnb.setEntityId(entityId);
        dnb.setStudyId(sb.getId());
        dnb.setEntityType(entityType);
        dnb.setDescription(description);
        dnb.setDiscrepancyNoteTypeId(1);
        dnb.setResolutionStatusId(1);
        dnb.setColumn("value");
        dnb.setOwner(ub);
        if (parentId != null) {
            dnb.setParentDnId(parentId);
        }
        dnb = (DiscrepancyNoteBean) getDiscrepancyNoteDao().create(dnb);
        getDiscrepancyNoteDao().createMapping(dnb);
        return dnb;

    }

    /**
     * 
     * @param itemName
     * @param message
     * @param eventCrf
     * @param itemData
     * @param parentId
     * @param uab
     * @param ds
     * @param studyId
     * @param detailedNotes
     * @param discrepancyNoteTypeId
     * @return
     */
    public DiscrepancyNoteBean createDiscrepancyNote(String itemName, String message, EventCrf eventCrf, ItemData itemData,
            Integer parentId, UserAccountBean uab, DataSource ds, int studyId,String detailedNotes,int discrepancyNoteTypeId) {
    	
        DiscrepancyNoteBean note = new DiscrepancyNoteBean();       
        note.setDescription(message);
        note.setDetailedNotes(detailedNotes);
        note.setOwner(uab);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(ResolutionStatus.OPEN.getId());
        note.setDiscrepancyNoteTypeId(discrepancyNoteTypeId);
        if (parentId != null) {
            note.setParentDnId(parentId);
        }

        note.setField(itemName);
        note.setStudyId(studyId);
        note.setEntityName(itemName);
        note.setEntityType("ItemData");
        note.setEntityValue(itemData.getValue());

        note.setEventName(eventCrf.getStudyEvent().getStudyEventDefinition().getName());
        note.setEventStart(eventCrf.getDateCreated());
       
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(eventCrf.getStudySubject().getStudySubjectId());
        note.setSubjectName(ss.getName());

        note.setEntityId(itemData.getItemDataId());
        note.setColumn("value");

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(ds);
        note = (DiscrepancyNoteBean) dndao.create(note);      
        dndao.createMapping(note);
      
        return note;
    }

    private DiscrepancyNoteDAO getDiscrepancyNoteDao() {
        discrepancyNoteDao = this.discrepancyNoteDao != null ? discrepancyNoteDao : new DiscrepancyNoteDAO(ds);
        return discrepancyNoteDao;
    }

}
