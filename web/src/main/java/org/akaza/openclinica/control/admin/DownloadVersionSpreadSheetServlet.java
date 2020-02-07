/* OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.servlet.ServletOutputStream;

/**
 * @author jxu
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class DownloadVersionSpreadSheetServlet extends SecureController {
    public static String CRF_ID = "crfId";

    public static String CRF_VERSION_NAME = "crfVersionName";

    public static String CRF_VERSION_ID = "crfVersionId";

    public static String CRF_VERSION_TEMPLATE = "CRF_Template_lc_v1.0.xls";

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MANAGE_STUDY_SERVLET, resexception.getString("not_study_director"), "1");

    }

    private CoreResources getCoreResources() {
        return (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources");
    }

    @Override
    public void processRequest() throws Exception {
        String dir = SQLInitServlet.getField("filePath") + "crf" + File.separator + "new" + File.separator;
        // YW 09-10-2007 << Now CRF_Design_Template_v2.xls is located at
        // $CATALINA_HOME/webapps/OpenClinica-instanceName/properties
        FormProcessor fp = new FormProcessor(request);

        String crfIdString = fp.getString(CRF_ID);
        int crfVersionId = fp.getInt(CRF_VERSION_ID);

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());

        CRFVersionBean version = (CRFVersionBean) cvdao.findByPK(crfVersionId);

        boolean isTemplate = fp.getBoolean("template");

        String excelFileName = crfIdString + version.getOid() + ".xls";

        // aha, what if it's the old style? next line is for backwards compat,
        // tbh 07/2008
        File excelFile = null;
        String oldExcelFileName = crfIdString + version.getName() + ".xls";
        if (isTemplate) {
            // excelFile = new File(dir + CRF_VERSION_TEMPLATE);
            excelFile = getCoreResources().getFile(CRF_VERSION_TEMPLATE, "crf" + File.separator + "original" + File.separator);
            excelFileName = CRF_VERSION_TEMPLATE;
            // FileOutputStream fos = new FileOutputStream(excelFile);
            // IOUtils.copy(getCoreResources().getInputStream(CRF_VERSION_TEMPLATE), fos);
            // IOUtils.closeQuietly(fos);
        } else {
            excelFile = new File(dir + excelFileName);
            // backwards compat
            File oldExcelFile = new File(dir + oldExcelFileName);
            if (oldExcelFile.exists() && oldExcelFile.length() > 0) {
                if (!excelFile.exists() || excelFile.length() <= 0) {
                    // if the old name exists and the new name does not...
                    excelFile = oldExcelFile;
                    excelFileName = oldExcelFileName;
                }
            }

        }
        logger.info("looking for : " + excelFile.getName());
        if (!excelFile.exists() || excelFile.length() <= 0) {
            addPageMessage(respage.getString("the_excel_is_not_available_on_server_contact"));
            forwardPage(Page.CRF_LIST_SERVLET);
        } else {
            response.setHeader("Content-disposition", "attachment; filename=\"" + excelFileName + "\";");
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Pragma", "public");

            ServletOutputStream op = response.getOutputStream();
            DataInputStream in = null;
            try {
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Pragma", "public");
                response.setContentLength((int) excelFile.length());

                byte[] bbuf = new byte[(int) excelFile.length()];
                in = new DataInputStream(new FileInputStream(excelFile));
                int length;
                while ((in != null) && ((length = in.read(bbuf)) != -1)) {
                    op.write(bbuf, 0, length);
                }

                in.close();
                op.flush();
                op.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            } finally {
                if (in != null) {
                    in.close();
                }
                if (op != null) {
                    op.close();
                }
            }
        }

    }
}
