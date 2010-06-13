package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.job.ImportSpringJob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * view Import File Server, by Tom Hickerson, 2010
 * @author thickerson, purpose is to be able to show an external file in a log
 * to a user 
 *
 */
public class ViewLogMessageServlet extends SecureController {

    private static final String LOG_MESSAGE = "logmsg";
    private static final String FILE_NAME = "filename";

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {// ?

            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");// TODO

        // allow only admin-level users, currently

    }

    @Override
    protected void processRequest() throws Exception {
        try {
            File destDirectory = new File(ImportSpringJob.IMPORT_DIR_2);
            FormProcessor fp = new FormProcessor(request);
            String regex = "\\s+"; // all whitespace, one or more times
            String replacement = "_"; // replace with underscores
            String fileName = fp.getString("n");
            File logDestDirectory =
                new File(destDirectory + File.separator + fileName.replaceAll(regex, replacement) + ".log.txt" + File.separator + "log.txt");
            //            StringBuffer sbu = new StringBuffer();
            //            BufferedReader r = new BufferedReader(new FileReader(logDestDirectory));
            //            char[] buffer = new char[1024];
            //            int amount = 0;
            //            while ((amount = r.read(buffer, 0, buffer.length)) != -1) {
            //                sbu.append(buffer);
            //            }
            //            r.close();
            String fileContents = readFromFile(logDestDirectory);
            request.setAttribute(this.LOG_MESSAGE, fileContents);
            request.setAttribute(this.FILE_NAME, fileName);
            forwardPage(Page.VIEW_LOG_MESSAGE);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("found IO exception: " + e.getMessage());
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            //throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");
            forwardPage(Page.MENU);
        }
    }

    public static String readFromFile(File filename) throws java.io.FileNotFoundException, java.io.IOException {

        StringBuffer readBuffer = new StringBuffer();
        BufferedReader fileReader = new BufferedReader(new FileReader(filename));

        char[] readChars = new char[1024];
        int count;
        while ((count = fileReader.read(readChars)) >= 0) {
            readBuffer.append(readChars, 0, count);
        }
        fileReader.close();
        return readBuffer.toString();

    }

}
