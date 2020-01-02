package org.akaza.openclinica.control.submit;

import java.util.HashMap;
import java.util.Map;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

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
        formURL = enketo.getFormPreviewURL(null,crf_oid + DASH + formLayout.getXform() + QUERY_FLAVOR);
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
        if(currentStudy != null) {
            Map<String, EnketoCredentials> credentialsMap = (Map<String, EnketoCredentials>) session.getAttribute("EnketoCredentialsMap");
            if (credentialsMap == null) {
                credentialsMap = new HashMap<String, EnketoCredentials>();
                credentials = EnketoCredentials.getInstance(currentStudy.getOc_oid());
                credentialsMap.put(currentStudy.getOc_oid(), credentials);
                session.setAttribute("EnketoCredentialsMap", credentialsMap);
            } else if (credentialsMap.get(currentStudy.getOc_oid()) == null) {
                credentials = EnketoCredentials.getInstance(currentStudy.getOc_oid());
                credentialsMap.put(currentStudy.getOc_oid(), credentials);
            } else
                credentials = credentialsMap.get(currentStudy.getOc_oid());
        }
        return credentials;
    }
}
