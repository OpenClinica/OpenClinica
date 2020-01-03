
package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class JobDetailDao extends AbstractDomainDao<JobDetail> {

    @Override
    Class<JobDetail> domainClass() {
        // TODO Auto-generated method stub
        return JobDetail.class;
    }

    public List<JobDetail> findAllNonDeletedJobsBySiteExceptPublishedStudies(int siteId, int userId) {
        String query = "from " + getDomainClassName() + "   where   site.studyId=:siteId " +
                "and status !=:jobDeletedStatus and createdBy.userId =:userId and type !=:type";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("siteId", siteId);
        q.setParameter("userId", userId);
        q.setParameter("type", JobType.PUBLISH_STUDY);
        q.setParameter("jobDeletedStatus", JobStatus.DELETED);
        return (ArrayList<JobDetail>) q.list();
    }


    public List<JobDetail> findAllNonDeletedJobsByStudyExceptPublishedStudies(int studyId, int userId) {
        String query = "from " + getDomainClassName() + "   where   study.studyId=:studyId  " +
                "and status !=:jobDeletedStatus and createdBy.userId =:userId  and type !=:type";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", studyId);
        q.setParameter("userId", userId);
        q.setParameter("type", JobType.PUBLISH_STUDY);
        q.setParameter("jobDeletedStatus", JobStatus.DELETED);
        return (ArrayList<JobDetail>) q.list();
    }

    public JobDetail findByUuid(String uuid) {
        String query = "from " + getDomainClassName() + "   where   uuid=:uuid ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("uuid", uuid);
        return (JobDetail) q.uniqueResult();
    }

    public List<JobDetail> findByStudyIdAndStatusAndJobType(int studyId, Enum status, JobType jobType) {
        String query = "from " + getDomainClassName() + " where study.studyId=:studyId and status=:status and type=:jobType";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("studyId", studyId);
        q.setParameter("status", status);
        q.setParameter("jobType", jobType);
        return (ArrayList<JobDetail>) q.list();
    }
}
