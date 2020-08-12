package org.akaza.openclinica.control.submit;

import java.util.Locale;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

public class SubmitDataServletTest extends TestCase {

    public SubmitDataServletTest() {
    }

    public static void setup(){
    	ResourceBundleProvider.updateLocale(new Locale("us"));
    	
    }
    
    // Scenario
    // Data Entry Person (site) can access Subject    

    public void test_SubmitDataServlet_MayViewData() {    	
    	UserAccountBean ub = new UserAccountBean();
    	//StudyUserRoleBean currentRole = new StudyUserRoleBean();
    	//currentRole.setRole(Role.COORDINATOR);
    	
    	StudyUserRoleBean studyUserRoleBeanMock = mock(StudyUserRoleBean.class);

    	// Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT);
    	boolean result1 = SubmitDataUtil.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(true, result1);

        // Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT2);
    	boolean result2 = SubmitDataUtil.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(true, result2);

        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.ADMIN);
        boolean result3 = SubmitDataUtil.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(false, result3);
    
        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.INVALID);
        boolean result4 = SubmitDataUtil.mayViewData(ub, studyUserRoleBeanMock);
        assertEquals(false, result4);
    }
    
    // Scenario
    // Data Entry Person (site) can access Notes & Discrepancies
    public void test_SubmitDataServlet_MaySubmitData() {    	
    	UserAccountBean ub = new UserAccountBean();
    	//StudyUserRoleBean currentRole = new StudyUserRoleBean();
    	//currentRole.setRole(Role.COORDINATOR);
    	
    	StudyUserRoleBean studyUserRoleBeanMock = mock(StudyUserRoleBean.class);

    	// Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT);
    	boolean result1 = SubmitDataUtil.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(true, result1);

        // Positive Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.RESEARCHASSISTANT2);
    	boolean result2 = SubmitDataUtil.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(true, result2);

        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.ADMIN);
        boolean result3 = SubmitDataUtil.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(false, result3);
    
        // Negative Testing
    	when(studyUserRoleBeanMock.getRole()).thenReturn(Role.INVALID);
        boolean result4 = SubmitDataUtil.maySubmitData(ub, studyUserRoleBeanMock);
        assertEquals(false, result4);
    }
  



}