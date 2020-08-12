/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.openclinica.kafka.KafkaService;
import com.openclinica.kafka.dto.EventAttributeChangeDTO;
import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.StudyEventService;
import core.org.akaza.openclinica.service.auth.TokenService;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author jxu
 *
 *         Removes a study event and all its related event CRFs, items
 */
public class RemoveStudyEventServlet extends SecureController {

    @Autowired
    @Qualifier("studyEventJDBCDao")
    private StudyEventDAO studyEventDAO;
    @Autowired
    @Qualifier("eventCRFJDBCDao")
    private EventCRFDAO eventCRFDAO;
    @Autowired
    @Qualifier("StudyEventService")
    private StudyEventService studyEventService;

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.LIST_STUDY_SUBJECTS, respage.getString("current_study_frozen"));

        if (ub.isSysAdmin()) {
            return;
        }

        if (!currentRole.getRole().equals(Role.MONITOR) ){
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int studyEventId = fp.getInt("id");// studyEventId
        int studySubId = fp.getInt("studySubId");// studySubjectId
/*        studyEventDAO = (StudyEventDAO) SpringServletAccess.getApplicationContext(context).getBean("studyEventJDBCDao");
        eventCRFDAO = (EventCRFDAO) SpringServletAccess.getApplicationContext(context).getBean("eventCRFJDBCDao");
        studyEventService = (StudyEventService) SpringServletAccess.getApplicationContext(context).getBean("StudyEventService");*/

        StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());

        if (studyEventId == 0) {
            addPageMessage(respage.getString("please_choose_a_SE_to_remove"));
            request.setAttribute("id", Integer.toString(studySubId));
            forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
        } else {

            StudyEventBean event = (StudyEventBean) studyEventDAO.findByPK(studyEventId);
            event.getStudyEventDefinition().getOid();

            StudySubjectBean studySub = (StudySubjectBean) subdao.findByPK(studySubId);
            request.setAttribute("studySub", studySub);

            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            Study study = (Study) getStudyDao().findByPK(studySub.getStudyId());

            request.setAttribute("study", study);

            String action = request.getParameter("action");
            if ("confirm".equalsIgnoreCase(action)) {

                EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
                // find all crfs in the definition
                ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllByEventDefinitionId(study, sed.getId());

                ArrayList eventCRFs = eventCRFDAO.findAllByStudyEvent(event);

                // construct info needed on view study event page
                DisplayStudyEventBean displayEvent = new DisplayStudyEventBean();
                displayEvent.setStudyEvent(event);

                ArrayList displayEventCrfs = studyEventService.getDisplayEventCRFs(eventCRFs, eventDefinitionCRFs, currentRole, ub);
                displayEvent.setDisplayEventCRFs(displayEventCrfs);

                request.setAttribute("displayEvent", displayEvent);

                forwardPage(Page.REMOVE_STUDY_EVENT);
            } else {
                logger.info("submit to remove the event from study");
                // remove event from study

                if (event.isSigned()) {
                    event.setSigned(Boolean.FALSE);
                }

                event.setStatus(Status.DELETED);
                event.setRemoved(Boolean.TRUE);
                event.setUpdater(ub);
                event.setUpdatedDate(new Date());
                studyEventDAO.update(event);

                if(studySub.getStatus().equals(Status.SIGNED)){
                    studySub.setStatus(Status.AVAILABLE);
                    studySub.setUpdater(ub);
                    studySub.setUpdatedDate(new Date());
                    subdao.update(studySub);
                }

                ArrayList eventCRFs = eventCRFDAO.findAllByStudyEvent(event);

                ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                for (int k = 0; k < eventCRFs.size(); k++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                        ArrayList itemDatas = iddao.findAllByEventCRFId(eventCRF.getId());
                        for (int a = 0; a < itemDatas.size(); a++) {
                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                                DiscrepancyNoteDAO dnDao = new DiscrepancyNoteDAO(sm.getDataSource());
                                List dnNotesOfRemovedItem = dnDao.findParentNotesOnlyByItemData(item.getId());
                                if (!dnNotesOfRemovedItem.isEmpty()) {
                                    DiscrepancyNoteBean itemParentNote = null;
                                    for (Object obj : dnNotesOfRemovedItem) {
                                        if (((DiscrepancyNoteBean) obj).getParentDnId() == 0) {
                                            itemParentNote = (DiscrepancyNoteBean) obj;
                                        }
                                    }
                                    DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
                                    if (itemParentNote != null) {
                                        dnb.setParentDnId(itemParentNote.getId());
                                        dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
                                        dnb.setThreadUuid(itemParentNote.getThreadUuid());
                                    }
                                    dnb.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());  // set to closed-modified
                                    dnb.setStudyId(currentStudy.getStudyId());
                                    dnb.setAssignedUserId(ub.getId());
                                    dnb.setOwner(ub);
                                    dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
                                    dnb.setEntityId(item.getId());
                                    dnb.setColumn("value");
                                    dnb.setCreatedDate(new Date());
                                    String detailedNotes="The item has been removed, this Query has been Closed.";
                                    dnb.setDetailedNotes(detailedNotes);
                                    dnDao.create(dnb);
                                    dnDao.createMapping(dnb);
                                    itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED_MODIFIED.getId());  // set to closed-modified
                                    itemParentNote.setDetailedNotes(detailedNotes);
                                    dnDao.update(itemParentNote);
                                }
                            }



                }

                String alertMessage = respage.getString("the_event") + " " + event.getStudyEventDefinition().getName() + " "
                        + respage.getString("has_been_removed_from_the_subject_record_for") + " " + studySub.getLabel() + " "
                        + respage.getString("in_the_study") + " " + study.getName() + ".";

                addPageMessage(alertMessage);

                request.setAttribute("id", Integer.toString(studySubId));
                forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET);
            }
        }
    }

}
