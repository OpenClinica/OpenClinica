package core.org.akaza.openclinica.dao;

import core.org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import core.org.akaza.openclinica.domain.technicaladmin.ConfigurationBean;
import core.templates.HibernateOcDbTestCase;
import org.junit.Ignore;

@Ignore
public class ConfigurationDaoTest extends HibernateOcDbTestCase {

    public ConfigurationDaoTest() {
        super();
    }

    public void testSaveOrUpdate() {
        ConfigurationDao configurationDao = (ConfigurationDao) getContext().getBean("configurationDao");
        ConfigurationBean configurationBean = new ConfigurationBean();
        configurationBean.setKey("user.test");
        configurationBean.setValue("test");
        configurationBean.setDescription("Testing attention please");

        configurationBean = configurationDao.saveOrUpdate(configurationBean);

        assertNotNull("Persistant id is null", configurationBean.getId());
    }

    public void testfindById() {
        ConfigurationDao configurationDao = (ConfigurationDao) getContext().getBean("configurationDao");
        ConfigurationBean configurationBean = configurationDao.findById(-1);

        assertEquals("Key should be test.test", "test.test", configurationBean.getKey());
    }

    public void testfindByKey() {
        ConfigurationDao configurationDao = (ConfigurationDao) getContext().getBean("configurationDao");
        ConfigurationBean configurationBean = configurationDao.findByKey("test.test");

        assertEquals("Key should be test.test", "test.test", configurationBean.getKey());
    }
}