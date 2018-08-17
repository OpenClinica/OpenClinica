/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.logic.odmExport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.ElementRefBean;
import org.akaza.openclinica.bean.odmbeans.GlobalVariablesBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionIncludeBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.bean.odmbeans.OdmStudyBean;
import org.akaza.openclinica.bean.odmbeans.RangeCheckBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.OdmExtractDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.service.PermissionService;
import org.springframework.http.HttpRequest;

/**
 * A class for ODM metadata of one study.
 *
 * @author ywang (May, 2009)
 */

public class MetadataUnit extends OdmUnit {
    private OdmStudyBean odmStudy;
    private StudyBean parentStudy;
    private RuleSetRuleDao ruleSetRuleDao;
    private PermissionService permissionService;
    private UserAccountBean userAccountBean;
    private StudyBean study;

    public static final String FAKE_STUDY_NAME = "OC_FORM_LIB_STUDY";
    public static final String FAKE_STUDY_OID = "OC_FORM_LIB";
    public static final String FAKE_STUDY_EVENT_OID = "OC_FORM_LIB_SE";
    public static final String FAKE_SE_NAME = "OC_FORM_LIB_SE_NAME";
    public static final String FAKE_SE_REPEATING = "NO";
    public static final String FAKE_SE_TYPE = "SCHEDULED";
    public static final String FAKE_FR_MANDATORY = "No";

    public MetadataUnit() {
    }

    public MetadataUnit(DataSource ds, StudyBean study, int category) {
        super(ds, study, category);
        this.odmStudy = new OdmStudyBean();
        if (study.getParentStudyId() > 0) {
            this.parentStudy = (StudyBean) new StudyDAO(ds).findByPK(study.getParentStudyId());
        } else {
            this.parentStudy = new StudyBean();
        }
    }

    public MetadataUnit(DataSource ds, boolean showArchived) {
        super(ds, showArchived);
        this.ds = ds;
    }

    public MetadataUnit(DataSource ds) {
        this.ds = ds;
    }

    public MetadataUnit(DataSource ds, DatasetBean dataset, ODMBean odmBean, StudyBean study, int category, RuleSetRuleDao ruleSetRuleDao,
            boolean showArchived ,PermissionService permissionService ,UserAccountBean userAccountBean) {
        super(ds, dataset, odmBean, study, category, showArchived);
        this.odmStudy = new OdmStudyBean();
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.permissionService = permissionService;
        this.userAccountBean = userAccountBean;
        this.study=study;
        if (study.getParentStudyId() > 0) {
            this.parentStudy = (StudyBean) new StudyDAO(ds).findByPK(study.getParentStudyId());
        } else {
            this.parentStudy = new StudyBean();
        }
    }

    public void collectOdmStudy(String formVersionOID) {
        StudyBean study = studyBase.getStudy();
        String studyOID = study.getOid();
        if (studyOID == null || studyOID.length() <= 0) {
            logger.info("Constructed studyOID using study_id because oc_oid is missing from the table - study.");
            studyOID = "" + study.getId();
        }
        odmStudy.setOid(studyOID);
        if (studyOID.equals(FAKE_STUDY_OID)) {

            collectGlobalVariables();
            collectBasicDefinitions(formVersionOID);
            collectMetaDataVersion(formVersionOID);
        } else {
            collectGlobalVariables();
            collectBasicDefinitions();
            collectMetaDataVersion();

        }

    }

    public void collectOdmStudy() {
        StudyBean study = studyBase.getStudy();
        String studyOID = study.getOid();
        if (studyOID == null || studyOID.length() <= 0) {
            logger.info("Constructed studyOID using study_id because oc_oid is missing from the table - study.");
            studyOID = "" + study.getId();
        }
        collectGlobalVariables();
        collectBasicDefinitions();
        collectMetaDataVersion();
    }

