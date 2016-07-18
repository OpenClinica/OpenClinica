package org.akaza.openclinica.web.filter;

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Adds the <code>ROLE_USER</code> to all LDAP users which succeed on authenticating.
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 */
public class OpenClinicaLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    public Collection<GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        Collection<GrantedAuthority> auths = new ArrayList<GrantedAuthority>(1);
        auths.add(new SimpleGrantedAuthority("ROLE_USER"));
        return auths;
    }

}
