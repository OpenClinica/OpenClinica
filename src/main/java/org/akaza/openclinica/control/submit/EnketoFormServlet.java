package org.akaza.openclinica.control.submit;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.core.LockInfo;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.xform.XformParser;
import core.org.akaza.openclinica.domain.xform.dto.*;
import core.org.akaza.openclinica.service.crfdata.EnketoUrlService;
import core.org.akaza.openclinica.service.crfdata.FormUrlObject;
import core.org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import core.org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import core.org.akaza.openclinica.web.pform.OpenRosaServices;
import core.org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class EnketoFormServlet extends SecureController {

    private static final long serialVersionUID = 6508949502349649137L;

    public static final String STUDYSUBJECTID = "studySubjectId";
    public static final String MODE = "mode";
    public static final String FORM_URL = "formURL";
    public static final String CRF_VERSION_ID = "crfVersionId";
    public static final String FORM_LAYOUT_ID = "formLayoutId";
    public static final String STUDY_EVENT_ID = "studyEventId";
    public static final String EVENT_CRF_ID = "eventCrfId";
    public static final String QUERY_FLAVOR = "-query";
    public static final String PARTICIPATE_FLAVOR = "-participate";
    public static final String NO_FLAVOR = "";
    public static final String VIEW_MODE = "view";
    public static final String EDIT_MODE = "edit";
    public static final String PARTICIPATE_MODE = "participate";
    public static final String PREVIEW_MODE = "preview";
    public static final String JINI = "jini";

    @Override
    protected void processRequest() throws Exception {
        FormLayoutDao formLayoutDao = (FormLayoutDao) SpringServletAccess.getApplicationContext(context).getBean("formLayoutDao");
        StudyEventDao studyEventDao = (StudyEventDao) SpringServletAccess.getApplicationContext(context).getBean("studyEventDaoDomain");
        EventCrfDao eventCrfDao = (EventCrfDao) SpringServletAccess.getApplicationContext(context).getBean("eventCrfDao");

        EnketoUrlService enketoUrlService = (EnketoUrlService) SpringServletAccess.getApplicationContext(context).getBean("enketoUrlService");
        EnketoCredentials enketoCredentials = (EnketoCredentials) SpringServletAccess.getApplicationContext(context).getBean("enketoCredentials");
        XformParser xformParser = (XformParser) SpringServletAccess.getApplicationContext(context).getBean("xformParser");
        OpenRosaServices openRosaServices = (OpenRosaServices) SpringServletAccess.getApplicationContext(context).getBean("openRosaServices");

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
            request.setAttribute(STUDYSUBJECTID, studyEvent.getStudySubject().getLabel());
        }
        subjectContext.setFormLayoutOid(formLayout.getOcOid());
        subjectContext.setUserAccountId(String.valueOf(ub.getId()));
        subjectContext.setStudyOid((currentStudy.getOc_oid()));
        subjectContext.setFormLoadMode(mode);
        contextHash = cache.putSubjectContext(subjectContext);
        logger.info("Subject Context info *** {} *** ",subjectContext.toString());

        Study parentStudy = enketoCredentials.getParentStudy(currentStudy.getOc_oid());
        StudyUserRoleBean currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        Role role = currentRole.getRole();
        EventCrf eventCrf = null;
        boolean preview=false;
        if(eventCrfId==0 && studyEvent==null){
            preview=true;
        }


        if (eventCrfId == 0 && studyEvent != null && formLayout != null) {
            eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEventId, studyEvent.getStudySubject().getStudySubjectId(), formLayoutId);
            if (eventCrf != null) {
                eventCrfId = eventCrf.getEventCrfId();
                logger.debug("The event crf status changed from not-started to started");
            }

        } else {
            eventCrf = eventCrfDao.findById(eventCrfId);
        }
        final EventCrf ec = eventCrf;
        if (eventCrfId != 0 || studyEvent != null) {
            if (!StringUtils.equalsIgnoreCase(mode, "preview") &&
                    hasFormAccess(ec) != true) {
                forwardPage(Page.NO_ACCESS);
                return;
            }
        }
        String loadWarning = generateErrorMessage(studyEvent, formLayout);
        boolean isFormLocked = determineCRFLock(studyEvent, formLayout);

        String flavor = "";
        if (mode.equals(PARTICIPATE_MODE)) {
            flavor = PARTICIPATE_FLAVOR;
        } else {
            flavor = QUERY_FLAVOR;
        }


   List<Bind> binds = openRosaServices.getBinds(formLayout, flavor, parentStudy.getOc_oid());
        boolean formContainsContactData=false;
        if(openRosaServices.isFormContainsContactData(binds))
            formContainsContactData=true;


        if (Integer.valueOf(eventCrfId) > 0 || (Integer.valueOf(eventCrfId) == 0 && formContainsContactData && !preview)) {
            logger.info("eventCrfId:" + eventCrfId + " user:" + ub.getName());
            formUrlObject = enketoUrlService.getActionUrl(contextHash, subjectContext, parentStudy.getOc_oid(), formLayout,
                    flavor, null, role, mode, loadWarning, isFormLocked,formContainsContactData,binds,ub);
        } else if (Integer.valueOf(eventCrfId) == 0) {
            logger.info("eventCrfId is zero user:" + ub.getName());
            String hash = formLayout.getXform();
            formUrlObject = enketoUrlService.getInitialDataEntryUrl(contextHash, subjectContext, parentStudy.getOc_oid(),
                    flavor, role, mode, hash, loadWarning, isFormLocked);
        }
        logger.info("isFormLocked: " + isFormLocked + ": formUrlObject.isLockOn()" + formUrlObject.isLockOn() + " for user:" + ub.getName());
        if (!isFormLocked && formUrlObject.isLockOn()) {
            getEventCrfLocker().lock(studyEvent, formLayout, currentPublicStudy.getSchemaName(), ub.getId(), request.getSession().getId());
        }
        request.setAttribute(FORM_URL, formUrlObject.getFormUrl());
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

    private boolean determineCRFLock(StudyEvent studyEvent, FormLayout formLayout) {
        if (getEventCrfLocker().isLocked(studyEvent, formLayout, currentPublicStudy.getSchemaName(), ub.getId(), request.getSession().getId())) {
            return true;
        }
        return false;

    }

    private String generateErrorMessage(StudyEvent studyEvent, FormLayout formLayout) {
        LockInfo lockInfo = getEventCrfLocker().getLockOwner(studyEvent, formLayout, currentPublicStudy.getSchemaName());
        if (lockInfo == null)
            return null;
        UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
        UserAccountBean ubean = (UserAccountBean) udao.findByPK(lockInfo.getUserId());
        String errorData = resword.getString("CRF_unavailable")
                 + " User " + ubean.getName() + " " + resword.getString("Currently_entering_data")
                + " " + resword.getString("CRF_reopen_enter_data");
        request.setAttribute("errorData", errorData);
        return errorData;
    }
}
