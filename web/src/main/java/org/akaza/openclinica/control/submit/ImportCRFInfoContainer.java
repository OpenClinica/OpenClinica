package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.*;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportCRFInfoContainer {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    // (Subject) (Event) (Form)
    private Map<String, Map<String, Map<String, String>>> importCRFMap;
    private List<ImportCRFInfo> importCRFList;

    /*
     * Purpose: Iterates over ODM to populate 2 objects: 1. importCRFList: A List of EventCRFs and information on how to
     * process them. 2. importCRFMap: A Map multi-layer map of Subject/Event/Form only populated when the subsequent
     * EventCRF passes the UpsertOn rules.
     */
    public ImportCRFInfoContainer(ODMContainer odmContainer, DataSource ds) {
        importCRFList = new ArrayList<ImportCRFInfo>();

        ArrayList<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
        EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        UpsertOnBean upsert = odmContainer.getCrfDataPostImportContainer().getUpsertOn();
        // If Upsert bean is not present, create one with default settings
        if (upsert == null)
            upsert = new UpsertOnBean();
        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOID);
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();

        Map<String, Map<String, Map<String, String>>> subjectMap = new HashMap<String, Map<String, Map<String, String>>>();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());

            Map<String, Map<String, String>> eventMap = new HashMap<String, Map<String, String>>();
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());

                StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
                // @pgawade 16-March-2011 Do not allow the data import
                // if event status is one of the - stopped, signed,
                // locked
                Map<String, String> formMap = new HashMap<String, String>();
                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {

                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersionBean);
                        // what if we have begun with creating a study
                        // event, but haven't entered data yet? this would
                        // have us with a study event, but no corresponding
                        // event crf, yet.
                        if (eventCrfBeans.isEmpty()) {
                            logger.debug("   found no event crfs from Study Event id " + studyEventBean.getId() + ", location " + studyEventBean.getLocation());

                            ImportCRFInfo importCrfInfo = new ImportCRFInfo(studyOID, subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(),
                                    formDataBean.getFormOID());
                            importCrfInfo.setPreImportStage(DataEntryStage.UNCOMPLETED);
                            String crfStatus = formDataBean.getEventCRFStatus();
                            if (crfStatus != null && crfStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName()))
                                importCrfInfo.setPostImportStage(DataEntryStage.INITIAL_DATA_ENTRY);
                            if ((studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED) || studyEventBean
                                    .getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED))) {

                                if (!upsert.isNotStarted()) {
                                    importCrfInfo.setProcessImport(false);
                                    importCrfInfo.setEventCRFID(null);
                                }
                            }
                            importCRFList.add(importCrfInfo);
                            if (importCrfInfo.isProcessImport())
                                formMap.put(formDataBean.getFormOID(), "true");
                        }

                        for (EventCRFBean ecb : eventCrfBeans) {
                            ImportCRFInfo importCrfInfo = new ImportCRFInfo(studyOID, subjectDataBean.getSubjectOID(), studyEventDataBean.getStudyEventOID(),
                                    formDataBean.getFormOID());
                            importCrfInfo.setPreImportStage(ecb.getStage());
                            String crfStatus = formDataBean.getEventCRFStatus();
                            if (crfStatus != null && crfStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName()))
                                importCrfInfo.setPostImportStage(DataEntryStage.INITIAL_DATA_ENTRY);
                            importCrfInfo.setEventCRFID(new Integer(ecb.getId()));
                            if (!(ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY) && upsert.isDataEntryStarted())
                                    && !(ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) && upsert.isDataEntryComplete()))
                                importCrfInfo.setProcessImport(false);
                            importCRFList.add(importCrfInfo);
                            if (importCrfInfo.isProcessImport())
                                formMap.put(formDataBean.getFormOID(), "true");
                        }
                    }
                } // formdata loop
                if (formMap.size() > 0)
                    eventMap.put(studyEventDataBean.getStudyEventOID(), formMap);
            } // study event loop
            if (eventMap.size() > 0)
                subjectMap.put(subjectDataBean.getSubjectOID(), eventMap);
        } // subject data loop
        importCRFMap = subjectMap;
    }

    public int getCountSkippedEventCrfs() {
        int countSkippedCrfs = 0;

        for (ImportCRFInfo importCrf : importCRFList) {
            if (!importCrf.isProcessImport())
                countSkippedCrfs++;
        }
        return countSkippedCrfs;
    }

    public List<ImportCRFInfo> getImportCRFList() {
        return importCRFList;
    }

    public void setImportCRFList(List<ImportCRFInfo> importCRFList) {
        this.importCRFList = importCRFList;
    }

    public Map<String, Map<String, Map<String, String>>> getImportCRFMap() {
        return importCRFMap;
    }

    public void setImportCRFMap(Map<String, Map<String, Map<String, String>>> importCRFMap) {
        this.importCRFMap = importCRFMap;
    }

}
