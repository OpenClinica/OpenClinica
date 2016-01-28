package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.IdtView;
import org.akaza.openclinica.domain.datamap.ItemData;

public class IdtViewDao extends AbstractDomainDao<IdtView> {

    @Override
    Class<IdtView> domainClass() {
        // TODO Auto-generated method stub
        return IdtView.class;
    }

    public List<IdtView> findPaginatedIdtViewDataFiltered(int studyId, int pStudyId, int per_page, int page, String operation, String crf_name,
            String study_subject_label, String option) {

        String query = " from " + getDomainClassName() + " do where event_crf_id " + option + " in "
                + "(select distinct eventCrfId from do where path IS NOT NULL and (tagStatus !='done' or tagStatus is null))";

        if (!study_subject_label.equals(""))
            query = query + " and studySubjectLabel= '" + study_subject_label + "'";

        query = query + " and ((";

        if (!crf_name.equals(""))
            query = query + " crfName = '" + crf_name + "' and";

        query = query + " eventCrfStatusId=1) or eventCrfStatusId=2) and studyId= " + studyId + " " + operation + " parentStudyId=" + pStudyId
                + " order by itemDataId ";

        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setMaxResults(per_page); // limit
        q.setFirstResult((page - 1) * per_page); // offset
        return (List<IdtView>) q.list();
    }

}
