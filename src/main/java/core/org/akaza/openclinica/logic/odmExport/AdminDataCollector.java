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
import core.org.akaza.openclinica.bean.odmbeans.OdmAdminDataBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.job.JobTerminationMonitor;

/**
 * Populate ODM AdminData Element for a ODM XML file. It supports:
 * <ul>
 * <li>ODM XML file contains only one ODM AdminData element. </li>
 * <li>ODM XML file contains multiple AdminData elements - one parent study
 * and its site(s). </li>
 * </ul>
 *
 * @author ywang (March, 2010)
 */

public class AdminDataCollector extends OdmDataCollector {
    private LinkedHashMap<String, OdmAdminDataBean> odmAdminDataMap;

    public AdminDataCollector(DataSource ds, Study currentStudy, StudyDao studyDao) {
        super(ds, currentStudy, studyDao);
        this.odmAdminDataMap = new LinkedHashMap<String, OdmAdminDataBean>();
    }

    /**
     *
     * @param ds
     * @param dataset
     */
    public AdminDataCollector(DataSource ds, DatasetBean dataset, Study currentStudy, StudyDao studyDao) {
        super(ds, dataset, currentStudy, studyDao);
        this.odmAdminDataMap = new LinkedHashMap<String, OdmAdminDataBean>();
    }

    @Override
    public void collectFileData() {
        this.collectOdmAdminDataMap();
    }

    public void collectOdmAdminDataMap() {
        Iterator<OdmStudyBase> it = this.getStudyBaseMap().values().iterator();
        while (it.hasNext()) {
            JobTerminationMonitor.check();
            OdmStudyBase u = it.next();
            AdminDataUnit adata = new AdminDataUnit(this.ds, this.dataset, this.getOdmbean(), u.getStudy(), this.getCategory(), studyDao);
            adata.setCategory(this.getCategory());
            adata.collectOdmAdminData();
            odmAdminDataMap.put(u.getStudy().getOc_oid(), adata.getOdmAdminData());
        }
    }

    public LinkedHashMap<String, OdmAdminDataBean> getOdmAdminDataMap() {
        return odmAdminDataMap;
    }

    public void setOdmClinicalDataMap(LinkedHashMap<String, OdmAdminDataBean> odmAdminDataMap) {
        this.odmAdminDataMap = odmAdminDataMap;
    }

}