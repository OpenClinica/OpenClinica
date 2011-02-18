package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.logic.LoadLabsService;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import javax.sql.DataSource;

public class RollbackLoadLabsEndpoint extends AbstractCabigDomEndpoint {

    public RollbackLoadLabsEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {

        super(dataSource, messages, coreResources);
    }

    /**
     * Load Labs Rollback, tbh 02/2011 The difference between rollback here and rollback elsewhere; we are deleting event crfs and data, per the logic described
     * originally in DeleteEventCRFServlet, from the web branch of OpenClinica.
     */
    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        System.out.println("Request text rollback load labs");
        NodeList nlist = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedClinicalResult");

        this.logNodeList(nlist);
        try {
            LoadLabsService labService = new LoadLabsService();
            ODMContainer odmContainer =
                labService.generateLoadLabsObject(requestElement, getStudyDao(), getSubjectDao(), getItemDao(), getStudySubjectDao(), getItemGroupDao(),
                        getCrfVersionDao(), getStudyEventDefinitionDao());
            StudyBean studyBean = getStudyDao().findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());
            SubjectBean subjectBean = getSubjectDao().findByUniqueIdentifier(odmContainer.getSubjectUniqueIdentifier());
            StudySubjectBean studySubjectBean = getStudySubjectDao().findBySubjectIdAndStudy(subjectBean.getId(), studyBean);
            StudyEventDefinitionBean studyEventDefBean =
                getStudyEventDefinitionDao().findByOid(
                        odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getStudyEventData().get(0).getStudyEventOID());
            CRFVersionBean crfVersionBean =
                getCrfVersionDao().findByOid(
                        odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getStudyEventData().get(0).getFormData().get(0).getFormOID());
            // int ordinal = 1;// ??
            int ordinal = getStudyEventDao().getMaxSampleOrdinal(studyEventDefBean, studySubjectBean); // minus one?
            StudyEventBean studyEventBean =
                (StudyEventBean) getStudyEventDao().findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(), studyEventDefBean.getId(), ordinal);
            System.out.println("found study event bean: " + studyEventBean.getId() + " ordinal " + studyEventBean.getSampleOrdinal());
            EventCRFBean eventCrfBean = (EventCRFBean) getEventCrfDao().findByEventCrfVersion(studyEventBean, crfVersionBean);
            System.out.println("found event crf bean: " + eventCrfBean.getId() + " ");
            DiscrepancyNoteDAO discNoteDao = new DiscrepancyNoteDAO(dataSource);
            // uncharacteristic of our previous design
            ArrayList<ItemDataBean> itemData = getItemDataDao().findAllByEventCRFId(eventCrfBean.getId());
            logger.info("submit to delete the event CRF from event");
            // delete all the item data first
            for (ItemDataBean item : itemData) {

                ArrayList<DiscrepancyNoteBean> discrepancyList = discNoteDao.findExistingNotesForItemData(item.getId());
                getItemDataDao().deleteDnMap(item.getId());
                for (DiscrepancyNoteBean noteBean : discrepancyList) {

                    discNoteDao.deleteNotes(noteBean.getId());
                }
                item.setUpdater(getUserAccount());
                getItemDataDao().updateUser(item);
                getItemDataDao().delete(item.getId());
            }
            // delete event crf
            getEventCrfDao().delete(eventCrfBean.getId());

        } catch (Exception e) {
            e.printStackTrace();

            if (e.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + e.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) e;
                return mapLoadLabsErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + e.getClass().getName());
                return mapLoadLabsErrorConfirmation(e.getMessage(), new CCBusinessFaultException("Error with Data Rollback Operations", "CC10300"));
            }
        }
        return this.mapLoadLabsConfirmation();
        // return this.mapLoadLabsErrorConfirmation(message, exception)

    }
}
