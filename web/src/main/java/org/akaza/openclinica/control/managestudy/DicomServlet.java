package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class DicomServlet extends SecureController {

    public final static String PARTICIPANT_ID = "pid";
    public final static String ACCESSION_ID = "accid";
    public final static String TARGET = "target";

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String participantId = fp.getString("pid");
        String accessionId = fp.getString("accid");
        String target = fp.getString("target");

        request.setAttribute(PARTICIPANT_ID, participantId);
        request.setAttribute(ACCESSION_ID, accessionId);
        request.setAttribute(TARGET, target);

        forwardPage(Page.DICOM_UPLOADER);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }
}
