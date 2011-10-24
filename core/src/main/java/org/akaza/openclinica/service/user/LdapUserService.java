package org.akaza.openclinica.service.user;

import java.text.MessageFormat;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.akaza.openclinica.domain.user.LdapUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;

/**
 * Provides access to user information retrieved from LDAP/Active Directory.
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 */
@Service
public class LdapUserService {


    @Autowired
    private ContextSource contextSource;

    @Value("s[ldap.enabled]")
    private String ldapEnabledProperty;

    @Value("s[ldap.passwordRecoveryURL]")
    private String passwordRecoveryURL;

    @Value("s[ldap.userSearch.query]")
    private String userSearchQuery;

    @Value("s[ldap.userSearch.baseDn]")
    private String userSearchBase;

    private LdapTemplate ldapTemplate;

    @PostConstruct // Eclipse warning here is an Eclipse bug, not an issue with the code
    public void init() {
        ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(true);
    }

    private final AttributesMapper ldapUserAttributesMapper = new AttributesMapper() {

        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            LdapUser u = new LdapUser();
            u.setDistinguishedName(attToString(attributes, "distinguishedName"));
            u.setUsername(attToString(attributes, "sAMAccountName"));
            u.setFirstName(attToString(attributes, "givenName"));
            u.setLastName(attToString(attributes, "sn"));
            u.setEmail(attToString(attributes, "mail"));
            u.setOrganization(attToString(attributes, "company"));
            return u;
        }

        private String attToString(Attributes a, String key) throws NamingException {
            Attribute att = a.get(key);
            if (att != null) {
                return a.get(key).get().toString();
            }
            return null;
        }
    };

    /**
     * Retrieves a list of users within a <code>baseDn</code> matching a <code>filter</code>. The filter must be a
     * non-empty string.
     *
     * @param filter
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<LdapUser> listUsers(String filter) {
        assert(!StringUtils.isEmpty(filter));
        String query = MessageFormat.format(userSearchQuery, filter);
        return ldapTemplate.search(userSearchBase, query, ldapUserAttributesMapper);
    }

    /**
     * Loads a user by its distinguished name
     *
     * @param dn
     * @return
     */
    public LdapUser loadUser(String dn) {
        return (LdapUser) ldapTemplate.lookup(dn, ldapUserAttributesMapper);
    }

    public boolean isLdapServerConfigured() {
        return Boolean.parseBoolean(ldapEnabledProperty);
    }

    public String getPasswordRecoveryURL() {
        return passwordRecoveryURL;
    }

}
