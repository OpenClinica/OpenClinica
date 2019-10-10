
package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class JobDetailDao extends AbstractDomainDao<JobDetail> {

    @Override
    Class<JobDetail> domainClass() {
        // TODO Auto-generated method stub
        return JobDetail.class;
    }

    public List<JobDetail> findAllNonDeletedJobsBySite(int siteId,int userId) {
        String query = "from " + getDomainClassName() + "   where   site.studyId=:siteId " +
                "and status !=:jobDeletedStatus and createdBy.userId =:userId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("siteId", siteId);
        q.setParameter("userId", userId);
        q.setParameter("jobDeletedStatus", JobStatus.DELETED );
        return (ArrayList<JobDetail>) q.list();
    }


    public List<JobDetail> findAllNonDeletedJobsByStudy(int studyId,int userId) {
        String query = "from " + getDomainClassName() + "   where   study.studyId=:studyId  "+
        "and status !=:jobDeletedStatus and createdBy.userId =:userId";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", studyId);
        q.setParameter("userId", userId);
        q.setParameter("jobDeletedStatus", JobStatus.DELETED );
        return (ArrayList<JobDetail>) q.list();
    }

    public JobDetail findByUuid(String uuid) {
        String query = "from " + getDomainClassName() + "   where   uuid=:uuid ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uuid", uuid);
        return (JobDetail) q.uniqueResult();
    }


}