    private void collectGlobalVariables() {
        StudyBean study = studyBase.getStudy();
        String sn = study.getName();
        String sd = study.getSummary().trim();
        String pn = study.getIdentifier();
        if (parentStudy.getId() > 0) {
            sn = parentStudy.getName() + " - " + study.getName();
            sd = parentStudy.getSummary().trim() + " - " + study.getSummary().trim();
            pn = parentStudy.getIdentifier() + " - " + study.getIdentifier();
        }
        GlobalVariablesBean gv = this.odmStudy.getGlobalVariables();
        gv.setStudyName(sn);
        gv.setStudyDescription(sd);
        gv.setProtocolName(pn);
    }

    private void collectBasicDefinitions() {
        int studyid = studyBase.getStudy().getParentStudyId() > 0 ? studyBase.getStudy().getParentStudyId() : studyBase.getStudy().getId();
        new OdmExtractDAO(this.ds).getBasicDefinitions(studyid, odmStudy.getBasicDefinitions());
    }

    private void collectBasicDefinitions(String formVersionOID) {
        new OdmExtractDAO(this.ds).getBasicDefinitions(formVersionOID, odmStudy.getBasicDefinitions());
    }

    /**
     * To retrieve the ODM with form version OID as one of the parameters
     * 
     * @param formVersionOID
     */
    private void collectMetaDataVersion(String formVersionOID) {
        StudyBean study = studyBase.getStudy();
        OdmExtractDAO oedao = new OdmExtractDAO(this.ds, showArchived);
        MetaDataVersionBean metadata = this.odmStudy.getMetaDataVersion();

        ODMBean odmBean = new ODMBean();
        odmBean.setODMVersion("oc1.3");
        setOdmBean(odmBean);

        ArrayList<StudyEventDefinitionBean> sedBeansInStudy = (ArrayList<StudyEventDefinitionBean>) studyBase.getSedBeansInStudy();
        if (sedBeansInStudy == null || sedBeansInStudy.size() < 1) {
            logger.info("null, because there is no study event definition in this study.");
            return;
        }

        if (metadata.getOid() == null || metadata.getOid().length() <= 0) {
            metadata.setOid("v1.0.0");
        }
        if (metadata.getName() == null || metadata.getName().length() <= 0) {
            metadata.setName("MetaDataVersion_v1.0.0");
        }

        oedao.getODMMetadataForForm(metadata, formVersionOID, this.odmBean.getODMVersion());

    }

