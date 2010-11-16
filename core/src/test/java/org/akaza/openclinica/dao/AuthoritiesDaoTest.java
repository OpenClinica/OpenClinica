package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

import java.util.List;

public class AuthoritiesDaoTest extends HibernateOcDbTestCase {

    public AuthoritiesDaoTest() {
        super();
    }

    public void testSaveOrUpdate() {
    	AuthoritiesDao authoritiesDao = (AuthoritiesDao) getContext().getBean("authoritiesDao");
        AuthoritiesBean authorities = new AuthoritiesBean();
        authorities.setUsername("krikor");
        authorities.setAuthority("ROLE_USER");
        authorities = authoritiesDao.saveOrUpdate(authorities);

        assertNotNull("Persistant id is null", authorities.getId());
    }

    public void testFindById() {
    	AuthoritiesDao authoritiesDao = (AuthoritiesDao) getContext().getBean("authoritiesDao");
        
    	AuthoritiesBean authorities = null;
    	authorities = authoritiesDao.findById(-1);

        // Test Authorities
        assertNotNull("RuleSet is null", authorities);
        assertEquals("The id of the retrieved Domain Object should be -1", new Integer(-1), authorities.getId());
   }

    public void testFindByUsername() {

    	AuthoritiesDao authoritiesDao = (AuthoritiesDao) getContext().getBean("authoritiesDao");
        
    	AuthoritiesBean authorities = null;
    	authorities = authoritiesDao.findByUsername("root");
    	
    	// Test Authorities
        assertNotNull("RuleSet is null", authorities);
        assertEquals("The id of the retrieved Domain Object should be -1", new Integer(-1), authorities.getId());
    }
}