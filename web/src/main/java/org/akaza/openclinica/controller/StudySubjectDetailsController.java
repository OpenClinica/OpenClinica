package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller("studySubjectDetailsController")
public class StudySubjectDetailsController {

    //Autowire the class that handles the sidebar structure with a configured
    //bean named "sidebarInit"
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    @Autowired
    private StudySubjectDao studySubjectDao;
    @Autowired
    private StudyDao studyDao;


    @RequestMapping("/subjects")
    public ModelMap getSubjects(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = true, value = "label") String label) {

        ModelMap modelMap = new ModelMap();

        StudyBean study = (StudyBean) request.getSession().getAttribute("study");
        StudySubject studySubject = studySubjectDao.findByLabelAndStudy(label, studyDao.findByOcOID(study.getOid()));
        modelMap.addAttribute("xmlPath", "/rest/clinicaldata/xml/view/" + study.getOid() + "/" + studySubject.getOcOid() + "/*/*");
        modelMap.addAttribute("jsonPath", "/rest/clinicaldata/json/view/" + study.getOid() + "/" + studySubject.getOcOid() + "/*/*");
        return modelMap;
    }
}
