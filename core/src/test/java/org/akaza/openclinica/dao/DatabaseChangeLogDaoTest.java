package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

public class DatabaseChangeLogDaoTest extends HibernateOcDbTestCase {

    private final Integer POSTGRES_COUNT = 833;
    private final Integer ORACLE_COUNT = 833;

    public DatabaseChangeLogDaoTest() {
        super();
    }

   /* public void testCount() {
        DatabaseChangeLogDao databaseChangeLogDao = (DatabaseChangeLogDao) getContext().getBean("databaseChangeLogDao");
        Long count = databaseChangeLogDao.count();

        if (getDbName().equals("postgres")) {
            assertEquals("Total Count should be", String.valueOf(POSTGRES_COUNT), String.valueOf(count));
        }
        if (getDbName().equals("oracle")) {
            assertEquals("Total Count should be", String.valueOf(ORACLE_COUNT), String.valueOf(count));
        }

    }

    public void testfindAll() {
        DatabaseChangeLogDao databaseChangeLogDao = (DatabaseChangeLogDao) getContext().getBean("databaseChangeLogDao");
        DatabaseChangeLogBean databaseChangeLogBean = null;
        ArrayList<DatabaseChangeLogBean> databaseChangeLogBeans = databaseChangeLogDao.findAll();
        databaseChangeLogBean = databaseChangeLogBeans.get(0);

        if (getDbName().equals("postgres")) {
            assertEquals("Total Count should be", String.valueOf(POSTGRES_COUNT), String.valueOf(databaseChangeLogBeans.size()));
        }
        if (getDbName().equals("oracle")) {
            assertEquals("Total Count should be", String.valueOf(ORACLE_COUNT), String.valueOf(databaseChangeLogBeans.size()));
        }
        assertNotNull(databaseChangeLogBean);

    }
*/
    public void testfindById() {
        DatabaseChangeLogDao databaseChangeLogDao = (DatabaseChangeLogDao) getContext().getBean("databaseChangeLogDao");
        DatabaseChangeLogBean databaseChangeLogBean = null;
        databaseChangeLogBean = databaseChangeLogDao.findById("1235684743487-1", "pgawade (generated)", "migration/initDB/initDB.xml");

        assertNotNull(databaseChangeLogBean);
        assertEquals("Author should be pgawade (generated)", "pgawade (generated)", databaseChangeLogBean.getAuthor());

    }
    public void tearDown(){

        super.tearDown();
    }

}