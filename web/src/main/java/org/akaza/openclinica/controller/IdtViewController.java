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
import org.akaza.openclinica.dao.hibernate.IdtViewDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.datamap.IdtView;
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
public class IdtViewController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    IdtViewDao idtViewDao;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    StudyDAO sdao;

    @RequestMapping(value = "/paginated", params = { "studyoid", "page", "per_page" }, method = RequestMethod.GET)
    public ResponseEntity<List<IdtView>> getPaginatedIdtViewData(@RequestParam("studyoid") String studyOid, @RequestParam("page") int page,
            @RequestParam("per_page") int per_page) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        List<IdtView> idtDTO = null;
        if (page == 0)
            page = 1;
        if (per_page == 0)
            per_page = 30; // default to 30 records / page

        System.out.println("I'm in getPaginatedIdtViewData method");

        StudyBean parentStudy = getParentStudy(studyOid);
        Integer pStudyId = parentStudy.getId();
        Integer studyId = getStudy(studyOid).getId();

    //    String crf_name="Groups_Adverse_Events";
        String crf_name="";
        
    //    String study_subject_label="Sub B 101";
        String study_subject_label="";
        String option="";   // either null or "not" to reverse filter
        
        if (studyId == pStudyId) {
            // parent Study
            
            idtDTO = getIdtViewDao().findPaginatedIdtViewDataFiltered(studyId, pStudyId, per_page, page, "OR" ,crf_name,study_subject_label,option);
        } else {
            // Site
            idtDTO = getIdtViewDao().findPaginatedIdtViewDataFiltered(studyId, pStudyId, per_page, page, "AND",crf_name ,study_subject_label,option);
        }
        return new ResponseEntity<List<IdtView>>(idtDTO, HttpStatus.OK);

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

    public IdtViewDao getIdtViewDao() {
        return idtViewDao;
    }




}
