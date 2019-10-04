package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.StudyParameterValue;
import org.springframework.transaction.annotation.Transactional;


public class StudyParameterValueDao extends AbstractDomainDao<StudyParameterValue> {
	
    @Override
    public Class<StudyParameterValue> domainClass() {
        return StudyParameterValue.class;
    }

    @Transactional
	public StudyParameterValue findByStudyIdParameter(int studyId, String parameter) {
        String query = "from " + getDomainClassName() + " study_parameter_value where study_parameter_value.study.studyId = :studyid and study_parameter_value.studyParameter = :parameter ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyid", studyId);
        q.setString("parameter", parameter);
        return (StudyParameterValue) q.uniqueResult();
    }
}
