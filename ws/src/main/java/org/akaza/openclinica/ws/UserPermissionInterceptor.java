package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.util.Locale;

public class UserPermissionInterceptor implements EndpointInterceptor {

    private final DataSource dataSource;

    public UserPermissionInterceptor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            if (request != null) {
                request.setAttribute("requestSchema", getSchemaFromStudyOid((String)request.getAttribute("studyOid")));
            }
        }

        UserAccountBean userAccountBean = ((UserAccountBean) userAccountDao.findByUserName(username));
        Boolean result = userAccountBean.getRunWebservices();
        if (!result) {
            SoapBody response = ((SoapMessage) messageContext.getResponse()).getSoapBody();
            response.addClientOrSenderFault("Authorization is required to execute SOAP web services with this account.Please contact your administrator.",
                    Locale.ENGLISH);
            return false;

        } else {
           return result;
        }
    }

    private String getSchemaFromStudyOid(String studyOid) {
        StudyDAO studyDAO = new StudyDAO(dataSource);
        StudyBean studyBean = studyDAO.findByOid(studyOid);
        return studyBean.getSchemaName();
    }
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object o, Exception e) throws Exception {
    }

}
