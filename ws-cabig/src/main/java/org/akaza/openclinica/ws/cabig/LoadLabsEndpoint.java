package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.logic.LoadLabsService;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

public class LoadLabsEndpoint extends AbstractCabigDomEndpoint {

    public LoadLabsEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {

        super(dataSource, messages, coreResources);
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        System.out.println("Request text load labs");
        NodeList nlist = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedClinicalResult");
        this.logNodeList(nlist);
        try {
            // generate an odmContainer, which contains the lab data
            LoadLabsService labService = new LoadLabsService();
            ODMContainer odmContainer =
                labService.generateLoadLabsObject(requestElement, getStudyDao(), getSubjectDao(), getItemDao(), getStudySubjectDao(), getItemGroupDao(),
                        getCrfVersionDao(), getStudyEventDefinitionDao());
            // ^ unlike the other two request endpoints this will take the whole document and parse it in the service
            // push the odm container into the database using the data import service
            StudyBean studyBean = getStudyDao().findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());

            SubjectBean subjectBean = getSubjectDao().findByUniqueIdentifier(odmContainer.getSubjectUniqueIdentifier());
            StudySubjectBean studySubjectBean = getStudySubjectDao().findBySubjectIdAndStudy(subjectBean.getId(), studyBean);
            // findByOid(odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getSubjectOID());
            // generate a new study event, then import data
            StudyEventDefinitionBean studyEventDefBean =
                getStudyEventDefinitionDao().findByOid(
                        odmContainer.getCrfDataPostImportContainer().getSubjectData().get(0).getStudyEventData().get(0).getStudyEventOID());
            StudyEventBean newStudyEventBean = createNewStudyEventBean(studyEventDefBean, studySubjectBean);
            System.out.println("created new study event bean " + newStudyEventBean.getId() + " sed " + newStudyEventBean.getStudyEventDefinitionId() + " ord "
                + newStudyEventBean.getSampleOrdinal());
            // or do we update instead if it exists? no, because we delete on rollback
            ArrayList<String> messages = labService.importData(dataSource, coreResources, studyBean, getUserAccount(), odmContainer, newStudyEventBean);
            if ("fail".equals(messages.get(0))) {
                return this.mapLoadLabsErrorConfirmation(messages.get(1), new CCBusinessFaultException("Invalid Lab Data", "CC10310"));
            } else {
                // include warnings, include other error codes
                return this.mapLoadLabsConfirmation();
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (e.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + e.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) e;
                return mapLoadLabsErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + e.getClass().getName());
                return mapLoadLabsErrorConfirmation(e.getMessage(), new CCBusinessFaultException("Error with Data Capture Operations", "CC10300"));
            }
        }
        // return this.mapLoadLabsErrorConfirmation(message, exception)

    }

    private StudyEventBean createNewStudyEventBean(StudyEventDefinitionBean sedBean, StudySubjectBean studySubjectBean) {
        int ordinal = getStudyEventDao().getMaxSampleOrdinal(sedBean, studySubjectBean);
        StudyEventBean testStudyEventBean =
            (StudyEventBean) getStudyEventDao().findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(), sedBean.getId(), ordinal);
        if (testStudyEventBean != null && testStudyEventBean.getId() > 0) {
            return testStudyEventBean;
        } else {
            StudyEventBean newStudyEventBean = new StudyEventBean();
            newStudyEventBean.setCreatedDate(new Date(System.currentTimeMillis()));
            // set date started
            newStudyEventBean.setDateStarted(new Date(System.currentTimeMillis()));
            newStudyEventBean.setStudyEventDefinition(sedBean);
            newStudyEventBean.setStudyEventDefinitionId(sedBean.getId());
            newStudyEventBean.setSampleOrdinal(ordinal);// to be updated
            newStudyEventBean.setStartTimeFlag(true);
            newStudyEventBean.setEndTimeFlag(false);
            newStudyEventBean.setOwner(getUserAccount());
            newStudyEventBean.setStatus(Status.AVAILABLE);
            newStudyEventBean.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
            // StudySubjectBean studySubject = getStudySubjectDao().findBySubjectIdAndStudy(subjectId, study)
            newStudyEventBean.setStudySubject(studySubjectBean);
            newStudyEventBean.setStudySubjectId(studySubjectBean.getId());
            newStudyEventBean = (StudyEventBean) getStudyEventDao().create(newStudyEventBean);
            return newStudyEventBean;
        }
    }
}
