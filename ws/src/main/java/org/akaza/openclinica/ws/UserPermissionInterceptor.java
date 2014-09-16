package org.akaza.openclinica.ws;

import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapMessage;

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

    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        // TODO Auto-generated method stub
        return true;
    }

    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        // TODO Auto-generated method stub
        return true;
    }

    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception e)  {
        // TODO Auto-generated method stub
        //return true;
    }
    
    
}
