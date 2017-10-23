package org.akaza.openclinica.control.submit;

import java.util.HashMap;
import java.util.Map;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

public class ParticipantFormServlet extends SecureController {

    public static final String CRF_ID = "crfOID";
    public static final String FORM_URL = "formURL";
    public static final String QUERY_FLAVOR = "-query";
    public static final String DASH = "-";

    @Override
    protected void processRequest() throws Exception {
        String crf_oid = request.getParameter(CRF_ID);
        String formURL = null;

        FormLayoutDao formLayoutDao = (FormLayoutDao) SpringServletAccess.getApplicationContext(context).getBean("formLayoutDao");
        FormLayout formLayout = formLayoutDao.findByOcOID(crf_oid);

        // Build Enketo URL for CRF version.
        EnketoCredentials credentials = getCredentials();
        EnketoAPI enketo = new EnketoAPI(credentials);
        context.setAttribute("SS_OID", "");
        formURL = enketo.getFormPreviewURL(crf_oid + DASH + formLayout.getXform() + QUERY_FLAVOR);
        if (!formURL.equals("")) {
            response.sendRedirect(formURL);
        } else {
            if (credentials.getServerUrl() == null) {
                addPageMessage(respage.getString("pform_preview_missing_url"));
            } else {
                if ((credentials.getApiKey() != null) && (credentials.getOcInstanceUrl() != null)) {
                    addPageMessage(respage.getString("pform_preview_forbidden"));
                } else {
                    addPageMessage(respage.getString("participate_not_available"));
                }
            }
            forwardPage(Page.MENU_SERVLET);
        }
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        // Can validate user has proper permissions to access this page.
        // Throw InsufficientPermissionException if they don't.
        // For now we allow everyone access to this page.
        return;
    }

    private EnketoCredentials getCredentials() throws Exception {
        EnketoCredentials credentials = null;
        Map<String, EnketoCredentials> credentialsMap = (Map<String, EnketoCredentials>) session.getAttribute("EnketoCredentialsMap");
        if (credentialsMap == null) {
            credentialsMap = new HashMap<String, EnketoCredentials>();
            credentials = EnketoCredentials.getInstance(currentStudy.getOid());
            credentialsMap.put(currentStudy.getOid(), credentials);
            session.setAttribute("EnketoCredentialsMap", credentialsMap);
        } else if (credentialsMap.get(currentStudy.getOid()) == null) {
            credentials = EnketoCredentials.getInstance(currentStudy.getOid());
            credentialsMap.put(currentStudy.getOid(), credentials);
        } else
            credentials = credentialsMap.get(currentStudy.getOid());

        return credentials;
    }
}
