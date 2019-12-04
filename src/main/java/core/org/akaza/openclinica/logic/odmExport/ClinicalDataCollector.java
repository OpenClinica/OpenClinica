/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package core.org.akaza.openclinica.logic.odmExport;

import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.job.JobTerminationMonitor;

/**
 * Populate ODM ClinicalData Element for a ODM XML file. It supports:
 * <ul>
 * <li>ODM XML file contains only one ODM ClinicalData element. </li>
 * <li>ODM XML file contains multiple ClinicalData elements - one parent study
 * and its site(s). </li>
 * </ul>
 *
 * @author ywang (May, 2009)
 */

public class ClinicalDataCollector extends OdmDataCollector {
    private LinkedHashMap<String, OdmClinicalDataBean> odmClinicalDataMap;
    public static String datasetFiltered = "NO";


    /**
     *
     * @param ds
     * @param dataset
     */
    public ClinicalDataCollector(DataSource ds, DatasetBean dataset, Study currentStudy, StudyDao studyDao) {
        super(ds, dataset, currentStudy, studyDao);
        this.odmClinicalDataMap = new LinkedHashMap<String, OdmClinicalDataBean>();
    }

    @Override
    public void collectFileData() {
        this.collectOdmRoot();
        this.collectOdmClinicalDataMap();
    }

    public void collectOdmClinicalDataMap() {
        Iterator<OdmStudyBase> it = this.getStudyBaseMap().values().iterator();
        while (it.hasNext()) {
            JobTerminationMonitor.check();
            OdmStudyBase u = it.next();
            ClinicalDataUnit cdata = new ClinicalDataUnit(this.ds, this.dataset, this.getOdmbean(), u.getStudy(), this.getCategory(), studyDao);
            cdata.setCategory(this.getCategory());
            StudySubjectDAO ssdao = new StudySubjectDAO(this.ds);
            cdata.setStudySubjectIds(ssdao.findStudySubjectIdsByStudyIds(u.getStudy().getStudyId()+""));
            cdata.collectOdmClinicalData();
            odmClinicalDataMap.put(u.getStudy().getOc_oid(), cdata.getOdmClinicalData());
        }
    }

    public LinkedHashMap<String, OdmClinicalDataBean> getOdmClinicalDataMap() {
        return odmClinicalDataMap;
    }

    public void setOdmClinicalDataMap(LinkedHashMap<String, OdmClinicalDataBean> odmClinicalDataMap) {
        this.odmClinicalDataMap = odmClinicalDataMap;
    }

}