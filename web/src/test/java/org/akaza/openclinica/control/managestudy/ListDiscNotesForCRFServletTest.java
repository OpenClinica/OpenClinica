package org.akaza.openclinica.control.managestudy;

import junit.framework.TestCase;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ListDiscNotesForCRFServletTest extends TestCase {

    public ListDiscNotesForCRFServletTest() {
    }

    public static void setup(){
    	ResourceBundleProvider.updateLocale(new Locale("us"));
    	
    }
    
    // Scenario
    // Data Entry Person (site) can access Subject    

    public void test_ListDiscNotesForCRFServlet_MayViewDN() {    	
    	UserAccountBean ub = new UserAccountBean();
    	//StudyUserRoleBean currentRole = new StudyUserRoleBean();
    	//currentRole.setRole(Role.COORDINATOR);
    	
    	StudyUserRoleBean studyUserRoleBeanMock = mock(StudyUserRoleBean.class);

    	// Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT);
    	boolean result1 = ListDiscNotesForCRFServlet.mayViewDN(ub, studyUserRoleBeanMock);
        assertEquals(true, result1);

        // Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT2);
    	boolean result2 = ListDiscNotesForCRFServlet.mayViewDN(ub, studyUserRoleBeanMock);
        assertEquals(true, result2);

        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.ADMIN);
        boolean result3 = ListDiscNotesForCRFServlet.mayViewDN(ub, studyUserRoleBeanMock);
        assertEquals(false, result3);
    
        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.INVALID);
        boolean result4 = ListDiscNotesForCRFServlet.mayViewDN(ub, studyUserRoleBeanMock);
        assertEquals(false, result4);
    }
    
 
}