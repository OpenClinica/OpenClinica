package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.service.crfdata.EnketoUrlService;
import org.akaza.openclinica.service.crfdata.FormUrlObject;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.lang.StringUtils;

public class EnketoFormServlet extends SecureController {

    private static final long serialVersionUID = 6508949502349649137L;

    public static final String ORIGINATING_PAGE = "originatingPage";
    public static final String STUDYSUBJECTID = "studySubjectId";
    public static final String MODE = "mode";
    public static final String FORM_URL1 = "formURL1";
    public static final String FORM_URL2 = "formURL2";
    public static final String CRF_VERSION_ID = "crfVersionId";
    public static final String FORM_LAYOUT_ID = "formLayoutId";
    public static final String STUDY_EVENT_ID = "studyEventId";
    public static final String EVENT_CRF_ID = "eventCrfId";
    public static final String QUERY_FLAVOR = "-query";
    public static final String NO_FLAVOR = "";
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";

    @Override
    protected void processRequest() throws Exception {
        FormLayoutDao formLayoutDao = (FormLayoutDao) SpringServletAccess.getApplicationContext(context).getBean("formLayoutDao");
        StudyEventDao studyEventDao = (StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
        EventCrfDao eventCrfDao = (EventCrfDao) SpringServletAccess.getApplicationContext(context).getBean("eventCrfDao");

        EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
        EnketoCredentials enketoCredentials = (EnketoCredentials) SpringServletAccess.getApplicationContext(context).getBean("enketoCredentials");
        String mode = request.getParameter(MODE);
        String originatingPage = request.getParameter(ORIGINATING_PAGE);
        request.setAttribute(ORIGINATING_PAGE, originatingPage);
        int formLayoutId = Integer.valueOf(request.getParameter(FORM_LAYOUT_ID));
        int studyEventId = Integer.valueOf(request.getParameter(STUDY_EVENT_ID));
        int eventCrfId = Integer.valueOf(request.getParameter(EVENT_CRF_ID));


        FormUrlObject formUrlObject = null;

        StudyEvent studyEvent = studyEventDao.findById(Integer.valueOf(studyEventId));
        FormLayout formLayout = formLayoutDao.findById(Integer.valueOf(formLayoutId));

        // Cache the subject context for use during xform submission
        PFormCache cache = PFormCache.getInstance(context);
        PFormCacheSubjectContextEntry subjectContext = new PFormCacheSubjectContextEntry();
        String contextHash = null;
        if (studyEvent != null) {
            StudySubject studySubject = studyEvent.getStudySubject();
            subjectContext.setStudySubjectOid(studySubject.getOcOid());
            subjectContext.setStudyEventDefinitionId(String.valueOf(studyEvent.getStudyEventDefinition().getStudyEventDefinitionId()));
            subjectContext.setOrdinal(String.valueOf(studyEvent.getSampleOrdinal()));
            subjectContext.setStudyEventId(String.valueOf(studyEvent.getStudyEventId()));
        }
        subjectContext.setFormLayoutOid(formLayout.getOcOid());
        subjectContext.setUserAccountId(String.valueOf(ub.getId()));
        subjectContext.setStudyOid((currentStudy.getOid()));
        subjectContext.setFormLoadMode(mode);
        contextHash = cache.putSubjectContext(subjectContext);

        Study parentStudy = enketoCredentials.getParentStudy(currentStudy.getOid());
        StudyUserRoleBean currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        Role role = currentRole.getRole();
        if (eventCrfId == 0 && studyEvent != null && formLayout != null) {
            EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEventId, studyEvent.getStudySubject().getStudySubjectId(), formLayoutId);
            if (eventCrf != null) {
                eventCrfId = eventCrf.getEventCrfId();
                logger.debug("The event crf status changed from not-started to started");
            }

        }

        String loadWarning = generateErrorMessage(studyEvent, formLayout);
        boolean isFormLocked = determineCRFLock(studyEvent, formLayout);
        if (Integer.valueOf(eventCrfId) > 0) {
            formUrlObject = enketoUrlService.getActionUrl(contextHash, subjectContext, parentStudy.getOc_oid(), formLayout,
                    QUERY_FLAVOR, null, role, mode, loadWarning, isFormLocked);
        } else if (Integer.valueOf(eventCrfId) == 0) {
            String hash = formLayout.getXform();
            formUrlObject = enketoUrlService.getInitialDataEntryUrl(contextHash, subjectContext, parentStudy.getOc_oid(),
                    QUERY_FLAVOR, role, mode, hash, loadWarning, isFormLocked);
        }
        if (!isFormLocked && formUrlObject.isLockOn()) {
            getEventCrfLocker().lock(studyEvent, formLayout, currentPublicStudy.getSchemaName(), ub.getId());
        }
        int hashIndex = formUrlObject.getFormUrl().lastIndexOf("#");
        String part1 = formUrlObject.getFormUrl();
        String part2 = "";
        if (hashIndex != -1) {
            part1 = formUrlObject.getFormUrl().substring(0, hashIndex);
            part2 = formUrlObject.getFormUrl().substring(hashIndex);
        }
        request.setAttribute(FORM_URL1, part1);
        request.setAttribute(FORM_URL2, part2);

        // request.setAttribute(FORM_URL, "https://enke.to/i/::widgets?a=b");
        request.setAttribute(ORIGINATING_PAGE, originatingPage);
        if (studyEvent != null) {
            request.setAttribute(STUDYSUBJECTID, studyEvent.getStudySubject().getLabel());
        }
        forwardPage(Page.ENKETO_FORM_SERVLET);
    }

    @Override
    protected void mayProceed() throws InsufficientPermissionException {
        // Can validate user has proper permissions to access this page.
        // Throw InsufficientPermissionException if they don't.
        // For now we allow everyone access to this page.
        return;
    }

    private boolean determineCRFLock(StudyEvent studyEvent, FormLayout formLayout) {
        if (getEventCrfLocker().isLocked(studyEvent, formLayout, currentPublicStudy.getSchemaName(), ub.getId())) {
            return true;
        }
        return false;

    }

    private String generateErrorMessage(StudyEvent studyEvent, FormLayout formLayout) {
        Integer userId = getEventCrfLocker().getLockOwner(studyEvent, formLayout, currentPublicStudy.getSchemaName());
        if (userId == null)
            return null;
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean ubean = (UserAccountBean) udao.findByPK(userId);
        String errorData = resword.getString("CRF_unavailable")
                 + " User " + ubean.getName() + " " + resword.getString("Currently_entering_data")
                + " " + resword.getString("CRF_reopen_enter_data");
        request.setAttribute("errorData", errorData);
        return errorData;
    }
}
