package org.akaza.openclinica.controller.helper;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudyParameterValueDao;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * An "interceptor" class that sets up a UserAccount and stores it in the Session, before
 * another class is initialized and potentially uses that UserAccount.
 */
public class EnrollmentInterceptor extends HandlerInterceptorAdapter {

    public static final String USER_BEAN_NAME = "userBean";

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;
    @Autowired
    private StudyParameterValueDao studyParameterValueDao;
    
    @Autowired
    private StudyDao studyDao;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        if (httpServletRequest.getAttribute("enrollmentCapped") == null && httpServletRequest.getRequestURI().contains(httpServletRequest.getContextPath() + "/pages/")) {

            boolean isCapped = isEnrollmentCapped(httpServletRequest);
            httpServletRequest.setAttribute("enrollmentCapped", isCapped);
        }
        return true;
    }

    private boolean isEnrollmentCapEnforced(HttpServletRequest httpServletRequest, Study currentStudy) {
        String enrollmentCapStatus = currentStudy.getEnforceEnrollmentCap();
        if (enrollmentCapStatus == null)
            enrollmentCapStatus = "false";

        boolean capEnforced = Boolean.valueOf(enrollmentCapStatus);
        return capEnforced;
    }

    protected boolean isEnrollmentCapped(HttpServletRequest httpServletRequest) {

        boolean capIsOn;
        HttpSession session = httpServletRequest.getSession();
        Study currentStudy = (Study) session.getAttribute("study");

        if (currentStudy != null && currentStudy.getStudyId() != 0) {
            if (currentStudy.getStatus() != null && currentStudy.getStatus().isAvailable()) {


                StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
                int numberOfSubjects = studySubjectDAO.getCountofActiveStudySubjects();

                Study sb;
                if (currentStudy.isSite()) {
                    sb = (Study) studyDao.findByPK(currentStudy.getStudy().getStudyId());
                } else {
                    sb = (Study) studyDao.findByPK(currentStudy.getStudyId());
                }
                capIsOn = isEnrollmentCapEnforced(httpServletRequest, sb);
                int expectedTotalEnrollment = sb.getExpectedTotalEnrollment();

                if (numberOfSubjects >= expectedTotalEnrollment && capIsOn)
                    return true;
                else
                    return false;
            }
        }

        // If there is no current study it shouldn't matter if the variable is set to false.
        return false;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public StudyParameterValueDao getStudyParameterValueDao() {
        return studyParameterValueDao;
    }

    public void setStudyParameterValueDao(StudyParameterValueDao studyParameterValueDao) {
        this.studyParameterValueDao = studyParameterValueDao;
    }
}
