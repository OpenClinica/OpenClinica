package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;

public class StudySubjectDao extends AbstractDomainDao<StudySubject> {

    @Override
    Class<StudySubject> domainClass() {
        // TODO Auto-generated method stub
        return StudySubject.class;
    }

    public StudySubject findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("OCOID", OCOID);
        return (StudySubject) q.uniqueResult();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ArrayList<StudyEvent> fetchListSEs(String id) {
        String query = " from StudyEvent se where se.studySubject.ocOid = :id order by se.studyEventDefinition.ordinal,se.sampleOrdinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("id", id.toString());

        return (ArrayList<StudyEvent>) q.list();

    }

}
