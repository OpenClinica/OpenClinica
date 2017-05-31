package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.pform.PFormCache;

public class EnketoFormServlet extends SecureController {

    private static final long serialVersionUID = 6508949502349649137L;

    public static final String ORIGINATING_PAGE = "originatingPage";
    public static final String FORM_URL1 = "formURL1";
    public static final String FORM_URL2 = "formURL2";
    public static final String CRF_VERSION_ID = "crfVersionId";
    public static final String FORM_LAYOUT_ID = "formLayoutId";
    public static final String STUDY_EVENT_ID = "studyEventId";
    public static final String EVENT_CRF_ID = "eventCrfId";
    public static final String QUERY_FLAVOR = "-query";
    public static final String NO_FLAVOR = "";

    @Override
    protected void processRequest() throws Exception {
        FormLayoutDao formLayoutDao = (FormLayoutDao) SpringServletAccess.getApplicationContext(context).getBean("formLayoutDao");
        StudyEventDao studyEventDao = (StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
        EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
        EnketoCredentials enketoCredentials = (EnketoCredentials) SpringServletAccess.getApplicationContext(context).getBean("enketoCredentials");
        String originatingPage = request.getParameter(ORIGINATING_PAGE);
        String formLayoutId = request.getParameter(FORM_LAYOUT_ID);

        String studyEventId = request.getParameter(STUDY_EVENT_ID);
        String eventCrfId = request.getParameterValues(EVENT_CRF_ID)[0];
        String formUrl = null;

        StudyEvent studyEvent = studyEventDao.findByStudyEventId(Integer.valueOf(studyEventId));
        FormLayout formLayout = formLayoutDao.findById(Integer.valueOf(formLayoutId));

        // Cache the subject context for use during xform submission
        PFormCache cache = PFormCache.getInstance(context);
        PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
        subjectContext.setStudySubjectOid(studyEvent.getStudySubject().getOcOid());
        subjectContext.setStudyEventDefinitionId(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId());
        subjectContext.setOrdinal(studyEvent.getSampleOrdinal());
        subjectContext.setFormLayoutOid(formLayout.getOcOid());
        subjectContext.setUserAccountId(ub.getId());
        subjectContext.setStudyOid((currentStudy.getOid()));
        String contextHash = cache.putSubjectContext(subjectContext);
        Study study = enketoCredentials.getParentStudy(currentStudy.getOid());

        if (Integer.valueOf(eventCrfId) > 0) {
            formUrl = enketoUrlService.getEditUrl(contextHash, subjectContext, study.getOc_oid(), formLayout, studyEvent, QUERY_FLAVOR);
        } else {
            formUrl = enketoUrlService.getInitialDataEntryUrl(contextHash, subjectContext, study.getOc_oid(), QUERY_FLAVOR);
        }
        int hashIndex = formUrl.lastIndexOf("#");
        String part1 = formUrl;
        String part2 = "";
        if (hashIndex != -1) {
            part1 = formUrl.substring(0, hashIndex);
            part2 = formUrl.substring(hashIndex);
        }
        request.setAttribute(FORM_URL1, part1);
        request.setAttribute(FORM_URL2, part2);

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
