package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.MonitorView;

public class MonitorViewDao extends AbstractDomainDao<MonitorView> {

    @Override
    Class<MonitorView> domainClass() {
        // TODO Auto-generated method stub
        return MonitorView.class;
    }

    

    public List <MonitorView> findPaginatedMonitorViewData(int studyId,int pStudyId, int per_page, int page , String operation) {
        String query = "from " + getDomainClassName() + " where studyId= "+ studyId +" "+ operation +" parentStudyId="+ pStudyId +" order by itemDataId ";

        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setMaxResults(per_page);     // limit 
        q.setFirstResult((page-1)*per_page);       // offset
        return  (List<MonitorView>) q.list();
    }

    public Integer findTotalCountMonitorViewData(int studyId,int pStudyId, String operation) {
        String query = "from " + getDomainClassName() + " where studyId= "+ studyId +" "+ operation +" parentStudyId="+ pStudyId +" order by itemDataId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return  q.list().size();
    }
    
}
