package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class DicomServlet extends SecureController {

    public final static String PARTICIPANT_ID = "pid";
    public final static String ACCESSION_ID = "accid";
    public final static String TARGET = "target";
    public final static String STUDY_OID = "studyOID";
    public final static String EVENT_OID = "eventOID";
    public final static String EVENT_REPEAT_KEY = "eventRepeatKey";
    public final static String FORM_OID = "formOID";
    public final static String ITEM_GROUP_OID = "itemGroupOID";
    public final static String ITEM_GROUP_REPEAT_KEY = "itemGroupRepeatKey";
    public final static String ITEM_OID = "itemOID";

    @Override
    protected void processRequest() throws Exception {
        /*
        Example URL : https://kai.eu.openclinica.io/OpenClinica/Dicom?pid=ppp&accid=aaa&target=sss.eee[er].fff.ggg[gr].iii
        ppp = PID
        aaa = Accession
        sss = Study OID
        eee = Event OID
        er = Event Repeat Key
        fff = Form OID
        ggg = Item Group OID
        gr = Item Group Repeat Key
        iii = Item OID
        */
        FormProcessor fp = new FormProcessor(request);
        String participantId = fp.getString("pid");
        String accessionId = fp.getString("accid");
        String studyOID = null, eventOID = null, eventRepeatKey = null, formOID = null,
                itemGroupOID = null, itemGroupRepeatKey = null, itemOID = null;
        String target = fp.getString("target");
        String[] targetArr = target.split("\\.");

        if (targetArr.length > 0) {
            studyOID = targetArr[0];
            if (targetArr.length > 1 && !targetArr[1].isEmpty()) {
                String[] erk = targetArr[1].split("\\[");
                eventOID = erk[0];
                if (erk.length > 1) {
                    eventRepeatKey = erk[1].split("]")[0];
                }
                if (targetArr.length > 2 && !targetArr[2].isEmpty()) {
                    formOID = targetArr[2];
                    if ( targetArr.length > 3 && !targetArr[3].isEmpty()) {
                        String[] igoid = targetArr[3].split("\\[");
                        itemGroupOID = igoid[0];
                        if (igoid.length > 1) {
                            itemGroupRepeatKey = igoid[1].split("]")[0];
                        }
                        if (targetArr.length > 4 && !targetArr[4].isEmpty())
                            itemOID = targetArr[4];
                    }
                }
            }
        }

        request.setAttribute(PARTICIPANT_ID, participantId);
        request.setAttribute(ACCESSION_ID, accessionId);
        request.setAttribute(STUDY_OID, studyOID);
        request.setAttribute(EVENT_OID, eventOID);
        request.setAttribute(EVENT_REPEAT_KEY, eventRepeatKey);
        request.setAttribute(FORM_OID, formOID);
        request.setAttribute(ITEM_GROUP_OID, itemGroupOID);
        request.setAttribute(ITEM_GROUP_REPEAT_KEY, itemGroupRepeatKey);
        request.setAttribute(ITEM_OID, itemOID);

        forwardPage(Page.DICOM_UPLOADER);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {

    }
}
