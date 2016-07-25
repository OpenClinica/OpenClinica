package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.dao.admin.AuditDAO;
import java.util.ArrayList;

public class ViewItemAuditLogServlet extends SecureController {

    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }

        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"));
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS, resexception.getString("not_study_director"), "1");
    }

    public void processRequest () throws Exception{
        AuditDAO adao = new AuditDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);
        String auditTable = fp.getString("auditTable");
        if(auditTable.equalsIgnoreCase("studysub")){
            auditTable = "study_subject";
        }else if(auditTable.equalsIgnoreCase("eventcrf")){
            auditTable = "event_crf";
        }else if(auditTable.equalsIgnoreCase("studyevent")){
            auditTable = "study_event";
        }else if(auditTable.equalsIgnoreCase("itemdata")){
            auditTable = "item_data";
        }
        int entityId = fp.getInt("entityId");
        ArrayList itemAuditEvents = adao.findItemAuditEvents(entityId, auditTable);
        request.setAttribute("itemAudits", itemAuditEvents);
        forwardPage(Page.AUDIT_LOGS_ITEMS);
    }
}
