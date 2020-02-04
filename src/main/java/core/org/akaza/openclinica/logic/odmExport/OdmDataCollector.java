/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package core.org.akaza.openclinica.logic.odmExport;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import core.org.akaza.openclinica.bean.odmbeans.ODMBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Prepare data for one ODM XML file.
 * 
 * @author ywang (May, 2008)
 */

public abstract class OdmDataCollector {
    protected DataSource ds;
    protected DatasetBean dataset;
    private ODMBean odmbean;
    protected boolean showArchived;

    protected StudyDao studyDao;
    // key is study/site oc_oid.
    private LinkedHashMap<String, OdmStudyBase> studyBaseMap;
    // 0: one Study Element; 1: one parent study and its sites
    private int category;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmDataCollector() {
    }


    public OdmDataCollector(DataSource ds, StudyDao studyDao) {
        this.ds = ds;
        this.studyDao = studyDao;
    }
    public OdmDataCollector(DataSource ds, Study study, boolean showArchived, StudyDao studyDao) {
        this(ds, study, studyDao);
        this.showArchived = showArchived;
    }

    /**
     * Constructor for an ODM XML file containing information of the passed
     * Study. If it is a site,
     * only contains information of this site. If it is a study,
     * contains information of this study and its sites if appliable.
     * 
     * @param ds
     * @param study
     */
    public OdmDataCollector(DataSource ds, Study study, StudyDao studyDao) {
        this.ds = ds;
        if (this.ds == null) {
            logger.info("DataSource is null!");
            return;
        }
        this.studyDao = studyDao;
        if (study == null)
            createFakeStudyObj();

        else {

            if (study == null || study.getStudyId() < 1) {
                logger.info("There is no study.");
                return;
            }
            dataset = new DatasetBean();
            odmbean = new ODMBean();

            if (study != null) {
                study = studyDao.findStudyWithSPVByStudyId(study.getStudyId());
                if (study.isSite()) {
                    this.studyBaseMap = new LinkedHashMap<String, OdmStudyBase>();
                    this.studyBaseMap.put(study.getOc_oid(), new OdmStudyBase(ds, study));
                    this.category = 0;
                } else {
                    this.studyBaseMap = populateCompletedStudyBaseMap(study.getStudyId());
                    category = 1;
                }
            }
        }
    }

    private void createFakeStudyObj() {
        dataset = new DatasetBean();
        odmbean = new ODMBean();

        Study studyBean = new Study();
        studyBean.setName(MetadataUnit.FAKE_STUDY_NAME);
        studyBean.setOc_oid(MetadataUnit.FAKE_STUDY_OID);
        studyBean.setStudy(new Study());
        StudyEventDefinitionBean sedFake = new StudyEventDefinitionBean();
        sedFake.setName(MetadataUnit.FAKE_SE_NAME);
        sedFake.setOid(MetadataUnit.FAKE_STUDY_EVENT_OID);

        List<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        seds.add(sedFake);

        LinkedHashMap<String, OdmStudyBase> Bases = new LinkedHashMap<String, OdmStudyBase>();
        Bases.put(studyBean.getOc_oid(), new OdmStudyBase(ds, studyBean, seds));
        // this.studyBaseMap = new LinkedHashMap<String, OdmStudyBase>();
        this.studyBaseMap = Bases;

    }

    /**
     * Constructor for dataset of current study. If current study is a site,
     * only contains information of this site. If current study is a study,
     * contains information of this study and its sites if appliable.
     * 
     * @param ds
     * @param dataset
     */
    public OdmDataCollector(DataSource ds, DatasetBean dataset, Study currentStudy, StudyDao studyDao) {
        this.ds = ds;
        if (this.ds == null) {
            logger.info("DataSource is null!");
            return;
        }
        this.studyDao = studyDao;
        this.dataset = dataset;
        odmbean = new ODMBean();
        Study study = (Study) studyDao.findStudyWithSPVByStudyId(dataset.getStudyId());
        if (currentStudy.isSite()) {
            this.studyBaseMap = new LinkedHashMap<String, OdmStudyBase>();
            this.studyBaseMap.put(study.getOc_oid(), new OdmStudyBase(ds, study));
            this.category = 0;
        } else {
            int parentStudyId = study.isSite() ? study.getStudy().getStudyId() : study.getStudyId();
            this.studyBaseMap = populateCompletedStudyBaseMap(parentStudyId);
            category = 1;
        }
    }

