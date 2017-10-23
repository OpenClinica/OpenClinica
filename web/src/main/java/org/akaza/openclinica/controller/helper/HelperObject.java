package org.akaza.openclinica.controller.helper;

import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HelperObject {
    List<EventCRFBean> eventCrfListToMigrate;
    FormLayoutBean sourceCrfVersionBean;
    FormLayoutBean targetCrfVersionBean;
    ReportLog reportLog;
    StudyBean stBean;
    CRFBean cBean;
    HttpServletRequest request;
    DataSource dataSource;
    UserAccountBean userAccountBean;
    ResourceBundle resterms;
    String urlBase;
    OpenClinicaMailSender openClinicaMailSender;
    EventCrfDao eventCrfDao;
    StudyEventDao studyEventDao;
    StudySubjectDao studySubjectDao;
    CrfVersionDao crfVersionDao;
    FormLayoutDao formLayoutDao;
    SessionFactory sessionFactory;
    Session session;
    String schema;

    public HelperObject() {
        // TODO Auto-generated constructor stub
    }

    public FormLayoutBean getSourceCrfVersionBean() {
        return sourceCrfVersionBean;
    }

    public void setSourceCrfVersionBean(FormLayoutBean sourceCrfVersionBean) {
        this.sourceCrfVersionBean = sourceCrfVersionBean;
    }

    public FormLayoutBean getTargetCrfVersionBean() {
        return targetCrfVersionBean;
    }

    public void setTargetCrfVersionBean(FormLayoutBean targetCrfVersionBean) {
        this.targetCrfVersionBean = targetCrfVersionBean;
    }

    public ReportLog getReportLog() {
        return reportLog;
    }

    public void setReportLog(ReportLog reportLog) {
        this.reportLog = reportLog;
    }

    public StudyBean getStBean() {
        return stBean;
    }

    public void setStBean(StudyBean stBean) {
        this.stBean = stBean;
    }

    public CRFBean getcBean() {
        return cBean;
    }

    public void setcBean(CRFBean cBean) {
        this.cBean = cBean;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserAccountBean getUserAccountBean() {
        return userAccountBean;
    }

    public void setUserAccountBean(UserAccountBean userAccountBean) {
        this.userAccountBean = userAccountBean;
    }

    public ResourceBundle getResterms() {
        return resterms;
    }

    public void setResterms(ResourceBundle resterms) {
        this.resterms = resterms;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public OpenClinicaMailSender getOpenClinicaMailSender() {
        return openClinicaMailSender;
    }

    public void setOpenClinicaMailSender(OpenClinicaMailSender openClinicaMailSender) {
        this.openClinicaMailSender = openClinicaMailSender;
    }

    public List<EventCRFBean> getEventCrfListToMigrate() {
        return eventCrfListToMigrate;
    }

    public void setEventCrfListToMigrate(List<EventCRFBean> eventCrfListToMigrate) {
        this.eventCrfListToMigrate = eventCrfListToMigrate;
    }

    public EventCrfDao getEventCrfDao() {
        return eventCrfDao;
    }

    public void setEventCrfDao(EventCrfDao eventCrfDao) {
        this.eventCrfDao = eventCrfDao;
    }

    public StudyEventDao getStudyEventDao() {
        return studyEventDao;
    }

    public void setStudyEventDao(StudyEventDao studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    public StudySubjectDao getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(StudySubjectDao studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public CrfVersionDao getCrfVersionDao() {
        return crfVersionDao;
    }

    public void setCrfVersionDao(CrfVersionDao crfVersionDao) {
        this.crfVersionDao = crfVersionDao;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public FormLayoutDao getFormLayoutDao() {
        return formLayoutDao;
    }

    public void setFormLayoutDao(FormLayoutDao formLayoutDao) {
        this.formLayoutDao = formLayoutDao;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }
}