    private void collectMetaDataVersion() {
        ArrayList<StudyEventDefinitionBean> sedBeansInStudy = (ArrayList<StudyEventDefinitionBean>) studyBase.getSedBeansInStudy();
        if (sedBeansInStudy == null || sedBeansInStudy.size() < 1) {
            logger.info("null, because there is no study event definition in this study.");
            return;
        }

        String permissionTags = "";
        permissionTags =permissionService.getPermissionTagsStringWithoutRequest(study,userAccountBean.getUserUuid());


        StudyBean study = studyBase.getStudy();

        StudyConfigService studyConfig = new StudyConfigService(this.ds);
        study = studyConfig.setParametersForStudy(study);

        MetaDataVersionBean metadata = this.odmStudy.getMetaDataVersion();
        metadata.setStudy(study);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(this.ds);
        int parentId = study.getParentStudyId() > 0 ? study.getParentStudyId() : study.getId();
        StudyParameterValueBean spv = spvdao.findByHandleAndStudy(parentId, "discrepancyManagement");
        metadata.setSoftHard(spv.getValue().equalsIgnoreCase("true") ? "Hard" : "Soft");

        OdmExtractDAO oedao = new OdmExtractDAO(this.ds, showArchived);
        int studyId = study.getId();
        int parentStudyId = study.getParentStudyId() > 0 ? study.getParentStudyId() : studyId;
        if (this.getCategory() == 1 && study.isSite(study.getParentStudyId())) {
            // populate MetaDataVersion attributes
            if (dataset != null) {
                metadata.setOid(dataset.getODMMetaDataVersionOid() + "-" + study.getOid());
                metadata.setName(dataset.getODMMetaDataVersionName() + "-" + study.getOid());
                this.setParentMetaDataVersionOid(dataset.getODMMetaDataVersionOid());
            }
            if (metadata.getOid() == null || metadata.getOid().length() <= 0) {
                metadata.setOid("v1.0.0" + "-" + study.getOid());
                this.setParentMetaDataVersionOid("v1.0.0");
            }
            if (metadata.getName() == null || metadata.getName().length() <= 0) {
                metadata.setName("MetaDataVersion_v1.0.0" + "-" + study.getOid());
            }

            // populate Include
            this.collectIncludeFromParentInSameFile();


            // populate protocol
            oedao.getUpdatedSiteMetadata(parentStudyId, studyId, metadata, this.odmBean.getODMVersion(),permissionTags);
        } else {
            if (dataset != null) {
                metadata.setOid(dataset.getODMMetaDataVersionOid());
                metadata.setName(dataset.getODMMetaDataVersionName());
            }
            if (metadata.getOid() == null || metadata.getOid().length() <= 0) {
                metadata.setOid("v1.0.0");
            }
            if (metadata.getName() == null || metadata.getName().length() <= 0) {
                metadata.setName("MetaDataVersion_v1.0.0");
            }

            // populate Include
            String psOid = new String();
            String pmOid = new String();
            if (dataset != null) {
                psOid = dataset.getODMPriorStudyOid();
                pmOid = dataset.getODMPriorMetaDataVersionOid();
            }
            if (pmOid != null && pmOid.length() > 0) {
                MetaDataVersionIncludeBean ib = metadata.getInclude();
                ib.setMetaDataVersionOID(pmOid);
                if (psOid != null && psOid.length() > 0) {
                    ib.setStudyOID(psOid);
                } else {
                    ib.setStudyOID(study.getOid());
                }
            }

            // populate protocol
            // Set<Integer> nullCodeSet = oedao.getMetadata(parentStudyId,
            // studyId,
            // metadata, this.getODMBean().getODMVersion());
            // studyBase.setNullClSet(nullCodeSet);
            oedao.getMetadata(parentStudyId, studyId, metadata, this.odmBean.getODMVersion(),permissionTags);
            metadata.setRuleSetRules(getRuleSetRuleDao().findByRuleSetStudyIdAndStatusAvail(parentStudyId));
        }
    }

    /**
     * Include parent study metadata which is in the same ODM XML file.
     *
     * @return
     */
    private void collectIncludeFromParentInSameFile() {
        MetaDataVersionIncludeBean ib = this.odmStudy.getMetaDataVersion().getInclude();
        String metaOid = this.getParentMetaDataVersionOid();
        String pstudyOID = this.parentStudy.getOid();
        if (pstudyOID == null || pstudyOID.length() <= 0) {
            pstudyOID = "" + this.parentStudy.getId();
        }
        ib.setMetaDataVersionOID(metaOid);
        ib.setStudyOID(pstudyOID);
    }

    public static String getOdmItemDataType(int responseTypeId, int OCDataTypeId) {
        // 3: checkbox; 7: multi-select
        if (responseTypeId == 3 || responseTypeId == 7) {
            return "text";
        } else {
            return getOdmItemDataType(OCDataTypeId);
        }
    }

    /**
     * Handle mapping among odm-1.2 datatypes and openclinica datatypes
     *
     * @param OCDataTypeId
     * @return
     */
    public static String getOdmItemDataType(int OCDataTypeId) {
        switch (OCDataTypeId) {
        // BL //BN //ED //TEL //ST
        case 1:
            return "text";
        case 2:
            return "text";
        case 3:
            return "text";
        case 4:
            return "text";
        case 5:
            return "text";
        case 8:
            return "text";
        case 10: // partial-date
            return "partialDate";
        // INT
        case 6:
            return "integer";
        // REAL
        case 7:
            return "float";
        // DATE
        case 9:
            return "date";
        default:
            return "text";
        }
    }

