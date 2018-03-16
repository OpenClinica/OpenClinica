/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.logic.odmExport;

import java.util.HashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for elements in ODM XML file
 *
 * @author ywang (May, 2009)
 */

public class OdmUnit {
    protected DataSource ds;
    protected DatasetBean dataset;
    protected OdmStudyBase studyBase;
    protected ODMBean odmBean;
    // contains all item_ids which have codelist with null value(s)
    protected static HashMap<String, String> nullValueMap = new HashMap<String, String>();
    private String parentMetaDataVersionOid;
    private String parentOdmStudyOid; // ODM Study element OID
    // 0: one Study Element; 1: one parent study and its sites
    private int category;
    protected boolean showArchived;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OdmUnit() {
    }

    public OdmUnit(DataSource ds, boolean showArchived) {
        this.ds = ds;
        this.showArchived = showArchived;
    }

    public OdmUnit(DataSource ds, StudyBean study, int category) {
        this.ds = ds;
        this.dataset = new DatasetBean();
        this.studyBase = new OdmStudyBase(this.ds, study);
        this.odmBean = new ODMBean();
        nullValueMap = initialNullValueMap();
        parentMetaDataVersionOid = "";
        parentOdmStudyOid = "";
        this.category = category;
    }

    public OdmUnit(DataSource ds, DatasetBean dataset, ODMBean odmBean, StudyBean study, int category, boolean showArchived) {
        this(ds, dataset, odmBean, study, category);
        this.showArchived = showArchived;
    }

    public OdmUnit(DataSource ds, DatasetBean dataset, ODMBean odmBean, StudyBean study, int category) {
        this.ds = ds;
        this.dataset = dataset.getId() > 0 ? dataset : new DatasetBean();
        this.studyBase = new OdmStudyBase(this.ds, study);
        this.odmBean = odmBean;
        nullValueMap = initialNullValueMap();
        parentMetaDataVersionOid = "";
        parentOdmStudyOid = "";
        this.category = category;
    }

    private HashMap<String, String> initialNullValueMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("UNK", ".U");
        map.put("NA", ".A");
        map.put("NI", ".I");
        map.put("NASK", ".K");
        map.put("NAV", ".V");
        map.put("PINF", ".P");
        map.put("NINF", ".N");
        map.put("MSK", ".M");
        map.put("ASKU", ".S");
        map.put("OTH", ".O");
        map.put("NP", ".X");
        map.put("NPE", ".E");
        return map;
    }

    public DataSource getDataSource() {
        return ds;
    }

    public void setDataSource(DataSource ds) {
        this.ds = ds;
    }

    public DatasetBean getDataset() {
        return dataset;
    }

    public void setDataset(DatasetBean dataset) {
        this.dataset = dataset;
    }

    public ODMBean getOdmBean() {
        return odmBean;
    }

    public void setOdmBean(ODMBean odmBean) {
        this.odmBean = odmBean;
    }

    public static HashMap<String, String> getNullValueMap() {
        return nullValueMap;
    }

    public static void setNullValueMap(HashMap<String, String> nullValueMap) {
        OdmUnit.nullValueMap = nullValueMap;
    }

    public OdmStudyBase getStudyBase() {
        return studyBase;
    }

    public void setStudyBase(OdmStudyBase studyBase) {
        this.studyBase = studyBase;
    }

    public String getParentMetaDataVersionOid() {
        return parentMetaDataVersionOid;
    }

    public void setParentMetaDataVersionOid(String parentMetaDataVersionOid) {
        this.parentMetaDataVersionOid = parentMetaDataVersionOid;
    }

    public String getParentOdmStudyOid() {
        return parentOdmStudyOid;
    }

    public void setParentOdmStudyOid(String parentOdmStudyOid) {
        this.parentOdmStudyOid = parentOdmStudyOid;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
