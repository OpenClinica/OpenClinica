/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.ListCRFRow;
import org.akaza.openclinica.web.pform.EnketoAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Lists all the CRF and their CRF versions
 *
 * @author jxu
 */
public class ListCRFServlet extends SecureController {
    Locale locale;

    // < ResourceBundle resexception,respage,resword,restext,resworkflow;
    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < resword =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",locale);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);
        // < resworkflow =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.workflow",locale);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);

        if (ub.isSysAdmin() || ub.isTechAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MANAGE_STUDY_SERVLET, resexception.getString("not_study_director"), "1");

    }

    /**
     * Finds all the crfs
     *
     */
    @Override
    public void processRequest() throws Exception {
        if (currentStudy.getParentStudyId() > 0) {
            addPageMessage(respage.getString("no_crf_available_study_is_a_site"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }

        session.removeAttribute("version");
        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);

        if(module.equalsIgnoreCase("admin") && !(ub.isSysAdmin()||ub.isTechAdmin())){
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }
        request.setAttribute(MODULE, module);
        
        // if coming from change crf version -> display message
        String crfVersionChangeMsg = fp.getString("isFromCRFVersionBatchChange");
        if (crfVersionChangeMsg != null && !crfVersionChangeMsg.equals("")) {
            addPageMessage(crfVersionChangeMsg);
        }

        String dir = SQLInitServlet.getField("filePath") + "crf" + File.separator + "new" + File.separator;// for
        // crf
        // version
        // spreadsheet
        logger.debug("found directory: " + dir);

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
        ArrayList crfs = (ArrayList) cdao.findAll();
        for (int i = 0; i < crfs.size(); i++) {
            CRFBean eb = (CRFBean) crfs.get(i);
            logger.debug("crf id:" + eb.getId());
            ArrayList versions = (ArrayList) vdao.findAllByCRF(eb.getId());

            // check whether the speadsheet is available on the server
            for (int j = 0; j < versions.size(); j++) {
                CRFVersionBean cv = (CRFVersionBean) versions.get(j);
                File file = new File(dir + eb.getId() + cv.getOid() + ".xls");
                logger.debug("looking in " + dir + eb.getId() + cv.getOid() + ".xls");
                if (file.exists()) {
                    cv.setDownloadable(true);
                } else {
                    File file2 = new File(dir + eb.getId() + cv.getName() + ".xls");
                    logger.debug("initial failed, looking in " + dir + eb.getId() + cv.getName() + ".xls");
                    if (file2.exists()) {
                        cv.setDownloadable(true);
                    }
                }
            }
            eb.setVersions(versions);
            

        }
        // request.setAttribute("crfs", crfs);

        EntityBeanTable table = fp.getEntityBeanTable();
        ArrayList allRows = ListCRFRow.generateRowsFromBeans(crfs);

        String[] columns =
          { resword.getString("CRF_name"), resword.getString("date_updated"), resword.getString("last_updated_by"), resword.getString("crf_oid"),
            resword.getString("versions"), resword.getString("version_oid"), resword.getString("date_created"), resword.getString("owner"),
            resword.getString("status"), resword.getString("download"), resword.getString("actions") };

        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(3);
        table.hideColumnLink(4); // oid column
        //BWP 3281: make the "owner" column sortable; table.hideColumnLink(7);
        table.hideColumnLink(8);
        table.setQuery("ListCRF", new HashMap());
        table.addLink(resword.getString("blank_CRF_template"), "DownloadVersionSpreadSheet?template=1");
        // YW << add "Enterprise CRF Catalog" link
        String crfCatalogField = "crfCatalog";
//        table.addLink(resword.getString("openclinica_CRF_catalog"), SQLInitServlet.getEnterpriseField(crfCatalogField));
        // YW >>
        // TODO add i18n links to the above, tbh
        table.addLink(resword.getString("create_a_new_CRF"), "CreateCRFVersion?module=" + module);
        table.setRows(allRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        request.setAttribute("study", currentStudy);

        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        panel.setSubmitDataModule(false);
        panel.setExtractData(false);
        panel.setCreateDataset(false);

        if (crfs.size() > 0) {
            setToPanel("CRFs", new Integer(crfs.size()).toString());
        }

        setToPanel(resword.getString("create_CRF"), respage.getString("br_create_new_CRF_entering"));

        setToPanel(resword.getString("create_CRF_version"), respage.getString("br_create_new_CRF_uploading"));
        setToPanel(resword.getString("revise_CRF_version"), respage.getString("br_if_you_owner_CRF_version"));
        setToPanel(resword.getString("CRF_spreadsheet_template"), respage.getString("br_download_blank_CRF_spreadsheet_from"));
        setToPanel(resword.getString("example_CRF_br_spreadsheets"), respage.getString("br_download_example_CRF_instructions_from"));
        forwardPage(Page.CRF_LIST);
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

}
