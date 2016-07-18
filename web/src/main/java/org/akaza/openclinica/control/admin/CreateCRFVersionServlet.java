/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.admin.NewCRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.exception.CRFReadingException;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Create a new CRF verison by uploading excel file
 * 
 * @author jxu
 */
public class CreateCRFVersionServlet extends SecureController {

    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();

    // < ResourceBundleresword,resexception,respage;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        if (ub.isSysAdmin()) {
            return;
        }
        Role r = currentRole.getRole();
        if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR)) {
            return;
        }
        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(true);

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());

        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);
        // keep the module in the session
        session.setAttribute(MODULE, module);
        request.setAttribute("xformEnabled", CoreResources.getField("xform.enabled"));
        String action = request.getParameter("action");
        CRFVersionBean version = (CRFVersionBean) session.getAttribute("version");

        if (StringUtil.isBlank(action)) {
            logger.debug("action is blank");
            request.setAttribute("version", version);
            forwardPage(Page.CREATE_CRF_VERSION);
        } else if ("confirm".equalsIgnoreCase(action)) {
            String dir = SQLInitServlet.getField("filePath");
            if (!new File(dir).exists()) {
                logger.debug("The filePath in datainfo.properties is invalid " + dir);
                addPageMessage(resword.getString("the_filepath_you_defined"));
                forwardPage(Page.CREATE_CRF_VERSION);
                // BWP 01/13/2009 >>
                return;
                // >>
            }
            // All the uploaded files will be saved in filePath/crf/original/
            String theDir = dir + "crf" + File.separator + "original" + File.separator;
            if (!new File(theDir).isDirectory()) {
                new File(theDir).mkdirs();
                logger.debug("Made the directory " + theDir);
            }
            // MultipartRequest multi = new MultipartRequest(request, theDir, 50 * 1024 * 1024);
            String tempFile = "";
            try {
                tempFile = uploadFile(theDir, version);
            } catch (CRFReadingException crfException) {
                Validator.addError(errors, "excel_file", crfException.getMessage());
                String msg = crfException.getMessage();
                request.setAttribute("formMessages", errors);
                forwardPage(Page.CREATE_CRF_VERSION);
                return;
            } catch (Exception e) {
                //
                logger.warn("*** Found exception during file upload***");
                e.printStackTrace();
            }
            session.setAttribute("tempFileName", tempFile);
            // YW, at this point, if there are errors, they point to no file
            // provided and/or not xls format
            if (errors.isEmpty()) {
                String s = ((NewCRFBean) session.getAttribute("nib")).getVersionName();
                if (s.length() > 255) {
                    Validator.addError(errors, "excel_file", resword.getString("the_version_CRF_version_more_than_255"));
                } else if (s.length() <= 0) {
                    Validator.addError(errors, "excel_file", resword.getString("the_VERSION_column_was_blank"));
                }
                version.setName(s);
                if (version.getCrfId() == 0) {
                    version.setCrfId(fp.getInt("crfId"));
                }
                session.setAttribute("version", version);
            }
            if (!errors.isEmpty()) {
                logger.debug("has validation errors ");
                request.setAttribute("formMessages", errors);
                forwardPage(Page.CREATE_CRF_VERSION);
            } else {
                CRFBean crf = (CRFBean) cdao.findByPK(version.getCrfId());
                ArrayList versions = (ArrayList) vdao.findAllByCRF(crf.getId());
                for (int i = 0; i < versions.size(); i++) {
                    CRFVersionBean version1 = (CRFVersionBean) versions.get(i);
                    if (version.getName().equals(version1.getName())) {
                        // version already exists
                        logger.debug("Version already exists; owner or not:" + ub.getId() + "," + version1.getOwnerId());
                        if (ub.getId() != version1.getOwnerId()) {// not owner
                            addPageMessage(respage.getString("CRF_version_try_upload_exists_database") + version1.getOwner().getName()
                                    + respage.getString("please_contact_owner_to_delete"));
                            forwardPage(Page.CREATE_CRF_VERSION);
                            return;
                        } else {// owner,
                            ArrayList definitions = edao.findByDefaultVersion(version1.getId());
                            if (!definitions.isEmpty()) {// used in
                                // definition
                                request.setAttribute("definitions", definitions);
                                forwardPage(Page.REMOVE_CRF_VERSION_DEF);
                                return;
                            } else {// not used in definition
                                int previousVersionId = version1.getId();
                                version.setId(previousVersionId);
                                session.setAttribute("version", version);
                                session.setAttribute("previousVersionId", new Integer(previousVersionId));
                                forwardPage(Page.REMOVE_CRF_VERSION_CONFIRM);
                                return;
                            }
                        }
                    }
                }
                // didn't find same version in the DB,let user upload the excel
                // file
                logger.debug("didn't find same version in the DB,let user upload the excel file.");

                // List excelErr =
                // ((ArrayList)request.getAttribute("excelErrors"));
                List excelErr = (ArrayList) session.getAttribute("excelErrors");
                logger.debug("excelErr.isEmpty()=" + excelErr.isEmpty());
                if (excelErr != null && excelErr.isEmpty()) {
                    addPageMessage(resword.getString("congratulations_your_spreadsheet_no_errors"));
                    forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PREVIEW);
                } else {
                    logger.debug("OpenClinicaException thrown, forwarding to CREATE_CRF_VERSION_CONFIRM.");
                    forwardPage(Page.CREATE_CRF_VERSION_CONFIRM);
                }

                return;
            }
        } else if ("confirmsql".equalsIgnoreCase(action)) {
            NewCRFBean nib = (NewCRFBean) session.getAttribute("nib");
            if (nib != null && nib.getItemQueries() != null) {
                request.setAttribute("openQueries", nib.getItemQueries());
            } else {
                request.setAttribute("openQueries", new HashMap());
            }
            boolean canDelete = false;
            // check whether need to delete previous version
            Boolean deletePreviousVersion = (Boolean) session.getAttribute("deletePreviousVersion");
            Integer previousVersionId = (Integer) session.getAttribute("previousVersionId");
            if (deletePreviousVersion != null && deletePreviousVersion.equals(Boolean.TRUE) && previousVersionId != null && previousVersionId.intValue() > 0) {
                logger.debug("Need to delete previous version");
                // whether we can delete
                canDelete = canDeleteVersion(previousVersionId.intValue());
                if (!canDelete) {
                    logger.debug("but cannot delete previous version");
                    if (session.getAttribute("itemsHaveData") == null && session.getAttribute("eventsForVersion") == null) {
                        addPageMessage(respage.getString("you_are_not_owner_some_items_cannot_delete"));
                    }
                    if (session.getAttribute("itemsHaveData") == null) {
                        session.setAttribute("itemsHaveData", new ArrayList());
                    }
                    if (session.getAttribute("eventsForVersion") == null) {
                        session.setAttribute("eventsForVersion", new ArrayList());
                    }
                    forwardPage(Page.CREATE_CRF_VERSION_NODELETE);
                    return;
                }
                ArrayList<ItemBean> nonSharedItems = (ArrayList<ItemBean>) vdao.findNotSharedItemsByVersion(previousVersionId.intValue());
                // htaycher: here is the trick we need to put in nib1.setItemQueries()
                // update statements for shared items and insert for nonShared that were just deleted 5927
                HashMap item_table_statements = new HashMap();
                ArrayList<String> temp = new ArrayList<String>(nonSharedItems.size());

                for (ItemBean item : nonSharedItems) {
                    temp.add(item.getName());
                    item_table_statements.put(item.getName(), nib.getBackupItemQueries().get(item.getName()));
                }
                for (String item_name : (Set<String>) nib.getItemQueries().keySet()) {
                    // check if item shared
                    if (!temp.contains(item_name)) {
                        item_table_statements.put(item_name, nib.getItemQueries().get(item_name));
                    }
                }
                // statements to run
                if (!nonSharedItems.isEmpty()) {
                    request.setAttribute("openQueries", item_table_statements);
                }

                // htaycher: put all statements in
                nib.setItemQueries(item_table_statements);
                session.setAttribute("nib", nib);
            }

            // submit
            logger.debug("commit sql");
            NewCRFBean nib1 = (NewCRFBean) session.getAttribute("nib");
            if (nib1 != null) {
                try {
                    if (canDelete) {
                        nib1.deleteInsertToDB();
                    } else {
                        nib1.insertToDB();
                    }
                    request.setAttribute("queries", nib1.getQueries());
                    // YW << for add a link to "View CRF Version Data Entry".
                    // For this purpose, CRFVersion id is needed.
                    // So the latest CRFVersion Id of A CRF Id is it.
                    CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
                    ArrayList crfvbeans = new ArrayList();

                    logger.debug("CRF-ID [" + version.getCrfId() + "]");
                    int crfVersionId = 0;
                    String versionOID = null;
                    if (version.getCrfId() != 0) {
                        crfvbeans = cvdao.findAllByCRFId(version.getCrfId());
                        CRFVersionBean cvbean = (CRFVersionBean) crfvbeans.get(crfvbeans.size() - 1);
                        crfVersionId = cvbean.getId();
                        versionOID = cvbean.getOid();
                        for (Iterator iter = crfvbeans.iterator(); iter.hasNext();) {
                            cvbean = (CRFVersionBean) iter.next();
                            if (crfVersionId < cvbean.getId()) {
                                crfVersionId = cvbean.getId();
                            }
                        }
                    }
                    // Not needed; crfVersionId will be autoboxed in Java 5
                    // this was added for the old CVS java compiler
                    Integer cfvID = new Integer(crfVersionId);
                    if (cfvID == 0) {
                        cfvID = cvdao.findCRFVersionId(nib1.getCrfId(), nib1.getVersionName());
                    }
                    CRFVersionBean finalVersion = (CRFVersionBean) cvdao.findByPK(cfvID);
                    version.setCrfId(nib1.getCrfId());

                    version.setOid(finalVersion.getOid());

                    CRFBean crfBean = (CRFBean) cdao.findByPK(version.getCrfId());
                    crfBean.setUpdatedDate(version.getCreatedDate());
                    crfBean.setUpdater(ub);
                    cdao.update(crfBean);

                    // workaround to get a correct file name below, tbh 06/2008
                    request.setAttribute("crfVersionId", cfvID);
                    // YW >>
                    // return those properties to initial values
                    session.removeAttribute("version");
                    session.removeAttribute("eventsForVersion");
                    session.removeAttribute("itemsHaveData");
                    session.removeAttribute("nib");
                    session.removeAttribute("deletePreviousVersion");
                    session.removeAttribute("previousVersionId");

                    // save new version spreadsheet
                    String tempFile = (String) session.getAttribute("tempFileName");
                    if (tempFile != null) {
                        logger.debug("*** ^^^ *** saving new version spreadsheet" + tempFile);
                        try {
                            String dir = SQLInitServlet.getField("filePath");
                            File f = new File(dir + "crf" + File.separator + "original" + File.separator + tempFile);
                            // check to see whether crf/new/ folder exists
                            // inside, if not,
                            // creates
                            // the crf/new/ folder
                            String finalDir = dir + "crf" + File.separator + "new" + File.separator;

                            if (!new File(finalDir).isDirectory()) {
                                logger.debug("need to create folder for excel files" + finalDir);
                                new File(finalDir).mkdirs();
                            }

                            // String newFile = version.getCrfId() +
                            // version.getName() + ".xls";

                            String newFile = version.getCrfId() + version.getOid() + ".xls";
                            logger.debug("*** ^^^ *** new file: " + newFile);
                            File nf = new File(finalDir + newFile);
                            logger.debug("copying old file " + f.getName() + " to new file " + nf.getName());
                            copy(f, nf);
                            // ?
                        } catch (IOException ie) {
                            logger.debug("==============");
                            addPageMessage(respage.getString("CRF_version_spreadsheet_could_not_saved_contact"));
                        }

                    }
                    session.removeAttribute("tempFileName");
                    session.removeAttribute(MODULE);
                    session.removeAttribute("excelErrors");
                    session.removeAttribute("htmlTab");
                    forwardPage(Page.CREATE_CRF_VERSION_DONE);
                } catch (OpenClinicaException pe) {
                    logger.debug("--------------");
                    session.setAttribute("excelErrors", nib1.getErrors());
                    // request.setAttribute("excelErrors", nib1.getErrors());
                    forwardPage(Page.CREATE_CRF_VERSION_ERROR);
                }
            } else {
                forwardPage(Page.CREATE_CRF_VERSION);
            }
        } else if ("delete".equalsIgnoreCase(action)) {
            logger.debug("user wants to delete previous version");
            List excelErr = (ArrayList) session.getAttribute("excelErrors");
            logger.debug("for overwrite CRF version, excelErr.isEmpty()=" + excelErr.isEmpty());
            if (excelErr != null && excelErr.isEmpty()) {
                addPageMessage(resword.getString("congratulations_your_spreadsheet_no_errors"));
                session.setAttribute("deletePreviousVersion", Boolean.TRUE);// should be moved to excelErr != null block
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PREVIEW);
            } else {
                session.setAttribute("deletePreviousVersion", Boolean.FALSE);// should be moved to excelErr != null
                                                                             // block
                logger.debug("OpenClinicaException thrown, forwarding to CREATE_CRF_VERSION_CONFIRM.");
                forwardPage(Page.CREATE_CRF_VERSION_CONFIRM);
            }

        }
    }

    /**
     * Uploads the excel version file
     * 
     * @param version
     * @throws Exception
     */
    public String uploadFile(String theDir, CRFVersionBean version) throws Exception {
        List<File> theFiles = uploadHelper.returnFiles(request, context, theDir);
        // Enumeration files = multi.getFileNames();
        errors.remove("excel_file");
        String tempFile = null;
        for (File f : theFiles) {
            // while (files.hasMoreElements()) {
            // String name = (String) files.nextElement();
            // File f = multi.getFile(name);
            if (f == null || f.getName() == null) {
                logger.debug("file is empty.");
                Validator.addError(errors, "excel_file", resword.getString("you_have_to_provide_spreadsheet"));
                session.setAttribute("version", version);
                return tempFile;
            } else if (f.getName().indexOf(".xls") < 0 && f.getName().indexOf(".XLS") < 0) {
                logger.debug("file name:" + f.getName());
                Validator.addError(errors, "excel_file", respage.getString("file_you_uploaded_not_seem_excel_spreadsheet"));
                session.setAttribute("version", version);
                return tempFile;

            } else {
                logger.debug("file name:" + f.getName());
                tempFile = f.getName();
                // create the inputstream here, so that it can be enclosed in a
                // try/finally block and closed :: BWP, 06/08/2007
                FileInputStream inStream = null;
                FileInputStream inStreamClassic = null;
                SpreadSheetTableRepeating htab = null;
                SpreadSheetTableClassic sstc = null;
                // create newCRFBean here
                NewCRFBean nib = null;
                try {
                    inStream = new FileInputStream(theDir + tempFile);

                    // *** now change the code here to generate sstable, tbh
                    // 06/07
                    htab = new SpreadSheetTableRepeating(inStream, ub,
                    // SpreadSheetTable htab = new SpreadSheetTable(new
                    // FileInputStream(theDir + tempFile), ub,
                            version.getName(), locale, currentStudy.getId());

                    htab.setMeasurementUnitDao((MeasurementUnitDao) SpringServletAccess.getApplicationContext(context).getBean("measurementUnitDao"));

                    if (!htab.isRepeating()) {
                        inStreamClassic = new FileInputStream(theDir + tempFile);
                        sstc = new SpreadSheetTableClassic(inStreamClassic, ub, version.getName(), locale, currentStudy.getId());
                        sstc.setMeasurementUnitDao((MeasurementUnitDao) SpringServletAccess.getApplicationContext(context).getBean("measurementUnitDao"));
                    }
                    // logger.debug("finishing with feedin file-input-stream, did
                    // we error out here?");

                    if (htab.isRepeating()) {
                        htab.setCrfId(version.getCrfId());
                        // not the best place for this but for now...
                        session.setAttribute("new_table", "y");
                    } else {
                        sstc.setCrfId(version.getCrfId());
                    }

                    if (htab.isRepeating()) {
                        nib = htab.toNewCRF(sm.getDataSource(), respage);
                    } else {
                        nib = sstc.toNewCRF(sm.getDataSource(), respage);
                    }

                    // bwp; 2/28/07; updated 6/11/07;
                    // This object is created to pull preview information out of
                    // the
                    // spreadsheet
                    HSSFWorkbook workbook = null;
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(theDir + tempFile);
                        workbook = new HSSFWorkbook(inputStream);
                        // Store the Sections, Items, Groups, and CRF name and
                        // version information
                        // so they can be displayed in a preview. The Map
                        // consists of the
                        // names "sections," "items," "groups," and "crf_info"
                        // as keys, each of which point
                        // to a Map containing data on those CRF sections.

                        // Check if it's the old template
                        Preview preview;
                        if (htab.isRepeating()) {

                            // the preview uses date formatting with default
                            // values in date fields: yyyy-MM-dd
                            preview = new SpreadsheetPreviewNw();

                        } else {
                            preview = new SpreadsheetPreview();

                        }
                        session.setAttribute("preview_crf", preview.createCrfMetaObject(workbook));
                    } catch (Exception exc) { // opening the stream could
                        // throw FileNotFoundException
                        exc.printStackTrace();
                        String message = resword.getString("the_application_encountered_a_problem_uploading_CRF");
                        logger.debug(message + ": " + exc.getMessage());
                        this.addPageMessage(message);
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException io) {
                                // ignore this close()-related exception
                            }
                        }
                    }
                    ArrayList ibs = isItemSame(nib.getItems(), version);

                    if (!ibs.isEmpty()) {
                        ArrayList warnings = new ArrayList();
                        warnings.add(resexception.getString("you_may_not_modify_items"));
                        for (int i = 0; i < ibs.size(); i++) {
                            ItemBean ib = (ItemBean) ibs.get(i);
                            if (ib.getOwner().getId() == ub.getId()) {
                                warnings.add(resword.getString("the_item") + " '" + ib.getName() + "' "
                                        + resexception.getString("in_your_spreadsheet_already_exists") + ib.getDescription() + "), DATA_TYPE("
                                        + ib.getDataType().getName() + "), UNITS(" + ib.getUnits() + "), " + resword.getString("and_or") + " PHI_STATUS("
                                        + ib.isPhiStatus() + "). UNITS " + resword.getString("and") + " DATA_TYPE(PDATE to DATE) "
                                        + resexception.getString("will_not_be_changed_if") + " PHI, DESCRIPTION, DATA_TYPE from PDATE to DATE "
                                        + resexception.getString("will_be_changed_if_you_continue"));
                            } else {
                                warnings.add(resword.getString("the_item") + " '" + ib.getName() + "' "
                                        + resexception.getString("in_your_spreadsheet_already_exists") + ib.getDescription() + "), DATA_TYPE("
                                        + ib.getDataType().getName() + "), UNITS(" + ib.getUnits() + "), " + resword.getString("and_or") + " PHI_STATUS("
                                        + ib.isPhiStatus() + "). " + resexception.getString("these_field_cannot_be_modified_because_not_owner"));
                            }

                            request.setAttribute("warnings", warnings);
                        }
                    }
                    ItemBean ib = isResponseValid(nib.getItems(), version);
                    if (ib != null) {

                        nib.getErrors().add(
                                resword.getString("the_item") + ": " + ib.getName() + " " + resexception.getString("in_your_spreadsheet_already_exits_in_DB"));
                    }
                } catch (IOException io) {
                    logger.warn("Opening up the Excel file caused an error. the error message is: " + io.getMessage());

                } finally {
                    if (inStream != null) {
                        try {
                            inStream.close();
                        } catch (IOException ioe) {
                        }
                    }
                    if (inStreamClassic != null) {
                        try {
                            inStreamClassic.close();
                        } catch (IOException ioe) {
                        }
                    }
                }
                // request.setAttribute("excelErrors", .getErrors());
                session.setAttribute("excelErrors", nib.getErrors());
                session.setAttribute("htmlTable", nib.getHtmlTable());
                session.setAttribute("nib", nib);
            }
        }
        return tempFile;
    }

    /**
     * Checks whether the version can be deleted
     * 
     * @param previousVersionId
     * @return
     */
    private boolean canDeleteVersion(int previousVersionId) {
        CRFVersionDAO cdao = new CRFVersionDAO(sm.getDataSource());
        ArrayList items = null;
        ArrayList itemsHaveData = new ArrayList();
        // boolean isItemUsedByOtherVersion =
        // cdao.isItemUsedByOtherVersion(previousVersionId);
        // if (isItemUsedByOtherVersion) {
        // ArrayList itemsUsedByOtherVersion = (ArrayList)
        // cdao.findItemUsedByOtherVersion(previousVersionId);
        // session.setAttribute("itemsUsedByOtherVersion",itemsUsedByOtherVersion);
        // return false;
        EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
        ArrayList events = ecdao.findAllByCRFVersion(previousVersionId);
        if (!events.isEmpty()) {
            session.setAttribute("eventsForVersion", events);
            return false;
        }
        items = cdao.findNotSharedItemsByVersion(previousVersionId);
        for (int i = 0; i < items.size(); i++) {
            ItemBean item = (ItemBean) items.get(i);
            if (ub.getId() != item.getOwner().getId()) {
                logger.debug("not owner" + item.getOwner().getId() + "<>" + ub.getId());
                return false;
            }
            if (cdao.hasItemData(item.getId())) {
                itemsHaveData.add(item);
                logger.debug("item has data");
                session.setAttribute("itemsHaveData", itemsHaveData);
                return false;
            }
        }

        // user is the owner and item not have data,
        // delete previous version with non-shared items
        NewCRFBean nib = (NewCRFBean) session.getAttribute("nib");
        nib.setDeleteQueries(cdao.generateDeleteQueries(previousVersionId, items));
        session.setAttribute("nib", nib);
        return true;

    }

    /**
     * Checks whether the item with same name has the same other fields: units, phi_status if no, they are two different
     * items, cannot have the same same
     * 
     * @param items
     *            items from excel
     * @return the items found
     */
    private ArrayList isItemSame(HashMap items, CRFVersionBean version) {
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ArrayList diffItems = new ArrayList();
        Set names = items.keySet();
        Iterator it = names.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            ItemBean newItem = (ItemBean) idao.findByNameAndCRFId(name, version.getCrfId());
            ItemBean item = (ItemBean) items.get(name);
            if (newItem.getId() > 0) {
                if (!item.getUnits().equalsIgnoreCase(newItem.getUnits()) || item.isPhiStatus() != newItem.isPhiStatus()
                        || item.getDataType().getId() != newItem.getDataType().getId() || !item.getDescription().equalsIgnoreCase(newItem.getDescription())) {

                    logger.debug("found two items with same name but different units/phi/datatype/description");

                    diffItems.add(newItem);
                }
            }
        }

        return diffItems;
    }

    private ItemBean isResponseValid(HashMap items, CRFVersionBean version) {
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemFormMetadataDAO metadao = new ItemFormMetadataDAO(sm.getDataSource());
        Set names = items.keySet();
        Iterator it = names.iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            ItemBean oldItem = (ItemBean) idao.findByNameAndCRFId(name, version.getCrfId());
            ItemBean item = (ItemBean) items.get(name);
            if (oldItem.getId() > 0) {// found same item in DB
                ArrayList metas = metadao.findAllByItemId(oldItem.getId());
                for (int i = 0; i < metas.size(); i++) {
                    ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metas.get(i);
                    ResponseSetBean rsb = ifmb.getResponseSet();
                    if (hasDifferentOption(rsb, item.getItemMeta().getResponseSet()) != null) {
                        return item;
                    }
                }

            }
        }
        return null;

    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    /**
     * When the version is added, for each non-new item OpenClinica should check the RESPONSE_OPTIONS_TEXT, and
     * RESPONSE_VALUES used for the item in other versions of the CRF.
     * 
     * For a given RESPONSE_VALUES code, the associated RESPONSE_OPTIONS_TEXT string is different than in a previous
     * version
     * 
     * For a given RESPONSE_OPTIONS_TEXT string, the associated RESPONSE_VALUES code is different than in a previous
     * version
     * 
     * @param oldRes
     * @param newRes
     * @return The original option
     */
    public ResponseOptionBean hasDifferentOption(ResponseSetBean oldRes, ResponseSetBean newRes) {
        ArrayList oldOptions = oldRes.getOptions();
        ArrayList newOptions = newRes.getOptions();
        if (oldOptions.size() != newOptions.size()) {
            // if the sizes are different, means the options don't match
            return null;

        } else {
            for (int i = 0; i < oldOptions.size(); i++) {// from database
                ResponseOptionBean rob = (ResponseOptionBean) oldOptions.get(i);
                String text = rob.getText();
                String value = rob.getValue();
                for (int j = i; j < newOptions.size(); j++) {// from
                    // spreadsheet
                    ResponseOptionBean rob1 = (ResponseOptionBean) newOptions.get(j);
                    // changed by jxu on 08-29-06, to fix the problem of cannot
                    // recognize
                    // the same responses
                    String text1 = restoreQuotes(rob1.getText());

                    String value1 = restoreQuotes(rob1.getValue());

                    if (StringUtil.isBlank(text1) && StringUtil.isBlank(value1)) {
                        // this response label appears in the spreadsheet
                        // multiple times, so
                        // ignore the checking for the repeated ones
                        break;
                    }
                    if (text1.equalsIgnoreCase(text) && !value1.equals(value)) {
                        logger.debug("different response value:" + value1 + "|" + value);
                        return rob;
                    } else if (!text1.equalsIgnoreCase(text) && value1.equals(value)) {
                        logger.debug("different response text:" + text1 + "|" + text);
                        return rob;
                    }
                    break;
                }
            }

        }
        return null;
    }

    /**
     * Copy one file to another
     * 
     * @param src
     * @param dst
     * @throws IOException
     */
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * restoreQuotes, utility function meant to replace double quotes in strings with single quote. Don''t -> Don't, for
     * example. If the option text has single quote, it is changed to double quotes for SQL compatability, so we will
     * change it back before the comparison
     * 
     * @param subj
     *            the subject line
     * @return A string with all the quotes escaped.
     */
    public String restoreQuotes(String subj) {
        if (subj == null) {
            return null;
        }
        String returnme = "";
        String[] subjarray = subj.split("''");
        if (subjarray.length == 1) {
            returnme = subjarray[0];
        } else {
            for (int i = 0; i < subjarray.length - 1; i++) {
                returnme += subjarray[i];
                returnme += "'";
            }
            returnme += subjarray[subjarray.length - 1];
        }
        return returnme;
    }
}
