package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.domain.datamap.FormLayout;
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
    public static final String FORM_LAYOUT_ID = "formLayoutId";
    public static final String STUDY_EVENT_ID = "studyEventId";
    public static final String EVENT_CRF_ID = "eventCrfId";

    @Override
    protected void processRequest() throws Exception {
        // CrfVersionDao crfVersionDao = (CrfVersionDao)
        // SpringServletAccess.getApplicationContext(context).getBean("crfVersionDao");
        FormLayoutDao formLayoutDao = (FormLayoutDao) SpringServletAccess.getApplicationContext(context).getBean("formLayoutDao");
        StudyEventDao studyEventDao = (StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
        EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
        String originatingPage = request.getParameter(ORIGINATING_PAGE);
        // String crfVersionId = request.getParameter(CRF_VERSION_ID);
        String formLayoutId = request.getParameter(FORM_LAYOUT_ID);

        String studyEventId = request.getParameter(STUDY_EVENT_ID);
        String eventCrfId = request.getParameterValues(EVENT_CRF_ID)[0];
        String formUrl = null;

        StudyEvent studyEvent = studyEventDao.findByStudyEventId(Integer.valueOf(studyEventId));
        // CrfVersion crfVersion = crfVersionDao.findByCrfVersionId(Integer.valueOf(crfVersionId));
        FormLayout formLayout = formLayoutDao.findById(Integer.valueOf(formLayoutId));

        // Cache the subject context for use during xform submission
        PFormCache cache = PFormCache.getInstance(context);
        PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
        subjectContext.setStudySubjectOid(studyEvent.getStudySubject().getOcOid());
        subjectContext.setStudyEventDefinitionId(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId());
        subjectContext.setOrdinal(studyEvent.getSampleOrdinal());
        subjectContext.setFormLayoutOid(formLayout.getOcOid());
        // subjectContext.setCrfVersionOid(crfVersion.getOcOid());
        subjectContext.setUserAccountId(ub.getId());
        String contextHash = cache.putSubjectContext(subjectContext);
        String flavor = "-query";

        if (Integer.valueOf(eventCrfId) > 0) {
            formUrl = enketoUrlService.getEditUrl(contextHash, subjectContext, currentStudy.getOid(), formLayout, studyEvent, flavor);
        } else {
            formUrl = enketoUrlService.getInitialDataEntryUrl(contextHash, subjectContext, currentStudy.getOid(), flavor);
        }
        request.setAttribute(FORM_URL, formUrl);
        // request.setAttribute(FORM_URL, "https://enke.to/i/::widgets?a=b");
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
