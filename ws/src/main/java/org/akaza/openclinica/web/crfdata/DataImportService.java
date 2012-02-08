package org.akaza.openclinica.web.crfdata;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.ImportDataRuleRunnerContainer;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.web.job.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;


/**
 *
 * @author thickerson, daniel
 *
 */
public class DataImportService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    ResourceBundle respage;
    Locale locale;

    private ImportCRFDataService dataService;

    /**
     * Import Data, the logic which imports the data for our data service.  Note that
     * we will return three strings
     * string 0: status, either 'success', 'fail', or 'warn'.
     * string 1: the message string which will be returned in our soap response
     * string 2: the audit message, currently not used but will be saved in the event of a success.
     *
     * import consist from 3 steps
     * 1) parse xml and extract data
     * 2) validation
     * 3) data submission
     *
     * @author thickerson
     * @param dataSource
     * @param resources
     * @param studyBean
     * @param userBean
     * @param xml
     * @return
     * @throws Exception
     */
//    public ArrayList<String> importData(DataSource dataSource, CoreResources resources, StudyBean studyBean, UserAccountBean userBean, String xml)  throws Exception {
//        locale = new Locale("en-US");
//        ResourceBundleProvider.updateLocale(locale);
//        respage = ResourceBundleProvider.getPageMessagesBundle();
//        TriggerService triggerService = new TriggerService();
//
//        ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
//        EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);
//
//        StringBuffer msg = new StringBuffer();
//        StringBuffer auditMsg = new StringBuffer();
//        Mapping myMap = new Mapping();
//
//        InputStream xsdFile = resources.getInputStream("ODM1-3-0.xsd");//new File(propertiesPath + File.separator + "ODM1-3-0.xsd");
//        InputStream xsdFile2 = resources.getInputStream("ODM1-2-1.xsd");//new File(propertiesPath + File.separator + "ODM1-2-1.xsd");
//        boolean fail = false;
//        InputStream mapInputStream = resources.getInputStream("cd_odm_mapping.xml");
//
//        myMap.loadMapping(new InputSource(mapInputStream));
//        Unmarshaller um1 = new Unmarshaller(myMap);
//        ODMContainer odmContainer = new ODMContainer();
//        // checking to see what we get
//        //logger.debug(this.convertStreamToString(xsdFile));
//        if (xml != null) {
//            msg.append(" ");
//        } else {
//            msg.append(" " + respage.getString("unreadable_file") + ": ");
//        }
//
//        try {
//            //logger.debug("working on the following xml");
//           logger.debug(xml);
//            //logger.debug("xsd File: " + xsdFile.toString());
//            // File xsdFileFinal = new File(xsdFile);
//            // schemaValidator.validateAgainstSchema(xml, xsdFile);
//            // removing schema validation since we are presented with the chicken v egg error problem
//            odmContainer = (ODMContainer) um1.unmarshal(new StringReader(xml));
//
//           logger.debug("Found crf data container for study oid: " + odmContainer.getCrfDataPostImportContainer().getStudyOID());
//           logger.debug("found length of subject list: " + odmContainer.getCrfDataPostImportContainer().getSubjectData().size());
//        } catch (Exception me1) {
//            // fail against one, try another
//            me1.printStackTrace();
//           logger.debug("failed in unmarshaling, trying another version = "+me1.getMessage());
//            try {
//                // schemaValidator.validateAgainstSchema(xml, xsdFile2);
//                // for backwards compatibility, we also try to validate vs
//                // 1.2.1 ODM 06/2008
//                odmContainer = (ODMContainer) um1.unmarshal(new StringReader(xml));
//            } catch (Exception me2) {
//                // not sure if we want to report me2
//                me2.printStackTrace();
//
//                MessageFormat mf = new MessageFormat("");
//                mf.applyPattern(respage.getString("your_xml_is_not_well_formed"));
//                Object[] arguments = { me1.getMessage() };
//                msg.append(mf.format(arguments) + " ");
//                auditMsg.append(mf.format(arguments) + " ");
//                // break here with an exception
//               logger.debug("found an error with XML: " + msg.toString());
//                // throw new Exception(msg.toString());
//                // instead of breaking the entire operation, we should
//                // continue looping
//                return getReturnList("fail",msg.toString(), auditMsg.toString());
//            }
//        }
//        // next: check, then import
//        List<String> errors = new ArrayList<String>();
//        try {
//           logger.debug("passing an odm container and study bean id: " + studyBean.getId());
//            errors = getImportCRFDataService(dataSource).validateStudyMetadata(odmContainer, studyBean.getId());
//        } catch (Exception eee) {
//           logger.debug("found exception: " + eee.getMessage());
//            // eee.printStackTrace();
//        }
//        // this needs to be replaced with the study name from the job, since
//        // the user could be in any study ...
//        if (errors != null) {
//            // add to session
//            // forward to another page
//           logger.debug(errors.toString());
//            for (String error : errors) {
//                msg.append(error + " ");
//            }
//            if (errors.size() > 0) {
//
//                return getReturnList("fail",msg.toString(), auditMsg.toString());
//            } else {
//                msg.append(respage.getString("passed_study_check") + " ");
//                msg.append(respage.getString("passed_oid_metadata_check") + " ");
//                auditMsg.append(respage.getString("passed_study_check") + " ");
//                auditMsg.append(respage.getString("passed_oid_metadata_check") + " ");
//            }
//
//        }
//        // validation errors, the same as in the ImportCRFDataServlet. DRY?
//        List<EventCRFBean> eventCRFBeans = getImportCRFDataService(dataSource).fetchEventCRFBeans(odmContainer, userBean);
//
//        ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
//       logger.debug("found a list of eventCRFBeans: " + eventCRFBeans.toString());
//
//        List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
//        HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
//        HashMap<String, String> hardValidationErrors = new HashMap<String, String>();
//
//        // -- does the event already exist? if not, fail
//        if (!eventCRFBeans.isEmpty()) {
//            for (EventCRFBean eventCRFBean : eventCRFBeans) {
//                DataEntryStage dataEntryStage = eventCRFBean.getStage();
//                Status eventCRFStatus = eventCRFBean.getStatus();
//
//               logger.debug("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage " + dataEntryStage.getName() + ", status "
//                        + eventCRFStatus.getName());
//                if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
//                        || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
//                        || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
    //                    || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
