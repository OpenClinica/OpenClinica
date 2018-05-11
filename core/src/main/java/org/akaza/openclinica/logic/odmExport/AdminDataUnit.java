/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.logic.odmExport;


import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.bean.odmbeans.OdmAdminDataBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.OdmExtractDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;

/**
 * A class for ODM AdminData of one study.
 *
 * @author ywang (March, 2010)
 */

public class AdminDataUnit extends OdmUnit {
    private OdmAdminDataBean odmAdminData;
    
    public AdminDataUnit() {
    }

    public AdminDataUnit(DataSource ds, StudyBean study, int category) {
        super(ds, study, category);
        this.odmAdminData = new OdmAdminDataBean();
    }

    public AdminDataUnit(DataSource ds, DatasetBean dataset, ODMBean odmBean, StudyBean study, int category) {
        super(ds, dataset, odmBean, study, category);
        this.odmAdminData = new OdmAdminDataBean();
    }

    public void collectOdmAdminData() {
        StudyBean study = studyBase.getStudy();
        String studyOID = study.getOid();
        StudyDAO studyDAO = new StudyDAO(this.ds);
        StudyBean publicStudy = studyDAO.getPublicStudy(studyOID);
        if (studyOID == null || studyOID.length() <= 0) {
            logger.info("Constructed studyOID using study_id because oc_oid is missing from the table - study.");
            studyOID = "" + study.getId();
        }
        odmAdminData.setStudyOID(studyOID);

        OdmExtractDAO oedao = new OdmExtractDAO(this.ds);
        if (this.getCategory() == 1 && study.isSite(study.getParentStudyId())) {
            String mvoid = "";
            if (this.dataset != null && this.dataset.getId() > 0) {
                mvoid = this.dataset.getODMMetaDataVersionOid();
            }
            if (mvoid.length() > 0) {
                mvoid += "-" + studyOID;
            } else {
                mvoid = "v1.0.0" + "-" + studyOID;
            }
            odmAdminData.setMetaDataVersionOID(mvoid);

        } else {
            odmAdminData.setMetaDataVersionOID(this.dataset.getODMMetaDataVersionOid());
            if (odmAdminData.getMetaDataVersionOID() == null || odmAdminData.getMetaDataVersionOID().length() <= 0) {
                odmAdminData.setMetaDataVersionOID("v1.0.0");
            }
        }
        oedao.getAdminData(publicStudy, this.dataset, odmAdminData, this.odmBean.getODMVersion());
    }

    public OdmAdminDataBean getOdmAdminData() {
        return odmAdminData;
    }

    public void setOdmAdminData(OdmAdminDataBean odmAdminData) {
        this.odmAdminData = odmAdminData;
    }
}