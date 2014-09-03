
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.pform.EnketoAPI;

public class ParticipantFormServlet extends SecureController {

	public static final String CRF_ID = "crfOID";
	public static final String FORM_URL = "formURL";

	@Override
	protected void processRequest() throws Exception {
		String crf_oid = request.getParameter(CRF_ID);
        String formURL = null;
        
        //Build Enketo URL  for CRF version.
        //TODO: In upcoming stories need to check whether study is enabled for Participant Portal.
        boolean participantPortalEnabled = true;
        if (participantPortalEnabled)
        {
            String ocOpenRosaURL = CoreResources.getField("sysURL.base") + "rest2/openrosa/S_DEFAULTS1/";
            //TODO: In upcoming stories, the parameters should be pulled from somewhere rather than hardcoded.
            String enketoToken = "enketorules";
            String enketoURL = "http://192.168.15.187:8005/api/v1";
            EnketoAPI enketo = new EnketoAPI(enketoURL,enketoToken,ocOpenRosaURL);  
            formURL = enketo.getFormURL(crf_oid);
            response.sendRedirect(formURL);
        }
        forwardPage(Page.PARTICIPANT_FORM_SERVLET);
	}

	@Override
	protected void mayProceed() throws InsufficientPermissionException {
		// Can validate user has proper permissions to access this page.
		// Throw InsufficientPermissionException if they don't.
		// For now we allow everyone access to this page.
		return;		
	}

}
