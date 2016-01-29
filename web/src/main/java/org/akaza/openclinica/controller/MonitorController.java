package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.login.UserDTO;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.MonitorViewDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.MonitorView;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.fop.area.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import java.awt.print.Pageable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
@RequestMapping(value = "/monitoring")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class MonitorController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    MonitorViewDao monitorViewDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    StudyDAO sdao;

    @RequestMapping(value = "/paginated", params = { "studyoid", "page", "per_page" }, method = RequestMethod.GET)
    public  ResponseEntity<List<MonitorView>> getPaginatedMonitorViewData(@RequestParam("studyoid") String studyOid, @RequestParam("page") int page,
            @RequestParam("per_page") int per_page) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        List<MonitorView> monitorViewDTO = null;
        if (page == 0)
            page = 1;
        if (per_page == 0)
            per_page = 30; // default to 30 records / page

        System.out.println("I'm in getPaginatedMonitorViewData method");

        StudyBean parentStudy = getParentStudy(studyOid);
        Integer pStudyId = parentStudy.getId();
        Integer studyId = getStudy(studyOid).getId();
        if (studyId == pStudyId) {
            // parent Study
            monitorViewDTO = getMonitorViewDao().findPaginatedMonitorViewData(studyId, pStudyId, per_page, page ,"OR");
        } else {
            // Site
            monitorViewDTO = getMonitorViewDao().findPaginatedMonitorViewData(studyId, pStudyId, per_page, page ,"AND");
        }
        return new ResponseEntity<List <MonitorView>>(monitorViewDTO, HttpStatus.OK);

    }

    @RequestMapping(value = "/totalcount", params = { "studyoid"}, method = RequestMethod.GET)
    public  ResponseEntity <Integer> getTotalCountMonitorViewData(@RequestParam("studyoid") String studyOid ) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        int totalCount=0;
        System.out.println("I'm in getTotalCountMonitorViewData method");

        StudyBean parentStudy = getParentStudy(studyOid);
        Integer pStudyId = parentStudy.getId();
        Integer studyId = getStudy(studyOid).getId();
        if (studyId == pStudyId) {
            // parent Study
            totalCount = getMonitorViewDao().findTotalCountMonitorViewData(studyId, pStudyId,"OR");
        } else {
            // Site
            totalCount = getMonitorViewDao().findTotalCountMonitorViewData(studyId, pStudyId,"AND");
        }
        return new ResponseEntity <Integer>(totalCount, HttpStatus.OK);

    }

    

    
    
    
    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private StudyBean getStudy(Integer id) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    private StudyBean getParentStudy(Integer studyId) {
        StudyBean study = getStudy(studyId);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }
    public MonitorViewDao getMonitorViewDao() {
        return monitorViewDao;
    }

    public void setMonitorViewDao(MonitorViewDao monitorViewDao) {
        this.monitorViewDao = monitorViewDao;
    }

}
