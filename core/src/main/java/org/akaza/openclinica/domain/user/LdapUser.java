package org.akaza.openclinica.domain.user;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Represents a user's information retrieved from LDAP/Active Directory
 *
 * @author Doug Rodrigues (drodrigues@openclinica.com)
 *
 */
public class LdapUser implements Serializable {

    private static final long serialVersionUID = 989338796135101116L;

    private String distinguishedName;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String organization;

    public String getDistinguishedName() {
        return distinguishedName;
    }
    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    public String getEncodedDN() {
        try {
            return URLEncoder.encode(getDistinguishedName(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }


}
