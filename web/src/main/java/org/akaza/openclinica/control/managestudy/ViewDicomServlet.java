package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.service.DicomServiceClient;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.servlet.ServletException;

/**
 * Servlet to handle viewing DICOM images. This servlet calls dicom-service to retrieve the view url and redirects the user to that url.
 * @author svadla@openclinica.com
 */
public class ViewDicomServlet extends SecureController {

    @Autowired
    private DicomServiceClient dicomServiceClient;

    @Override
    public void init() throws ServletException {
        super.init();
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String participantId = fp.getString("pid");
        String accessionId = fp.getString("accid");
        String accessToken = (String) request.getSession().getAttribute("accessToken");
        String dicomViewUrl = dicomServiceClient.getDicomViewUrl(accessToken, participantId, accessionId);
        if (StringUtils.isNotBlank(dicomViewUrl)) {
            response.sendRedirect(dicomViewUrl);
        } else {
            forwardPage(Page.ERROR);
        }
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        // There are no access restrictions for this page. Anyone with the link to this page should be able to view DICOM images.
    }
}
