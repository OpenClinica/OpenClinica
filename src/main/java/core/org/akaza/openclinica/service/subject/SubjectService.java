package core.org.akaza.openclinica.service.subject;

import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.bean.submit.SubjectBean;
import core.org.akaza.openclinica.core.SessionManager;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.dao.submit.SubjectDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

public class SubjectService implements SubjectServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    SubjectDAO subjectDao;
    StudyParameterValueDAO studyParameterValueDAO;
    StudySubjectDAO studySubjectDao;
    UserAccountDAO userAccountDao;
    StudyDao studyDao;
    DataSource dataSource;

    public SubjectService(DataSource dataSource, StudyDao studyDao) {
        this.dataSource = dataSource;
        this.studyDao = studyDao;
    }

    public SubjectService(SessionManager sessionManager) {
        this.dataSource = sessionManager.getDataSource();
    }

    public List<StudySubjectBean> getStudySubject(Study study) {
        return getStudySubjectDao().findAllByStudy(study);

    }

    /*
     * (non-Javadoc)
     * @see core.org.akaza.openclinica.service.subject.SubjectServiceInterface#createSubject(core.org.akaza.openclinica.bean.submit.SubjectBean,
     * core.org.akaza.openclinica.bean.managestudy.Study)
     */
    public String createSubject(SubjectBean subjectBean, Study studyBean, Date enrollmentDate, String secondaryId) {
        if (subjectBean.getUniqueIdentifier() != null && subjectBean.getUniqueIdentifier().trim().length()> 0 && 
        		getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier()).getId() != 0) {
        	//we need to keep the label to transfer it to the StudySubjectBean later
        	String label = subjectBean.getLabel();
        	subjectBean = getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier());
        	subjectBean.setLabel(label);
        } else {
            subjectBean.setStatus(Status.AVAILABLE);
            subjectBean = getSubjectDao().create(subjectBean);
        }
        
        StudySubjectBean studySubject = createStudySubject(subjectBean, studyBean, enrollmentDate, secondaryId);
        getStudySubjectDao().createWithoutGroup(studySubject);
        return studySubject.getLabel();
    }

    private StudySubjectBean createStudySubject(SubjectBean subject, Study studyBean, Date enrollmentDate, String secondaryId) {
        StudySubjectBean studySubject = new StudySubjectBean();
        studySubject.setSecondaryLabel(secondaryId);
        studySubject.setOwner(getUserAccount());
        studySubject.setEnrollmentDate(enrollmentDate);
        studySubject.setSubjectId(subject.getId());
        studySubject.setStudyId(studyBean.getStudyId());
        studySubject.setStatus(Status.AVAILABLE);
        
        int handleStudyId = studyBean.isSite() ? studyBean.getStudy().getStudyId() : studyBean.getStudyId();
        StudyParameterValueBean subjectIdGenerationParameter = getStudyParameterValueDAO().findByHandleAndStudy(handleStudyId, "subjectIdGeneration");
        String idSetting = subjectIdGenerationParameter.getValue();
        if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
        	// Warning: Here we have a race condition. 
        	// At least, a uniqueness constraint should be set on the database! Better provide an atomic method which stores a new label in the database and returns it.  
            int nextLabel = getStudySubjectDao().findTheGreatestLabel() + 1;
            studySubject.setLabel(Integer.toString(nextLabel));
        } else {
        	studySubject.setLabel(subject.getLabel());
        	subject.setLabel(null);
        }
        
        return studySubject;

    }

    public void validateSubjectTransfer(SubjectTransferBean subjectTransferBean) {
        // TODO: Validate here
    }

    /**
     * Getting the first user account from the database. This would be replaced by an authenticated user who is doing the SOAP requests .
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {

        UserAccountBean user = new UserAccountBean();
        user.setId(1);
        return user;
    }

    /**
     * @return the subjectDao
     */
    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }
    
    public StudyParameterValueDAO getStudyParameterValueDAO() {
        return this.studyParameterValueDAO != null ? studyParameterValueDAO : new StudyParameterValueDAO(dataSource);
    }
    
    /**
     * @return the subjectDao
     */
    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }

    /**
     * @return the UserAccountDao
     */
    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

    /**
     * @return the datasource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @param datasource
     *            the datasource to set
     */
    public void setDatasource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}