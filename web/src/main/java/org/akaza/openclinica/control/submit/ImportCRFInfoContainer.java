package org.akaza.openclinica.control.submit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.UpsertOnBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.akaza.openclinica.bean.core.DataEntryStage.INITIAL_DATA_ENTRY;

public class ImportCRFInfoContainer {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    // (Subject) (Event) (Form)
    private Map<String, Map<String, Map<String, String>>> importCRFMap;
    private List<ImportCRFInfo> importCRFList;


    String stripNonAlphaNumeric(String input) {
        // Add capitalization too
        return input.trim().replaceAll("\\s+|\\W+", "");
    }

    String capitalize(String input) {
        return input.toUpperCase();
    }

    String truncateToXChars(String input, int x) {
        return input.length() > x ? input.substring(0, x) : input;
    }

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
                
                String sampleOrdinal = null;
                if(studyEventDataBean.getStudyEventRepeatKey() == null || studyEventDataBean.getStudyEventRepeatKey().trim().isEmpty()) {
                	sampleOrdinal =  "1";
                }else {
                	sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey();
                }
                
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

                    FormLayoutDAO formLayoutDAO = new FormLayoutDAO(ds);
                    String crfVersion = truncateToXChars(capitalize(stripNonAlphaNumeric(formDataBean.getFormLayoutName())), 10);
                    ArrayList<FormLayoutBean> formLayoutBeans = formLayoutDAO.findAllByOid(formDataBean.getFormOID() + "_" + crfVersion);
                    for (FormLayoutBean formLayoutBean : formLayoutBeans) {

                        CRFDAO crfDAO = new CRFDAO(ds);
                        CRFBean crfBean = (CRFBean) crfDAO.findByPK(formLayoutBean.getCrfId());
                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findAllByStudyEventAndCrfOrCrfVersionOid(studyEventBean, crfBean.getOid());
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
                            if (!isCRFStatusValid(crfStatus, upsert, null)) {
                                importCrfInfo.setProcessImport(false);
                                importCrfInfo.setEventCRFID(null);
                                importCrfInfo.setPreImportStage(DataEntryStage.INVALID);
                            } else if (crfStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName()))
                                importCrfInfo.setPostImportStage(DataEntryStage.INITIAL_DATA_ENTRY);
                            if ((studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED))) {

                                if (!upsert.isNotStarted()) {
                                    importCrfInfo.setProcessImport(false);
                                    importCrfInfo.setEventCRFID(null);
                                }
                            }
                            
                            /**
                             *  Common events will always be updated 
                             */
                            if(studyEventDefinitionBean.isTypeCommon()) {
                            	importCrfInfo.setProcessImport(true);
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
                            if (!isCRFStatusValid(crfStatus, upsert, ecb)) {
                                importCrfInfo.setProcessImport(false);
                                importCrfInfo.setEventCRFID(null);
                                importCrfInfo.setPreImportStage(DataEntryStage.INVALID);
                            } else if (crfStatus != null && crfStatus.equals(DataEntryStage.INITIAL_DATA_ENTRY.getName()))
                                importCrfInfo.setPostImportStage(DataEntryStage.INITIAL_DATA_ENTRY);
                            importCrfInfo.setEventCRFID(new Integer(ecb.getId()));
                            if (!(ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY) && upsert.isDataEntryStarted())
                                    && !(ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) && upsert.isDataEntryComplete()))
                                importCrfInfo.setProcessImport(false);
                            
                            /**
                             *  Common events will always be updated 
                             */
                            if(studyEventDefinitionBean.isTypeCommon()) {
                            	importCrfInfo.setProcessImport(true);
                            }
                            
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

    private boolean isCRFStatusValid(String crfStatus, UpsertOnBean upsert, EventCRFBean ecb) {

    
        if (ecb != null && ecb.getStatus() == Status.UNAVAILABLE)
            return false;
        if (StringUtils.equals(crfStatus, INITIAL_DATA_ENTRY.getName()) ||
                StringUtils.equals(crfStatus, DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE.getName()) ||
                StringUtils.equals(crfStatus, DataEntryStage.COMPLETE.getName()))
            return true;

        if (upsert.isDataEntryStarted() || upsert.isDataEntryComplete())
            return true;

        return false;

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
