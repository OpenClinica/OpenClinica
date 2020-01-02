package core.org.akaza.openclinica.web.filter.rest;

import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;

import com.sun.jersey.server.impl.application.WebApplicationContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class RestODMFilter implements ContainerRequestFilter, ResourceFilter {

    @Context
    HttpServletRequest request;
    @Context
    HttpServletResponse response;

    WebApplicationContext context;
    String studyOIDS;

    public static ResourceBundle restext;

    private static String GlOBAL_STUDY_OID = "*";

    @Override
    @Transactional
    public ContainerRequest filter(ContainerRequest containerRequest) {
        request.setAttribute("requestSchema", "public");
        // get tenant schema

        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");

        String studyOID = containerRequest.getPathSegments().get(3).getPath();

        // parse to get studyOID

        if (studyOID.equals(GlOBAL_STUDY_OID)) {
            if (checkAuth(userBean)) {
                return containerRequest;
            }
        } else {
            Study studyBean = getStudyByOID(studyOID);
            if (checkAuth(studyBean, userBean)) {
                request.setAttribute("requestSchema", studyBean.getSchemaName());
                return containerRequest;
            } else {
                if (studyBean.isSite()) {
                    int parentStudyID = studyBean.getStudy().getStudyId();
                    studyBean = getStudyByID(parentStudyID, getDataSource());
                    if (checkAuth(studyBean, userBean))
                        request.setAttribute("requestSchema", studyBean.getSchemaName());
                    return containerRequest;
                }
            }

            request.setAttribute(SecureController.PAGE_MESSAGE, "You don't have correct permission in your current Study.");
        }

        throw new WebApplicationException(Response.Status.FORBIDDEN);

    }

    private Boolean checkAuth(UserAccountBean userBean) {
        Boolean auth = false;

        ArrayList userRoles = userBean.getRoles();
        for (int i = 0; (i < userRoles.size() && auth == false); i++) {
            StudyUserRoleBean studyRole = (StudyUserRoleBean) userRoles.get(i);

            if (studyRole.getRole().equals(Role.ADMIN) || studyRole.getRole().equals(Role.COORDINATOR) || studyRole.getRole().equals(Role.STUDYDIRECTOR)) {
                auth = true;

            }
        }
        return auth;
    }

    private Boolean checkAuth(Study studyBean, UserAccountBean userBean) {
        Boolean auth = false;
        StudyUserRoleBean studyRole = getRoleByStudy(studyBean, getDataSource(), userBean);
        Role r = studyRole.getRole();
        if (r != null) {
            // r = userBean.getActiveStudyRole();
            if (r != null && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR))) {
                auth = true;
            }
            // else if(userBean.isTechAdmin()||userBean.isSysAdmin())
            // {
            if (r != null && (r.equals(Role.ADMIN) || r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.INVESTIGATOR)
                    || r.equals(Role.MONITOR) || r.equals(Role.RESEARCHASSISTANT) || r.equals(Role.RESEARCHASSISTANT2))) {

                auth = true;
            }
            // }
        }
        return auth;
    }

    private DataSource getDataSource() {
        return (DataSource) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean("dataSource");
    }

    public StudyDao getStudyDao() {
        return (StudyDao) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean("studyDaoDomain");

    }

    private Study getStudyByOID(String OID) {
        return getStudyDao().findByOcOID(OID);
    }

    private StudyUserRoleBean getRoleByStudy(Study studyBean, DataSource ds, UserAccountBean userBean) {
        UserAccountDAO userAccountDAO = new UserAccountDAO(ds);
        return userAccountDAO.findRoleByUserNameAndStudyId(userBean.getName(), studyBean.getStudyId());

    }


    private Study getStudyByID(int id, DataSource ds) {
        return (Study) getStudyDao().findByPK(id);
    }

    @Override
    public ContainerRequestFilter getRequestFilter() {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        // TODO Auto-generated method stub
        return null;
    }

}
