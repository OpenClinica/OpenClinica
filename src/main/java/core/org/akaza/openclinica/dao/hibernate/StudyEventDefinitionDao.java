package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public class StudyEventDefinitionDao extends AbstractDomainDao<StudyEventDefinition> {
	@Autowired
    private StudyDao studyDao;

    @Override
    public Class<StudyEventDefinition> domainClass() {
        return StudyEventDefinition.class;
    }
    
    public StudyEventDefinition findByStudyEventDefinitionId(int studyEventDefinitionId) {
        String query = "from " + getDomainClassName() + " study_event_definition  where study_event_definition.studyEventDefinitionId = :studyeventdefinitionid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("studyeventdefinitionid", studyEventDefinitionId);
        return (StudyEventDefinition) q.uniqueResult();
    }

    public ArrayList findAllByStudy(Study study) {

        if (study.isSite()) {
            // If the study has a parent than it is a site, in this case we
            // should get the event definitions of the parent
            Study parentStudy = new Study();
            parentStudy = (Study) studyDao.findById(study.getStudy().getStudyId());
            return findAllByStudy(parentStudy);
        } else {
            return findAllByStudy(study);
        }
    }

}
