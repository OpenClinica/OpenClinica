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

import java.text.SimpleDateFormat;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Base control servlet for all the other controllers
 * 
 * @author jxu
 * @deprecated
 */
@Deprecated
public abstract class Controller extends HttpServlet implements SingleThreadModel {

    protected ServletContext context;
    protected SessionManager sm;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected String logDir;
    protected String logLevel;

    protected HttpSession session;

    protected static final String PAGE_INFO = "pageInfo";// for showing page
    // wide message

    protected HashMap errors = new HashMap();// error messages on the page
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void init() throws ServletException {
        context = getServletContext();

    }

    /**
     * Process request
     * 
     * @param request
     * @param response
     * @throws OpenClinicaException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws OpenClinicaException {
        session = request.getSession();
        session.setMaxInactiveInterval(60 * 60 * 3);
        request.setAttribute(PAGE_INFO, "");
        sdf.setLenient(false);
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("OpenClinicaException:: openclinica.control.Controller:: " + e.getMessage());

            forwardPage(Page.ERROR, request, response);
        }
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
            process(request, response);
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
            process(request, response);
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
