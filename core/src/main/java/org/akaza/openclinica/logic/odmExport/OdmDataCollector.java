/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.logic.odmExport;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prepare data for one ODM XML file.
 * 
 * @author ywang (May, 2008)
 */

public abstract class OdmDataCollector {
    protected DataSource ds;
    protected DatasetBean dataset;
    private ODMBean odmbean;
    // key is study/site oc_oid.
    private LinkedHashMap<String, OdmStudyBase> studyBaseMap;
    // 0: one Study Element; 1: one parent study and its sites
    private int category;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmDataCollector() {
    }

    /**
     * Constructor for an ODM XML file containing information of the passed
     * StudyBean. If it is a site, 
     * only contains information of this site. If it is a study,
     * contains information of this study and its sites if appliable.
     * 
     * @param ds
     * @param study
     */
    public OdmDataCollector(DataSource ds, StudyBean study) {
        this.ds = ds;
        if (this.ds == null) {
            logger.info("DataSource is null!");
            return;
        }
        if(study == null)
        	    	createFakeStudyObj();
       
        else{
        	
	        if (study.getId() < 1) {
	            logger.info("There is no study.");
	            return;
	        }
	        dataset = new DatasetBean();
	        odmbean = new ODMBean();
	
	        if(study!=null)
	        { 
		        if (study.isSite(study.getParentStudyId())) {
		            this.studyBaseMap = new LinkedHashMap<String, OdmStudyBase>();
		            this.studyBaseMap.put(study.getOid(), new OdmStudyBase(ds, study));
		            this.category = 0;
		        } else {
		            int parentStudyId = study.getParentStudyId() > 0 ? study.getParentStudyId() : study.getId();
		            this.studyBaseMap = populateCompletedStudyBaseMap(parentStudyId);
		            category = 1;
		        }
	        }
        }
    }

    
    
    private void createFakeStudyObj()
    {
        dataset = new DatasetBean();
        odmbean = new ODMBean();
        
    	StudyBean studyBean = new StudyBean();
    	studyBean.setName(MetadataUnit.FAKE_STUDY_NAME);
    	studyBean.setOid(MetadataUnit.FAKE_STUDY_OID);
    	studyBean.setParentStudyId(0);
    	StudyEventDefinitionBean sedFake =  new StudyEventDefinitionBean();
    	sedFake.setName(MetadataUnit.FAKE_SE_NAME);
    	sedFake.setOid(MetadataUnit.FAKE_STUDY_EVENT_OID);
    	
    	
    	List<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
    	seds.add(sedFake);
    	
    	
    	LinkedHashMap<String, OdmStudyBase> Bases = new LinkedHashMap<String, OdmStudyBase>();
    	Bases.put(studyBean.getOid(),new OdmStudyBase(ds, studyBean,seds));
        //this.studyBaseMap = new LinkedHashMap<String, OdmStudyBase>();
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
    public OdmDataCollector(DataSource ds, DatasetBean dataset, StudyBean currentStudy) {
        this.ds = ds;
        if (this.ds == null) {
            logger.info("DataSource is null!");
            return;
        }
        this.dataset = dataset;
        odmbean = new ODMBean();
        StudyBean study = (StudyBean) new StudyDAO(ds).findByPK(dataset.getStudyId());
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            this.studyBaseMap = new LinkedHashMap<String, OdmStudyBase>();
            this.studyBaseMap.put(study.getOid(), new OdmStudyBase(ds, study));
            this.category = 0;
        } else {
            int parentStudyId = study.getParentStudyId() > 0 ? study.getParentStudyId() : study.getId();
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
        StudyDAO sdao = new StudyDAO(this.ds);
        for (StudyBean s : (ArrayList<StudyBean>) sdao.findAllByParentStudyIdOrderedByIdAsc(parentStudyId)) {
            Bases.put(s.getOid(), new OdmStudyBase(ds, s));
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
        StudyBean study = (StudyBean) new StudyDAO(this.ds).findByPK(studyId);
        Bases.put(study.getOid(), new OdmStudyBase(ds, study));
        return Bases;
    }

    public abstract void collectFileData();

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
        if(dataset.getId()>0) {
            odmbean.setFileOID(this.dataset.getName() + "D" + new SimpleDateFormat("yyyyMMddHHmmssZ").format(creationDatetime));
            odmbean.setDescription(this.dataset.getDescription().trim());
        } else {
            odmbean.setFileOID( "Study-Meta"+ "D" + new SimpleDateFormat("yyyyMMddHHmmssZ").format(creationDatetime));
            odmbean.setDescription("Study Metadata");
        }
        odmbean.setCreationDateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss" + sign + twoDigits.format(hours) + ":" + twoDigits.format(minutes))
                .format(creationDatetime));

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
}