    public static String getOdmItemDataType(int responseTypeId, int OCDataTypeId, String odmVersion) {
        if (odmVersion.contains("1.2")) {
            return getOdmItemDataType(responseTypeId, OCDataTypeId);
        } else {
            // 3: checkbox; 7: multi-select
            if (responseTypeId == 3 || responseTypeId == 7) {
                return "text";
            } else {
                switch (OCDataTypeId) {
                case 1:
                    return "boolean";
                // not be supported in openclinica-3.0.4.1
                // case 10:
                // return "partialDate";
                default:
                    return getOdmItemDataType(OCDataTypeId);
                }
            }
        }
    }

    public static int getSignificantDigits(String datatype, List<String> values, boolean hasCode) {
        if ("float".equalsIgnoreCase(datatype)) {
            return hasCode ? getSignificantDigits(values) : 6;
        }
        return new String().length();
    }

    public static int getSignificantDigits(String datatype, Set<String> values, boolean hasCode) {
        if ("float".equalsIgnoreCase(datatype)) {
            return hasCode ? getSignificantDigits(values) : 6;
        }
        return new String().length();
    }

    public static int getDataTypeLength(List<String> values) {
        int len = 0;
        for (String value : values) {
            len = Math.max(len, value.length());
        }
        return len;
    }

    public static int getDataTypeLength(Set<String> values) {
        int len = 0;
        Iterator<String> iter = values.iterator();
        while (iter.hasNext()) {
            String value = iter.next();
            len = Math.max(len, value.length());
        }
        return len;
    }

    public static int getSignificantDigits(List<String> values) {
        int d = 0;
        for (String v : values) {
            if (v != null && v.length() > 0) {
                double temp = 0;
                try {
                    temp = Double.parseDouble(v);
                } catch (NumberFormatException e) {
                    temp = 0;
                }
                d = temp != 0 ? Math.max(d, BigDecimal.valueOf(temp).scale()) : d;
            }
        }
        return d;
    }

    public static int getSignificantDigits(Set<String> values) {
        int d = 0;
        Iterator<String> iter = values.iterator();
        while (iter.hasNext()) {
            String v = iter.next();
            if (v != null && v.length() > 0) {
                double temp = 0;
                try {
                    temp = Double.parseDouble(v);
                } catch (NumberFormatException e) {
                    temp = 0;
                }
                d = temp != 0 ? Math.max(d, BigDecimal.valueOf(temp).scale()) : d;
            }
        }
        return d;
    }

    public static String getItemQuestionText(String header, String left, String right) {
        String t = header != null && header.length() > 0 ? header : "";
        if (left != null && left.length() > 0) {
            t += t.length() > 0 ? "  - " + left : left;
        }
        if (right != null && right.length() > 0) {
            t += t.length() > 0 ? "  - " + right : right;
        }
        return t;
    }

