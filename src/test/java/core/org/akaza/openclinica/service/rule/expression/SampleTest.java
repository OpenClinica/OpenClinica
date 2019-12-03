package core.org.akaza.openclinica.service.rule.expression;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.templates.HibernateOcDbTestCase;
import org.junit.Ignore;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class SampleTest extends HibernateOcDbTestCase {
    private CoreResources coreResources;
    private StudyDao studyDao;
    public SampleTest() {
        super();
        coreResources = (CoreResources) getContext().getBean("coreResources");
        studyDao = (StudyDao) getContext().getBean("studyDaoDomain");

    }

    public void testStatement() {
        Study study = (Study) studyDao.findByPK(1);
        assertNotNull(study);
    }
}