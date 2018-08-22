/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.logic.odmExport;

import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionProtocolBean;
import org.akaza.openclinica.bean.odmbeans.OdmStudyBean;
import org.akaza.openclinica.bean.odmbeans.UserBean;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.job.JobTerminationMonitor;
import org.akaza.openclinica.service.PermissionService;

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
    private EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao;
    private PermissionService permissionService;
    private UserAccountBean userAccountBean;
    private boolean crossForm;
    // protected final Logger logger =
    // LoggerFactory.getLogger(getClass().getName());

    public MetaDataCollector(DataSource ds, StudyBean study, RuleSetRuleDao ruleSetRuleDao, boolean showArchived, PermissionService permissionService, UserAccountBean userAccountBean , boolean crossForm, EventDefinitionCrfPermissionTagDao eventDefinitionCrfPermissionTagDao) {
        super(ds, study, showArchived);
        this.userAccountBean=userAccountBean;
        this.ruleSetRuleDao = ruleSetRuleDao;
        odmStudyMap = new LinkedHashMap<String, OdmStudyBean>();
        this.permissionService=permissionService;
        this.crossForm=crossForm;
        this.eventDefinitionCrfPermissionTagDao = eventDefinitionCrfPermissionTagDao;
    }

    public MetaDataCollector(DataSource ds, StudyBean study, RuleSetRuleDao ruleSetRuleDao,UserAccountBean userAccountBean ,PermissionService permissionService) {
        super(ds, study);
        this.userAccountBean=userAccountBean;
        this.permissionService=permissionService;
        this.ruleSetRuleDao = ruleSetRuleDao;
        odmStudyMap = new LinkedHashMap<String, OdmStudyBean>();

    }

    public MetaDataCollector(DataSource ds, DatasetBean dataset, StudyBean currentStudy, RuleSetRuleDao ruleSetRuleDao ,PermissionService permissionService,UserAccountBean userAccountBean) {
        super(ds, dataset, currentStudy);
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.permissionService =permissionService;
        this.userAccountBean = userAccountBean;
        odmStudyMap = new LinkedHashMap<String, OdmStudyBean>();
    }

    public MetaDataCollector(DataSource ds, RuleSetRuleDao ruleSetRuleDao) {
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
            StudyBean study = u.getStudy();
            MetadataUnit meta = new MetadataUnit(this.ds, this.dataset, this.getOdmbean(), study, this.getCategory(), getRuleSetRuleDao(), showArchived ,permissionService,userAccountBean,crossForm,eventDefinitionCrfPermissionTagDao );
            meta.collectOdmStudy(null);
            if (this.getCategory() == 1) {
                if (study.isSite(study.getParentStudyId())) {
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
            odmStudyMap.put(u.getStudy().getOid(), meta.getOdmStudy());
        }
    }

    public void collectMetadataUnitMap(String formVersionOID) {
        Iterator<OdmStudyBase> it = this.getStudyBaseMap().values().iterator();
        MetaDataVersionProtocolBean protocol = new MetaDataVersionProtocolBean();
        while (it.hasNext()) {
            JobTerminationMonitor.check();
            OdmStudyBase u = it.next();
            StudyBean study = u.getStudy();
            MetadataUnit meta = new MetadataUnit(this.ds);
            meta.setStudyBase(u);
            meta.setOdmStudy(new OdmStudyBean());
            meta.setParentStudy(new StudyBean());

            meta.collectOdmStudy(formVersionOID);
            if (this.getCategory() == 1) {
                if (study.isSite(study.getParentStudyId())) {
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
            odmStudyMap.put(u.getStudy().getOid(), meta.getOdmStudy());
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