    public static List<RangeCheckBean> getItemRangeCheck(String func, String constraint, String errorMessage, String muOid) {
        // at this time only supports one measurement unit for a RangeCheck,
        // and it is the same as its item unit
        ArrayList<ElementRefBean> unitRefs = new ArrayList<ElementRefBean>();
        ElementRefBean unit = new ElementRefBean();
        unit.setElementDefOID(muOid);
        unitRefs.add(unit);

        List<RangeCheckBean> rcs = new ArrayList<RangeCheckBean>();
        // final String[] odmComparator = { "LT", "LE", "GT", "GE", "EQ", "NE",
        // "IN", "NOTIN" };
        String[] s = func.split("\\(");
        RangeCheckBean rc = new RangeCheckBean();
        if (s[0].equalsIgnoreCase("range")) {
            String[] values = s[1].split("\\,");
            String smaller = values[0];
            String larger = values[1].trim();
            larger = larger.substring(0, larger.length() - 1);
            rc.setComparator("GE");
            rc.setSoftHard(constraint);
            rc.getErrorMessage().setText(errorMessage);
            rc.setCheckValue(smaller);
            rc.setMeasurementUnitRefs(unitRefs);
            rcs.add(rc);
            rc = new RangeCheckBean();
            rc.setComparator("LE");
            rc.setSoftHard(constraint);
            rc.getErrorMessage().setText(errorMessage);
            rc.setCheckValue(larger);
            rc.setMeasurementUnitRefs(unitRefs);
            rcs.add(rc);
        } else {
            rc = new RangeCheckBean();
            String value = s[1].trim();
            value = value.substring(0, value.length() - 1);
            if (s[0].equalsIgnoreCase("gt")) {
                rc.setComparator("GT");
            } else if (s[0].equalsIgnoreCase("lt")) {
                rc.setComparator("LT");
            } else if (s[0].equalsIgnoreCase("gte")) {
                rc.setComparator("GE");
            } else if (s[0].equalsIgnoreCase("lte")) {
                rc.setComparator("LE");
            } else if (s[0].equalsIgnoreCase("ne")) {
                rc.setComparator("NE");
            } else if (s[0].equalsIgnoreCase("eq")) {
                rc.setComparator("EQ");
            }
            rc.setSoftHard(constraint);
            rc.getErrorMessage().setText(errorMessage);
            rc.setCheckValue(value);
            rc.setMeasurementUnitRefs(unitRefs);
            rcs.add(rc);
        }
        return rcs;
    }

    public static boolean needCodeList(int rsTypeId, int datatypeid) {
        if ((rsTypeId == 5 || rsTypeId == 6) && (datatypeid == 5 || datatypeid == 6 || datatypeid == 7)) {
            return true;
        }
        return false;
    }

    public static boolean needMultiSelectList(int rsTypeId) {
        if (rsTypeId == 3 || rsTypeId == 7) {
            return true;
        }
        return false;
    }

    public static LinkedHashMap<String, String> parseCode(String rsText, String rsValue, String nullValue) {
        LinkedHashMap<String, String> code = parseCode(rsText, rsValue);
        String[] nulls = nullValue.split(",");
        for (String s : nulls) {
            s = s.trim().toUpperCase();
            if (s.length() > 0 && nullValueMap.containsKey(s)) {
                code.put(nullValueMap.get(s), s);
            }
        }
        return code;
    }

    public static LinkedHashMap<String, String> parseCode(String rsText, String rsValue) {
        LinkedHashMap<String, String> code = new LinkedHashMap<String, String>();
        // code below following logic of the method
        // "setOptions(String optionsText, String optionsValues)"
        // in ResponseSetBean.java
        String[] keys = rsValue.replaceAll("\\\\,", "##").split(",");
        String[] values = rsText.replaceAll("\\\\,", "##").split(",");
        if (values == null) {
            return code;
        }
        if (keys == null) {
            keys = new String[0];
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null || values[i].length() <= 0) {
                continue;
            }
            String v = values[i].trim().replaceAll("##", ",");
            if (keys.length <= i || keys[i] == null || keys[i].length() <= 0) {
                code.put(v, v);
            } else {
                code.put(keys[i].trim().replaceAll("##", ","), v);
            }
        }
        return code;
    }

    @Override
    public OdmStudyBase getStudyBase() {
        return studyBase;
    }

    @Override
    public void setStudyBase(OdmStudyBase studyBase) {
        this.studyBase = studyBase;
    }

    public OdmStudyBean getOdmStudy() {
        return odmStudy;
    }

    public void setOdmStudy(OdmStudyBean odmStudy) {
        this.odmStudy = odmStudy;
    }

    public RuleSetRuleDao getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public StudyBean getParentStudy() {
        return parentStudy;
    }

    public void setParentStudy(StudyBean parentStudy) {
        this.parentStudy = parentStudy;
    }

}
