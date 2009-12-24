/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.core;

import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.view.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Base control servlet for all the other controller
 * 
 * @author jxu
 * @deprecated
 */
@Deprecated
public abstract class ControlServlet extends HttpServlet implements SingleThreadModel {
    protected ServletContext context;
    protected SessionManager sm;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected String smtpMail;
    protected String tempDir;
    protected String logDir;
    protected String logLevel;
    protected int emailSetting;

    protected HttpSession session;

    protected static final String PAGE_INFO = "pageInfo";// for showing page
    // wide message

    protected HashMap messages = new HashMap();// message on the page, not for

    // errors

    @Override
    public void init() throws ServletException {
        context = getServletContext();
        smtpMail = getInitParameter("smtpDriver");
        String tempEmail = getInitParameter("emailSetting");
        logLevel = getInitParameter("logLevel");
        logDir = getInitParameter("logDir");

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws OpenClinicaException {
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * 
     * @param request
     *            servlet request
     * @param response
     *            servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Forwards to a jsp page
     * 
     * @param jspPage
     * @param request
     * @param response
     */
    protected void forwardPage(Page jspPage, HttpServletRequest request, HttpServletResponse response) {
        try {
            context.getRequestDispatcher(jspPage.getFileName()).forward(request, response);
        } catch (Exception se) {
            se.printStackTrace();
        }

    }

}
