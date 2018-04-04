package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.junit.Ignore;

import java.util.Date;

@Ignore
public class AuditUserLoginDaoTest extends HibernateOcDbTestCase {

    public AuditUserLoginDaoTest() {
        super();
    }

    public void testSaveOrUpdate() {
        AuditUserLoginDao auditUserLoginDao = (AuditUserLoginDao) getContext().getBean("auditUserLoginDao");
        AuditUserLoginBean auditUserLoginBean = new AuditUserLoginBean();
        auditUserLoginBean.setUserName("testUser");
        auditUserLoginBean.setLoginAttemptDate(new Date());
        auditUserLoginBean.setLoginStatus(LoginStatus.SUCCESSFUL_LOGIN);

        auditUserLoginBean = auditUserLoginDao.saveOrUpdate(auditUserLoginBean);

        assertNotNull("Persistant id is null", auditUserLoginBean.getId());
    }

    public void testfindById() {
        AuditUserLoginDao auditUserLoginDao = (AuditUserLoginDao) getContext().getBean("auditUserLoginDao");
        AuditUserLoginBean auditUserLoginBean = auditUserLoginDao.findById(-1);

        assertEquals("UserName should be testUser", "testUser", auditUserLoginBean.getUserName());
    }
}