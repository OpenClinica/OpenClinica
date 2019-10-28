package core.org.akaza.openclinica.service.rule.expression;

import core.org.akaza.openclinica.bean.managestudy.StudyBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.managestudy.StudyDAO;
import core.org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.junit.Ignore;

@Ignore
public class SampleTest extends HibernateOcDbTestCase {
    private CoreResources coreResources;

    public SampleTest() {
        super();
        coreResources = (CoreResources) getContext().getBean("coreResources");

    }

    public void testStatement() {
        StudyDAO studyDao = new StudyDAO(getDataSource());
        StudyBean study = (StudyBean) studyDao.findByPK(1);
        assertNotNull(study);
    }
}