/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package core.org.akaza.openclinica.logic.odmExport;

import java.util.HashMap;
import java.util.Set;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.odmbeans.ODMBean;
import core.org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import core.org.akaza.openclinica.dao.extract.OdmExtractDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;

/**
 * A class for one ODM ClinicalData Element.
 *
 * @author ywang (May, 2009)
 */

public class ClinicalDataUnit extends OdmUnit {
    private OdmClinicalDataBean odmClinicalData;
    private String studySubjectIds;
    private String permissionTagsString;
    private Set<Integer> edcSet;
    private StudyDao studyDao;
    public ClinicalDataUnit() {
    }

    public ClinicalDataUnit(DataSource ds, Study study, int category) {
        super(ds, study, category);
        this.odmClinicalData = new OdmClinicalDataBean();
    }

    public ClinicalDataUnit(DataSource ds, DatasetBean dataset, ODMBean odmBean, Study study, int category, StudyDao studyDao) {
        super(ds, dataset, odmBean, study, category);
        this.studyDao = studyDao;
        this.odmClinicalData = new OdmClinicalDataBean();
    }

    public ClinicalDataUnit(DataSource ds, DatasetBean dataset, ODMBean odmBean, Study study, int category, String studySubjectIds,String permissionTagsString , Set<Integer> edcSet, StudyDao studyDao) {
        super(ds, dataset, odmBean, study, category);
        this.studyDao = studyDao;
        this.permissionTagsString=permissionTagsString;
        this.odmClinicalData = new OdmClinicalDataBean();
        this.studySubjectIds = studySubjectIds;
        this.edcSet=edcSet;
    }

    public void collectOdmClinicalData() {
        Study study = studyBase.getStudy();
        String studyOID = study.getOc_oid();
        if (studyOID == null || studyOID.length() <= 0) {
            logger.info("Constructed studyOID using study_id because oc_oid is missing from the table - study.");
            studyOID = "" + study.getStudyId();
        }
        odmClinicalData.setStudyOID(studyOID);

        OdmExtractDAO oedao = new OdmExtractDAO(this.ds,edcSet, studyDao);

        if (this.getCategory() == 1 && study.isSite()) {
            String mvoid = "";
            if (this.dataset != null) {
                mvoid = this.dataset.getODMMetaDataVersionOid();
            }
            if (mvoid.length() > 0) {
                mvoid += "-" + studyOID;
            } else {
                mvoid = "v1.0.0" + "-" + studyOID;
            }
            odmClinicalData.setMetaDataVersionOID(mvoid);

        } else {
            odmClinicalData.setMetaDataVersionOID(this.dataset.getODMMetaDataVersionOid());
            if (odmClinicalData.getMetaDataVersionOID() == null || odmClinicalData.getMetaDataVersionOID().length() <= 0) {
                odmClinicalData.setMetaDataVersionOID("v1.0.0");
            }
        }
        oedao.getClinicalData(study, this.dataset, odmClinicalData, this.odmBean.getODMVersion(), studySubjectIds, this.odmBean.getOdmType(),permissionTagsString);
    }

    public OdmClinicalDataBean getOdmClinicalData() {
        return odmClinicalData;
    }

    public void setOdmClinicalData(OdmClinicalDataBean odmClinicalData) {
        this.odmClinicalData = odmClinicalData;
    }

    public static Boolean isNull(String itValue, String key, HashMap<String, String> nullValueCVs) {
        if (nullValueCVs.containsKey(key)) {
            String[] nullvalues = nullValueCVs.get(key).split(",");
            String[] values = itValue.split(",");
            for(String v : values) {
                v = v.trim();
                for (String n : nullvalues) {
                    if (v.equals(n)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getStudySubjectIds() {
        return studySubjectIds;
    }

    public void setStudySubjectIds(String studySubjectIds) {
        this.studySubjectIds = studySubjectIds;
    }


}