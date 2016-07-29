package org.akaza.openclinica.service.rule.expression;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.templates.HibernateOcDbTestCase;

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