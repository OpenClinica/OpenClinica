package org.akaza.openclinica.control.admin;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.rule.FileUploadHelper;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

public class CreateXformCRFVersionServlet extends SecureController {
    Locale locale;
    FileUploadHelper uploadHelper = new FileUploadHelper();

    @Override
    protected void processRequest() throws Exception {
        // TODO Auto-generated method stub
        System.out.println("hello world");

        FormProcessor fp = new FormProcessor(request);
        // checks which module the requests are from
        String module = fp.getString(MODULE);

        System.out.println("Received an xform:");
        System.out.println(fp.getString("xformText"));

        String dir = SQLInitServlet.getField("filePath");
        if (!new File(dir).exists()) {
            logger.debug("The filePath in datainfo.properties is invalid " + dir);
            addPageMessage(resword.getString("the_filepath_you_defined"));
            forwardPage(Page.CREATE_CRF_VERSION);
            return;
        }
        // All the uploaded files will be saved in filePath/crf/original/
        String theDir = dir + "crf" + File.separator + "original" + File.separator;
        if (!new File(theDir).isDirectory()) {
            new File(theDir).mkdirs();
            logger.debug("Made the directory " + theDir);
        }

        List<File> theFiles = uploadHelper.returnFiles(request, context, theDir);

        forwardPage(Page.CREATE_XFORM_CRF_VERSION_SERVLET);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
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

}
