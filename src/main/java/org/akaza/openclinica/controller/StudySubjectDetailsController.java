package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
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

        Study study = (Study) request.getSession().getAttribute("study");
        StudySubject studySubject = studySubjectDao.findByLabelAndStudyOrParentStudy(label, studyDao.findByOcOID(study.getOc_oid()));
        modelMap.addAttribute("studyOid", study.getOc_oid());
        modelMap.addAttribute("studySubjectOid", studySubject.getOcOid());
        return modelMap;
    }
}
