package org.akaza.openclinica.controller.user;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.service.user.LdapUserService;
import org.akaza.openclinica.service.user.OpenClinicaLdapUserSearch;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
@Controller
public class LdapUserController {

    private static final String PAGE_LIST_LDAP_USERS = "listLdapUsers";

    private static final Logger logger = LoggerFactory.getLogger(LdapUserController.class.getName());

    @Autowired
    private LdapUserService ldapUserService;

    @Autowired
    private UserAccountDAO userAccountDao;

    @Autowired
    private OpenClinicaLdapUserSearch openClinicaLdapUserSearch;

    @RequestMapping("/listLdapUsers")
    public String listLdapUsers(HttpServletRequest req,
            @RequestParam(value = "filter", required = false) String filter) {

        // If no filter is provided, just render the page without results table
        if (!StringUtils.isEmpty(filter)) {

            // Retrieve existing users to remove them from LDAP search results.
            @SuppressWarnings("unchecked")
            Collection<UserAccountBean> existingAccounts = userAccountDao.findAll();

            Set<String> existingUsernames = new HashSet<String>(existingAccounts.size());
            for (UserAccountBean existingAccount : existingAccounts) {
                existingUsernames.add(existingAccount.getName());
            }

            req.setAttribute("memberList", ldapUserService.listNewUsers(filter, existingUsernames));
        }

        return PAGE_LIST_LDAP_USERS;
    }

    @RequestMapping("/selectLdapUser")
    public String selectLdapUser(HttpServletRequest req,
                                 @RequestParam(value="dn", required = false) String dn,
                                 @RequestParam(value="un", required = false) String un
                                 ) {
        // A request without "dn" or "un" parameter is used to just close the iFrame
        try{
            if (!StringUtils.isEmpty(dn)) {
                req.getSession().setAttribute("ldapUser", ldapUserService.loadUser(dn));
            } else if(!StringUtils.isEmpty(un)) {
                req.getSession().setAttribute("ldapUser", ldapUserService.getLdapUser(openClinicaLdapUserSearch.searchForUser(un)));
            }
        }catch (Exception ex){
            logger.error("Error getting ldap user: " + ex.getMessage(), ex);
        }
        return "redirect:/CreateUserAccount";
    }

}