//                    permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
//                } else {
//                    // break out here with an exception
//
//                    // throw new
//                    // Exception("Your listed Event CRF in the file " +
//                    // f.getName() +
//                    // " does not exist, or has already been locked for import."
//                    // );
//                    MessageFormat mf = new MessageFormat("");
//                    mf.applyPattern(respage.getString("your_listed_crf_in_the_file"));
//                    Object[] arguments = { "???FileName???" };
//                    // TODO need a different message than the above
//                    msg.append(mf.format(arguments) + " ");
//                    auditMsg.append(mf.format(arguments) + " ");
//                    continue;
//                }
//            }
//
//            if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
//                msg.append(respage.getString("passed_event_crf_status_check") + " ");
//                auditMsg.append(respage.getString("passed_event_crf_status_check") + " ");
//            } else {
//                fail = true;
//                msg.append(respage.getString("the_event_crf_not_correct_status") + " ");
//                auditMsg.append(respage.getString("the_event_crf_not_correct_status") + " ");
//            }
//
//            // create a 'fake' request to generate the validation errors
//            // here, tbh 05/2009
//
//            MockHttpServletRequest request = new MockHttpServletRequest();
//            // Locale locale = new Locale("en-US");
//            request.addPreferredLocale(locale);
//            try {
//                List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
//                tempDisplayItemBeanWrappers =
//                    getImportCRFDataService(dataSource).lookupValidationErrors(request, odmContainer, userBean, totalValidationErrors, hardValidationErrors,
//                            permittedEventCRFIds);
//               logger.debug("size of total validation errors: " + totalValidationErrors.size());
//                //htaycher no submission if errors
//                if (hardValidationErrors.isEmpty() && totalValidationErrors.isEmpty())
//                {
//                	displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
//                }
//                else
//                {
//                	 ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
//                	 String messages =""; auditMsg = new StringBuffer("");
//                 	if (!hardValidationErrors.isEmpty()) {
//                       //check here where to get group repeat key
//                        messages = triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors,"1");
//                        auditMsg.append(messages);
//                    }
//                 	if (!totalValidationErrors.isEmpty()) {
//                    	messages = triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors,"1");
//                    	auditMsg.append(messages);
//                    }
//                    return getReturnList("fail",msg.toString(), auditMsg.toString());
//                }
//            } catch (NullPointerException npe1) {
//                // what if you have 2 event crfs but the third is a fake?
//                npe1.printStackTrace();
//                fail = true;
//               logger.debug("threw a NPE after calling lookup validation errors");
//                msg.append(respage.getString("an_error_was_thrown_while_validation_errors") + " ");
//               logger.debug("=== threw the null pointer, import === " + npe1.getMessage());
//            } catch (OpenClinicaException oce1) {
//                fail = true;
//               logger.debug("threw an OCE after calling lookup validation errors " + oce1.getOpenClinicaMessage());
//                msg.append(oce1.getOpenClinicaMessage() + " ");
//               logger.debug("=== threw the openclinica message, import === " + oce1.getOpenClinicaMessage());
//            }
//        } else {
//            // fail = true;
//            // break here with an exception
//            msg.append(respage.getString("no_event_crfs_matching_the_xml_metadata") + " ");
//            // throw new Exception(msg.toString());
//            return getReturnList("fail",msg.toString(), auditMsg.toString());
//        }
//        boolean discNotesGenerated = false;
//        if (fail) {
//            // in place of nulls, need to return a message
//            return getReturnList("fail",msg.toString(), auditMsg.toString());
//
//        } else {
//
//            msg.append(respage.getString("passing_crf_edit_checks") + " ");
//            auditMsg.append(respage.getString("passing_crf_edit_checks") + " ");
//            // session.setAttribute("importedData",
//            // displayItemBeanWrappers);
//            // session.setAttribute("validationErrors",
//            // totalValidationErrors);
//            // session.setAttribute("hardValidationErrors",
//            // hardValidationErrors);
//            // above are to be sent to the user, but what kind of message
//            // can we make of them here?
//
//            // if hard validation errors are present, we only generate one
//            // table
//            // otherwise, we generate the other two: validation errors and
//            // valid data
//           logger.debug("found total validation errors: " + totalValidationErrors.size());
//            SummaryStatsBean ssBean = getImportCRFDataService(dataSource).generateSummaryStatsBean(odmContainer, displayItemBeanWrappers);
//            // msg.append("===+");
//            // the above is a special key that we will use to split the
//            // message into two parts
//            // a shorter version for the audit and
//            // a longer version for the email
//            // resetting the msg, since we don't need messages up until now
//            msg = new StringBuffer("");
//            auditMsg = new StringBuffer("");
//            msg.append(triggerService.generateSummaryStatsMessage(ssBean, respage, totalValidationErrors));
//            // session.setAttribute("summaryStats", ssBean);
//            // will have to set hard edit checks here as well
//            // session.setAttribute("subjectData",
//            ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
//            // forwardPage(Page.VERIFY_IMPORT_SERVLET);
//            // instead of forwarding, go ahead and save it all, sending a
//            // message at the end
//
//
//
//
//            CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
//            for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
//
//                int eventCrfBeanId = -1;
//                EventCRFBean eventCrfBean = new EventCRFBean();
//
//                logger.debug("right before we check to make sure it is savable: " + wrapper.isSavable());
//                if (wrapper.isSavable()) {
//                    ArrayList<Integer> eventCrfInts = new ArrayList<Integer>();
//                    logger.debug("wrapper problems found : " + wrapper.getValidationErrors().toString());
//                    for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
//                        eventCrfBeanId = displayItemBean.getData().getEventCRFId();
//                        eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
//                        logger.debug("found value here: " + displayItemBean.getData().getValue());
//                        logger.debug("found status here: " + eventCrfBean.getStatus().getName());
//                        ItemDataBean itemDataBean = new ItemDataBean();
//                        itemDataBean =
//                            itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(), displayItemBean
//                                    .getData().getOrdinal());
//                        if (wrapper.isOverwrite() && itemDataBean.getStatus() != null) {
//                            logger.debug("just tried to find item data bean on item name " + displayItemBean.getItem().getName());
//                            itemDataBean.setUpdatedDate(new Date());
//                            itemDataBean.setUpdater(userBean);
//                            itemDataBean.setValue(displayItemBean.getData().getValue());
//                            // set status?
//                            itemDataDao.update(itemDataBean);
//                            logger.debug("updated: " + itemDataBean.getItemId());
//                            // need to set pk here in order to create dn
//                            displayItemBean.getData().setId(itemDataBean.getId());
//                        } else {
//                            itemDataDao.create(displayItemBean.getData());
//                           logger.debug("created: " + displayItemBean.getData().getItemId());
//                            ItemDataBean itemDataBean2 =
//                                itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(), displayItemBean
//                                        .getData().getOrdinal());
//                            //logger.debug("found: id " + itemDataBean2.getId() + " name " + itemDataBean2.getName());
//                            displayItemBean.getData().setId(itemDataBean2.getId());
//                        }
//                        ItemDAO idao = new ItemDAO(dataSource);
//                        ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
//                        //logger.debug("*** checking for validation errors: " + ibean.getName());
//                        String itemOid =
//                            displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_" + displayItemBean.getData().getOrdinal()
//                            + "_" + wrapper.getStudySubjectOid();
//                        //logger.debug("+++ found validation errors hash map: " + wrapper.getValidationErrors().toString());
//                        if (wrapper.getValidationErrors().containsKey(itemOid)) {
//                            ArrayList messageList = (ArrayList) wrapper.getValidationErrors().get(itemOid);
//                            for (int iter = 0; iter < messageList.size(); iter++) {
//                                String message = (String) messageList.get(iter);
//
//                                DiscrepancyNoteBean parentDn =
//                                    createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, userBean, dataSource, studyBean);
//                                createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), userBean, dataSource, studyBean);
//                                discNotesGenerated = true;
//                               logger.debug("*** created disc note with message: " + message);
//                                auditMsg.append(wrapper.getStudySubjectOid()+ ": " + ibean.getOid() + ": " + message + "---");
//                                // split by this ? later, tbh
//                                // displayItemBean);
//                            }
//                        }
//                        if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
//                            crfBusinessLogicHelper.markCRFComplete(eventCrfBean, userBean);
//                            //logger.debug("*** just updated event crf bean: " + eventCrfBean.getId());
//                            eventCrfInts.add(new Integer(eventCrfBean.getId()));
//                        }
//                    }
//                }
//            }
//            }
//            // msg.append("===+");
//            // msg.append(respage.getString("data_has_been_successfully_import") + " ");
//            // auditMsg.append(respage.getString("data_has_been_successfully_import") + " ");
//
//            //            MessageFormat mf = new MessageFormat("");
//            //            mf.applyPattern(respage.getString("you_can_review_the_data"));
//            //            Object[] arguments = { SQLInitServlet.getField("sysURL.base") };
//            // msg.append(mf.format(arguments));
//            // auditMsg.append(mf.format(arguments));
//
//
//
//        if (!discNotesGenerated) {
//            return getReturnList("success",msg.toString(), auditMsg.toString());
//        } else {
//            return getReturnList("warn",msg.toString(), auditMsg.toString());
//        }
//    }
//
    /*
     * VALIDATE data on all levels
     *
     * msg - contains status messages
     * @return
     * list of errors
     */
    public List<String> validateData(ODMContainer odmContainer,DataSource dataSource, CoreResources resources,
    		StudyBean studyBean, UserAccountBean userBean,
    		List<DisplayItemBeanWrapper> displayItemBeanWrappers) {
        respage = ResourceBundleProvider.getPageMessagesBundle();
        TriggerService triggerService = new TriggerService();

        StringBuffer auditMsg = new StringBuffer();
        List<String> errors = new ArrayList<String>();

        logger.debug("passing an odm container and study bean id: " + studyBean.getId());
        errors = getImportCRFDataService(dataSource).validateStudyMetadata(odmContainer, studyBean.getId());
        // this needs to be replaced with the study name from the job, since
        // the user could be in any study ...
        if (errors != null && errors.size()>0) { return errors;}

        //htaycher: return back later?
        auditMsg.append(respage.getString("passed_study_check") + " ");
        auditMsg.append(respage.getString("passed_oid_metadata_check") + " ");

        // validation errors, the same as in the ImportCRFDataServlet. DRY?
        List<EventCRFBean> eventCRFBeans = getImportCRFDataService(dataSource).fetchEventCRFBeans(odmContainer, userBean);

        ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
        logger.debug("found a list of eventCRFBeans: " + eventCRFBeans.toString());


        // -- does the event already exist? if not, fail
        if (eventCRFBeans.isEmpty()) {
        	errors.add(respage.getString("no_event_crfs_matching_the_xml_metadata"));
        	return errors;
        }
        for (EventCRFBean eventCRFBean : eventCRFBeans)
        {
            DataEntryStage dataEntryStage = eventCRFBean.getStage();
            Status eventCRFStatus = eventCRFBean.getStatus();

            logger.debug("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage " + dataEntryStage.getName() + ", status "
                    + eventCRFStatus.getName());
            if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                    || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                    || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
            } else {
            	errors.add(respage.getString("your_listed_crf_in_the_file") + " "+eventCRFBean.getEventName());
                continue;
            }
        }

        if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
        	auditMsg.append(respage.getString("passed_event_crf_status_check") + " ");
        } else {
        	auditMsg.append(respage.getString("the_event_crf_not_correct_status") + " ");
        }

        //  List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
        HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
        HashMap<String, String> hardValidationErrors = new HashMap<String, String>();

           try {
                List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
                //htaycher: this should be rewritten with validator not to use request to store data
                MockHttpServletRequest request = new MockHttpServletRequest();
                request.addPreferredLocale(locale);

                tempDisplayItemBeanWrappers =
                    getImportCRFDataService(dataSource).lookupValidationErrors( request, odmContainer, userBean, totalValidationErrors, hardValidationErrors,
                            permittedEventCRFIds);
                displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
                 logger.debug("size of total validation errors: " + ( totalValidationErrors.size() + hardValidationErrors.size()));
               	 ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
                	if (!hardValidationErrors.isEmpty()) {
                       //check here where to get group repeat key
                       errors.add( triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors,"1"));
                	}
                 	if (!totalValidationErrors.isEmpty()) {
                    	errors.add( triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors,"1"));
                    }

            } catch (NullPointerException npe1) {
                // what if you have 2 event crfs but the third is a fake?
                npe1.printStackTrace();
                errors.add(respage.getString("an_error_was_thrown_while_validation_errors"));
                logger.debug("=== threw the null pointer, import === " + npe1.getMessage());
            } catch (OpenClinicaException oce1) {
                errors.add(oce1.getOpenClinicaMessage());
                logger.debug("=== threw the openclinica message, import === " + oce1.getOpenClinicaMessage());
           }

            auditMsg.append(respage.getString("passing_crf_edit_checks") + " ");

      return errors;

    }



    public ArrayList<String> submitData(ODMContainer odmContainer,
    		DataSource dataSource,
    		StudyBean studyBean, UserAccountBean userBean,
    		List<DisplayItemBeanWrapper> displayItemBeanWrappers
    )  throws Exception {

         boolean discNotesGenerated = false;

         ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
         EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);

         StringBuffer auditMsg = new StringBuffer();
         int eventCrfBeanId = -1;EventCRFBean eventCrfBean;
         ArrayList<Integer> eventCrfInts;
         ItemDataBean itemDataBean;

         CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
         for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
                logger.debug("right before we check to make sure it is savable: " + wrapper.isSavable());
                if (wrapper.isSavable())
                {
                    eventCrfInts = new ArrayList<Integer>();
                    logger.debug("wrapper problems found : " + wrapper.getValidationErrors().toString());
                    if ( wrapper.getDisplayItemBeans() != null && wrapper.getDisplayItemBeans().size() == 0)
                    {
                    	return getReturnList("fail","", "No items to submit. Please check your XML.");
                    }
                    for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                        eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                        eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                        logger.debug("found value here: " + displayItemBean.getData().getValue());
                        logger.debug("found status here: " + eventCrfBean.getStatus().getName());
                        itemDataBean =  itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(),
                        		displayItemBean.getData().getOrdinal());
                        if (wrapper.isOverwrite() && itemDataBean.getStatus() != null)
                        {
                            logger.debug("just tried to find item data bean on item name " + displayItemBean.getItem().getName());
                            itemDataBean.setUpdatedDate(new Date());
                            itemDataBean.setUpdater(userBean);
                            itemDataBean.setValue(displayItemBean.getData().getValue());
                            // set status?
                            itemDataDao.update(itemDataBean);
                            logger.debug("updated: " + itemDataBean.getItemId());
                            // need to set pk here in order to create dn
                            displayItemBean.getData().setId(itemDataBean.getId());
                        } else
                        {
                            itemDataDao.create(displayItemBean.getData());
                            logger.debug("created: " + displayItemBean.getData().getItemId());
                            itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(),
                        		   displayItemBean.getData().getOrdinal());
                            //logger.debug("found: id " + itemDataBean2.getId() + " name " + itemDataBean2.getName());
                            displayItemBean.getData().setId(itemDataBean.getId());
                        }
                        ItemDAO idao = new ItemDAO(dataSource);
                        ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                        //logger.debug("*** checking for validation errors: " + ibean.getName());
                        String itemOid =
                            displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_" + displayItemBean.getData().getOrdinal()
                            + "_" + wrapper.getStudySubjectOid();
                        //logger.debug("+++ found validation errors hash map: " + wrapper.getValidationErrors().toString());
                        if (wrapper.getValidationErrors().containsKey(itemOid)) {
                            ArrayList<String> messageList = (ArrayList<String>) wrapper.getValidationErrors().get(itemOid);
                            for (String message: messageList) {
                                 DiscrepancyNoteBean parentDn =
                                    createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, userBean, dataSource, studyBean);
                                createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), userBean, dataSource, studyBean);
                                discNotesGenerated = true;
                               logger.debug("*** created disc note with message: " + message);
                                auditMsg.append(wrapper.getStudySubjectOid()+ ": " + ibean.getOid() + ": " + message + "---");
                                // split by this ? later, tbh
                                // displayItemBean);
                            }
                        }
                        if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
                            crfBusinessLogicHelper.markCRFComplete(eventCrfBean, userBean);
                            //logger.debug("*** just updated event crf bean: " + eventCrfBean.getId());
                            eventCrfInts.add(new Integer(eventCrfBean.getId()));
                        }
                    }
                }
            }
          if (!discNotesGenerated) {
            return getReturnList("success","", auditMsg.toString());
        } else {
            return getReturnList("warn","", auditMsg.toString());
        }
    }




    public  DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean, DisplayItemBean displayItemBean,
            Integer parentId, UserAccountBean uab, DataSource ds, StudyBean study) {

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
        note.setStudyId(study.getId());
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

    public List<ImportDataRuleRunnerContainer> runRulesSetup(DataSource dataSource, StudyBean studyBean,
            UserAccountBean userBean, List<SubjectDataBean> subjectDataBeans, RuleSetServiceInterface ruleSetService) {
        List<ImportDataRuleRunnerContainer> containers = new ArrayList<ImportDataRuleRunnerContainer>();
        ImportDataRuleRunnerContainer container;
        for (SubjectDataBean subjectDataBean : subjectDataBeans) {
            container = new ImportDataRuleRunnerContainer();
            container.init(dataSource, studyBean, subjectDataBean, ruleSetService);
            if(container.getShouldRunRules())   containers.add(container);

        }
        if(containers != null && ! containers.isEmpty())
            ruleSetService.runRulesInImportData(containers, studyBean, userBean, ExecutionMode.DRY_RUN);
        return containers;
    }


    public void runRules(StudyBean studyBean, UserAccountBean userBean,
            List<ImportDataRuleRunnerContainer> containers, RuleSetServiceInterface ruleSetService,
            ExecutionMode executionMode) {
        if(containers != null && ! containers.isEmpty())
            ruleSetService.runRulesInImportData(containers, studyBean, userBean, executionMode);
    }

    private ImportCRFDataService getImportCRFDataService(DataSource dataSource) {
    	if (locale == null) {locale = new Locale("en-US");}
        dataService = this.dataService != null? dataService : new ImportCRFDataService(dataSource, locale);
        return dataService;
    }

    private ArrayList<String> getReturnList(String status,String msg, String auditMsg) {
        ArrayList<String> retList = new ArrayList<String>(3);
        retList.add(status);
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;
    }
   }

