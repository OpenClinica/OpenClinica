/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.service.rule.expression;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.templates.OcDbTestCase;

public class SampleTest extends OcDbTestCase {

    public SampleTest() {
        super();
    }

    public void testStatement() {
        StudyDAO studyDao = new StudyDAO(getDataSource());
        StudyBean study = (StudyBean) studyDao.findByPK(1);
        assertNotNull(study);
    }
}