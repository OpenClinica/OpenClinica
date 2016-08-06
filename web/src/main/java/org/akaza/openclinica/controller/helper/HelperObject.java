package org.akaza.openclinica.controller.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.core.OpenClinicaMailSender;

public class HelperObject {
    List<EventCRFBean> crfMigrationReportList;
    CRFVersionBean sourceCrfVersionBean;
    CRFVersionBean targetCrfVersionBean;
    ReportLog reportLog;
    StudyBean stBean;
    CRFBean cBean;
    HttpServletRequest request;
    DataSource dataSource;
    UserAccountBean userAccountBean;
    ResourceBundle resterms;
    String urlBase;
    OpenClinicaMailSender openClinicaMailSender;

    public HelperObject(ArrayList<EventCRFBean> crfMigrationReportList, CRFVersionBean sourceCrfVersionBean, CRFVersionBean targetCrfVersionBean,
            ReportLog reportLog, StudyBean stBean, CRFBean cBean, HttpServletRequest request, DataSource dataSource, UserAccountBean userAccountBean,
            ResourceBundle resterms, String urlBase, OpenClinicaMailSender openClinicaMailSender) {
        super();
        this.crfMigrationReportList = crfMigrationReportList;
        this.sourceCrfVersionBean = sourceCrfVersionBean;
        this.targetCrfVersionBean = targetCrfVersionBean;
        this.reportLog = reportLog;
        this.stBean = stBean;
        this.cBean = cBean;
        this.request = request;
        this.dataSource = dataSource;
        this.userAccountBean = userAccountBean;
        this.resterms = resterms;
        this.urlBase = urlBase;
        this.openClinicaMailSender = openClinicaMailSender;
    }

    public HelperObject() {
        // TODO Auto-generated constructor stub
    }

    public List<EventCRFBean> getCrfMigrationReportList() {
        return crfMigrationReportList;
    }

    public void setCrfMigrationReportList(List<EventCRFBean> crfMigrationReportList2) {
        this.crfMigrationReportList = crfMigrationReportList2;
    }

    public CRFVersionBean getSourceCrfVersionBean() {
        return sourceCrfVersionBean;
    }

    public void setSourceCrfVersionBean(CRFVersionBean sourceCrfVersionBean) {
        this.sourceCrfVersionBean = sourceCrfVersionBean;
    }

    public CRFVersionBean getTargetCrfVersionBean() {
        return targetCrfVersionBean;
    }

    public void setTargetCrfVersionBean(CRFVersionBean targetCrfVersionBean) {
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

}
