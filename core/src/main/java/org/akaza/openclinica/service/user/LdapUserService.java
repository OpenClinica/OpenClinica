package org.akaza.openclinica.service.user;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.akaza.openclinica.domain.user.LdapUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.SpringSecurityLdapTemplate;
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

    @Value("s[ldap.loginQuery]")
    private String loginQuery;

    @Value("s[ldap.passwordRecoveryURL]")
    private String passwordRecoveryURL;

    @Value("s[ldap.userSearch.query]")
    private String userSearchQuery;

    @Value("s[ldap.userSearch.baseDn]")
    private String userSearchBase;

    @Value("s[ldap.userData.distinguishedName]")
    private String keyDistinguishedName;

    @Value("s[ldap.userData.username]")
    private String keyUsername;

    @Value("s[ldap.userData.firstName]")
    private String keyFirstName;

    @Value("s[ldap.userData.lastName]")
    private String keyLastname;

    @Value("s[ldap.userData.email]")
    private String keyEmail;

    @Value("s[ldap.userData.organization]")
    private String keyOrganization;

    private SpringSecurityLdapTemplate ldapTemplate;

    @PostConstruct // Eclipse warning here is an Eclipse bug, not an issue with the code
    public void init() {
        ldapTemplate = new SpringSecurityLdapTemplate(contextSource);
        ldapTemplate.setIgnorePartialResultException(true);
    }

    private final AttributesMapper ldapUserAttributesMapper = new AttributesMapper() {

        public Object mapFromAttributes(Attributes attributes) throws NamingException {
            LdapUser u = new LdapUser();
            u.setDistinguishedName(attToString(attributes, keyDistinguishedName));
            u.setUsername(attToString(attributes, keyUsername));
            u.setFirstName(attToString(attributes, keyFirstName));
            u.setLastName(attToString(attributes, keyLastname));
            u.setEmail(attToString(attributes, keyEmail));
            u.setOrganization(attToString(attributes, keyOrganization));
            return u;
        }

        private String attToString(Attributes a, String key) throws NamingException {
            if (!StringUtils.isEmpty(key)) { // Check if the key for this attribute was defined in the properties file
                Attribute att = a.get(key);
                if (att != null) {
                    return a.get(key).get().toString();
                }
            }
            return null;
        }
    };

    /**
     * Retrieves a list of users matching a <code>filter</code>. The filter must be a
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
     * Retrieves a list of users matching a <code>filter</code> which usernames are not present in
     * <code>existingUsers</code>.
     *
     * @param filter
     * @param existingUsers
     * @return
     */
    public List<LdapUser> listNewUsers(String filter, Set<String> existingUsers) {
        List<LdapUser> result = listUsers(filter);
        if (existingUsers != null) {
            Iterator<LdapUser> it = result.iterator();

            while (it.hasNext()) {
                LdapUser user = it.next();
                if (existingUsers.contains(user.getUsername())) {
                    it.remove();
                }
            }
        }
        return result;
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

    public DirContextOperations searchForUser(String username) {
        try {
            return ldapTemplate.searchForSingleEntry(userSearchBase, loginQuery, new String[] { username });
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    public boolean isLdapServerConfigured() {
        return Boolean.parseBoolean(ldapEnabledProperty);
    }

    public String getPasswordRecoveryURL() {
        return passwordRecoveryURL;
    }

}
