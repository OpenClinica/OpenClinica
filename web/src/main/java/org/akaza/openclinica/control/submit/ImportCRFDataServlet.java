/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.crfdata.ImportCRFDataService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Create a new CRF verison by uploading excel file. Makes use of several other classes to validate and provide accurate
 * validation. More specifically, uses XmlSchemaValidationHelper, ImportCRFDataService, ODMContainer, and others to
 * import all the XML in the ODM 1.3 standard.
 * 
 * @author Krikor Krumlian, Tom Hickerson updated Apr-May 2008
 */
public class ImportCRFDataServlet extends SecureController {

    Locale locale;

    XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
    FileUploadHelper uploadHelper = new FileUploadHelper();

    // < ResourceBundleresword,resexception,respage;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
        checkStudyFrozen(Page.MENU_SERVLET, respage.getString("current_study_frozen"));

        locale = LocaleResolver.getLocale(request);
        if (ub.isSysAdmin()) {
            return;
        }

        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT)
                || r.equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);

        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        // keep the module in the session
        session.setAttribute(MODULE, module);

        String action = request.getParameter("action");
        CRFVersionBean version = (CRFVersionBean) session.getAttribute("version");

        File xsdFile = new File(SpringServletAccess.getPropertiesDir(context) + "ODM1-3-0.xsd");
        File xsdFile2 = new File(SpringServletAccess.getPropertiesDir(context) + "ODM1-2-1.xsd");

        if (StringUtil.isBlank(action)) {
            logger.info("action is blank");
            request.setAttribute("version", version);
            forwardPage(Page.IMPORT_CRF_DATA);
        }
        if ("confirm".equalsIgnoreCase(action)) {
            String dir = SQLInitServlet.getField("filePath");
            if (!new File(dir).exists()) {
                logger.info("The filePath in datainfo.properties is invalid " + dir);
                addPageMessage(respage.getString("filepath_you_defined_not_seem_valid"));
                forwardPage(Page.IMPORT_CRF_DATA);
            }
            // All the uploaded files will be saved in filePath/crf/original/
            String theDir = dir + "crf" + File.separator + "original" + File.separator;
            if (!new File(theDir).isDirectory()) {
                new File(theDir).mkdirs();
                logger.info("Made the directory " + theDir);
            }
            // MultipartRequest multi = new MultipartRequest(request, theDir, 50 * 1024 * 1024);
            File f = null;
            try {
                f = uploadFile(theDir, version);

            } catch (Exception e) {
                logger.warn("*** Found exception during file upload***");
                e.printStackTrace();

            }
            if (f == null) {
                forwardPage(Page.IMPORT_CRF_DATA);
            }

            // TODO
            // validation steps
            // 1. valid xml - validated by file uploader below

            // LocalConfiguration config = LocalConfiguration.getInstance();
            // config.getProperties().setProperty(
            // "org.exolab.castor.parser.namespaces",
            // "true");
            // config
            // .getProperties()
            // .setProperty("org.exolab.castor.sax.features",
            // "http://xml.org/sax/features/validation,
            // http://apache.org/xml/features/validation/schema,
            // http://apache.org/xml/features/validation/schema-full-checking");
            // // above sets to validate against namespace

            Mapping myMap = new Mapping();
            // @pgawade 18-April-2011 Fix for issue 8394
            String ODM_MAPPING_DIRPath = CoreResources.ODM_MAPPING_DIR;
            myMap.loadMapping(ODM_MAPPING_DIRPath + File.separator + "cd_odm_mapping.xml");

            Unmarshaller um1 = new Unmarshaller(myMap);
            // um1.addNamespaceToPackageMapping("http://www.openclinica.org/ns/odm_ext_v130/v3.1", "OpenClinica");
            // um1.addNamespaceToPackageMapping("http://www.cdisc.org/ns/odm/v1.3"
            // ,
            // "ODMContainer");
            boolean fail = false;
            ODMContainer odmContainer = new ODMContainer();
            session.removeAttribute("odmContainer");
            try {

                // schemaValidator.validateAgainstSchema(f, xsdFile);
                // utf-8 compliance, tbh 06/2009
                InputStreamReader isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
                odmContainer = (ODMContainer) um1.unmarshal(isr);

                logger.debug("Found crf data container for study oid: " + odmContainer.getCrfDataPostImportContainer().getStudyOID());
                logger.debug("found length of subject list: " + odmContainer.getCrfDataPostImportContainer().getSubjectData().size());
                // 2. validates against ODM 1.3
                // check it all below, throw an exception and route to a
                // different
                // page if not working

                // TODO this block of code needs the xerces serializer in order
                // to
                // work

                // StringWriter myWriter = new StringWriter();
                // Marshaller m1 = new Marshaller(myWriter);
                //
                // m1.setProperty("org.exolab.castor.parser.namespaces",
                // "true");
                // m1
                // .setProperty("org.exolab.castor.sax.features",
                // "http://xml.org/sax/features/validation,
                // http://apache.org/xml/features/validation/schema,
                // http://apache.org/xml/features/validation/schema-full-checking
                // ");
                //
                // m1.setMapping(myMap);
                // m1.setNamespaceMapping("",
                // "http://www.cdisc.org/ns/odm/v1.3");
                // m1.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3
                // ODM1-3.xsd");
                // m1.marshal(odmContainer);
                // if you havent thrown it, you wont throw it here
                addPageMessage(respage.getString("passed_xml_validation"));
            } catch (Exception me1) {
                me1.printStackTrace();
                // expanding it to all exceptions, but hoping to catch Marshal
                // Exception or SAX Exceptions
                logger.info("found exception with xml transform");
                //
                logger.info("trying 1.2.1");
                try {
                    schemaValidator.validateAgainstSchema(f, xsdFile2);
                    // for backwards compatibility, we also try to validate vs
                    // 1.2.1 ODM 06/2008
                    InputStreamReader isr = new InputStreamReader(new FileInputStream(f), "UTF-8");
                    odmContainer = (ODMContainer) um1.unmarshal(isr);
                } catch (Exception me2) {
                    // not sure if we want to report me2
                    MessageFormat mf = new MessageFormat("");
                    mf.applyPattern(respage.getString("your_xml_is_not_well_formed"));
                    Object[] arguments = { me1.getMessage() };
                    addPageMessage(mf.format(arguments));
                    //
                    // addPageMessage("Your XML is not well-formed, and does not
                    // comply with the ODM 1.3 Schema. Please check it, and try
                    // again. It returned the message: "
                    // + me1.getMessage());
                    // me1.printStackTrace();
                    forwardPage(Page.IMPORT_CRF_DATA);
                    // you can't really wait to forward because then you throw
                    // NPEs
                    // in the next few parts of the code
                }
            }
            // TODO need to output further here
            // 2.a. is the study the same one that the user is in right now?
            // 3. validates against study metadata
            // 3.a. is that study subject in that study?
            // 3.b. is that study event def in that study?
            // 3.c. is that site in that study?
            // 3.d. is that crf version in that study event def?
            // 3.e. are those item groups in that crf version?
            // 3.f. are those items in that item group?

            List<String> errors = getImportCRFDataService().validateStudyMetadata(odmContainer, ub.getActiveStudyId(), locale);
            if (CollectionUtils.isNotEmpty(errors)) {
                // add to session
                // forward to another page
                logger.info(errors.toString());
                for (String error : errors) {
                    addPageMessage(error);
                }
                if (errors.size() > 0) {
                    // fail = true;
                    forwardPage(Page.IMPORT_CRF_DATA);
                }
            } else {
                addPageMessage(respage.getString("passed_study_check"));
                addPageMessage(respage.getString("passed_oid_metadata_check"));
            }
            logger.debug("passed error check");
            // TODO ADD many validation steps before we get to the
            // session-setting below
            // 4. is the event in the correct status to accept data import?
            // -- scheduled, data entry started, completed
            // (and the event should already be created)
            // (and the event should be independent, ie not affected by other
            // events)

            Boolean eventCRFStatusesValid = getImportCRFDataService().eventCRFStatusesValid(odmContainer, ub);
            ImportCRFInfoContainer importCrfInfo = new ImportCRFInfoContainer(odmContainer, sm.getDataSource());
            // The eventCRFBeans list omits EventCRFs that don't match UpsertOn rules. If EventCRF did not exist and
            // doesn't match upsert, it won't be created.
          
            errors.addAll((ArrayList<String>) getImportCRFDataService().validateEventCRFBeans(odmContainer, ub,request));
           
        	if (CollectionUtils.isNotEmpty(errors)) {
                // add to session
                // forward to another page
                logger.info(errors.toString());
                for (String error : errors) {
                    addPageMessage(error);
                }
                if (errors.size() > 0) {
                    // fail = true;
                    forwardPage(Page.IMPORT_CRF_DATA);
                    
                    return;
                }
            } else {                    
                addPageMessage(respage.getString("passed_common_events_check"));
            }
           
        	 HashMap fetchEventCRFBeansResult =  getImportCRFDataService().fetchEventCRFBeans(odmContainer, ub, Boolean.FALSE,request);
        	 
             List<EventCRFBean> eventCRFBeans = (List<EventCRFBean>) fetchEventCRFBeansResult.get("eventCRFBeans");
             ArrayList<StudyEventBean> studyEventBeans = (ArrayList<StudyEventBean>) fetchEventCRFBeansResult.get("studyEventBeans");
            
            
            List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
            HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
            HashMap<String, String> hardValidationErrors = new HashMap<String, String>();
            // The following map is used for setting the EventCRF status post import.
            HashMap<String, String> importedCRFStatuses = getImportCRFDataService().fetchEventCRFStatuses(odmContainer);
            // @pgawade 17-May-2011 Fix for issue#9590 - collection of
            // eventCRFBeans is returned as null
            // when status of one the events in xml file is either stopped,
            // signed or locked.
            // Instead of repeating the code to fetch the events in xml file,
            // method in the ImportCRFDataService is modified for this fix.
            if (eventCRFBeans == null) {
                fail = true;
                addPageMessage(respage.getString("no_event_status_matching"));
            } else {
                List<EventCRFBean> permittedEventCRFs = new ArrayList<EventCRFBean>();
                logger.info("found a list of eventCRFBeans: " + eventCRFBeans.toString());

                // List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
                // HashMap<String, String> totalValidationErrors = new
                // HashMap<String, String>();
                // HashMap<String, String> hardValidationErrors = new
                // HashMap<String, String>();
                logger.debug("found event crfs " + eventCRFBeans.size());
                // -- does the event already exist? if not, fail
                if (!eventCRFBeans.isEmpty()) {
                    for (EventCRFBean eventCRFBean : eventCRFBeans) {
                        DataEntryStage dataEntryStage = eventCRFBean.getStage();
                        Status eventCRFStatus = eventCRFBean.getStatus();

                        logger.info("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage " + dataEntryStage.getName() + ", status "
                                + eventCRFStatus.getName());
                        if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
                                || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
                                || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
                                || dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
                            // actually want the negative
                            // was status == available and the stage questions, but
                            // when you are at 'data entry complete' your status is
                            // set to 'unavailable'.
                            // >> tbh 09/2008
                            // HOWEVER, when one event crf is removed and the rest
                            // are good, what happens???
                            // need to create a list and inform that one is blocked
                            // and the rest are not...
                            //
                            permittedEventCRFs.add(eventCRFBean);
                        } else {
                            // fail = true;
                            // addPageMessage(respage.getString(
                            // "the_event_crf_not_correct_status"));
                            // forwardPage(Page.IMPORT_CRF_DATA);
                        }
                    }

                    // so that we don't repeat this following message
                    // did we exclude all the event CRFs? if not, pass, else fail
                    if (eventCRFBeans.size() >= permittedEventCRFs.size()) {
                        addPageMessage(respage.getString("passed_event_crf_status_check"));
                    } else {
                        fail = true;
                        addPageMessage(respage.getString("the_event_crf_not_correct_status"));
                    }
                    // do they all have to have the right status to move
                    // forward? answer from bug tracker = no
                    // 5. do the items contain the correct data types?

                    // 6. are all the related OIDs present?
                    // that is to say, do we chain all the way down?
                    // this is covered by the OID Metadata Check

                    // 7. do the edit checks pass?
                    // only then can we pass on to VERIFY_IMPORT_SERVLET

                    // do we overwrite?

                    // XmlParser xp = new XmlParser();
                    // List<HashMap<String, String>> importedData =
                    // xp.getData(f);

                    // now we generate hard edit checks, and have to set that to the
                    // screen. get that from the service, generate a summary bean to
                    // set to either
                    // page in the workflow, either verifyImport.jsp or import.jsp

                    try {
                        List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();

                        tempDisplayItemBeanWrappers = getImportCRFDataService().lookupValidationErrors(request, odmContainer, ub, totalValidationErrors,
                                hardValidationErrors, permittedEventCRFs, locale);
                        logger.debug("generated display item bean wrappers " + tempDisplayItemBeanWrappers.size());
                        logger.debug("size of total validation errors: " + totalValidationErrors.size());
                        displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
                    } catch (NullPointerException npe1) {
                        // what if you have 2 event crfs but the third is a fake?
                        fail = true;
                        logger.debug("threw a NPE after calling lookup validation errors");
                        System.out.println(ExceptionUtils.getStackTrace(npe1));
                        addPageMessage(respage.getString("an_error_was_thrown_while_validation_errors"));
                        // npe1.printStackTrace();
                    } catch (OpenClinicaException oce1) {
                        fail = true;
                        logger.debug("threw an OCE after calling lookup validation errors " + oce1.getOpenClinicaMessage());
                        addPageMessage(oce1.getOpenClinicaMessage());
                    }
                } else if (!eventCRFStatusesValid) {
                    fail = true;
                    addPageMessage(respage.getString("the_event_crf_not_correct_status"));
                } else {
                    fail = true;
                    addPageMessage(respage.getString("no_event_crfs_matching_the_xml_metadata"));
                }
                // for (HashMap<String, String> crfData : importedData) {
                // DisplayItemBeanWrapper displayItemBeanWrapper =
                // testing(request,
                // crfData);
                // displayItemBeanWrappers.add(displayItemBeanWrapper);
                // errors = displayItemBeanWrapper.getValidationErrors();
                //
                // }
            }
            if (fail) {
                logger.debug("failed here - forwarding...");
                forwardPage(Page.IMPORT_CRF_DATA);
            } else {
                addPageMessage(respage.getString("passing_crf_edit_checks"));
                session.setAttribute("odmContainer", odmContainer);
                session.setAttribute("importedData", displayItemBeanWrappers);
                session.setAttribute("validationErrors", totalValidationErrors);
                session.setAttribute("hardValidationErrors", hardValidationErrors);
                session.setAttribute("importedCRFStatuses", importedCRFStatuses);
                session.setAttribute("importCrfInfo", importCrfInfo);
                // above are updated 'statically' by the method that originally
                // generated the wrappers; soon the only thing we will use
                // wrappers for is the 'overwrite' flag

                logger.debug("+++ content of total validation errors: " + totalValidationErrors.toString());
                SummaryStatsBean ssBean = getImportCRFDataService().generateSummaryStatsBean(odmContainer, displayItemBeanWrappers, importCrfInfo);
                session.setAttribute("summaryStats", ssBean);
                // will have to set hard edit checks here as well
                session.setAttribute("subjectData", odmContainer.getCrfDataPostImportContainer().getSubjectData());
                forwardPage(Page.VERIFY_IMPORT_SERVLET);
            }
            // }
        }

    }

    /*
     * Given the MultipartRequest extract the first File validate that it is an xml file and then return it.
     */
    private File getFirstFile() {
        File f = null;
        List<File> files = uploadHelper.returnFiles(request, context);
        for (File file : files) {
            // Enumeration files = multi.getFileNames();
            // if (files.hasMoreElements()) {
            // String name = (String) files.nextElement();
            // f = multi.getFile(name);
            f = file;
            if (f == null || f.getName() == null) {
                logger.info("file is empty.");
                Validator.addError(errors, "xml_file", "You have to provide an XML file!");
            } else if (f.getName().indexOf(".xml") < 0 && f.getName().indexOf(".XML") < 0) {
                logger.info("file name:" + f.getName());
                // TODO change the message below
                addPageMessage(respage.getString("file_you_uploaded_not_seem_xml_file"));
                f = null;
            }
        }
        return f;
    }

    /**
     * Uploads the xml file
     * 
     * @param version
     * @throws Exception
     */
    public File uploadFile(String theDir, CRFVersionBean version) throws Exception {

        return getFirstFile();
    }

    public ImportCRFDataService getImportCRFDataService() {
        return (ImportCRFDataService) SpringServletAccess.getApplicationContext(context).getBean("importCRFDataService");
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    public void errorCheck(List<String> errors) {
        if (errors != null) {
            // add to session
            // forward to another page
            logger.info(errors.toString());
            for (String error : errors) {
                addPageMessage(error);
            }
            if (errors.size() > 0) {
                // fail = true;
                forwardPage(Page.IMPORT_CRF_DATA);
            } else {
                addPageMessage(respage.getString("passed_study_check"));
                addPageMessage(respage.getString("passed_oid_metadata_check"));
            }

        }

    }

}
