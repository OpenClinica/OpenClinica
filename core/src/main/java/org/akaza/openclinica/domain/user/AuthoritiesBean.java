/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.user;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <p>
 * Spring Security authorities table
 * </p>
 * 
 * @author Krikor Krumlian
 */
@Entity
@Table(name = "authorities")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "authorities_id_seq") })
public class AuthoritiesBean extends AbstractMutableDomainObject {

    String username;
    String authority;

    public AuthoritiesBean() {
        setDefaultAuthority();
    }

    public AuthoritiesBean(String username) {
        this.username = username;
        setDefaultAuthority();
    }

    public void setDefaultAuthority() {
        this.authority = "ROLE_USER";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
