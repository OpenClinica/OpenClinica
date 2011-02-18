package org.akaza.openclinica.web.crfdata;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.web.job.TriggerService;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.MessageFormat;
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

    // private ResourceLoader resourceLoader;
    //    
    // public static String PROPERTIES_DIR;
    //    
    // public void setPROPERTIES_DIR() {
    // String resource = "classpath:properties/placeholder.properties";
    // System.out.println("Resource " + resource);
    // Resource scr = resourceLoader.getResource(resource);
    // String absolutePath = null;
    // try {
    // System.out.println("Resource" + resource);
    // absolutePath = scr.getFile().getAbsolutePath();
    // System.out.println("Resource" + ((ClassPathResource) scr).getPath());
    // System.out.println("Resource" + resource);
    // PROPERTIES_DIR = absolutePath.replaceAll("placeholder.properties", "");
    // System.out.println("Resource" + PROPERTIES_DIR);
    // } catch (IOException e) {
    // e.printStackTrace();
    // throw new OpenClinicaSystemException(e.getMessage(), e.fillInStackTrace());
    // }
    //
    // }

    public String convertStreamToString(InputStream is) throws IOException {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine() method. We iterate until the BufferedReader return null which means there's
         * no more data to read. Each line will appended to a StringBuilder and returned as String.
         */
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                is.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    public ArrayList<String> importProcessedData(DataSource dataSource, CoreResources resources, StudyBean studyBean, UserAccountBean userBean,
            ODMContainer odmContainer, StringBuffer msg, StringBuffer auditMsg, boolean checkEventCrf, StudyEventBean studyEventBean) throws Exception {
        List<String> errors = new ArrayList<String>();
        TriggerService triggerService = new TriggerService();
        locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle();

        ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
        EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);
        boolean fail = false;
        try {
            System.out.println("passing an odm container and study bean id: " + studyBean.getId());
            errors = getImportCRFDataService(dataSource).validateStudyMetadata(odmContainer, studyBean.getId());
        } catch (Exception eee) {
            System.out.println("found exception: " + eee.getMessage());
            // eee.printStackTrace();
        }
        // this needs to be replaced with the study name from the job, since
        // the user could be in any study ...
        if (errors != null) {
            // add to session
            // forward to another page
            System.out.println(errors.toString());
            for (String error : errors) {
                msg.append(error + " ");
            }
            if (errors.size() > 0) {

                return getFailReturnList(msg.toString(), auditMsg.toString());
            } else {
                msg.append(respage.getString("passed_study_check") + " ");
                msg.append(respage.getString("passed_oid_metadata_check") + " ");
                auditMsg.append(respage.getString("passed_study_check") + " ");
                auditMsg.append(respage.getString("passed_oid_metadata_check") + " ");
            }

        }
        // end cut here, skip event crf validation, since we create it, tbh 02/2011
        // the below creates the event crf, apparently, so let's go with it
        // validation errors, the same as in the ImportCRFDataServlet. DRY?
        List<EventCRFBean> eventCRFBeans = getImportCRFDataService(dataSource).fetchEventCRFBeans(odmContainer, userBean, studyEventBean);

        ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
        System.out.println("found a list of eventCRFBeans: " + eventCRFBeans.toString());

        List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
        HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
        HashMap<String, String> hardValidationErrors = new HashMap<String, String>();

        // -- does the event already exist? if not, fail
        if (!eventCRFBeans.isEmpty()) {
            for (EventCRFBean eventCRFBean : eventCRFBeans) {
                DataEntryStage dataEntryStage = eventCRFBean.getStage();
                Status eventCRFStatus = eventCRFBean.getStatus();

                System.out.println("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage " + dataEntryStage.getName() + ", status "
                    + eventCRFStatus.getName());
                if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                    || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
                    || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                    permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
                } else {
                    // break out here with an exception

                    // throw new
                    // Exception("Your listed Event CRF in the file " +
                    // f.getName() +
                    // " does not exist, or has already been locked for import."
                    // );
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("your_listed_crf_in_the_file"));
                    Object[] arguments = { "???FileName???" };
                    // TODO need a different message than the above
                    msg.append(mf.format(arguments) + " ");
                    auditMsg.append(mf.format(arguments) + " ");
                    continue;
                }
            }

            if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
                msg.append(respage.getString("passed_event_crf_status_check") + " ");
                auditMsg.append(respage.getString("passed_event_crf_status_check") + " ");
            } else {
                fail = true;
                msg.append(respage.getString("the_event_crf_not_correct_status") + " ");
                auditMsg.append(respage.getString("the_event_crf_not_correct_status") + " ");
            }

            // create a 'fake' request to generate the validation errors
            // here, tbh 05/2009

            // split here? tbh 02/2011
            MockHttpServletRequest request = new MockHttpServletRequest();
            // Locale locale = new Locale("en-US");
            request.addPreferredLocale(locale);
            try {
                List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
                tempDisplayItemBeanWrappers =
                    getImportCRFDataService(dataSource).lookupValidationErrors(request, odmContainer, userBean, totalValidationErrors, hardValidationErrors,
                            permittedEventCRFIds, studyEventBean);
                System.out.println("size of total validation errors: " + totalValidationErrors.size());
                displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
            } catch (NullPointerException npe1) {
                // what if you have 2 event crfs but the third is a fake?
                npe1.printStackTrace();
                fail = true;
                System.out.println("threw a NPE after calling lookup validation errors");
                msg.append(respage.getString("an_error_was_thrown_while_validation_errors") + " ");
                System.out.println("=== threw the null pointer, import === " + npe1.getMessage());
            } catch (OpenClinicaException oce1) {
                fail = true;
                System.out.println("threw an OCE after calling lookup validation errors " + oce1.getOpenClinicaMessage());
                msg.append(oce1.getOpenClinicaMessage() + " ");
                System.out.println("=== threw the openclinica message, import === " + oce1.getOpenClinicaMessage());
            }
        } else {
            // fail = true;
            // break here with an exception
            msg.append(respage.getString("no_event_crfs_matching_the_xml_metadata") + " ");
            // throw new Exception(msg.toString());
            return getFailReturnList(msg.toString(), auditMsg.toString());
        }
        // split here? tbh 02/2011
        boolean discNotesGenerated = false;
        if (fail) {
            // in place of nulls, need to return a message
            return getFailReturnList(msg.toString(), auditMsg.toString());

        } else {

            msg.append(respage.getString("passing_crf_edit_checks") + " ");
            auditMsg.append(respage.getString("passing_crf_edit_checks") + " ");
            // session.setAttribute("importedData",
            // displayItemBeanWrappers);
            // session.setAttribute("validationErrors",
            // totalValidationErrors);
            // session.setAttribute("hardValidationErrors",
            // hardValidationErrors);
            // above are to be sent to the user, but what kind of message
            // can we make of them here?

            // if hard validation errors are present, we only generate one
            // table
            // otherwise, we generate the other two: validation errors and
            // valid data
            System.out.println("found total validation errors: " + totalValidationErrors.size());
            SummaryStatsBean ssBean = getImportCRFDataService(dataSource).generateSummaryStatsBean(odmContainer, displayItemBeanWrappers);
            // msg.append("===+");
            // the above is a special key that we will use to split the
            // message into two parts
            // a shorter version for the audit and
            // a longer version for the email
            // resetting the msg, since we don't need messages up until now
            msg = new StringBuffer("");
            auditMsg = new StringBuffer("");
            msg.append(triggerService.generateSummaryStatsMessage(ssBean, respage, totalValidationErrors));
            // session.setAttribute("summaryStats", ssBean);
            // will have to set hard edit checks here as well
            // session.setAttribute("subjectData",
            ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
            // forwardPage(Page.VERIFY_IMPORT_SERVLET);
            // instead of forwarding, go ahead and save it all, sending a
            // message at the end

            if (!hardValidationErrors.isEmpty()) {

                msg.append(triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors, false));
            } else {
                if (!totalValidationErrors.isEmpty()) {
                    msg.append(triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors, false));
                }
                msg.append(triggerService.generateValidMessage(subjectData, totalValidationErrors));
            }

            CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
            for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {

                int eventCrfBeanId = -1;
                EventCRFBean eventCrfBean = new EventCRFBean();

                // System.out.println("right before we check to make sure it is savable: " + wrapper.isSavable());
                if (wrapper.isSavable()) {
                    ArrayList<Integer> eventCrfInts = new ArrayList<Integer>();
                    // System.out.println("wrapper problems found : " + wrapper.getValidationErrors().toString());
                    for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
                        eventCrfBeanId = displayItemBean.getData().getEventCRFId();
                        eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
                        // System.out.println("found value here: " + displayItemBean.getData().getValue());
                        // System.out.println("found status here: " + eventCrfBean.getStatus().getName());
                        ItemDataBean itemDataBean = new ItemDataBean();
                        itemDataBean =
                            itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(), displayItemBean.getData()
                                    .getOrdinal());
                        if (wrapper.isOverwrite() && itemDataBean.getStatus() != null) {
                            // System.out.println("just tried to find item data bean on item name " + displayItemBean.getItem().getName());
                            itemDataBean.setUpdatedDate(new Date());
                            itemDataBean.setUpdater(userBean);
                            itemDataBean.setValue(displayItemBean.getData().getValue());
                            // set status?
                            itemDataDao.update(itemDataBean);
                            // System.out.println("updated: " + itemDataBean.getItemId());
                            // need to set pk here in order to create dn
                            displayItemBean.getData().setId(itemDataBean.getId());
                        } else {
                            itemDataDao.create(displayItemBean.getData());
                            System.out.println("created: " + displayItemBean.getData().getItemId());
                            ItemDataBean itemDataBean2 =
                                itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(), eventCrfBean.getId(), displayItemBean
                                        .getData().getOrdinal());
                            // System.out.println("found: id " + itemDataBean2.getId() + " name " + itemDataBean2.getName());
                            displayItemBean.getData().setId(itemDataBean2.getId());
                        }
                        ItemDAO idao = new ItemDAO(dataSource);
                        ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
                        // System.out.println("*** checking for validation errors: " + ibean.getName());
                        String itemOid =
                            displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_" + displayItemBean.getData().getOrdinal() + "_"
                                + wrapper.getStudySubjectOid();
                        // System.out.println("+++ found validation errors hash map: " + wrapper.getValidationErrors().toString());
                        if (wrapper.getValidationErrors().containsKey(itemOid)) {
                            ArrayList messageList = (ArrayList) wrapper.getValidationErrors().get(itemOid);
                            for (int iter = 0; iter < messageList.size(); iter++) {
                                String message = (String) messageList.get(iter);

                                DiscrepancyNoteBean parentDn =
                                    createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, null, userBean, dataSource, studyBean);
                                createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(), userBean, dataSource, studyBean);
                                discNotesGenerated = true;
                                System.out.println("*** created disc note with message: " + message);
                                auditMsg.append(wrapper.getStudySubjectOid() + ": " + ibean.getOid() + ": " + message + "---");
                                // split by this ? later, tbh
                                // displayItemBean);
                            }
                        }
                        if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
                            // do we need to mark complete, yes we probably do
                            crfBusinessLogicHelper.markCRFComplete(eventCrfBean, userBean);
                            // System.out.println("*** just updated event crf bean: " + eventCrfBean.getId());
                            eventCrfInts.add(new Integer(eventCrfBean.getId()));
                        }
                    }
                }
            }
            // msg.append("===+");
            // msg.append(respage.getString("data_has_been_successfully_import") + " ");
            // auditMsg.append(respage.getString("data_has_been_successfully_import") + " ");

            // MessageFormat mf = new MessageFormat("");
            // mf.applyPattern(respage.getString("you_can_review_the_data"));
            // Object[] arguments = { SQLInitServlet.getField("sysURL.base") };
            // msg.append(mf.format(arguments));
            // auditMsg.append(mf.format(arguments));
        }

        if (!discNotesGenerated) {
            return getSuccessReturnList(msg.toString(), auditMsg.toString());
        } else {
            return getWarningReturnList(msg.toString(), auditMsg.toString());
        }
    }

    /**
     * Import Data, the logic which imports the data for our data service. Note that we will return three strings string 0: status, either 'success', 'fail', or
     * 'warn'. string 1: the message string which will be returned in our soap response string 2: the audit message, currently not used but will be saved in the
     * event of a success.
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
    public ArrayList<String> importData(DataSource dataSource, CoreResources resources, StudyBean studyBean, UserAccountBean userBean, String xml)
            throws Exception {
        locale = new Locale("en-US");
        ResourceBundleProvider.updateLocale(locale);
        respage = ResourceBundleProvider.getPageMessagesBundle();

        StringBuffer msg = new StringBuffer();
        StringBuffer auditMsg = new StringBuffer();
        Mapping myMap = new Mapping();

        InputStream xsdFile = resources.getInputStream("ODM1-3-0.xsd");// new File(propertiesPath + File.separator + "ODM1-3-0.xsd");
        InputStream xsdFile2 = resources.getInputStream("ODM1-2-1.xsd");// new File(propertiesPath + File.separator + "ODM1-2-1.xsd");

        InputStream mapInputStream = resources.getInputStream("cd_odm_mapping.xml");

        myMap.loadMapping(new InputSource(mapInputStream));
        Unmarshaller um1 = new Unmarshaller(myMap);
        ODMContainer odmContainer = new ODMContainer();
        // checking to see what we get
        // System.out.println(this.convertStreamToString(xsdFile));
        if (xml != null) {
            msg.append(" ");
        } else {
            msg.append(" " + respage.getString("unreadable_file") + ": ");
        }

        try {
            // System.out.println("working on the following xml");
            System.out.println(xml);
            // System.out.println("xsd File: " + xsdFile.toString());
            // File xsdFileFinal = new File(xsdFile);
            // schemaValidator.validateAgainstSchema(xml, xsdFile);
            // removing schema validation since we are presented with the chicken v egg error problem
            odmContainer = (ODMContainer) um1.unmarshal(new StringReader(xml));

            System.out.println("Found crf data container for study oid: " + odmContainer.getCrfDataPostImportContainer().getStudyOID());
            System.out.println("found length of subject list: " + odmContainer.getCrfDataPostImportContainer().getSubjectData().size());
        } catch (Exception me1) {
            // fail against one, try another
            me1.printStackTrace();
            System.out.println("failed in unmarshaling, trying another version = " + me1.getMessage());
            try {
                // schemaValidator.validateAgainstSchema(xml, xsdFile2);
                // for backwards compatibility, we also try to validate vs
                // 1.2.1 ODM 06/2008
                odmContainer = (ODMContainer) um1.unmarshal(new StringReader(xml));
            } catch (Exception me2) {
                // not sure if we want to report me2
                me2.printStackTrace();

                MessageFormat mf = new MessageFormat("");
                mf.applyPattern(respage.getString("your_xml_is_not_well_formed"));
                Object[] arguments = { me1.getMessage() };
                msg.append(mf.format(arguments) + " ");
                auditMsg.append(mf.format(arguments) + " ");
                // break here with an exception
                System.out.println("found an error with XML: " + msg.toString());
                // throw new Exception(msg.toString());
                // instead of breaking the entire operation, we should
                // continue looping
                return getFailReturnList(msg.toString(), auditMsg.toString());
            }
        }
        /*
         * tbh 02/2011: perform a split-up of the code so that we can use this in the caBIG Load Labs interface
         */
        // next: check, then import
        // first cut here, tbh 02/2011
        // cut all the way to the end and pass it an odm container and a generated event crf? just a thought
        return importProcessedData(dataSource, resources, studyBean, userBean, odmContainer, msg, auditMsg, true, null);
    }

    public ArrayList<String> getFailReturnList(String msg, String auditMsg) {
        ArrayList<String> retList = new ArrayList<String>();
        retList.add("fail");
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;
    }

    public ArrayList<String> getSuccessReturnList(String msg, String auditMsg) {
        ArrayList<String> retList = new ArrayList<String>();
        retList.add("success");
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;
    }

    public ArrayList<String> getWarningReturnList(String msg, String auditMsg) {
        ArrayList<String> retList = new ArrayList<String>();
        retList.add("warn");
        retList.add(msg.toString());
        retList.add(auditMsg.toString());
        return retList;
    }

    public static DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean, DisplayItemBean displayItemBean,
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
        System.out.println("trying to create mapping with " + note.getId() + " " + note.getEntityId() + " " + note.getColumn() + " " + note.getEntityType());
        dndao.createMapping(note);
        System.out.println("just created mapping");
        return note;
    }

    private ImportCRFDataService getImportCRFDataService(DataSource dataSource) {
        dataService = this.dataService != null ? dataService : new ImportCRFDataService(dataSource, locale);
        return dataService;
    }

    // private CoreResources getCoreResources() {
    // return (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
    // }
}
