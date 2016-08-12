package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.pform.PFormCache;

public class EnketoFormServlet extends SecureController {

    private static final long serialVersionUID = 6508949502349649137L;

    public static final String ORIGINATING_PAGE = "originatingPage";
    public static final String FORM_URL = "formURL";
    public static final String CRF_VERSION_ID = "crfVersionId";
    public static final String STUDY_EVENT_ID = "studyEventId";
    public static final String EVENT_CRF_ID = "eventCrfId";

    @Override
    protected void processRequest() throws Exception {
        CrfVersionDao crfVersionDao = (CrfVersionDao) SpringServletAccess.getApplicationContext(context).getBean("crfVersionDao");
        StudyEventDao studyEventDao = (StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
        EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
        
        String originatingPage = request.getParameter(ORIGINATING_PAGE);
        String crfVersionId = request.getParameter(CRF_VERSION_ID);
        String studyEventId = request.getParameter(STUDY_EVENT_ID);
        String eventCrfId = request.getParameterValues(EVENT_CRF_ID)[0];
        String formUrl = null;

        StudyEvent studyEvent = studyEventDao.findByStudyEventId(Integer.valueOf(studyEventId));
        CrfVersion crfVersion = crfVersionDao.findByCrfVersionId(Integer.valueOf(crfVersionId));
        
        //Cache the subject context for use during xform submission
        PFormCache cache = PFormCache.getInstance(context);
        PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
        subjectContext.setStudySubjectOid(studyEvent.getStudySubject().getOcOid());
        subjectContext.setStudyEventDefinitionId(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId());
        subjectContext.setOrdinal(studyEvent.getSampleOrdinal());
        subjectContext.setCrfVersionOid(crfVersion.getOcOid());
        String contextHash = cache.putSubjectContext(subjectContext);
        

        if (Integer.valueOf(eventCrfId) > 0) {
            //Get Edit Url
            formUrl = enketoUrlService.getEditUrl(contextHash, subjectContext, currentStudy.getOid());
        } else {
            //Get Normal Url
            formUrl = enketoUrlService.getUrl(contextHash, subjectContext, currentStudy.getOid());
        }
        request.setAttribute(FORM_URL, formUrl);
        request.setAttribute(ORIGINATING_PAGE, originatingPage);

        forwardPage(Page.ENKETO_FORM_SERVLET);
    }


    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        // Can validate user has proper permissions to access this page.
        // Throw InsufficientPermissionException if they don't.
        // For now we allow everyone access to this page.
        return;
    }

}
