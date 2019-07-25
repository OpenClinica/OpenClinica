package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class DicomUploaderServlet extends SecureController {

    public final static String PARTICIPANT_ID = "participantId";
    public final static String ACCESSION_ID = "accessionId";
    public final static String STUDY_OID = "studyOID";
    public final static String EVENT_OID = "eventOID";
    public final static String EVENT_REPEAT_KEY = "eventRepeatKey";
    public final static String FORM_OID = "formOID";
    public final static String ITEM_GROUP_OID = "itemGroupOID";
    public final static String ITEM_GROUP_REPEAT_KEY = "itemGroupRepeatKey";
    public final static String ITEM_OID = "itemOID";
    public final static String ACCESS_TOKEN = "accessToken";

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String participantId = fp.getString("participantId");
        String accessionId = fp.getString("accessionId");
        String studyOID = fp.getString("studyOID");
        String eventOID = fp.getString("eventOID");
        String eventRepeatKey = fp.getString("eventRepeatKey");
        String formOID = fp.getString("formOID");
        String itemGroupOID = fp.getString("itemGroupOID");
        String itemGroupRepeatKey = fp.getString("itemGroupRepeatKey");
        String itemOID = fp.getString("itemOID");

        request.setAttribute(PARTICIPANT_ID, participantId);
        request.setAttribute(ACCESSION_ID, accessionId);
        request.setAttribute(STUDY_OID, studyOID);
        request.setAttribute(EVENT_OID, eventOID);
        request.setAttribute(EVENT_REPEAT_KEY, eventRepeatKey);
        request.setAttribute(FORM_OID, formOID);
        request.setAttribute(ITEM_GROUP_OID, itemGroupOID);
        request.setAttribute(ITEM_GROUP_REPEAT_KEY, itemGroupRepeatKey);
        request.setAttribute(ITEM_OID, itemOID);
        request.setAttribute(ACCESS_TOKEN, request.getSession().getAttribute("accessToken"));

        forwardPage(Page.DICOM_UPLOADER);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }
}