    /**
     * Populate a HashMap<String, OdmStudyBase> of a study and its sites if
     * appliable, the key is study/site oc_oid.
     * 
     * @param parentStudyId
     * @return
     */
    public LinkedHashMap<String, OdmStudyBase> populateCompletedStudyBaseMap(int parentStudyId) {
        LinkedHashMap<String, OdmStudyBase> Bases = new LinkedHashMap<String, OdmStudyBase>();
        for (Study s : (ArrayList<Study>) studyDao.findAllByParentStudyIdOrderedByIdAsc(parentStudyId)) {
            Bases.put(s.getOc_oid(), new OdmStudyBase(ds, s));
        }
        return Bases;
    }

    /**
     * Populate a HashMap<String, OdmStudyBase> of a single study/site. The key
     * is study/site oc_oid.
     * 
     * @param studyId
     * @return
     */
    public LinkedHashMap<String, OdmStudyBase> populateStudyBaseMap(int studyId) {
        LinkedHashMap<String, OdmStudyBase> Bases = new LinkedHashMap<String, OdmStudyBase>();
        Study study = (Study) studyDao.findByPK(studyId);
        Bases.put(study.getOc_oid(), new OdmStudyBase(ds, study));
        return Bases;
    }

    /**
     * Create an ODMBean with default ODMBean properties.
     * 
     * @return
     */
    public void collectOdmRoot() {
        if (this.dataset == null) {
            logger.info("empty ODMBean has been returned because dataset is null!");
            return;
        }
        Date creationDatetime = new Date();
        SimpleDateFormat localTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        TimeZone timeZone = TimeZone.getDefault();
        localTime.setTimeZone(timeZone);
        int offset = localTime.getTimeZone().getOffset(creationDatetime.getTime());
        String sign = "+";
        if (offset < 0) {
            offset = -offset;
            sign = "-";
        }
        int hours = offset / 3600000;
        int minutes = (offset - hours * 3600000) / 60000;
        DecimalFormat twoDigits = new DecimalFormat("00");
        if (dataset.getId() > 0) {
            odmbean.setFileOID(this.dataset.getName() + "D" + new SimpleDateFormat("yyyyMMddHHmmssZ").format(creationDatetime));
            odmbean.setDescription(this.dataset.getDescription().trim());
        } else {
            odmbean.setFileOID("Study-Meta" + "D" + new SimpleDateFormat("yyyyMMddHHmmssZ").format(creationDatetime));
            odmbean.setDescription("Study Metadata");
        }
        odmbean.setCreationDateTime(
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" + sign + twoDigits.format(hours) + ":" + twoDigits.format(minutes)).format(creationDatetime));

    }

    public void setDataset(DatasetBean dataset) {
        this.dataset = dataset;
    }

    public DatasetBean getDataset() {
        return this.dataset;
    }

    public void setODMBean(ODMBean odmbean) {
        this.odmbean = odmbean;
    }

    public ODMBean getODMBean() {
        return this.odmbean;
    }

    public DataSource getDataSource() {
        return ds;
    }

    public void setDataSource(DataSource ds) {
        this.ds = ds;
    }

    public ODMBean getOdmbean() {
        return odmbean;
    }

    public void setOdmbean(ODMBean odmbean) {
        this.odmbean = odmbean;
    }

    public HashMap<String, OdmStudyBase> getStudyBaseMap() {
        return studyBaseMap;
    }

    public void setStudyBaseMap(LinkedHashMap<String, OdmStudyBase> studyBaseMap) {
        this.studyBaseMap = studyBaseMap;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    /**
     * 
     */
    public void collectFileData() {
        // TODO Auto-generated method stub

    }
}
