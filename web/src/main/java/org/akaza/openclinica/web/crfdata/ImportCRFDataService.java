package org.akaza.openclinica.web.crfdata;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.bean.submit.crfdata.UpsertOnBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.ImportCRFInfoContainer;
import org.akaza.openclinica.controller.dto.CommonEventContainerDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.importdata.ImportDataHelper;
import org.akaza.openclinica.logic.importdata.PipeDelimitedDataHelper;
import org.akaza.openclinica.service.ViewStudySubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class ImportCRFDataService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final DataSource ds;
    private ItemDataDAO itemDataDao;
    private String skipMatchCriteriaSql;
    private PipeDelimitedDataHelper pipeDelimitedDataHelper;
    private RestfulServiceHelper restfulServiceHelper;


    @Autowired
    private ViewStudySubjectService viewStudySubjectService;

    @Autowired
    private UserAccountDao userAccountDao;

    @Autowired
    private StudyEventDao studyEventDao;


    public ImportCRFDataService(DataSource ds) {
        this.ds = ds;
    }

    /*
     * purpose: look up EventCRFBeans by the following: Study Subject, Study Event, CRF Version, using the
     * findByEventSubjectVersion method in EventCRFDAO. May return more than one, hmm.
     */
    public HashMap fetchEventCRFBeans(ODMContainer odmContainer, UserAccountBean ub, Boolean persistEventCrfs,HttpServletRequest request) throws OpenClinicaException {
    	HashMap fetchEventCRFBeansResult = new HashMap();
        ArrayList<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        //StudyEventBean List to hold new common event
        ArrayList<StudyEventBean> studyEventBeans = new ArrayList<StudyEventBean>();
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
        if (studyBean.getStatus() != Status.AVAILABLE) {
            logger.error("Study status is :" + studyBean.getStatus() + ", so dataImport is not allowed.");
            return fetchEventCRFBeansResult ;
        }
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
            	boolean isNewStudyEvent = false;
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());


                StudyEventBean studyEventBean = null;

                UserAccount userAccount = userAccountDao.findById(ub.getId());
                
                
              
                studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
                                
                if (!studyEventDefinitionBean.isTypeCommon()) {
                    // @pgawade 16-March-2011 Do not allow the data import
                    // if event status is one of the - stopped, signed,
                    // locked
                    if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                            || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                            || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                        return null;
                    }
                }
                
                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
                    CRFDAO crfDAO = new CRFDAO(ds);

                    /**
                     *  if repeat to import same data file,only first time need to create  for common events
                     *  
                     *  and have current logic :
		                if not found, the insert,
		                if found, just update
                     */
                    if (studyEventDefinitionBean.isTypeCommon()){

                        String formOid = formDataBean.getFormOID();
                        CRFBean crfBean = crfDAO.findByOid(formOid);
                        CommonEventContainerDTO commonEventContainerDTO = addCommonForm(studyEventDefinitionBean,crfBean, studySubjectBean,userAccount,studyBean);
                        
                        /**
                         *  data from mirth may don't have repeat key, and need to check SKIP logic
                         *  will test skip logic first
                         */
                        String sqlStr;
                        ArrayList matchCriterias;
                        boolean matchedAndSkip=false;;
    					try {
    						sqlStr = this.buildSkipMatchCriteriaSql(request, studyBean.getOid(), studySubjectBean.getOid(),studyEventDataBean.getStudyEventOID());
    						if(sqlStr != null) {
    							matchCriterias = this.getItemDataDao().findSkipMatchCriterias(sqlStr); 
        		                matchedAndSkip = this.needToSkip(matchCriterias, formDataBeans);
    						}
    		                    
    		                if(matchedAndSkip) {
    		                	 // do something	
    		                  ;// return null;
    		                }else {
    		                	if(studyEventDefinitionBean.isRepeating()) {
    		                		/**
    		                		 *  keep existing logic -- import XML file must provide  orrect repeat key and will sue that key
    		                		 */
    		                		String comeFromPipe = (String) request.getHeader("PIPETEXT");
    	                        	if(comeFromPipe!=null && comeFromPipe.equals("PIPETEXT")) {
    	                        		sampleOrdinal =	commonEventContainerDTO.getMaxOrdinal() + 1 + "";
    	                        	}

    		                	}else {
    		                		sampleOrdinal =	 "1";
    		                	}
    		                	
    		                	
    		                }
    					} catch (OpenClinicaException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
                        //for common events, if not provided studyEventRepeatKey, if pass skip check, then will automatically assign 
    					//the next repeat key for repeat event 
                     
                    	studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                                studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
                        
                        if(studyEventBean == null || studyEventBean.getId() == 0) {
                        	                        	
                        	 StudyEventBean tempStudyEventBean = new StudyEventBean();
                             Date today = new Date();
                             tempStudyEventBean.setCreatedDate(today);
                             tempStudyEventBean.setDateStarted(today);
                             tempStudyEventBean.setName(studyEventDefinitionBean.getName());
                             tempStudyEventBean.setOwner(ub);
                             ArrayList eventCRFs = new ArrayList<>();
                             eventCRFs.add(commonEventContainerDTO.getEventCrf());
                             tempStudyEventBean.setEventCRFs(eventCRFs);
                             tempStudyEventBean.setOwnerId(ub.getId());
                             tempStudyEventBean.setStudySubject(studySubjectBean);
                             tempStudyEventBean.setUpdater(ub);
                             tempStudyEventBean.setUpdatedDate(today);
                             tempStudyEventBean.setStudySubjectId(studySubjectBean.getId());
                             tempStudyEventBean.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
                             tempStudyEventBean.setStatus(Status.AVAILABLE);
                             tempStudyEventBean.setStudyEventDefinitionId(studyEventDefinitionBean.getId());
                             tempStudyEventBean.setSampleOrdinal(Integer.parseInt(sampleOrdinal));
                             
                            // FIX for API will create later after all validation pass
                             String comeFromPipe = (String) request.getHeader("PIPETEXT");
                         	if(comeFromPipe!=null && comeFromPipe.equals("PIPETEXT")) {
                         	   isNewStudyEvent = true;
                               studyEventBean = tempStudyEventBean;
                         	}else {
                         		//UI will keep current pattern,  will use the same pattern as API to fix later
                         		studyEventBean = (StudyEventBean) studyEventDAO.create(tempStudyEventBean);
                         	}                                                     
                                                       
                             /**
                              * After create a new study event, update the repeat key in the data, so make sure that it is synchronized
                              */
                             studyEventDataBean.setStudyEventRepeatKey(sampleOrdinal);
                             
                            
                        }else {
                        	if(!(studyEventDefinitionBean.isRepeating())) {
                        		//OC-10365
                        		if(!(studyEventDataBean.getStudyEventRepeatKey().equals("1"))) {
                        			// if same sample ordinal, NOT update at this time(current requirements), return same error 
                    				String err_msg ="For Non-Repeating event, found existing event in system - form "+ formOid +" , repeatKey: " + sampleOrdinal + "  StudyEventOID: " + studyEventDataBean.getStudyEventOID();                                                                          
                                    
                                    throw new OpenClinicaException(err_msg,"");
                        		}
                        		
                        	}
                        }
                    
                                              
                       
                    }


                    ArrayList<FormLayoutBean> formLayoutBeans = getFormLayoutBeans(formDataBean, ds);

                    for (FormLayoutBean formLayoutBean : formLayoutBeans) {
                        ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByCRFId(formLayoutBean.getCrfId());
                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectFormLayout(studyEventBean, studySubjectBean, formLayoutBean);
                        CRFVersionBean crfVersionBean = crfVersionBeans.get(0);
                        // what if we have begun with creating a study
                        // event, but haven't entered data yet? this would
                        // have us with a study event, but no corresponding
                        // event crf, yet.
                        if (eventCrfBeans.isEmpty()) {
                            logger.debug("   found no event crfs from Study Event id " + studyEventBean.getId() + ", location " + studyEventBean.getLocation());
                            // spell out criteria and create a bean if
                            // necessary, avoiding false-positives
                            if ((studyEventDefinitionBean.isTypeCommon()
                                    ||studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) && upsert.isNotStarted()) {

                                EventCRFBean newEventCrfBean = buildEventCrfBean(studySubjectBean, studyEventBean, formLayoutBean, crfVersionBean, ub);
                                if (persistEventCrfs){
                                  // if is new event, all it's EventCRFBean will be created later  
                                   if(isNewStudyEvent) {
                                		studyEventBean.getEventCRFs().add(newEventCrfBean); 
                                   }else {
                                	   newEventCrfBean = (EventCRFBean) eventCrfDAO.create(newEventCrfBean);
                                   }
                                   
                                  
                                }
                                

                                logger.debug("   created and added new event crf");

                                if (!eventCRFBeans.contains(newEventCrfBean)) {
                                    eventCRFBeans.add(newEventCrfBean);
                                }

                            }
                        }

                        // below to prevent duplicates
                        for (EventCRFBean ecb : eventCrfBeans) {
                        	//new requirments:common events has nothing related to upsert setting
                        	if(studyEventDefinitionBean.isTypeCommon()) {
                        		 if ((upsert.isDataEntryStarted() && ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY))
                                         || (ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)))
                                     if (!eventCRFBeans.contains(ecb)) {
                                         eventCRFBeans.add(ecb);
                                     }
                        	}else {
                        		 if ((upsert.isDataEntryStarted() && ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY))
                                         || (upsert.isDataEntryComplete() && ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)))
                                     if (!eventCRFBeans.contains(ecb)) {
                                         eventCRFBeans.add(ecb);
                                     }
                        	}
                           
                        }
                    }                                                                       
                }
                
                if(isNewStudyEvent) {
             	   studyEventBeans.add(studyEventBean);
                }    
                
            }
        }
        
        // update hashMap
        
        fetchEventCRFBeansResult.put("studyEventBeans", studyEventBeans);
        fetchEventCRFBeansResult.put("eventCRFBeans", eventCRFBeans);
        // if it's null, throw an error, since they should be existing beans for
        // iteration one
        return fetchEventCRFBeansResult;
    }

    public CommonEventContainerDTO addCommonForm(StudyEventDefinitionBean studyEventDefinition, CRFBean crf, StudySubjectBean studySubject,
                                                 UserAccount userAccount, StudyBean study) {

        final String COMMON = "common";
        EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);
        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(ds);
        EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(ds);

        if (study == null || study.getId() == 0) {
            logger.error("Study  is null");
            return null;
        } else if (study.getParentStudyId() == 0) {
            logger.debug("the study with Oid {} is a Parent study", study.getOid());
        } else {
            logger.debug("the study with Oid {} is a Site study", study.getOid());
        }

        if (studySubject == null || studySubject.getId() == 0 ) {
            logger.error("StudySubject is null");
            return null;
        }
        if (studyEventDefinition == null || studyEventDefinition.getId() == 0) {
            logger.error("StudyEventDefinition is null");
            return null;
        } else if (!studyEventDefinition.getType().equals(COMMON)) {
            logger.error("StudyEventDefinition with Oid {} is not a Common Type Event", studyEventDefinition.getOid());
            return null;
        }
        if (crf == null || crf.getId() == 0) {
            logger.error("Crf is null");
            return null;
        }

        EventDefinitionCRFBean edc = eventDefinitionCRFDAO.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getId(),
                crf.getId(),study.getId());

        //EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getStudyEventDefinitionId(), crf.getCrfId(), study.getStudyId());
        if (edc == null || edc.getId() == 0) {
            edc = eventDefinitionCRFDAO.findByStudyEventDefinitionIdAndCRFIdAndStudyId(studyEventDefinition.getId(), crf.getId(),
                    study.getParentStudyId());
        }
        if (edc == null || edc.getStatus().equals(Status.DELETED) || edc.getStatus().equals(Status.AUTO_DELETED)) {
            logger.error("EventDefinitionCrf for StudyEventDefinition Oid {},Crf Oid {} and Study Oid {}is null or has Removed Status",
                    studyEventDefinition.getOid(), crf.getOid(), study.getOid());
            return null;
        }
        FormLayoutBean formLayoutBean = null;
        int formLayoutId = edc.getDefaultVersionId();
        if (formLayoutId == 0) {
            logger.error("FormLayout is null");
            return null;
        }else{
            formLayoutBean = (FormLayoutBean)formLayoutDAO.findByPK(formLayoutId);
        }

        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        List<StudyEventBean> studyEvents = studyEventDAO.findAllByDefinitionAndSubject(studyEventDefinition, studySubject);
        Integer maxOrdinal;
        StudyEventBean studyEvent;
        int eventCrfId = 0;
        EventCRFBean eventCrf = null;
        if (studyEvents.size() == 0) {
            logger.debug("No previous study event found for this studyEventDef Oid {} and subject Oid{}", studyEventDefinition.getOid(),
                    studySubject.getOid());
            maxOrdinal = 0;
        } else {
            maxOrdinal = studyEventDAO.getMaxSampleOrdinal(studyEventDefinition,studySubject);
        }

        if (!studyEventDefinition.isRepeating()) {
            logger.debug("StudyEventDefinition with Oid {} is Non Repeating", studyEventDefinition.getOid());
            for (StudyEventBean stEvent : studyEvents) {
                ArrayList<EventCRFBean> eventCRFBeans = eventCRFDAO.findByEventSubjectFormLayout(stEvent, studySubject, formLayoutBean);
                if (eventCRFBeans != null && eventCRFBeans.size() > 0) {
                    eventCrf = eventCRFBeans.get(0);
                    eventCrfId = eventCrf.getId();
                    logger.debug("EventCrf with StudyEventDefinition Oid {},Crf Oid {} and StudySubjectOid {} already exist in the System",
                            studyEventDefinition.getOid(), crf.getOid(), studySubject.getOid());
                    break;
                }
            }
        }

        CommonEventContainerDTO commonEventContainerDTO = new CommonEventContainerDTO();
        commonEventContainerDTO.setEventCrfId(eventCrfId);
        commonEventContainerDTO.setFormLayoutBean(formLayoutBean);
        commonEventContainerDTO.setEventCRFBean(eventCrf);
        commonEventContainerDTO.setStudyEventDefinitionBean(studyEventDefinition);
        commonEventContainerDTO.setUserAccount(userAccount);
        commonEventContainerDTO.setStudySubjectBean(studySubject);
        commonEventContainerDTO.setMaxOrdinal(maxOrdinal);

        return commonEventContainerDTO;
    }
    
        
    /**
     * This method focus on some specific validation about the XML file data, such as repeatKey,
     * 
     * This method should call before any data update/insert
     * 
     * @param odmContainer
     * @param ub
     * @param persistEventCrfs
     * @return
     */
    @SuppressWarnings("rawtypes")
	public ArrayList<String> validateEventCRFBeans(ODMContainer odmContainer, UserAccountBean ub,HttpServletRequest request) {
    	
    	ArrayList<String> errors = new ArrayList<String>(); 
    	ArrayList<String> commonEventsFormRepeatKeys = new ArrayList<String>(); 
    	String commonEventFormRepeatKey = null;
    	HashMap<String,Integer> maxOrdinalbySubjectEvent = new HashMap<>();
    	String commonEventSubjectEventKey = null;
    	
    	HashMap<String,String> commonNonRepeatingEventSubjectKeys = new HashMap<>();
    	String commonNonRepeatingEventSubjectKey = null;
                
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
        if (studyBean.getStatus() != Status.AVAILABLE) {            
            errors.add("Study status is :" + studyBean.getStatus() + ", so dataImport is not allowed.");                    
            return errors;
        }
        
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
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


                StudyEventBean studyEventBean = null;

                UserAccount userAccount = userAccountDao.findById(ub.getId());
              
                studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));

                if (!studyEventDefinitionBean.isTypeCommon()) {
                    // @pgawade 16-March-2011 Do not allow the data import
                    // if event status is one of the - stopped, signed,
                    // locked
                    if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                            || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                            || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                    	errors.add("Do not allow the data import, if scheduled event is one of the status - stopped, signed,locked");
                        return errors;
                    }
                }

                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
                    CRFDAO crfDAO = new CRFDAO(ds);

                    if (studyEventDefinitionBean.isTypeCommon()){
                    	
                    	 /**
                    	  * "skipMatchCriteria != null" means this data flow come from Mirth,
                          *  Data from the Mirth will skip this repeat key check, because it has no repeat key
                          */
                        String skipMatchCriteria = request.getHeader("SkipMatchCriteria");
                        if(skipMatchCriteria != null && skipMatchCriteria.trim().length() >1) {
                        	continue;
                        }
                    	
                    	Boolean isRepeating = studyEventDefinitionBean.isRepeating();

                        String formOid = formDataBean.getFormOID();
                        CRFBean crfBean = crfDAO.findByOid(formOid);
                        logger.info("KMETRIC31a: In formDataBeans loop {}", formDataBeans.size());
                        CommonEventContainerDTO commonEventContainerDTO = addCommonForm(studyEventDefinitionBean,crfBean, studySubjectBean,userAccount,studyBean);
                        logger.info("KMETRIC31b: In formDataBeans loop {}", formDataBeans.size());
                        
                        /**
                         *  Here check the xml data
                         *  
                         *  For common event, no matter repeating or non repeating,if it has more than one forms, then each StudyEventData under
                         *   the same event,
                         *  each should have different repeat key
                         *  
                         *  maxOrdinalSimulation will hold/track the value of future maxOrdinal after insert into database
                         */
                        commonEventSubjectEventKey = studyOID+subjectDataBean.getSubjectOID()+studyEventDataBean.getStudyEventOID();
                        Integer maxOrdinalSimulation = maxOrdinalbySubjectEvent.get(commonEventSubjectEventKey);
                        if(maxOrdinalSimulation == null) {
                        	maxOrdinalSimulation = commonEventContainerDTO.getMaxOrdinal();
                        	maxOrdinalbySubjectEvent.put(commonEventSubjectEventKey, maxOrdinalSimulation);
                        	
                        }
                      
                    	commonEventFormRepeatKey = studyOID+subjectDataBean.getSubjectOID()+studyEventDataBean.getStudyEventOID()+studyEventDataBean.getStudyEventRepeatKey();                        
                        if(commonEventFormRepeatKey!=null) {
                        	if(!(commonEventsFormRepeatKeys.contains(commonEventFormRepeatKey))) {
                        
                        		commonEventsFormRepeatKeys.add(commonEventFormRepeatKey);
                        	}else {
                        		
                        		 errors.add("Import different forms with same repeatKey: " + sampleOrdinal + " for common event  StudyEventOID: " + studyEventDataBean.getStudyEventOID());                             	
                                 return errors;
                        	}
                        }


                        // OC-9756 fix
                        if(!isRepeating) {
                        	 commonNonRepeatingEventSubjectKey = studyOID+subjectDataBean.getSubjectOID()+studyEventDataBean.getStudyEventOID()+formOid;
                        	 
                        	 if(!(commonNonRepeatingEventSubjectKeys.containsKey(commonNonRepeatingEventSubjectKey))) {                                 
                        		 commonNonRepeatingEventSubjectKeys.put(commonNonRepeatingEventSubjectKey, commonNonRepeatingEventSubjectKey);
                         	 }else {                         		
                         		 errors.add("Import same form " + formOid +" more than once with different repeatKeys: " + sampleOrdinal + " for NON repeating common event, StudyEventOID: " + studyEventDataBean.getStudyEventOID());                             	
                                 return errors;
                         	 }
                        	
                        	 
                        }                       
                        
                        //for common events, if not provided studyEventRepeatKey, then skip/reject
                        sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey();
                       
                        if(sampleOrdinal == null || sampleOrdinal.trim().isEmpty()) {
                        	String comeFromPipe = (String) request.getHeader("PIPETEXT");
                        	if(comeFromPipe!=null && comeFromPipe.equals("PIPETEXT")) {
                        		 if(!isRepeating) {
                        			 sampleOrdinal = 1 + "";
                        		 }else {
                        			// not provide repeat key in the data file, then get the next available one
                                     sampleOrdinal = commonEventContainerDTO.getMaxOrdinal()+1 +""; 
                        		 }
                        		
                        		 
                        	}else {
                        		 //for common events, if not provided studyEventRepeatKey, then skip/reject
                        		errors.add("Missing studyEventRepeatKey for common event  StudyEventOID: " + studyEventDataBean.getStudyEventOID());
                                
                                return errors;
                        	}
                        	                          
                        }

                    	// for same subject, same event, can't have same form more than once in NON repeating COMMON event -- found same formOID in database
                    	if(!isRepeating) {
                    		ArrayList seList = studyEventDAO.findAllByStudyEventDefinitionAndCrfOids(studyEventDefinitionBean.getOid(), crfBean.getOid());
                    		for (int j = 0; j < seList.size(); j++) {
                    			StudyEventBean seBean = (StudyEventBean) seList.get(j); 
                    			if(seBean.getStudySubjectId() == studySubjectBean.getId()) {
                    				if(!((seBean.getSampleOrdinal()+"").equals(sampleOrdinal))) {
                        				errors.add("For Non-Repeating common event, found existing event in system - form "+ formOid +" , repeatKey: " + sampleOrdinal + "  StudyEventOID: " + studyEventDataBean.getStudyEventOID());                                       
                                        return errors;	
                        			}else {                        				
                        				String comeFromPipe = (String) request.getHeader("PIPETEXT");
                                    	if(comeFromPipe!=null && comeFromPipe.equals("PIPETEXT")) {
                                    		//OC-10148
                                    		errors.add("For Non-Repeating common event, found existing event in system - form "+ formOid +" , repeatKey: " + sampleOrdinal + "  StudyEventOID: " + studyEventDataBean.getStudyEventOID());                                       
                                            return errors;	                            			
                                    	}else {
                                    		//OC-10365:XML import into non-repeating common event still allow overwrite
                                    		;
                                    	}
                        				
                        			}
                    			}
                    			
                             	
                    		}
                    	}
                    	
                    	// check existing data in database, if found it, then will update it
                    	studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                                studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
                    	ArrayList crfs =crfDAO.findAllByStudyEvent(studyEventBean.getId());
                    	
                    	for (int j = 0; j < crfs.size(); j++) {
                             
                    		 CRFBean crf = (CRFBean) crfs.get(j);                                                              
                             String existingFormOId = crf.getOid();
                             
                             if(!(formOid.equals(existingFormOId)) ) {
                            	 errors.add("Import in different forms with same repeatKey: " + sampleOrdinal + " for common event  StudyEventOID: " + studyEventDataBean.getStudyEventOID());
                            	
                                 return errors;
                             }
                    	}
                    	
                        if(studyEventBean == null || studyEventBean.getId() == 0) {
                        	
                        	int existingMaxOrdinal = maxOrdinalSimulation.intValue();
                        	
                            if(this.passOrdinalLogicCheck(Integer.parseInt(sampleOrdinal), existingMaxOrdinal)) {
                            	//if no any error at this point, update maxOrdinalSimulation
                            	maxOrdinalbySubjectEvent.put(commonEventSubjectEventKey, new Integer(maxOrdinalSimulation.intValue()+1));
                            	
                            }else {
                            	int correctRepeatKey = existingMaxOrdinal + 1;
                            	errors.add("Validation error found about studyEventRepeatKey, the value " + sampleOrdinal +" is too big, suggest to reset as "+ correctRepeatKey + " StudyEventOID: " + studyEventDataBean.getStudyEventOID());
                            	
                                return errors;
                            }
                        }





                    }else {
                    	//OC-10392 and check status:scheduled
                    	/*String formOid = formDataBean.getFormOID();
                        if(!(studyEventDefinitionBean.isRepeating()) && studyEventBean!=null && !(studyEventBean.getSubjectEventStatus().getName().equals("scheduled"))) {                        		
        	    	    		errors.add("For Non-Repeating visited event, found existing event in system - form "+ formOid +" , " +  "  StudyEventOID: " + studyEventDataBean.getStudyEventOID());
        	    	    		
        	    	    		return errors;
                        	}*/
                        	
                        }
                    }

                }
            }
       
        return errors;
    }

    /*
     * purpose: returns false if any of the forms/EventCRFs fail the UpsertOnBean rules.
     */
    public boolean eventCRFStatusesValid(ODMContainer odmContainer, UserAccountBean ub) {
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
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
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
                if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
                        || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
                    return true;
                }
                for (FormDataBean formDataBean : formDataBeans) {
                    ArrayList<FormLayoutBean> formLayoutBeans = getFormLayoutBeans(formDataBean, ds);
                    for (FormLayoutBean formLayoutBean : formLayoutBeans) {

                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectFormLayout(studyEventBean, studySubjectBean, formLayoutBean);
                        // what if we have begun with creating a study
                        // event, but haven't entered data yet? this would
                        // have us with a study event, but no corresponding
                        // event crf, yet.
                        if (eventCrfBeans.isEmpty()) {
                            logger.debug("   found no event crfs from Study Event id " + studyEventBean.getId() + ", location " + studyEventBean.getLocation());
                            if ((studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.DATA_ENTRY_STARTED)
                                    || studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED))) {

                            	//new requirments:common events has nothing related to upsert setting
                            	if(studyEventDefinitionBean.isTypeCommon()) {
                            		;
                            	}else {
                            		if (!upsert.isNotStarted())
                                    return false;
                            	}
                               

                            }
                        }

                        // below to prevent duplicates

                        for (EventCRFBean ecb : eventCrfBeans) {
                        	//new requirments:common events has nothing related to upsert setting
                        	if(studyEventDefinitionBean.isTypeCommon()) {
                        		if (!(ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY) )
                                        && !(ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) )) {
                                    return false;
                                }
                        	}else {
                        		if (!(ecb.getStage().equals(DataEntryStage.INITIAL_DATA_ENTRY) && upsert.isDataEntryStarted())
                                        && !(ecb.getStage().equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) && upsert.isDataEntryComplete())) {
                                    return false;
                                }
                        	}
                            
                        }
                    }
                }
            }
        }
        return true;
    }

    /*
     * purpose: Build a map of EventCRFs and the statuses they should have post-import. Assumes EventCRFs have been
     * created for "Not Started" forms.
     */
    public HashMap<String, String> fetchEventCRFStatuses(ODMContainer odmContainer) {
        HashMap<String, String> eventCRFStatuses = new HashMap<String, String>();
        EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);

        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOID);

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());

                StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));

                for (FormDataBean formDataBean : formDataBeans) {
                    FormLayoutDAO formLayoutDAO = new FormLayoutDAO(ds);

                    ArrayList<FormLayoutBean> formLayoutBeans = getFormLayoutBeans(formDataBean, ds);

                    for (FormLayoutBean formLayoutBean : formLayoutBeans) {

                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectFormLayout(studyEventBean, studySubjectBean, formLayoutBean);

                        if (eventCrfBeans == null || eventCrfBeans.size() == 0) {
                            String ecbId = studySubjectBean.getId() + "-" + studyEventBean.getId() + "-" + formLayoutBean.getId();
                            if (!eventCRFStatuses.keySet().contains(ecbId) && formDataBean.getEventCRFStatus() != null) {
                                eventCRFStatuses.put(ecbId, formDataBean.getEventCRFStatus());
                            }
                        }

                        for (EventCRFBean ecb : eventCrfBeans) {
                            String ecbId = ecb.getStudySubjectId() + "-" + ecb.getStudyEventId() + "-" + ecb.getFormLayoutId();
                            if (!eventCRFStatuses.keySet().contains(ecbId) && formDataBean.getEventCRFStatus() != null) {
                                eventCRFStatuses.put(ecbId, formDataBean.getEventCRFStatus());
                            }
                        }
                    }

                }
            }
        }
        return eventCRFStatuses;
    }

    public SummaryStatsBean generateSummaryStatsBean(ODMContainer odmContainer, List<DisplayItemBeanWrapper> wrappers, ImportCRFInfoContainer importCrfInfo) {
        int countSubjects = 0;
        int countEventCRFs = 0;
        int discNotesGenerated = 0;
        for (DisplayItemBeanWrapper wr : wrappers) {
            HashMap validations = wr.getValidationErrors();
            discNotesGenerated += validations.size();
        }
        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        countSubjects += subjectDataBeans.size();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                // this would be the place to add more stats
                for (FormDataBean formDataBean : formDataBeans) {
                    countEventCRFs += 1;
                }
            }
        }

        SummaryStatsBean ssBean = new SummaryStatsBean();
        ssBean.setDiscNoteCount(discNotesGenerated);
        ssBean.setEventCrfCount(countEventCRFs);
        ssBean.setStudySubjectCount(countSubjects);
        ssBean.setSkippedCrfCount(importCrfInfo.getCountSkippedEventCrfs());
        return ssBean;
    }

    public List<DisplayItemBeanWrapper> lookupValidationErrors(HttpServletRequest request, ODMContainer odmContainer, UserAccountBean ub,
                                                               HashMap<String, String> totalValidationErrors, HashMap<String, String> hardValidationErrors, List<EventCRFBean> permittedEventCRFs, Locale locale)
            throws OpenClinicaException {

        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(locale);

        DisplayItemBeanWrapper displayItemBeanWrapper = null;
        HashMap validationErrors = new HashMap();
        List<DisplayItemBeanWrapper> wrappers = new ArrayList<DisplayItemBeanWrapper>();
        ImportHelper importHelper = new ImportHelper();
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        DiscrepancyValidator discValidator = new DiscrepancyValidator(request, discNotes);
        // create a second Validator, this one for hard edit checks
        HashMap<String, String> hardValidator = new HashMap<String, String>();

        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyBean studyBean = studyDAO.findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(ds);
        CRFDAO crfDAO = new CRFDAO(ds);
        CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
        EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);

        HashMap<String, ItemDataBean> blankCheck = new HashMap<String, ItemDataBean>();
        String hardValidatorErrorMsgs = "";

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        int totalEventCRFCount = 0;
        int totalItemDataBeanCount = 0;
        // int totalValidationErrors = 0;
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<DisplayItemBean> displayItemBeans = new ArrayList<DisplayItemBean>();
            logger.debug("iterating through subject data beans: found " + subjectDataBean.getSubjectOID());
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            totalEventCRFCount += studyEventDataBeans.size();

            // ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
            // to stop repeats...?
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());

            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                int parentStudyId = studyBean.getParentStudyId();
                StudyEventDefinitionBean sedBean = sedDao.findByOidAndStudy(studyEventDataBean.getStudyEventOID(), studyBean.getId(), parentStudyId);
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                logger.debug("iterating through study event data beans: found " + studyEventDataBean.getStudyEventOID());

                int ordinal = 1;
                try {
                    ordinal = new Integer(studyEventDataBean.getStudyEventRepeatKey()).intValue();
                } catch (Exception e) {
                    // trying to catch NPEs, because tags can be without the
                    // repeat key
                }
                StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        sedBean.getId(), ordinal);

                displayItemBeans = new ArrayList<DisplayItemBean>();

                for (FormDataBean formDataBean : formDataBeans) {
                    Map<String, Integer> groupMaxOrdinals = new HashMap<String, Integer>();
                    displayItemBeanWrapper = null;

                    ArrayList<FormLayoutBean> formLayoutBeans = getFormLayoutBeans(formDataBean, ds);

                    FormLayoutBean formLayoutBean = formLayoutBeans.get(0);
                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                    if ((formLayoutBeans == null) || (formLayoutBeans.size() == 0)) {
                        MessageFormat mf = new MessageFormat("");
                        mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
                        Object[] arguments = { formDataBean.getFormOID() };

                        throw new OpenClinicaException(mf.format(arguments), "");
                    }
                    // if you have a mispelled form oid you get an error here
                    // need to error out gracefully and post an error
                    logger.debug("iterating through form beans: found " + formLayoutBean.getOid());
                    // may be the point where we cut off item groups etc and
                    // instead work on sections
                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByCRFId(formLayoutBean.getCrfId());
                    CRFVersionBean crfVersionBean = crfVersionBeans.get(0);
                    EventCRFBean eventCRFBean = buildEventCrfBean(studySubjectBean, studyEvent, formLayoutBean, crfVersionBean, ub);

                    EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(ds);
                    EventDefinitionCRFBean eventDefinitionCRF = eventDefinitionCRFDAO.findByStudyEventIdAndFormLayoutId(studyBean, studyEvent.getId(),
                            formLayoutBean.getId());
                    if (eventCRFBean != null) {
                        for (EventCRFBean permittedEventCRF : permittedEventCRFs) {
                            if (permittedEventCRF.getStudySubjectId() == eventCRFBean.getStudySubjectId()
                                    && permittedEventCRF.getStudyEventId() == eventCRFBean.getStudyEventId()
                                    && permittedEventCRF.getFormLayoutId() == eventCRFBean.getFormLayoutId()) {
                                permittedEventCRF.setStatus(eventCRFBean.getStatus());
                                eventCRFBean = permittedEventCRF;

                                for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                                    groupMaxOrdinals.put(itemGroupDataBean.getItemGroupOID(), 1);
                                }
                                // if and only if it's in the correct status do we need
                                // to generate the beans
                                // <<tbh, 09/2008
                                // also need to create a group name checker here for
                                // correctness, tbh
                                for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                                    ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                                    logger.debug("iterating through group beans: " + itemGroupDataBean.getItemGroupOID());
                                    // put a checker in here
                                    ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
                                    ItemGroupBean testBean = itemGroupDAO.findByOid(itemGroupDataBean.getItemGroupOID());
                                    if (testBean == null) {
                                        // TODO i18n of message
                                        // logger.debug("hit the exception for item groups! " +
                                        // itemGroupDataBean.getItemGroupOID());
                                        MessageFormat mf = new MessageFormat("");
                                        mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                                        Object[] arguments = { itemGroupDataBean.getItemGroupOID(), formDataBean.getFormOID() };

                                        throw new OpenClinicaException(mf.format(arguments), "");
                                    }
                                    totalItemDataBeanCount += itemDataBeans.size();

                                    for (ImportItemDataBean importItemDataBean : itemDataBeans) {
                                        logger.debug("   iterating through item data beans: " + importItemDataBean.getItemOID());
                                        ItemDAO itemDAO = new ItemDAO(ds);
                                        ItemFormMetadataDAO itemFormMetadataDAO = new ItemFormMetadataDAO(ds);

                                        List<ItemBean> itemBeans = itemDAO.findByOid(importItemDataBean.getItemOID());
                                        if (!itemBeans.isEmpty()) {
                                            ItemBean itemBean = itemBeans.get(0);
                                            logger.debug("   found " + itemBean.getName());
                                            // throw a null pointer? hopefully not if its been checked...
                                            DisplayItemBean displayItemBean = new DisplayItemBean();
                                            displayItemBean.setItem(itemBean);

                                            ArrayList<ItemFormMetadataBean> metadataBeans = itemFormMetadataDAO.findAllByItemId(itemBean.getId());
                                            logger.debug("      found metadata item beans: " + metadataBeans.size());
                                            // groupOrdinal = the ordinal in item groups, for repeating items
                                            int groupOrdinal = 1;
                                            if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
                                                try {
                                                    groupOrdinal = new Integer(itemGroupDataBean.getItemGroupRepeatKey()).intValue();
                                                    if (groupOrdinal > groupMaxOrdinals.get(itemGroupDataBean.getItemGroupOID())) {
                                                        groupMaxOrdinals.put(itemGroupDataBean.getItemGroupOID(), groupOrdinal);
                                                    }
                                                } catch (Exception e) {
                                                    // do nothing here currently, we are
                                                    // looking for a number format
                                                    // exception
                                                    // from the above.
                                                    logger.debug("found npe for group ordinals, line 344!");
                                                }
                                            }
                                            ItemDataBean itemDataBean = createItemDataBean(itemBean, eventCRFBean, importItemDataBean.getValue(), ub,
                                                    groupOrdinal);
                                            String newKey = groupOrdinal + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
                                                    + subjectDataBean.getSubjectOID();
                                            blankCheck.put(newKey, itemDataBean);
                                            logger.info("adding " + newKey + " to blank checks");
                                            if (!metadataBeans.isEmpty()) {
                                                ItemFormMetadataBean metadataBean = metadataBeans.get(0);
                                                // also
                                                // possible
                                                // nullpointer

                                                displayItemBean.setData(itemDataBean);
                                                displayItemBean.setMetadata(metadataBean);
                                                // set event def crf?
                                                displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
                                                String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                                                // if you do indeed leave off this in the XML it will pass but return
                                                // 'null'
                                                // tbh
                                                attachValidator(displayItemBean, importHelper, discValidator, hardValidator, request, eventCRFRepeatKey,
                                                        studySubjectBean.getOid(), respage);
                                                displayItemBeans.add(displayItemBean);

                                            } else {
                                                MessageFormat mf = new MessageFormat("");
                                                mf.applyPattern(respage.getString("no_metadata_could_be_found"));
                                                Object[] arguments = { importItemDataBean.getItemOID() };

                                                throw new OpenClinicaException(mf.format(arguments), "");
                                            }
                                        } else {
                                            // report the error there
                                            MessageFormat mf = new MessageFormat("");
                                            mf.applyPattern(respage.getString("no_item_could_be_found"));
                                            Object[] arguments = { importItemDataBean.getItemOID() };

                                            throw new OpenClinicaException(mf.format(arguments), "");
                                        }
                                    } // end item data beans
                                    logger.debug(".. found blank check: " + blankCheck.toString());
                                    // >> TBH #5548 did we create any repeated blank spots? If so, fill them in with an
                                    // Item
                                    // Data Bean

                                    // JN: this max Ordinal can cause problems as it assumes the latest values, and when
                                    // there
                                    // is an entry with nongroup towards the
                                    // end. Refer: MANTIS 6113

                                } // end item group data beans
                            }
                        } // matches if on permittedCRFIDs

                        CRFBean crfBean = crfDAO.findByLayoutId(formLayoutBean.getCrfId());
                        // seems like an extravagance, but is not contained in crf
                        // version or event crf bean
                        validationErrors = discValidator.validate();
                        // totalValidationErrors += validationErrors.size();
                        // totalValidationErrors.
                        // totalValidationErrors.addAll(validationErrors);
                        for (Object errorKey : validationErrors.keySet()) {
                            // logger.debug(errorKey.toString() + " -- " + validationErrors.get(errorKey));
                            // JN: to avoid duplicate errors
                            if (!totalValidationErrors.containsKey(errorKey.toString()))
                                totalValidationErrors.put(errorKey.toString(), validationErrors.get(errorKey).toString());
                            // assuming that this will be put back in to the core
                            // method's hashmap, updating statically, tbh 06/2008
                            logger.debug("+++ adding " + errorKey.toString());
                        }
                        logger.debug("-- hard validation checks: --");
                        for (Object errorKey : hardValidator.keySet()) {
                            logger.debug(errorKey.toString() + " -- " + hardValidator.get(errorKey));
                            hardValidationErrors.put(errorKey.toString(), hardValidator.get(errorKey));
                            // updating here 'statically' tbh 06/2008
                            hardValidatorErrorMsgs += hardValidator.get(errorKey) + "<br/><br/>";
                        }

                        String studyEventId = studyEvent.getId() + "";
                        String crfVersionId = formLayoutBean.getId() + "";

                        logger.debug("creation of wrapper: original count of display item beans " + displayItemBeans.size() + ", count of item data beans "
                                + totalItemDataBeanCount + " count of validation errors " + validationErrors.size() + " count of study subjects "
                                + subjectDataBeans.size() + " count of event crfs " + totalEventCRFCount + " count of hard error checks "
                                + hardValidator.size());
                        // check if we need to overwrite
                        DataEntryStage dataEntryStage = eventCRFBean.getStage();
                        Status eventCRFStatus = eventCRFBean.getStatus();
                        boolean overwrite = false;
                        // tbh >>
                        // //JN: Commenting out the following 2 lines, coz the prompt should come in the cases on
                        if (// eventCRFStatus.equals(Status.UNAVAILABLE) ||
                        dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                                || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                            overwrite = true;
                        }
                        // << tbh, adding extra statuses to prevent appending, 06/2009

                        // SummaryStatsBean ssBean = new SummaryStatsBean();
                        // ssBean.setDiscNoteCount(totalValidationErrors);
                        // ssBean.setEventCrfCount(totalEventCRFCount);
                        // ssBean.setStudySubjectCount(subjectDataBeans.size());
                        // // add other stats here, tbh
                        // not working here, need to do it in a different method,
                        // tbh

                        // summary stats added tbh 05/2008
                        // JN: Changed from validationErrors to totalValidationErrors to create discrepancy notes for
                        // all
                        // the
                        displayItemBeanWrapper = new DisplayItemBeanWrapper(displayItemBeans, true, overwrite, validationErrors, studyEventId, crfVersionId,
                                studyEventDataBean.getStudyEventOID(), studySubjectBean.getLabel(), eventCRFBean.getCreatedDate(), crfBean.getName(),
                                formLayoutBean.getName(), studySubjectBean.getOid(), studyEventDataBean.getStudyEventRepeatKey(), eventCRFBean);

                        // JN: Commenting out the following code, since we shouldn't re-initialize at this point, as
                        // validationErrors would get overwritten and the
                        // older errors will be overriden. Moving it after the form.
                        // Removing the comments for now, since it seems to be creating duplicate Discrepancy Notes.
                        validationErrors = new HashMap();
                        discValidator = new DiscrepancyValidator(request, discNotes);
                        // reset to allow for new errors...
                    }
                } // after forms
                  // validationErrors = new HashMap();
                  // discValidator = new DiscrepancyValidator(request, discNotes);
                if (displayItemBeanWrapper != null && displayItemBeans.size() > 0)
                    wrappers.add(displayItemBeanWrapper);
            } // after study events

            // remove repeats here? remove them below by only forwarding the
            // first
            // each wrapper represents an Event CRF and a Form, but we don't
            // have all events for all forms
            // need to not add a wrapper for every event + form combination,
            // but instead for every event + form combination which is present
            // look at the hack below and see what happens
        } // after study subjects

        // throw the OC exception here at the end, if hard edit checks are not
        // empty

        // we statically update the hash map here, so it should not have to be
        // thrown, tbh 06/2008
        if (!hardValidator.isEmpty()) {
            // throw new OpenClinicaException(hardValidatorErrorMsgs, "");
        }
        return wrappers;
    }

    private ItemDataBean createItemDataBean(ItemBean itemBean, EventCRFBean eventCrfBean, String value, UserAccountBean ub, int ordinal) {

        ItemDataBean itemDataBean = new ItemDataBean();
        itemDataBean.setItemId(itemBean.getId());
        itemDataBean.setCreatedDate(new Date());
        itemDataBean.setEventCRFId(eventCrfBean.getId());
        itemDataBean.setOrdinal(ordinal);
        itemDataBean.setOwner(ub);
        itemDataBean.setStatus(Status.UNAVAILABLE);
        itemDataBean.setValue(value);

        return itemDataBean;
    }

    private void attachValidator(DisplayItemBean displayItemBean, ImportHelper importHelper, DiscrepancyValidator v, HashMap<String, String> hardv,
                                 HttpServletRequest request, String eventCRFRepeatKey, String studySubjectOID, ResourceBundle respage) throws OpenClinicaException {
        org.akaza.openclinica.bean.core.ResponseType rt = displayItemBean.getMetadata().getResponseSet().getResponseType();
        String itemOid = displayItemBean.getItem().getOid() + "_" + eventCRFRepeatKey + "_" + displayItemBean.getData().getOrdinal() + "_" + studySubjectOID;
        // note the above, generating an ordinal on top of the OID to view
        // errors. adding event crf repeat key here, tbh
        // need to add a per-subject differentator, what if its a diff subject
        // with the same item, repeat key and ordinal???

        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
            ItemFormMetadataBean ifm = displayItemBean.getMetadata();
            String widthDecimal = ifm.getWidthDecimal();
            // what if it's a date? parse if out so that we go from iso 8601 to
            // mm/dd/yyyy
            if (displayItemBean.getItem().getDataType().equals(ItemDataType.DATE)) {
                // pass it if it is blank, tbh
                if (!"".equals(displayItemBean.getData().getValue())) {
                    String dateValue = displayItemBean.getData().getValue();
                    SimpleDateFormat sdf_sqldate = new SimpleDateFormat("yyyy-MM-dd");
                    sdf_sqldate.setLenient(false);
                    try {
                        Date originalDate = sdf_sqldate.parse(dateValue);
                        String replacementValue = new SimpleDateFormat("MM/dd/yyyy").format(originalDate);
                        displayItemBean.getData().setValue(dateValue);
                    } catch (ParseException pe1) {

                        // next version; fail if it does not pass iso 8601
                        MessageFormat mf = new MessageFormat("");
                        mf.applyPattern(respage.getString("you_have_a_date_value_which_is_not"));
                        Object[] arguments = { displayItemBean.getItem().getOid() };

                        hardv.put(itemOid, mf.format(arguments));

                    }
                }

            } else if (displayItemBean.getItem().getDataType().equals(ItemDataType.ST)) {
                int width = Validator.parseWidth(widthDecimal);
                if (width > 0 && displayItemBean.getData().getValue().length() > width) {
                    hardv.put(itemOid, "This value exceeds required width=" + width);
                }
            }
            // what if it's a number? should be only numbers
            else if (displayItemBean.getItem().getDataType().equals(ItemDataType.INTEGER)) {
                try {
                    Integer testInt = new Integer(displayItemBean.getData().getValue());
                    int width = Validator.parseWidth(widthDecimal);
                    if (width > 0 && displayItemBean.getData().getValue().length() > width) {
                        hardv.put(itemOid, "This value exceeds required width=" + width);
                    }
                    // now, didn't check decimal for testInt.
                } catch (Exception e) {// should be a sub class
                    // pass if blank, tbh 07/2010
                    if (!"".equals(displayItemBean.getData().getValue())) {
                        hardv.put(itemOid, "This value is not an integer.");
                    }
                }
            }
            // what if it's a float? should be only numbers
            else if (displayItemBean.getItem().getDataType().equals(ItemDataType.REAL)) {
                try {
                    Float testFloat = new Float(displayItemBean.getData().getValue());
                    int width = Validator.parseWidth(widthDecimal);
                    if (width > 0 && displayItemBean.getData().getValue().length() > width) {
                        hardv.put(itemOid, "This value exceeds required width=" + width);
                    }
                    int decimal = Validator.parseDecimal(widthDecimal);
                    if (decimal > 0 && BigDecimal.valueOf(new Double(displayItemBean.getData().getValue()).doubleValue()).scale() > decimal) {
                        hardv.put(itemOid, "This value exceeds required decimal=" + decimal);
                    }
                } catch (Exception ee) {
                    // pass if blank, tbh
                    if (!"".equals(displayItemBean.getData().getValue())) {
                        hardv.put(itemOid, "This value is not a real number.");
                    }
                }
            }
            // what if it's a phone number? how often does that happen?

            request.setAttribute(itemOid, displayItemBean.getData().getValue());
            displayItemBean = importHelper.validateDisplayItemBeanText(v, displayItemBean, itemOid);
            // errors = v.validate();

        } else if (rt.equals(ResponseType.CALCULATION) || rt.equals(ResponseType.GROUP_CALCULATION)) {
            if (displayItemBean.getItem().getDataType().equals(ItemDataType.REAL)) {
                ItemFormMetadataBean ifm = displayItemBean.getMetadata();
                String widthDecimal = ifm.getWidthDecimal();
                int decimal = Validator.parseDecimal(widthDecimal);
                if (decimal > 0) {
                    try {
                        Double d = new Double(displayItemBean.getData().getValue());
                        if (BigDecimal.valueOf(d).scale() > decimal) {
                            hardv.put(itemOid, "This value exceeds required decimal=" + decimal);
                        }
                    } catch (Exception e) {
                        if (!"".equals(displayItemBean.getData().getValue())) {
                            hardv.put(itemOid, "This value is not a real number.");
                        }
                    }
                }
            }
        }
        // JN commenting out the following as we dont need hard edits for these cases, they need to be soft stops and DN
        // should be filed REF MANTIS 6271

        else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            // logger.info(itemOid + "is a RADIO or
            // a SELECT ");
            // adding a new hard edit check here; response_option mismatch
            String theValue = matchValueWithOptions(displayItemBean, displayItemBean.getData().getValue(),
                    displayItemBean.getMetadata().getResponseSet().getOptions());
            request.setAttribute(itemOid, theValue);
            logger.debug("        found the value for radio/single: " + theValue);
            if (theValue == null && displayItemBean.getData().getValue() != null && !displayItemBean.getData().getValue().isEmpty()) {
                // fail it here
                logger.debug("-- theValue was NULL, the real value was " + displayItemBean.getData().getValue());
                hardv.put(itemOid, "This is not in the correct response set.");
                // throw new OpenClinicaException("One of your items did not
                // have the correct response set. Please review Item OID "
                // + displayItemBean.getItem().getOid() + " repeat key " +
                // displayItemBean.getData().getOrdinal(), "");
            }
            displayItemBean = importHelper.validateDisplayItemBeanSingleCV(v, displayItemBean, itemOid);
            // errors = v.validate();
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            // logger.info(itemOid + "is a CHECKBOX
            // or a SELECTMULTI ");
            String theValue = matchValueWithManyOptions(displayItemBean, displayItemBean.getData().getValue(),
                    displayItemBean.getMetadata().getResponseSet().getOptions());
            request.setAttribute(itemOid, theValue);
            // logger.debug(" found the value for checkbx/multi: " + theValue);
            if (theValue == null && displayItemBean.getData().getValue() != null && !displayItemBean.getData().getValue().isEmpty()) {
                // fail it here? found an 0,1 in the place of a NULL
                // logger.debug("-- theValue was NULL, the real value was " + displayItemBean.getData().getValue());
                hardv.put(itemOid, "This is not in the correct response set.");
            }
            displayItemBean = importHelper.validateDisplayItemBeanMultipleCV(v, displayItemBean, itemOid);
            // errors = v.validate();
        }
    }

    private String matchValueWithOptions(DisplayItemBean displayItemBean, String value, List options) {
        String returnedValue = null;
        if (!options.isEmpty()) {
            for (Object responseOption : options) {
                ResponseOptionBean responseOptionBean = (ResponseOptionBean) responseOption;
                if (responseOptionBean.getValue().equals(value)) {
                    // if (((ResponseOptionBean)
                    // responseOption).getText().equals(value)) {
                    displayItemBean.getData().setValue(((ResponseOptionBean) responseOption).getValue());
                    return ((ResponseOptionBean) responseOption).getValue();

                }
            }
        }
        return returnedValue;
    }

    /*
     * difference from the above is only a 'contains' in the place of an 'equals'. and a few other switches...also need
     * to keep in mind that there are non-null values that need to be taken into account
     */
    private String matchValueWithManyOptions(DisplayItemBean displayItemBean, String value, List options) {
        String returnedValue = null;
        // boolean checkComplete = true;
        String entireOptions = "";

        String[] simValues = value.split(",");

        String simValue = value.replace(",", "");
        // also remove all spaces, so they will fit up with the entire set
        // of options
        simValue = simValue.replace(" ", "");
        boolean checkComplete = true;
        if (!options.isEmpty()) {
            for (Object responseOption : options) {
                ResponseOptionBean responseOptionBean = (ResponseOptionBean) responseOption;
                // logger.debug("testing response option bean get value: " + responseOptionBean.getValue());
                // entireOptions += responseOptionBean.getValue() + "{0,1}|";//
                // once, or not at all
                entireOptions += responseOptionBean.getValue();
                // trying smth new, tbh 01/2009

                // logger.debug("checking on this string: " +
                // entireOptions + " versus this value " + simValue);
                // checkComplete = Pattern.matches(entireOptions, simValue); //
                // switch values, tbh

                // if (!checkComplete) {
                // return returnedValue;
                // }

            }
            // remove spaces, since they are causing problems:
            entireOptions = entireOptions.replace(" ", "");
            // following may be superfluous, tbh

            ArrayList nullValues = displayItemBean.getEventDefinitionCRF().getNullValuesList();

            for (Object nullValue : nullValues) {
                NullValue nullValueTerm = (NullValue) nullValue;
                entireOptions += nullValueTerm.getName();
                // logger.debug("found and added " + nullValueTerm.getName());
            }

            for (String sim : simValues) {
                sim = sim.replace(" ", "");
                // logger.debug("checking on this string: " + entireOptions + " versus this value " + sim);
                checkComplete = entireOptions.contains(sim);// Pattern.matches(
                // entireOptions,
                // sim);
                if (!checkComplete) {
                    return returnedValue;
                }
            }
        }
        return value;
    }

    /*
     * meant to answer the following questions 3.a. is that study subject in that study? 3.b. is that study event def in
     * that study? 3.c. is that site in that study? 3.d. is that crf version in that study event def? 3.e. are those
     * item groups in that crf version? 3.f. are those items in that item group?
     */
    public List<String> validateStudyMetadata(ODMContainer odmContainer, int currentStudyId, Locale locale) {

        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(locale);
        List<String> errors = new ArrayList<String>();
        MessageFormat mf = new MessageFormat("");

        // throw new OpenClinicaException(mf.format(arguments), "");
        try {
            StudyDAO studyDAO = new StudyDAO(ds);
            String studyOid = odmContainer.getCrfDataPostImportContainer().getStudyOID();
            StudyBean studyBean = studyDAO.findByOid(studyOid);
            if (studyBean == null) {
                mf.applyPattern(respage.getString("your_study_oid_does_not_reference_an_existing"));
                Object[] arguments = { studyOid };

                errors.add(mf.format(arguments));
                // errors.add("Your Study OID " + studyOid + " does not
                // reference an existing Study or Site in the database. Please
                // check it and try again.");
                // throw an error here because getting the ID would be difficult
                // otherwise
                logger.debug("unknown study OID");
                throw new OpenClinicaException("Unknown Study OID", "");

                // } else if (studyBean.getId() != currentStudyId) {
            } else if (!CoreResources.isPublicStudySameAsTenantStudy(studyBean, studyOid, ds)) {
                mf.applyPattern(respage.getString("your_current_study_is_not_the_same_as"));
                Object[] arguments = { studyBean.getName() };
                //
                // errors.add("Your current study is not the same as the Study "
                // + studyBean.getName()
                // + ", for which you are trying to enter data. Please log out
                // of your current study and into the study for which the data
                // is keyed.");
                errors.add(mf.format(arguments));
            }
            ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();

            StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
            StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
            StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
            FormLayoutDAO formLayoutDAO = new FormLayoutDAO(ds);
            ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
            ItemDAO itemDAO = new ItemDAO(ds);
            CRFDAO crfDAO = new CRFDAO(ds);
            EventDefinitionCRFDAO edcDAO = new EventDefinitionCRFDAO(ds);

            if (subjectDataBeans != null) {// need to do this so as not to
                // throw the exception below and
                // report all available errors, tbh
                for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                    String oid = subjectDataBean.getSubjectOID();
                    StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(oid, studyBean.getId());
                    if (studySubjectBean == null) {
                        mf.applyPattern(respage.getString("your_subject_oid_does_not_reference"));
                        Object[] arguments = { oid };
                        errors.add(mf.format(arguments));
                        logger.debug("logged an error with subject oid " + oid);
                    }

                    ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
                    if (studyEventDataBeans != null) {
                        for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                            String sedOid = studyEventDataBean.getStudyEventOID();
                            StudyEventDefinitionBean studyEventDefintionBean = studyEventDefinitionDAO.findByOidAndStudy(sedOid, studyBean.getId(),
                                    studyBean.getParentStudyId());
                            if (studyEventDefintionBean != null && studyEventDefintionBean.isTypeCommon()) {
                            	// Do nothing
                            }else if (studyEventDataBean.getStudyEventRepeatKey() == null || studyEventDataBean.getStudyEventRepeatKey().trim().isEmpty())
                                studyEventDataBean.setStudyEventRepeatKey("1");
                           

                            if (studyEventDefintionBean != null && studySubjectBean != null) {
                            	if (!(studyEventDefintionBean.isTypeCommon())){
	                                StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(
	                                        studySubjectBean.getId(), studyEventDefintionBean.getId(),
	                                        Integer.valueOf(studyEventDataBean.getStudyEventRepeatKey()));
	                              
	                                    // Do something probably not sure....
	                               if (studyEvent == null || studyEvent.getId() == 0) {
	                            	    if(studyEventDefintionBean.isRepeating()) {
	                            	    	mf.applyPattern(respage.getString("your_study_event_oid_for_subject_oid"));
		                                    Object[] arguments = { sedOid, oid };
		                                    errors.add(mf.format(arguments));
		                                    logger.debug("logged an error with se oid " + sedOid + " and subject oid " + oid);
	                            	    }else {
	                            	    	mf.applyPattern(respage.getString("your_study_non_repeating_event_oid_for_subject_oid_repeat_key"));
		                                    Object[] arguments = { studyEventDataBean.getStudyEventRepeatKey(),sedOid, oid };
		                                    errors.add(mf.format(arguments));
		                                    logger.debug("logged an error with se oid " + sedOid + " and subject oid " + oid + "and repeat key " + studyEventDataBean.getStudyEventRepeatKey());
	                            	    }
	                                   
	                                }
                            	}
                            } else if (studyEventDefintionBean == null) {
                                mf.applyPattern(respage.getString("your_study_event_oid_for_subject_oid"));
                                Object[] arguments = { sedOid, oid };
                                errors.add(mf.format(arguments));
                                logger.debug("logged an error with se oid " + sedOid + " and subject oid " + oid);
                            }

                            ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                            if (formDataBeans != null) {
                                for (FormDataBean formDataBean : formDataBeans) {
                                    String formOid = formDataBean.getFormOID();
                                    String formLayoutName = formDataBean.getFormLayoutName();
                                    CRFBean crfBean = crfDAO.findByOid(formOid);
                                    if (crfBean != null && studyEventDefintionBean != null) {
                                        EventDefinitionCRFBean edcBean = edcDAO.findByStudyEventDefinitionIdAndCRFId(studyEventDefintionBean.getId(),
                                                crfBean.getId());
                                        if (edcBean == null || edcBean.getId() == 0) {
                                            mf.applyPattern(respage.getString("your_form_oid_for_study_event_oid"));
                                            Object[] arguments = { formOid, sedOid };
                                            errors.add(mf.format(arguments));
                                        }
                                    }

                                    if (crfBean != null) {
                                        FormLayoutBean formLayoutBean = (FormLayoutBean) formLayoutDAO.findByFullName(formLayoutName, crfBean.getName());
                                        if (formLayoutBean == null || formLayoutBean.getId() == 0) {
                                            mf.applyPattern(respage.getString("your_form_layout_oid_for_form_oid"));
                                            Object[] arguments = { formLayoutName, formOid };
                                            errors.add(mf.format(arguments));
                                        }
                                    } else {
                                        mf.applyPattern(respage.getString("your_form_oid_did_not_generate"));
                                        Object[] arguments = { formOid };
                                        errors.add(mf.format(arguments));
                                    }

                                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
                                    if (itemGroupDataBeans != null) {
                                        for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                                            String itemGroupOID = itemGroupDataBean.getItemGroupOID();
                                            ItemGroupBean itemGroupBean = itemGroupDAO.findByOid(itemGroupOID);
                                            if (itemGroupBean != null && crfBean != null) {
                                                itemGroupBean = itemGroupDAO.findByOidAndCrf(itemGroupOID, crfBean.getId());
                                                if (itemGroupBean == null) {
                                                    mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                                                    Object[] arguments = { itemGroupOID, formOid };
                                                    errors.add(mf.format(arguments));
                                                }
                                            } else if (itemGroupBean == null) {
                                                mf.applyPattern(respage.getString("the_item_group_oid_did_not"));
                                                Object[] arguments = { itemGroupOID };
                                                errors.add(mf.format(arguments));
                                            }

                                            ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                                            if (itemDataBeans != null) {
                                                for (ImportItemDataBean itemDataBean : itemDataBeans) {
                                                    String itemOID = itemDataBean.getItemOID();
                                                    List<ItemBean> itemBeans = (List<ItemBean>) itemDAO.findByOid(itemOID);
                                                    if (itemBeans.size() != 0 && itemGroupBean != null) {
                                                        ItemBean itemBean = itemDAO.findItemByGroupIdandItemOid(itemGroupBean.getId(), itemOID);
                                                        if (itemBean == null) {
                                                            mf.applyPattern(respage.getString("your_item_oid_for_item_group_oid"));
                                                            Object[] arguments = { itemOID, itemGroupOID };
                                                            errors.add(mf.format(arguments));
                                                        }
                                                    } else if (itemBeans.size() == 0) {
                                                        mf.applyPattern(respage.getString("the_item_oid_did_not"));
                                                        Object[] arguments = { itemOID };
                                                        errors.add(mf.format(arguments));
                                                    }
                                                } // itemDataBean
                                            } // if (itemDataBeans != null)
                                        } // itemGroupDataBean
                                    } // if (itemGroupDataBeans != null)
                                } // formDataBean
                            } // if (formDataBeans != null)
                        } // StudyEventDataBean
                    } // if (studyEventDataBeans != null)
                } // subjectDataBean
            } // if (subjectDataBean !=null)
        } catch (OpenClinicaException oce) {

        } catch (NullPointerException npe) {
            logger.debug("found a nullpointer here");
        }
        // if errors == null you pass, if not you fail
        return errors;
    }

    private ItemDataDAO getItemDataDao() {
        itemDataDao = this.itemDataDao != null ? itemDataDao : new ItemDataDAO(ds);
        return itemDataDao;
    }

    private ArrayList<FormLayoutBean> getFormLayoutBeans(FormDataBean formDataBean, DataSource ds) {
        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(ds);
        CRFDAO crfDAO = new CRFDAO(ds);
        ArrayList<FormLayoutBean> formLayoutBeans = new ArrayList<>();

        String formOid = formDataBean.getFormOID();
        CRFBean crfBean = crfDAO.findByOid(formOid);
        if (crfBean != null) {
            FormLayoutBean formLayoutBean = (FormLayoutBean) formLayoutDAO.findByFullName(formDataBean.getFormLayoutName(), crfBean.getName());
            if (formLayoutBean != null && formLayoutBean.getId() != 0) {
                formLayoutBeans.add(formLayoutBean);
            }
        }

        return formLayoutBeans;
    }

    public EventCRFBean buildEventCrfBean(StudySubjectBean studySubjectBean, StudyEventBean studyEventBean, FormLayoutBean formLayoutBean,
                                          CRFVersionBean crfVersionBean, UserAccountBean ub) {
        EventCRFBean newEventCrfBean = new EventCRFBean();
        newEventCrfBean.setStudyEventId(studyEventBean.getId());
        newEventCrfBean.setStudySubjectId(studySubjectBean.getId());
        newEventCrfBean.setCRFVersionId(crfVersionBean.getId());
        newEventCrfBean.setFormLayoutId(formLayoutBean.getId());
        newEventCrfBean.setDateInterviewed(new Date());
        newEventCrfBean.setOwner(ub);
        newEventCrfBean.setInterviewerName(ub.getName());
        newEventCrfBean.setCompletionStatusId(1);// place
        // filler
        newEventCrfBean.setStatus(Status.AVAILABLE);
        newEventCrfBean.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
        return newEventCrfBean;
    }
    
    /*
     * purpose: Build a map of EventCRFs and the statuses they should have post-import. Assumes EventCRFs have been
     * created for "Not Started" forms.
     */
    public void fetchEventCRFStatuses(ODMContainer odmContainer, HashMap<Integer, String> importedCRFStatuses) {
        EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);

        String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOID);

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

                String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

                StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
                        studyBean.getId(), studyBean.getParentStudyId());
                logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject " + studySubjectBean.getName());

                StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));

                for (FormDataBean formDataBean : formDataBeans) {

                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);

                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByFormOid(formDataBean.getFormOID());
                    for (CRFVersionBean crfVersionBean : crfVersionBeans) {
                        ArrayList<EventCRFBean> eventCrfBeans = eventCrfDAO.findByEventSubjectVersion(studyEventBean, studySubjectBean, crfVersionBean);
                        for (EventCRFBean ecb : eventCrfBeans) {
                            Integer ecbId = new Integer(ecb.getId());

                            if (!importedCRFStatuses.keySet().contains(ecbId) && formDataBean.getEventCRFStatus() != null) {
                                importedCRFStatuses.put(ecb.getId(), formDataBean.getEventCRFStatus());
                            }
                        }
                    }

                }
            }
        }
    }
    
    public List<DisplayItemBeanWrapper> lookupValidationErrors(HttpServletRequest request, ODMContainer odmContainer, UserAccountBean ub,
                                                               HashMap<String, String> totalValidationErrors, HashMap<String, String> hardValidationErrors, ArrayList<Integer> permittedEventCRFIds, Locale locale)
            throws OpenClinicaException {

        ResourceBundleProvider.updateLocale(locale);
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(locale);

        DisplayItemBeanWrapper displayItemBeanWrapper = null;
        HashMap validationErrors = new HashMap();
        List<DisplayItemBeanWrapper> wrappers = new ArrayList<DisplayItemBeanWrapper>();
        ImportHelper importHelper = new ImportHelper();
        FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
        DiscrepancyValidator discValidator = new DiscrepancyValidator(request, discNotes);
        // create a second Validator, this one for hard edit checks
        HashMap<String, String> hardValidator = new HashMap<String, String>();

        StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
        StudyDAO studyDAO = new StudyDAO(ds);
        StudyBean studyBean = studyDAO.findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());
        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
        StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(ds);
        HashMap<String, ItemDataBean> blankCheck = new HashMap<String, ItemDataBean>();
        String hardValidatorErrorMsgs = "";

        ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
        int totalEventCRFCount = 0;
        int totalItemDataBeanCount = 0;
        // int totalValidationErrors = 0;
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            ArrayList<DisplayItemBean> displayItemBeans = new ArrayList<DisplayItemBean>();
            logger.debug("iterating through subject data beans: found " + subjectDataBean.getSubjectOID());
            ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
            totalEventCRFCount += studyEventDataBeans.size();

            // ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
            // to stop repeats...?
            StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());

            for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
                int parentStudyId = studyBean.getParentStudyId();
                StudyEventDefinitionBean sedBean = sedDao.findByOidAndStudy(studyEventDataBean.getStudyEventOID(), studyBean.getId(), parentStudyId);
                ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
                logger.debug("iterating through study event data beans: found " + studyEventDataBean.getStudyEventOID());
                
                // test skip
                String sqlStr = this.buildSkipMatchCriteriaSql(request, studyBean.getOid(), studySubjectBean.getOid(),studyEventDataBean.getStudyEventOID());
                ArrayList matchCriterias = this.getItemDataDao().findSkipMatchCriterias(sqlStr); 
                boolean matchedAndSkip = this.needToSkip(matchCriterias, formDataBeans);
                
                
                if(matchedAndSkip) {
                	//System.out.println("===============================matchedAndSkip========================");
                	/** log message:
        	    	 * RowNo | ParticipantID | Status | Message
        				1 | SS_SUB510 | SUCCESS | InsertU
        				2 | SS_SUB511 | FAILED  | errorCode.participantNotFound
        				3 | SS_SUB512 | SUCCESS | Update
        				4 | SS_SUB512 | SUCCESS | Skip
        	    	 */
                	String originalFileName = request.getHeader("originalFileName");
                	// sample file name like:originalFileName_123.txt,pipe_delimited_local_skip_2.txt
                	String recordNum = null;
                	if(originalFileName !=null) {
                		recordNum = originalFileName.substring(originalFileName.lastIndexOf("_")+1,originalFileName.indexOf("."));
                		originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf("_"));
                	}
                	String msg;
                	msg = recordNum + "|" + studySubjectBean.getOid() + "|SUCCESS|" + "Skip";    	    	

    	    		throw new OpenClinicaException(msg, "");
                }
                
                /**
                 * Didn't find skip match, then create/insert a new one
                 *  if without the repeat key, then prepare the next repeat key automatically for repeat event
                 */                
                int ordinal = 1;
                try {
                    ordinal = new Integer(studyEventDataBean.getStudyEventRepeatKey()).intValue();
                } catch (Exception e) {
                    // trying to catch NPEs, because tags can be without the repeat key
                	
                	
                }
                StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
                        sedBean.getId(), ordinal);

                if(studyEvent == null || studyEvent.getId() == 0) {
                	 StudyEventBean tempStudyEventBean = new StudyEventBean();
                     Date today = new Date();
                     tempStudyEventBean.setCreatedDate(today);
                     tempStudyEventBean.setDateStarted(today);
                     tempStudyEventBean.setName(sedBean.getName());
                     tempStudyEventBean.setOwner(ub);
                     ArrayList eventCRFs = new ArrayList<>();                    
                     tempStudyEventBean.setEventCRFs(eventCRFs);
                     tempStudyEventBean.setOwnerId(ub.getId());
                     tempStudyEventBean.setStudySubject(studySubjectBean);
                     tempStudyEventBean.setUpdater(ub);
                     tempStudyEventBean.setUpdatedDate(today);
                     tempStudyEventBean.setStudySubjectId(studySubjectBean.getId());
                     tempStudyEventBean.setSubjectEventStatus(SubjectEventStatus.DATA_ENTRY_STARTED);
                     tempStudyEventBean.setStatus(Status.AVAILABLE);
                     tempStudyEventBean.setStudyEventDefinitionId(sedBean.getId());
                     tempStudyEventBean.setSampleOrdinal(ordinal);                     
                   
                     studyEvent = tempStudyEventBean;                                               
                    
                }
                displayItemBeans = new ArrayList<DisplayItemBean>();

                for (FormDataBean formDataBean : formDataBeans) {
                    Map<String,Integer> groupMaxOrdinals = new HashMap<String,Integer>();
                    displayItemBeanWrapper = null;
                    CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
                    EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);
                    ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByFormOid(formDataBean.getFormOID());
                    ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();

                    if ((crfVersionBeans == null) || (crfVersionBeans.size() == 0)) {
                        MessageFormat mf = new MessageFormat("");
                        mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
                        Object[] arguments = { formDataBean.getFormOID() };

                        throw new OpenClinicaException(mf.format(arguments), "");
                    }
                    CRFVersionBean crfVersion = crfVersionBeans.get(0);
                    // if you have a mispelled form oid you get an error here
                    // need to error out gracefully and post an error
                    logger.debug("iterating through form beans: found " + crfVersion.getOid());
                    // may be the point where we cut off item groups etc and
                    // instead work on sections
                    
                    EventCRFBean eventCRFBean = eventCRFDAO.findByEventCrfVersion(studyEvent, crfVersion);
                    
                    if(eventCRFBean == null && studyEvent.getId()==0) {
                    	ArrayList<FormLayoutBean> formLayoutBeans = getFormLayoutBeans(formDataBean, ds);
                    	FormLayoutBean formLayoutBean = (FormLayoutBean)formLayoutBeans.get(0);                    	
                    	eventCRFBean = buildEventCrfBean(studySubjectBean, studyEvent, formLayoutBean, crfVersion, ub);
                    }
                    
                    EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(ds);
                    EventDefinitionCRFBean eventDefinitionCRF = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(studyBean, studyEvent.getId(),
                            crfVersion.getId());
                    if (eventCRFBean != null) {
                        if (permittedEventCRFIds.contains(new Integer(eventCRFBean.getId()))) {
                            
                            for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                                groupMaxOrdinals.put(itemGroupDataBean.getItemGroupOID(),1);
                            }
                            // if and only if it's in the correct status do we need
                            // to generate the beans
                            // <<tbh, 09/2008
                            // also need to create a group name checker here for
                            // correctness, tbh
                            for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
                                ArrayList<ItemBean> blankCheckItems = new ArrayList<ItemBean>();
                                ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
                                logger.debug("iterating through group beans: " + itemGroupDataBean.getItemGroupOID());
                                // put a checker in here
                                ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
                                ItemGroupBean testBean = itemGroupDAO.findByOid(itemGroupDataBean.getItemGroupOID());
                                if (testBean == null) {
                                    // TODO i18n of message
                                    // logger.debug("hit the exception for item groups! " +
                                    // itemGroupDataBean.getItemGroupOID());
                                    MessageFormat mf = new MessageFormat("");
                                    mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
                                    Object[] arguments = { itemGroupDataBean.getItemGroupOID(), formDataBean.getFormOID() };

                                    throw new OpenClinicaException(mf.format(arguments), "");
                                }
                                totalItemDataBeanCount += itemDataBeans.size();

                                for (ImportItemDataBean importItemDataBean : itemDataBeans) {
                                    logger.debug("   iterating through item data beans: " + importItemDataBean.getItemOID());
                                    ItemDAO itemDAO = new ItemDAO(ds);
                                    ItemFormMetadataDAO itemFormMetadataDAO = new ItemFormMetadataDAO(ds);

                                    List<ItemBean> itemBeans = itemDAO.findByOid(importItemDataBean.getItemOID());
                                    if (!itemBeans.isEmpty()) {
                                        ItemBean itemBean = itemBeans.get(0);
                                        logger.debug("   found " + itemBean.getName());
                                        // throw a null pointer? hopefully not if its been checked...
                                        DisplayItemBean displayItemBean = new DisplayItemBean();
                                        displayItemBean.setItem(itemBean);

                                        ArrayList<ItemFormMetadataBean> metadataBeans = itemFormMetadataDAO.findAllByItemId(itemBean.getId());
                                        logger.debug("      found metadata item beans: " + metadataBeans.size());
                                        // groupOrdinal = the ordinal in item groups, for repeating items
                                        int groupOrdinal = 1;
                                        if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
                                            try {
                                                groupOrdinal = new Integer(itemGroupDataBean.getItemGroupRepeatKey()).intValue();
                                                if (groupOrdinal > groupMaxOrdinals.get(itemGroupDataBean.getItemGroupOID())) {
                                                    groupMaxOrdinals.put(itemGroupDataBean.getItemGroupOID(),groupOrdinal);
                                                }
                                            } catch (Exception e) {
                                                // do nothing here currently, we are
                                                // looking for a number format
                                                // exception
                                                // from the above.
                                                logger.debug("found npe for group ordinals, line 344!");
                                            }
                                        }
                                        ItemDataBean itemDataBean = createItemDataBean(itemBean, eventCRFBean, importItemDataBean.getValue(), ub, groupOrdinal);
                                        blankCheckItems.add(itemBean);
                                        String newKey = groupOrdinal + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
                                                + subjectDataBean.getSubjectOID();
                                        blankCheck.put(newKey, itemDataBean);
                                        logger.info("adding " + newKey + " to blank checks");
                                        if (!metadataBeans.isEmpty()) {
                                            ItemFormMetadataBean metadataBean = metadataBeans.get(0);
                                            // also
                                            // possible
                                            // nullpointer

                                            displayItemBean.setData(itemDataBean);
                                            displayItemBean.setMetadata(metadataBean);
                                            // set event def crf?
                                            displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
                                            String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                                            // if you do indeed leave off this in the XML it will pass but return 'null'
                                            // tbh
                                            attachValidator(displayItemBean, importHelper, discValidator, hardValidator, request, eventCRFRepeatKey,
                                                    studySubjectBean.getOid(), respage);
                                            displayItemBeans.add(displayItemBean);

                                        } else {
                                            MessageFormat mf = new MessageFormat("");
                                            mf.applyPattern(respage.getString("no_metadata_could_be_found"));
                                            Object[] arguments = { importItemDataBean.getItemOID() };

                                            throw new OpenClinicaException(mf.format(arguments), "");
                                        }
                                    } else {
                                        // report the error there
                                        MessageFormat mf = new MessageFormat("");
                                        mf.applyPattern(respage.getString("no_item_could_be_found"));
                                        Object[] arguments = { importItemDataBean.getItemOID() };

                                        throw new OpenClinicaException(mf.format(arguments), "");
                                    }
                                }// end item data beans
                                logger.debug(".. found blank check: " + blankCheck.toString());
                                // >> TBH #5548 did we create any repeated blank spots? If so, fill them in with an Item
                                // Data Bean

                                // JN: this max Ordinal can cause problems as it assumes the latest values, and when
                                // there
                                // is an entry with nongroup towards the
                                // end. Refer: MANTIS 6113

                                for (int i = 1; i <= groupMaxOrdinals.get(itemGroupDataBean.getItemGroupOID()); i++) {
                                    for (ItemBean itemBean : blankCheckItems) {
                                        String newKey = i + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
                                                + subjectDataBean.getSubjectOID();
                                        if (blankCheck.get(newKey) == null) {
                                            // if it already exists, Do Not Add It.
                                            ItemDataBean itemDataCheck = getItemDataDao().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(),
                                                    eventCRFBean.getId(), i);
                                            logger.debug("found item data bean id: " + itemDataCheck.getId() + " for ordinal " + i);
                                            if (itemDataCheck.getId() == 0) {
                                                ItemDataBean blank = createItemDataBean(itemBean, eventCRFBean, "", ub, i);
                                                DisplayItemBean displayItemBean = new DisplayItemBean();
                                                displayItemBean.setItem(itemBean);
                                                displayItemBean.setData(blank);
                                                // displayItemBean.setMetadata(metadataBean);
                                                // set event def crf?
                                                displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
                                                String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
                                                // if you do indeed leave off this in the XML it will pass but return
                                                // 'null'

                                                displayItemBeans.add(displayItemBean);
                                                logger.debug("... adding display item bean");
                                            }
                                        }
                                        logger.debug("found a blank at " + i + ", adding " + blankCheckItems.size() + " blank items");
                                    }
                                }
                                blankCheckItems = new ArrayList<ItemBean>();
                            }// end item group data beans

                        }// matches if on permittedCRFIDs

                        CRFDAO crfDAO = new CRFDAO(ds);
                        CRFBean crfBean = crfDAO.findByVersionId(crfVersion.getCrfId());
                        // seems like an extravagance, but is not contained in crf
                        // version or event crf bean
                        validationErrors = discValidator.validate();
                        // totalValidationErrors += validationErrors.size();
                        // totalValidationErrors.
                        // totalValidationErrors.addAll(validationErrors);
                        for (Object errorKey : validationErrors.keySet()) {
                            // logger.debug(errorKey.toString() + " -- " + validationErrors.get(errorKey));
                            // JN: to avoid duplicate errors
                            if (!totalValidationErrors.containsKey(errorKey.toString()))
                                totalValidationErrors.put(errorKey.toString(), validationErrors.get(errorKey).toString());
                            // assuming that this will be put back in to the core
                            // method's hashmap, updating statically, tbh 06/2008
                            logger.debug("+++ adding " + errorKey.toString());
                        }
                        logger.debug("-- hard validation checks: --");
                        for (Object errorKey : hardValidator.keySet()) {
                            logger.debug(errorKey.toString() + " -- " + hardValidator.get(errorKey));
                            hardValidationErrors.put(errorKey.toString(), hardValidator.get(errorKey));
                            // updating here 'statically' tbh 06/2008
                            hardValidatorErrorMsgs += hardValidator.get(errorKey) + "<br/><br/>";
                        }

                        String studyEventId = studyEvent.getId() + "";
                        String crfVersionId = crfVersion.getId() + "";

                        logger.debug("creation of wrapper: original count of display item beans " + displayItemBeans.size() + ", count of item data beans "
                                + totalItemDataBeanCount + " count of validation errors " + validationErrors.size() + " count of study subjects "
                                + subjectDataBeans.size() + " count of event crfs " + totalEventCRFCount + " count of hard error checks "
                                + hardValidator.size());
                        // check if we need to overwrite
                        DataEntryStage dataEntryStage = eventCRFBean.getStage();
                        Status eventCRFStatus = eventCRFBean.getStatus();
                        boolean overwrite = false;
                        // tbh >>
                        // //JN: Commenting out the following 2 lines, coz the prompt should come in the cases on
                        if (// eventCRFStatus.equals(Status.UNAVAILABLE) ||
                        dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                                || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                            overwrite = true;
                        }
                        // << tbh, adding extra statuses to prevent appending, 06/2009

                        // SummaryStatsBean ssBean = new SummaryStatsBean();
                        // ssBean.setDiscNoteCount(totalValidationErrors);
                        // ssBean.setEventCrfCount(totalEventCRFCount);
                        // ssBean.setStudySubjectCount(subjectDataBeans.size());
                        // // add other stats here, tbh
                        // not working here, need to do it in a different method,
                        // tbh

                        // summary stats added tbh 05/2008
                        // JN: Changed from validationErrors to totalValidationErrors to create discrepancy notes for
                        // all
                        // the
                        displayItemBeanWrapper = new DisplayItemBeanWrapper(displayItemBeans, true, overwrite, validationErrors, studyEventId, crfVersionId,
                                studyEventDataBean.getStudyEventOID(), studySubjectBean.getLabel(), eventCRFBean.getCreatedDate(), crfBean.getName(),
                                crfVersion.getName(), studySubjectBean.getOid(), studyEventDataBean.getStudyEventRepeatKey(), eventCRFBean);

                        // JN: Commenting out the following code, since we shouldn't re-initialize at this point, as
                        // validationErrors would get overwritten and the
                        // older errors will be overriden. Moving it after the form.
                        // Removing the comments for now, since it seems to be creating duplicate Discrepancy Notes.
                        validationErrors = new HashMap();
                        discValidator = new DiscrepancyValidator(request, discNotes);
                        // reset to allow for new errors...
                    }
                }// after forms
                 // validationErrors = new HashMap();
                 // discValidator = new DiscrepancyValidator(request, discNotes);
                if (displayItemBeanWrapper != null && displayItemBeans.size() > 0)
                    wrappers.add(displayItemBeanWrapper);
            }// after study events

            // remove repeats here? remove them below by only forwarding the
            // first
            // each wrapper represents an Event CRF and a Form, but we don't
            // have all events for all forms
            // need to not add a wrapper for every event + form combination,
            // but instead for every event + form combination which is present
            // look at the hack below and see what happens
        }// after study subjects

        // throw the OC exception here at the end, if hard edit checks are not
        // empty

        // we statically update the hash map here, so it should not have to be
        // thrown, tbh 06/2008
        if (!hardValidator.isEmpty()) {
             throw new OpenClinicaException(hardValidatorErrorMsgs, "");
        }
        return wrappers;
    }
    
    /**
     * if (sampleOrdinal = existingMaxOrdinal + 1)
		// this is valid
		// Insert logic goes here ..... 
		if (sampleOrdinal == null)
			// Insert logic goes and deduce sampleOrdinal == existingMaxOrdinal + 1
		if (sampleOrdinal = existingMaxOrdinal)
			// Update logic goes here ....
		if (sampleOrdinal != existingMaxOrdinal && sampleOrdinal != existingMaxOrdinal +1)
			// Reject this 
		 * 
		 * 
		 * Note: 
		 * the above sampleOrdinal will have relationship with the value of StudyEventRepeatKey in xml file		 
		 * 
		 * String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean.getStudyEventRepeatKey();

     * @return
     */
    public boolean passOrdinalLogicCheck(int sampleOrdinal,int existingMaxOrdinal){
		boolean passCheck = true;
	
		// Reject this
		if (sampleOrdinal > existingMaxOrdinal && sampleOrdinal != existingMaxOrdinal +1) {
			passCheck = false;
		}
    		
    	return passCheck;	
    }
    
    /**
     * when skipMatchCriteria.equals("${SkipMatchCriteria}", this means in the mapping file, there is no 
     * skipMatchCriteria configuration
     * 
     * Sample sql:
     *  select i.oc_oid, id.value 
            from  s_study3prod.study_subject ss, 
		    s_study3prod.study s,
		    s_study3prod.item_data id,
			s_study3prod.item i, 
			s_study3prod.item_group fg, 
			s_study3prod.item_group_metadata fgim,
		    s_study3prod.event_crf ec,
		    s_study3prod.study_event se,
		    s_study3prod.study_event_definition sed
            where 
            ss.study_id=s.study_id
            and sed.study_id= ss.study_id
            and se.study_event_definition_id=sed.study_event_definition_id
            and ec.study_event_id=se.study_event_id
            and ec.study_subject_id=ss.study_subject_id
            and id.event_crf_id=ec.event_crf_id
		and fg.item_group_id=fgim.item_group_id
		and fgim.item_id=i.item_id
		and id.item_id=i.item_id
		and s.oc_oid='S_STUDY3(PROD)'
		and ss.oc_oid='SS_SUB1'
            and sed.oc_oid='SE_SCREENVISISTCHECKHISTORY'
         --    and fg.oc_oid='IG_MEDIC_RG1'
		--and (i.oc_oid = 'I_MEDIC_DIAGNOSIS' OR i.oc_oid= 'I_MEDIC_ONGOING')
		and fg.oc_oid in('IG_MEDIC_RG1','IG_MEDIC_RG2')
		and i.oc_oid in ( 'I_MEDIC_DIAGNOSIS','I_MEDIC_ONGOING')
		
     * @param request
     * @param subjectOID
     * @param eventOID
     * @return
     * @throws OpenClinicaException
     */
    
    public String buildSkipMatchCriteriaSql(HttpServletRequest request,  String studyOID,String subjectOID, String studyEventOID)
    		throws OpenClinicaException {
    	String finalSqlStr = null;
    	String baseSqlStr =" select se.study_event_id,i.oc_oid, id.value \r\n" + 
    			"            from  study_subject ss, \r\n" + 
    			"		    study s,\r\n" + 
    			"		    item_data id,\r\n" + 
    			"			item i, \r\n" + 
    			"			item_group fg, \r\n" + 
    			"			item_group_metadata fgim,\r\n" + 
    			"		    event_crf ec,\r\n" + 
    			"		    study_event se,\r\n" + 
    			"		    study_event_definition sed\r\n" + 
    			"            where \r\n" + 
    			"            ss.study_id=s.study_id\r\n" + 
    			"            and sed.study_id= ss.study_id\r\n" + 
    			"            and se.study_event_definition_id=sed.study_event_definition_id\r\n" + 
    			"            and ec.study_event_id=se.study_event_id\r\n" + 
    			"            and ec.study_subject_id=ss.study_subject_id\r\n" + 
    			"            and id.event_crf_id=ec.event_crf_id\r\n" + 
    			"		and fg.item_group_id=fgim.item_group_id\r\n" + 
    			"		and fgim.item_id=i.item_id\r\n" + 
    			"		and id.item_id=i.item_id\r\n";
    	
    	if(studyOID != null) {
    		baseSqlStr = baseSqlStr + " " + "and s.oc_oid='"+ studyOID +"'";
    	}
    	if(subjectOID != null) {
    		baseSqlStr = baseSqlStr + " " + "and ss.oc_oid='"+ subjectOID +"'";
    	}
    	if(studyEventOID != null) {
    		baseSqlStr = baseSqlStr + " " + "and sed.oc_oid='"+ studyEventOID +"'";
    	}
    	
    	String[] skipMatchCriteriaArray = null;
    	
    	String skipMatchCriteria = request.getHeader("SkipMatchCriteria");
    	if(skipMatchCriteria != null && skipMatchCriteria.trim().length() > 0 && !(skipMatchCriteria.equals("${SkipMatchCriteria}"))) {
    		skipMatchCriteriaArray = this.toArray(skipMatchCriteria, ",");
    		
    		String itemGroupOID;
        	String itemOID;
        	StringBuffer itemGroupOIDSmt = new StringBuffer(" and fg.oc_oid in(");
        	StringBuffer itemOIDSmt = new StringBuffer(" and i.oc_oid in (");
        	
        	for(int  i= 0; i< skipMatchCriteriaArray.length; i++) {
        		int k = skipMatchCriteriaArray[i].indexOf(".");
        		itemGroupOID = skipMatchCriteriaArray[i].substring(0, k);
        		itemOID =  skipMatchCriteriaArray[i].substring(k+1, skipMatchCriteriaArray[i].length()).trim();
        		
        		itemGroupOIDSmt.append("'" + itemGroupOID + "',");
        		itemOIDSmt.append("'" + itemOID + "',");
        		
        		
        	}
        	
        	itemGroupOIDSmt.append("'')");
    		itemOIDSmt.append("'')");
    		
    		finalSqlStr = baseSqlStr + " " + itemGroupOIDSmt.toString() + " "+ itemOIDSmt.toString() + " order by se.study_event_id";
    	}
		
    	logger.info("buildSkipMatchCriteriaSql============"+ finalSqlStr);
		return finalSqlStr;
    	
    }
    
    private String[] toArray(String columnNmsStr,String delemiterStr) {
		StringTokenizer st = new StringTokenizer(columnNmsStr, delemiterStr);
		int size =st.countTokens();
		String[] columnNms = new String[size];

		int i=0;
		while (st.hasMoreElements()) {
			String e =st.nextElement().toString();
			columnNms[i]=e;
			i++;
			//System.out.println(e);
			
		}
		return columnNms;
	}
    
    /**
     *  if return matched = true, then need to skip
     *  
     * @param matchCriterias
     * @param formDataBeans
     * @return
     */
    private boolean needToSkip(ArrayList matchCriterias,ArrayList<FormDataBean> formDataBeans) {
    	boolean matched = true;    	
	    String itemOID;
	    String itemValue;
	    
	    String itemOIDinODMxml;
	    String itemValueinODMxml;
	    
	    if(matchCriterias == null || matchCriterias.size() == 0) {
	    	return false;
	    }
 	    
	    Iterator it = matchCriterias.iterator();
	    
	    while(it.hasNext()) {
	    	HashMap rowhm =(HashMap) it.next();
	    	 
		    Iterator keyIt = rowhm.keySet().iterator();
		    boolean[] matchCheck = new boolean[rowhm.size()];
		    
		    int i = 0;
		    while(keyIt.hasNext()) {
		    	itemOID = (String) keyIt.next();
		    	itemValue = (String) rowhm.get(itemOID);
		    	
		    	
		    	for (FormDataBean formDataBean : formDataBeans) {
		    		
		    		 ArrayList<ImportItemGroupDataBean> importItemGroupDataBeans= formDataBean.getItemGroupData();
		    		 for(ImportItemGroupDataBean importItemGroupDataBean:importItemGroupDataBeans) {
		    			 
		    			 ArrayList<ImportItemDataBean> importItemDataBeans=importItemGroupDataBean.getItemData();
		    			 for(ImportItemDataBean importItemDataBean:importItemDataBeans) {
		    				 itemOIDinODMxml = importItemDataBean.getItemOID();
		    				 itemValueinODMxml = importItemDataBean.getValue();
		    				 
		    				 // find the itemOID first, then compare the value
		    				 if(itemOID.equals(itemOIDinODMxml) ) {
		    					 if(itemValue.equals(itemValueinODMxml)){
		    						 matchCheck[i] = true;
		    						 break;
		    					 }else {
		    						 matchCheck[i] = false;
		    					 }
		    				 }
		    				 
		    			 }
		    			 
		    			
		    		 }
		    		 
		    		
		    	 }
		    	
		    	i++;
		    	
		    }// inner-while loop
		    
		    
		    //checked each item value match , then match
		    matched = true;
	    	for(int k=0; k < matchCheck.length;k++) {	    		
	    		matched = matched && matchCheck[k];	    			    		 
	    	}
	    	
	    	if(matched) {
	    		break;
	    	}
    			    
	    }//outer- while loop
	    logger.info(matched+"matchCriterias============"+ matchCriterias);
	    
	    return matched;
	    }

	public String getSkipMatchCriteriaSql() {
		return skipMatchCriteriaSql;
	}

	public void setSkipMatchCriteriaSql(String skipMatchCriteriaSql) {
		this.skipMatchCriteriaSql = skipMatchCriteriaSql;
	}

	public PipeDelimitedDataHelper getPipeDelimitedDataHelper() {
		if(pipeDelimitedDataHelper == null) {
			pipeDelimitedDataHelper = new PipeDelimitedDataHelper();
		}
		return pipeDelimitedDataHelper;
	}

	public void setPipeDelimitedDataHelper(PipeDelimitedDataHelper importDataHelper) {
		this.pipeDelimitedDataHelper = importDataHelper;
	}

	public RestfulServiceHelper getRestfulServiceHelper() {
		if(restfulServiceHelper == null) {
			restfulServiceHelper = new RestfulServiceHelper(ds);
		}
		return restfulServiceHelper;
	}

	public void setRestfulServiceHelper(RestfulServiceHelper restfulServiceHelper) {
		this.restfulServiceHelper = restfulServiceHelper;
	}
	
	public void validateUserRole(ODMContainer odmContainer,HttpServletRequest request) throws OpenClinicaException {
      
        List<String> errors = new ArrayList<String>();
        MessageFormat mf = new MessageFormat("");

  
        StudyDAO studyDAO = new StudyDAO(ds);
        String studyOid = odmContainer.getCrfDataPostImportContainer().getStudyOID();
        StudyBean studyBean = studyDAO.findByOid(studyOid);
        UserAccountBean userBean = this.getPipeDelimitedDataHelper().getUserAccount(request);
        String userName=  userBean.getName();  
        
        String msg = this.getRestfulServiceHelper().verifyRole(userName, studyOid, null);
        if (msg != null) {           
            throw new OpenClinicaException(msg, msg);
        }              
    }
	

	public ArrayList<StudyEventBean> creatStudyEvent(ArrayList<StudyEventBean> studyEventBeans) {
		ArrayList<StudyEventBean> studyEventBeanCreatedList = new ArrayList<>();
		StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
		
		for(StudyEventBean tempStudyEventBean:studyEventBeans) {
			StudyEventBean e = (StudyEventBean) studyEventDAO.create(tempStudyEventBean);
			
			ArrayList<EventCRFBean> tempEventCRFBeans = new ArrayList<>();
			ArrayList<EventCRFBean> eventCRFBeans = tempStudyEventBean.getEventCRFs();
			// update study event information
			for(EventCRFBean eventCRFBean:eventCRFBeans) {
				if(eventCRFBean != null) {
					eventCRFBean.setStudyEvent(e);
					eventCRFBean.setStudyEventId(e.getId());
					tempEventCRFBeans.add(eventCRFBean);
				}
				
			}
			
			ArrayList<EventCRFBean> newEventCRFBeans = creatEventCRFBeans(tempEventCRFBeans);
			e.setEventCRFs(newEventCRFBeans);
			if(e != null) {
				studyEventBeanCreatedList.add(e);
			}
			
		}				
		return studyEventBeanCreatedList;
	}
	
	public StudyEventBean creatStudyEvent(StudyEventBean tempStudyEventBean) {
		StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
		StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO.create(tempStudyEventBean);
		
		ArrayList<EventCRFBean> eventCRFBeans = tempStudyEventBean.getEventCRFs();
		creatEventCRFBeans(eventCRFBeans);
		
		return studyEventBean;
	}
	
	public ArrayList<EventCRFBean> creatEventCRFBeans(ArrayList<EventCRFBean> eventCRFBeans) {
		ArrayList<EventCRFBean> eventCRFBeansCreated = new  ArrayList<>();
		EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
		
		for(EventCRFBean newEventCrfBean:eventCRFBeans) {
			if(newEventCrfBean != null) {
				EventCRFBean newCreatedEventCrfBean = (EventCRFBean) eventCrfDAO.create(newEventCrfBean);
				eventCRFBeansCreated.add(newCreatedEventCrfBean);
			}
			
		}
		
		
		return eventCRFBeansCreated;
	}
	
	 public List<DisplayItemBeanWrapper> getDisplayItemBeanWrappers(HttpServletRequest request, ODMContainer odmContainer, UserAccountBean ub,
             ArrayList<Integer> permittedEventCRFIds, Locale locale)
		throws OpenClinicaException {
		
		ResourceBundleProvider.updateLocale(locale);
		ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle(locale);
		
		DisplayItemBeanWrapper displayItemBeanWrapper = null;
		HashMap validationErrors = new HashMap();
		List<DisplayItemBeanWrapper> wrappers = new ArrayList<DisplayItemBeanWrapper>();
		ImportHelper importHelper = new ImportHelper();
		FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
		DiscrepancyValidator discValidator = new DiscrepancyValidator(request, discNotes);
		
		HashMap<String, String> hardValidator = new HashMap<String, String>();
		
		StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
		StudyDAO studyDAO = new StudyDAO(ds);
		StudyBean studyBean = studyDAO.findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
		StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(ds);
		HashMap<String, ItemDataBean> blankCheck = new HashMap<String, ItemDataBean>();
		String hardValidatorErrorMsgs = "";
		
		ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
		int totalEventCRFCount = 0;
		int totalItemDataBeanCount = 0;
		// int totalValidationErrors = 0;
		for (SubjectDataBean subjectDataBean : subjectDataBeans) {
		ArrayList<DisplayItemBean> displayItemBeans = new ArrayList<DisplayItemBean>();
		logger.debug("iterating through subject data beans: found " + subjectDataBean.getSubjectOID());
		ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
		totalEventCRFCount += studyEventDataBeans.size();
		
		// ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
		// to stop repeats...?
		StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(), studyBean.getId());
		
		for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
		int parentStudyId = studyBean.getParentStudyId();
		StudyEventDefinitionBean sedBean = sedDao.findByOidAndStudy(studyEventDataBean.getStudyEventOID(), studyBean.getId(), parentStudyId);
		ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
		logger.debug("iterating through study event data beans: found " + studyEventDataBean.getStudyEventOID());
	                
		int ordinal = 1;
		try {
			ordinal = new Integer(studyEventDataBean.getStudyEventRepeatKey()).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
		StudyEventBean studyEvent = (StudyEventBean) studyEventDAO.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
		sedBean.getId(), ordinal);
			
		displayItemBeans = new ArrayList<DisplayItemBean>();
		
		for (FormDataBean formDataBean : formDataBeans) {
			Map<String,Integer> groupMaxOrdinals = new HashMap<String,Integer>();
			displayItemBeanWrapper = null;
			CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
			EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);
			ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByFormOid(formDataBean.getFormOID());
			ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
			
			if ((crfVersionBeans == null) || (crfVersionBeans.size() == 0)) {
				MessageFormat mf = new MessageFormat("");
				mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
				Object[] arguments = { formDataBean.getFormOID() };
				
				throw new OpenClinicaException(mf.format(arguments), "");
			}
			CRFVersionBean crfVersion = crfVersionBeans.get(0);
			
			EventCRFBean eventCRFBean = eventCRFDAO.findByEventCrfVersion(studyEvent, crfVersion);
				
			EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(ds);
			EventDefinitionCRFBean eventDefinitionCRF = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(studyBean, studyEvent.getId(),
			crfVersion.getId());
			if (eventCRFBean != null) {
				if (permittedEventCRFIds.contains(new Integer(eventCRFBean.getId()))) {
					
					for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
						groupMaxOrdinals.put(itemGroupDataBean.getItemGroupOID(),1);
					}
					
						for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
						ArrayList<ItemBean> blankCheckItems = new ArrayList<ItemBean>();
						ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
						logger.debug("iterating through group beans: " + itemGroupDataBean.getItemGroupOID());
						// put a checker in here
						ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
						ItemGroupBean testBean = itemGroupDAO.findByOid(itemGroupDataBean.getItemGroupOID());
						if (testBean == null) {
						// TODO i18n of message
						// logger.debug("hit the exception for item groups! " +
						// itemGroupDataBean.getItemGroupOID());
						MessageFormat mf = new MessageFormat("");
						mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
						Object[] arguments = { itemGroupDataBean.getItemGroupOID(), formDataBean.getFormOID() };
						
						throw new OpenClinicaException(mf.format(arguments), "");
						}
						totalItemDataBeanCount += itemDataBeans.size();
						
						for (ImportItemDataBean importItemDataBean : itemDataBeans) {
						logger.debug("   iterating through item data beans: " + importItemDataBean.getItemOID());
						ItemDAO itemDAO = new ItemDAO(ds);
						ItemFormMetadataDAO itemFormMetadataDAO = new ItemFormMetadataDAO(ds);
						
						List<ItemBean> itemBeans = itemDAO.findByOid(importItemDataBean.getItemOID());
						if (!itemBeans.isEmpty()) {
						ItemBean itemBean = itemBeans.get(0);
						logger.debug("   found " + itemBean.getName());
						// throw a null pointer? hopefully not if its been checked...
						DisplayItemBean displayItemBean = new DisplayItemBean();
						displayItemBean.setItem(itemBean);
						
						ArrayList<ItemFormMetadataBean> metadataBeans = itemFormMetadataDAO.findAllByItemId(itemBean.getId());
						logger.debug("      found metadata item beans: " + metadataBeans.size());
						// groupOrdinal = the ordinal in item groups, for repeating items
						int groupOrdinal = 1;
						if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
							try {
								groupOrdinal = new Integer(itemGroupDataBean.getItemGroupRepeatKey()).intValue();
								if (groupOrdinal > groupMaxOrdinals.get(itemGroupDataBean.getItemGroupOID())) {
								  groupMaxOrdinals.put(itemGroupDataBean.getItemGroupOID(),groupOrdinal);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						ItemDataBean itemDataBean = createItemDataBean(itemBean, eventCRFBean, importItemDataBean.getValue(), ub, groupOrdinal);
						blankCheckItems.add(itemBean);
						String newKey = groupOrdinal + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
						+ subjectDataBean.getSubjectOID();
						blankCheck.put(newKey, itemDataBean);
						logger.info("adding " + newKey + " to blank checks");
						if (!metadataBeans.isEmpty()) {
							ItemFormMetadataBean metadataBean = metadataBeans.get(0);
						
							displayItemBean.setData(itemDataBean);
							displayItemBean.setMetadata(metadataBean);
							// set event def crf?
							displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);							
							displayItemBeans.add(displayItemBean);
							
						} else {
							MessageFormat mf = new MessageFormat("");
							mf.applyPattern(respage.getString("no_metadata_could_be_found"));
							Object[] arguments = { importItemDataBean.getItemOID() };
							
							throw new OpenClinicaException(mf.format(arguments), "");
							}
						} else {
							// report the error there
							MessageFormat mf = new MessageFormat("");
							mf.applyPattern(respage.getString("no_item_could_be_found"));
							Object[] arguments = { importItemDataBean.getItemOID() };
							
							throw new OpenClinicaException(mf.format(arguments), "");
						}
						}// end item data beans
										
						for (int i = 1; i <= groupMaxOrdinals.get(itemGroupDataBean.getItemGroupOID()); i++) {
							for (ItemBean itemBean : blankCheckItems) {
								String newKey = i + "_" + itemGroupDataBean.getItemGroupOID() + "_" + itemBean.getOid() + "_"
								+ subjectDataBean.getSubjectOID();
								if (blankCheck.get(newKey) == null) {
									// if it already exists, Do Not Add It.
									ItemDataBean itemDataCheck = getItemDataDao().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(),
									  eventCRFBean.getId(), i);
									logger.debug("found item data bean id: " + itemDataCheck.getId() + " for ordinal " + i);
									if (itemDataCheck.getId() == 0) {
										ItemDataBean blank = createItemDataBean(itemBean, eventCRFBean, "", ub, i);
										DisplayItemBean displayItemBean = new DisplayItemBean();
										displayItemBean.setItem(itemBean);
										displayItemBean.setData(blank);
									
										displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
										String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
										// if you do indeed leave off this in the XML it will pass but return
										// 'null'
										
										displayItemBeans.add(displayItemBean);
										logger.debug("... adding display item bean");
										}
								}
								logger.debug("found a blank at " + i + ", adding " + blankCheckItems.size() + " blank items");
							}
						}
						blankCheckItems = new ArrayList<ItemBean>();
					}// end item group data beans
					
					}// matches if on permittedCRFIDs
			
			CRFDAO crfDAO = new CRFDAO(ds);
			CRFBean crfBean = crfDAO.findByVersionId(crfVersion.getCrfId());
			
			String studyEventId = studyEvent.getId() + "";
			String crfVersionId = crfVersion.getId() + "";
			
			logger.debug("creation of wrapper: original count of display item beans " + displayItemBeans.size() + ", count of item data beans "
			+ totalItemDataBeanCount + " count of validation errors " + validationErrors.size() + " count of study subjects "
			+ subjectDataBeans.size() + " count of event crfs " + totalEventCRFCount + " count of hard error checks "
			+ hardValidator.size());
			// check if we need to overwrite
			DataEntryStage dataEntryStage = eventCRFBean.getStage();
			Status eventCRFStatus = eventCRFBean.getStatus();
			boolean overwrite = false;
			
			if (dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
			|| dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
				overwrite = true;
				}
			
			displayItemBeanWrapper = new DisplayItemBeanWrapper(displayItemBeans, true, overwrite, validationErrors, studyEventId, crfVersionId,
			studyEventDataBean.getStudyEventOID(), studySubjectBean.getLabel(), eventCRFBean.getCreatedDate(), crfBean.getName(),
			crfVersion.getName(), studySubjectBean.getOid(), studyEventDataBean.getStudyEventRepeatKey(), eventCRFBean);
			
			}
			}// after forms
		
			if (displayItemBeanWrapper != null && displayItemBeans.size() > 0)
				wrappers.add(displayItemBeanWrapper);
		}// after study events
		
		
		}// after study subjects
		
	
		return wrappers;
	}

}
