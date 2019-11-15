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
import core.org.akaza.openclinica.bean.odmbeans.MetaDataVersionProtocolBean;
import core.org.akaza.openclinica.bean.odmbeans.OdmStudyBean;
import core.org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.job.JobTerminationMonitor;

/**
 * Populate metadata for a ODM XML file. It supports:
 * <ul>
 * <li>ODM XML file contains only one ODM Study element.<br>
 * In this case, Include element references only to external MetadataVersion.
 * </li>
 * <li>ODM XML file contains multiple Study elements - one parent study and its
 * site(s). <br>
 * In this case, Include element can reference to both internal and external
 * MetadataVersion. ODM fields in dataset table are for the study. For site(s),
 * siteOID will be appended automatically.</li>
 * </ul>
 *
 * @author ywang (May, 2009)
 */

public class MetaDataCollector extends OdmDataCollector {
    private LinkedHashMap<String, OdmStudyBean> odmStudyMap;
    private static int textLength = 4000;
    private RuleSetRuleDao ruleSetRuleDao;

    private String permissionTagsString;

    // protected final Logger logger =
    // LoggerFactory.getLogger(getClass().getName());

    public MetaDataCollector(DataSource ds, Study study, RuleSetRuleDao ruleSetRuleDao, boolean showArchived, String permissionTagsString, StudyDao studyDao) {
        super(ds, study, showArchived, studyDao);
        this.ruleSetRuleDao = ruleSetRuleDao;
        odmStudyMap = new LinkedHashMap<String, OdmStudyBean>();
        this.permissionTagsString=permissionTagsString;
        }

    public MetaDataCollector(DataSource ds, Study study, RuleSetRuleDao ruleSetRuleDao,String permissionTagsString, StudyDao studyDao) {
        super(ds, study,studyDao);
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.permissionTagsString=permissionTagsString;
        odmStudyMap = new LinkedHashMap<String, OdmStudyBean>();

    }

    public MetaDataCollector(DataSource ds, DatasetBean dataset, Study currentStudy, RuleSetRuleDao ruleSetRuleDao , String permissionTagsString, StudyDao studyDao) {
        super(ds, dataset, currentStudy, studyDao);
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.permissionTagsString=permissionTagsString;
        odmStudyMap = new LinkedHashMap<String, OdmStudyBean>();
    }

    public MetaDataCollector(DataSource ds, RuleSetRuleDao ruleSetRuleDao, StudyDao studyDao) {
        super(ds,studyDao);
        this.ruleSetRuleDao = ruleSetRuleDao;


    }

    @Override
    public void collectFileData() {
        this.collectOdmRoot();
        this.collectMetadataUnitMap();
    }

    public void collectMetadataUnitMap() {
        Iterator<OdmStudyBase> it = this.getStudyBaseMap().values().iterator();
        MetaDataVersionProtocolBean protocol = new MetaDataVersionProtocolBean();
        while (it.hasNext()) {
            JobTerminationMonitor.check();
            OdmStudyBase u = it.next();
            Study study = u.getStudy();
            MetadataUnit meta = new MetadataUnit(this.ds, this.dataset, this.getOdmbean(), study, this.getCategory(), getRuleSetRuleDao(), showArchived,permissionTagsString, studyDao );
            meta.collectOdmStudy(null);
            if (this.getCategory() == 1) {
                if (study.isSite()) {
                    meta.getOdmStudy().setParentStudyOID(meta.getParentOdmStudyOid());
                    MetaDataVersionProtocolBean p = meta.getOdmStudy().getMetaDataVersion().getProtocol();
                    if (p != null && p.getStudyEventRefs().size() > 0) {
                    } else {
                        logger.error("site " + study.getName() + " will be assigned protocol with StudyEventRefs size=" + protocol.getStudyEventRefs().size());
                        meta.getOdmStudy().getMetaDataVersion().setProtocol(protocol);
                    }
                } else {
                    protocol = meta.getOdmStudy().getMetaDataVersion().getProtocol();

                }
            }
            odmStudyMap.put(u.getStudy().getOc_oid(), meta.getOdmStudy());
        }
    }

    public void collectMetadataUnitMap(String formVersionOID) {
        Iterator<OdmStudyBase> it = this.getStudyBaseMap().values().iterator();
        MetaDataVersionProtocolBean protocol = new MetaDataVersionProtocolBean();
        while (it.hasNext()) {
            JobTerminationMonitor.check();
            OdmStudyBase u = it.next();
            Study study = u.getStudy();
            MetadataUnit meta = new MetadataUnit(this.ds,studyDao);
            meta.setStudyBase(u);
            meta.setOdmStudy(new OdmStudyBean());
            meta.setParentStudy(new Study());

            meta.collectOdmStudy(formVersionOID);
            if (this.getCategory() == 1) {
                if (study.isSite()) {
                    meta.getOdmStudy().setParentStudyOID(meta.getParentOdmStudyOid());
                    MetaDataVersionProtocolBean p = meta.getOdmStudy().getMetaDataVersion().getProtocol();
                    if (p != null && p.getStudyEventRefs().size() > 0) {
                    } else {
                        logger.error("site " + study.getName() + " will be assigned protocol with StudyEventRefs size=" + protocol.getStudyEventRefs().size());
                        meta.getOdmStudy().getMetaDataVersion().setProtocol(protocol);
                    }
                } else {
                    protocol = meta.getOdmStudy().getMetaDataVersion().getProtocol();

                }
            }
            odmStudyMap.put(u.getStudy().getOc_oid(), meta.getOdmStudy());
        }
    }

    public LinkedHashMap<String, OdmStudyBean> getOdmStudyMap() {
        return odmStudyMap;
    }

    public void setOdmStudyMap(LinkedHashMap<String, OdmStudyBean> odmStudyMap) {
        this.odmStudyMap = odmStudyMap;
    }

    public static void setTextLength(int len) {
        textLength = len;
    }

    public static int getTextLength() {
        return textLength;
    }

    public RuleSetRuleDao getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public void collectFileData(String formVersionOID) {
        this.collectOdmRoot();
        this.collectMetadataUnitMap(formVersionOID);

    }

}
