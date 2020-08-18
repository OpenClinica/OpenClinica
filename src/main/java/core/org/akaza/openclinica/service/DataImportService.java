package core.org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import core.org.akaza.openclinica.bean.submit.DisplayItemBean;
import core.org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import core.org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import core.org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import core.org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import core.org.akaza.openclinica.web.job.TriggerService;

import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import core.org.akaza.openclinica.web.crfdata.ImportCRFDataService;

/**
 * 
 * @author thickerson, daniel
 * 
 */
@Service("DataImportService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class DataImportService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    ResourceBundle respage;

    public ResourceBundle getRespage() {
        return respage;
    }

    public void setRespage(ResourceBundle respage) {
        this.respage = respage;
    }

    Locale locales;

    @Autowired
    private StudyDao studyDao;
    public DataImportService(StudyDao studyDao) {
		super();
		this.studyDao = studyDao;
	}

	@Autowired
    private ImportCRFDataService importCRFDataService;

    public Locale getLocale() {
        if (locales == null)
            locales = new Locale("en-US");
        return locales;
    }

    public void setLocale(Locale locale) {
        if (locale == null)
            locale = new Locale("en-us");
        this.locales = locale;
    }

    private ImportCRFDataService dataService;

    public List<String> validateMetaData(ODMContainer odmContainer, DataSource dataSource, CoreResources resources, Study studyBean,
            UserAccountBean userBean, List<DisplayItemBeanWrapper> displayItemBeanWrappers, HashMap<Integer, String> importedCRFStatuses) {

        logger.debug("passing an odm container and study bean id: " + studyBean.getStudyId());
        List<String> errors = getImportCRFDataService().validateStudyMetadata(odmContainer, studyBean.getStudyId(),getLocale() );

        if (errors == null)
            return new ArrayList<String>();
        else
            return errors;

    }

    /**
     * Import Data, the logic which imports the data for our data service. Note that we will return three strings string
     * 0: status, either 'success', 'fail', or 'warn'. string 1: the message string which will be returned in our soap
     * response string 2: the audit message, currently not used but will be saved in the event of a success.
     * 
     * import consist from 3 steps 1) parse xml and extract data 2) validation 3) data submission
     * 
     * @author thickerson
     * @param dataSource
     * @param resources
     * @param studyBean
     * @param userBean
     * @param displayItemBeanWrappers
     * @param importedCRFStatuses
     * @return
     * @throws Exception
     * 
     *             /* VALIDATE data on all levels
     * 
     *             msg - contains status messages
     * @return list of errors
     */
    public HashMap validateData(ODMContainer odmContainer, DataSource dataSource, CoreResources resources, Study studyBean, UserAccountBean userBean,
            List<DisplayItemBeanWrapper> displayItemBeanWrappers, HashMap<Integer, String> importedCRFStatuses,HttpServletRequest request) {
        ResourceBundle respage = ResourceBundleProvider.getPageMessagesBundle();
        setRespage(respage);
        TriggerService triggerService = new TriggerService();

        StringBuffer auditMsg = new StringBuffer();
        List<String> errors = new ArrayList<String>();
        HashMap validateDataResult  = new HashMap<>();
        
        // htaycher: return back later?
        auditMsg.append(respage.getString("passed_study_check") + " ");
        auditMsg.append(respage.getString("passed_oid_metadata_check") + " ");

        try {
        // validation errors, the same as in the ImportCRFDataServlet. DRY?
        Boolean eventCRFStatusesValid = getImportCRFDataService().eventCRFStatusesValid(odmContainer, userBean);
        
        errors.addAll((ArrayList<String>) getImportCRFDataService().validateEventCRFBeans(odmContainer, userBean,request));        
        if(!(errors.isEmpty())) {
        	validateDataResult.put("errors", errors);
        	return validateDataResult;
        }
        
        HashMap fetchEventCRFBeansResult=  getImportCRFDataService().fetchEventCRFBeans(odmContainer, userBean, Boolean.TRUE,request);
        
        List<EventCRFBean> eventCRFBeans = (List<EventCRFBean>) fetchEventCRFBeansResult.get("eventCRFBeans");
        ArrayList<StudyEventBean> studyEventBeans = (ArrayList<StudyEventBean>) fetchEventCRFBeansResult.get("studyEventBeans");       
        validateDataResult.put("eventCRFBeans", eventCRFBeans);
        validateDataResult.put("studyEventBeans", studyEventBeans);
        
        // The following line updates a map that is used for setting the EventCRF status post import
        getImportCRFDataService().fetchEventCRFStatuses(odmContainer, importedCRFStatuses);

        ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();

        // -- does the event already exist? if not, fail
        if (eventCRFBeans == null) {
            errors.add(respage.getString("the_event_crf_not_correct_status"));            
        } else if (eventCRFBeans.isEmpty() && !eventCRFStatusesValid) {
            errors.add(respage.getString("the_event_crf_not_correct_status"));            
        } else if (eventCRFBeans.isEmpty()) {
            errors.add(respage.getString("no_event_crfs_matching_the_xml_metadata"));            
        }
        if(!(errors.isEmpty())) {
        	validateDataResult.put("errors", errors);
        	return validateDataResult;
        }
        logger.debug("found a list of eventCRFBeans: " + eventCRFBeans.toString());

        for (EventCRFBean eventCRFBean : eventCRFBeans) {


            logger.debug("Event CRF Bean: id " + eventCRFBean.getId() + ", status "
                    + eventCRFBean.getWorkflowStatus());
             if(!eventCRFBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
            } else {
                errors.add(respage.getString("your_listed_crf_in_the_file") + " " + eventCRFBean.getEventName());
                continue;
            }
        }

        if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
            auditMsg.append(respage.getString("passed_event_crf_status_check") + " ");
        } else {
            auditMsg.append(respage.getString("the_event_crf_not_correct_status") + " ");
        }

        // List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
        HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
        HashMap<String, String> hardValidationErrors = new HashMap<String, String>();

      
            List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
            // htaycher: this should be rewritten with validator not to use request to store data
           // MockHttpServletRequest request = new MockHttpServletRequest();
            //request.addPreferredLocale(getLocale());

            tempDisplayItemBeanWrappers = getImportCRFDataService().lookupValidationErrors(request, odmContainer, userBean, totalValidationErrors,
                    hardValidationErrors, permittedEventCRFIds, getLocale());
            displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
            logger.debug("size of total validation errors: " + (totalValidationErrors.size() + hardValidationErrors.size()));
            ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
            if (!hardValidationErrors.isEmpty()) {
                // check here where to get group repeat key
                errors.add(triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors, "1"));
            }
            if (!totalValidationErrors.isEmpty()) {
                errors.add(triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors, "1"));
            }

        } catch (NullPointerException npe1) {
            // what if you have 2 event crfs but the third is a fake?
            logger.error("Error while validating data",npe1);
            errors.add(respage.getString("an_error_was_thrown_while_validation_errors"));
            logger.debug("=== threw the null pointer, import === " + npe1.getMessage());
        } catch (OpenClinicaException oce1) {
        	if(oce1.errorID != null && oce1.errorID.trim().length() > 0) {
        		 errors.add(oce1.errorID+":"+oce1.getOpenClinicaMessage());
        	}else {
        		 errors.add(oce1.getOpenClinicaMessage());
        	}
           
            logger.debug("=== threw the openclinica message, import === " + oce1.getOpenClinicaMessage());
        }

        auditMsg.append(respage.getString("passing_crf_edit_checks") + " ");

        validateDataResult.put("errors", errors);
    	return validateDataResult;

    }

    public ArrayList<String> submitData(ODMContainer odmContainer, DataSource dataSource, Study studyBean, UserAccountBean userBean,
            List<DisplayItemBeanWrapper> displayItemBeanWrappers, Map<Integer, String> importedCRFStatuses) throws Exception {

        boolean discNotesGenerated = false;

        ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
        itemDataDao.setFormatDates(false);
        EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);

        StringBuffer auditMsg = new StringBuffer();
        int eventCrfBeanId = -1;
        EventCRFBean eventCrfBean = null;
        ArrayList<Integer> eventCrfInts;
        ItemDataBean itemDataBean;

        CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource, studyDao);
        for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
            boolean resetSDV = false;

            logger.debug("right before we check to make sure it is savable: " + wrapper.isSavable());
            if (wrapper.isSavable()) {
                eventCrfInts = new ArrayList<Integer>();
                logger.debug("wrapper problems found : " + wrapper.getValidationErrors().toString());
                if (wrapper.getDisplayItemBeans() != null && wrapper.getDisplayItemBeans().size() == 0) {
                    return getReturnList("fail", "", "No items to submit. Please check your XML.");
                }
                for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                    eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                    eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                    logger.debug("found value here: " + displayItemBean.getData().getValue());
                    logger.debug("found status here: " + eventCrfBean.getStatus().getName());
                    itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(), displayItemBean
                            .getData().getOrdinal());
                    if (wrapper.isOverwrite() && itemDataBean.isActive()) {

                        if (!itemDataBean.getValue().equals(displayItemBean.getData().getValue()))
                            resetSDV = true;

                        logger.debug("just tried to find item data bean on item name " + displayItemBean.getItem().getName());
                        itemDataBean.setUpdatedDate(new Date());
                        itemDataBean.setUpdater(userBean);
                        itemDataBean.setValue(displayItemBean.getData().getValue());

                        // set status?
                        itemDataDao.update(itemDataBean);
                        logger.debug("updated: " + itemDataBean.getItemId());
                        // need to set pk here in order to create dn
                        displayItemBean.getData().setId(itemDataBean.getId());
                    } else {
                        resetSDV = true;
                        itemDataDao.create(displayItemBean.getData());
                        logger.debug("created: " + displayItemBean.getData().getItemId());
                        itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(), displayItemBean
                                .getData().getOrdinal());
                        // logger.debug("found: id " + itemDataBean2.getId() + " name " + itemDataBean2.getName());
                        displayItemBean.getData().setId(itemDataBean.getId());
                    }
                    ItemDAO idao = new ItemDAO(dataSource);
                    ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                    // logger.debug("*** checking for validation errors: " + ibean.getName());
                    String itemOid = displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_" + displayItemBean.getData().getOrdinal()
                            + "_" + wrapper.getStudySubjectOid();
                    // logger.debug("+++ found validation errors hash map: " +
                    // wrapper.getValidationErrors().toString());
                    if (wrapper.getValidationErrors().containsKey(itemOid)) {
                        ArrayList<String> messageList = (ArrayList<String>) wrapper.getValidationErrors().get(itemOid);
                        for (String message : messageList) {
                            DiscrepancyNoteBean parentDn = createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, userBean, dataSource,
                                    studyBean);
                            createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), userBean, dataSource, studyBean);
                            discNotesGenerated = true;
                            logger.debug("*** created disc note with message: " + message);
                            auditMsg.append(wrapper.getStudySubjectOid() + ": " + ibean.getOid() + ": " + message + "---");
                            // split by this ? later, tbh
                            // displayItemBean);
                        }
                    }

                    if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
                        String eventCRFStatus = importedCRFStatuses.get(new Integer(eventCrfBean.getId()));

                        if ( (eventCrfBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.NOT_STARTED)
                                || eventCrfBean.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.INITIAL_DATA_ENTRY))
                                && eventCrfBean.isAvailable()) {
                        	
                            crfBusinessLogicHelper.markCRFStarted(eventCrfBean, userBean, true);
                        } else {
                        	
                            crfBusinessLogicHelper.markCRFComplete(eventCrfBean, userBean, true);
                        }
                        eventCrfInts.add(new Integer(eventCrfBean.getId()));
                    }
                }
                // Alter the SDV status if item data has been changed or added
                if (eventCrfBean != null && resetSDV  && eventCrfBean.getSdvStatus() != null &&
                        eventCrfBean.getSdvStatus() == SdvStatus.VERIFIED)
                    eventCrfDao.setSDVStatus(SdvStatus.CHANGED_SINCE_VERIFIED, userBean.getId(), eventCrfBean.getId());
            }
        }
        if (!discNotesGenerated) {
            return getReturnList("success", "", auditMsg.toString());
        } else {
            return getReturnList("warn", "", auditMsg.toString());
        }
    }

    public DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean, DisplayItemBean displayItemBean,
            Integer parentId, UserAccountBean uab, DataSource ds, Study study) {

        DiscrepancyNoteBean note = new DiscrepancyNoteBean();
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);
        note.setDescription(message);
        note.setDetailedNotes("Failed Validation Check");
        note.setOwner(uab);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(ResolutionStatus.OPEN.getId());
        note.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());
        if (parentId != null) {
            note.setParentDnId(parentId);
        }

        note.setField(itemBean.getName());
        note.setStudyId(study.getStudyId());
        note.setEntityName(itemBean.getName());
        note.setEntityType("ItemData");
        note.setEntityValue(displayItemBean.getData().getValue());

        note.setEventName(eventCrfBean.getName());
        note.setEventStart(eventCrfBean.getCreatedDate());
        note.setCrfName(displayItemBean.getEventDefinitionCRF().getCrfName());

        StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
        note.setSubjectName(ss.getName());

        note.setEntityId(displayItemBean.getData().getId());
        note.setColumn("value");

        DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(ds);
        note = (DiscrepancyNoteBean) dndao.create(note);
        // so that the below method works, need to set the entity above
        logger.debug("trying to create mapping with " + note.getId() + " " + note.getEntityId() + " " + note.getColumn() + " " + note.getEntityType());
        dndao.createMapping(note);
        logger.debug("just created mapping");
        return note;
    }

    public List<ImportDataRuleRunnerContainer> runRulesSetup(DataSource dataSource, Study studyBean, UserAccountBean userBean,
            List<SubjectDataBean> subjectDataBeans, RuleSetServiceInterface ruleSetService) {
        List<ImportDataRuleRunnerContainer> containers = new ArrayList<ImportDataRuleRunnerContainer>();
        if (ruleSetService.getCountByStudy(studyBean) > 0) {
            ImportDataRuleRunnerContainer container;
            for (SubjectDataBean subjectDataBean : subjectDataBeans) {
                container = new ImportDataRuleRunnerContainer();
                container.initRuleSetsAndTargets(dataSource, studyBean, subjectDataBean, ruleSetService);
                if (container.getShouldRunRules())
                    containers.add(container);
            }
            if (containers != null && !containers.isEmpty())
                ruleSetService.runRulesInImportData(containers, studyBean, userBean, ExecutionMode.DRY_RUN);
        }
        return containers;
    }

    public List<String> runRules(Study studyBean, UserAccountBean userBean, List<ImportDataRuleRunnerContainer> containers,
                                 RuleSetServiceInterface ruleSetService, ExecutionMode executionMode) {
        List<String> messages = new ArrayList<String>();
        if (containers != null && !containers.isEmpty()) {
            HashMap<String, ArrayList<String>> summary = ruleSetService.runRulesInImportData(containers, studyBean, userBean, executionMode);
            messages = extractRuleActionWarnings(summary);
        }
        return messages;
    }

    private List<String> extractRuleActionWarnings(HashMap<String, ArrayList<String>> summaryMap) {
        List<String> messages = new ArrayList<String>();
        if (summaryMap != null && !summaryMap.isEmpty()) {
            for (String key : summaryMap.keySet()) {
                StringBuilder mesg = new StringBuilder(key + " : ");
                for (String s : summaryMap.get(key)) {
                    mesg.append(s + ", ");
                }
                messages.add(mesg.toString());
            }
        }
        return messages;
    }

    public ImportCRFDataService getImportCRFDataService() {
        /*
         * if (locale == null) {locale = new Locale("en-US");} dataService = this.dataService != null? dataService : new
         * ImportCRFDataService(dataSource, locale);
         */
        return importCRFDataService;
    }

    private ArrayList<String> getReturnList(String status, String msg, String auditMsg) {
        ArrayList<String> retList = new ArrayList<String>(3);
        retList.add(status);
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;
    }
}