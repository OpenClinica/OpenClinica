package org.akaza.openclinica.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 *
 * Searches for a user in the LDAP structure by username
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
@Service("openClinicaLdapUserSearch")
public class OpenClinicaLdapUserSearch implements LdapUserSearch {

    @Autowired
    private LdapUserService ldapUserService;

    public DirContextOperations searchForUser(String username) throws UsernameNotFoundException {
        DirContextOperations result = ldapUserService.searchForUser(username);
        if (result == null) {
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }
        return result;
    }

}
