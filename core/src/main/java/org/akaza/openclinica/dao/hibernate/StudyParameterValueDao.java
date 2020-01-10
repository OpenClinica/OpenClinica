/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.StudyParameterValue;


public class StudyParameterValueDao extends AbstractDomainDao<StudyParameterValue> {
	
    @Override
    public Class<StudyParameterValue> domainClass() {
        return StudyParameterValue.class;
    }

	public StudyParameterValue findByStudyIdParameter(int studyId, String parameter) {
        String query = "from " + getDomainClassName() + " study_parameter_value where study_parameter_value.study.studyId = :studyid and study_parameter_value.studyParameter = :parameter ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyid", studyId);
        q.setString("parameter", parameter);
        return (StudyParameterValue) q.uniqueResult();
    }
}
