package org.akaza.openclinica.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
 
public class PrintCRFController extends AbstractController{
 
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        ModelAndView model = new ModelAndView("printcrf");
        return model;
    }

    private boolean mayProceed(HttpServletRequest request) {

       StudyUserRoleBean currentRole = (StudyUserRoleBean)request.getSession().getAttribute("userRole");
       Role r = currentRole.getRole();

       if (r.equals(Role.STUDYDIRECTOR) || r.equals(Role.COORDINATOR) || r.equals(Role.MONITOR)
               || currentRole.getRole().equals(Role.INVESTIGATOR) ) {
           return true;
       }
       return false;
   }

}
