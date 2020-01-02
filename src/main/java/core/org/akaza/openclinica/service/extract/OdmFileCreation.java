/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import core.org.akaza.openclinica.bean.extract.DatasetBean;
import core.org.akaza.openclinica.bean.extract.ExportFormatBean;
import core.org.akaza.openclinica.bean.extract.ExtractBean;
import core.org.akaza.openclinica.bean.extract.odm.AdminDataReportBean;
import core.org.akaza.openclinica.bean.extract.odm.FullReportBean;
import core.org.akaza.openclinica.bean.extract.odm.MetaDataReportBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.odmbeans.ODMBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import core.org.akaza.openclinica.dao.extract.DatasetDAO;
import core.org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.job.JobTerminationMonitor;
import core.org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import core.org.akaza.openclinica.logic.odmExport.ClinicalDataCollector;
import core.org.akaza.openclinica.logic.odmExport.ClinicalDataUnit;
import core.org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import core.org.akaza.openclinica.logic.odmExport.OdmStudyBase;
import core.org.akaza.openclinica.service.dto.ODMFilterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class OdmFileCreation {

    private static final Logger LOG = LoggerFactory.getLogger(OdmFileCreation.class);

    private RuleSetRuleDao ruleSetRuleDao;
    private DataSource dataSource;
    private CoreResources coreResources;
    private StudyDao studyDao;
    private static File files[] = null;
    private static List<File> oldFiles = new LinkedList<File>();

    public HashMap<String, Integer> createODMFile(String odmVersion, long sysTimeBegin, String generalFileDir, DatasetBean datasetBean, Study currentStudy,
                                                  String generalFileDirCopy, ExtractBean eb, Integer currentStudyId, Integer parentStudyId, String studySubjectNumber, boolean zipped,
                                                  boolean saveToDB, boolean deleteOld, String odmType, UserAccountBean userBean, ODMFilterDTO odmFilter, String permissionTagsString, String[] permissionTagsStringArray, Set<Integer> edcSet) {



        Integer ssNumber = getStudySubjectNumber(studySubjectNumber);
        MetaDataCollector mdc = new MetaDataCollector(dataSource, datasetBean, currentStudy, ruleSetRuleDao,permissionTagsString, studyDao);
        AdminDataCollector adc = new AdminDataCollector(dataSource, datasetBean, currentStudy, studyDao);
        ClinicalDataCollector cdc = new ClinicalDataCollector(dataSource, datasetBean, currentStudy, studyDao);

        MetaDataCollector.setTextLength(200);
        if (deleteOld) {
            File file = new File(generalFileDir);
            if (file.isDirectory()) {
                files = file.listFiles();
                oldFiles = Arrays.asList(files);
            }
        }
        if (odmVersion != null) {
            // by default odmVersion is 1.2
            if ("1.3".equals(odmVersion)) {
                ODMBean odmb = new ODMBean();
                odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 ODM1-3-0.xsd");
                ArrayList<String> xmlnsList = new ArrayList<String>();
                xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
                odmb.setXmlnsList(xmlnsList);
                odmb.setODMVersion("1.3");
                mdc.setODMBean(odmb);
                adc.setOdmbean(odmb);
                cdc.setODMBean(odmb);
            } else if ("oc1.2".equals(odmVersion)) {
                ODMBean odmb = new ODMBean();
                // odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.2 OpenClinica-ODM1-2-1.xsd");
                odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.2 OpenClinica-ODM1-2-1-OC1.xsd");
                ArrayList<String> xmlnsList = new ArrayList<String>();
                xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.2\"");
                // xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.2\"");
                xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v121/v3.1\"");
                xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
                odmb.setXmlnsList(xmlnsList);
                odmb.setODMVersion("oc1.2");
                mdc.setODMBean(odmb);
                adc.setOdmbean(odmb);
                cdc.setODMBean(odmb);
            } else if ("oc1.3".equals(odmVersion)) {
                ODMBean odmb = mdc.getODMBean();
                // odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0.xsd");
                // odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC1.xsd");
                odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC3-0.xsd");
                ArrayList<String> xmlnsList = new ArrayList<String>();
                xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
                // xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
                xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
                xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
                odmb.setXmlnsList(xmlnsList);
                odmb.setODMVersion("oc1.3");
                odmb.setOdmType(odmType);
                mdc.setODMBean(odmb);
                adc.setOdmbean(odmb);
                cdc.setODMBean(odmb);
            }

        }

        //////////////////////////////////////////
        ////////// MetaData Extraction //////////
        mdc.collectFileData();
        MetaDataReportBean metaReport = new MetaDataReportBean(mdc.getOdmStudyMap(), coreResources);
        metaReport.setODMVersion(odmVersion);
        metaReport.setOdmBean(mdc.getODMBean());
        metaReport.createChunkedOdmXml(Boolean.TRUE);

        long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        String ODMXMLFileName = mdc.getODMBean().getFileOID() + ".xml";

        int fId = createFileK(ODMXMLFileName, generalFileDir, metaReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false,
                zipped, deleteOld, userBean);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 = createFileK(ODMXMLFileName, generalFileDirCopy, metaReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE,
                    false, zipped, deleteOld, userBean);
        }
        //////////////////////////////////////////
        ////////// AdminData Extraction //////////

        adc.collectFileData();
        AdminDataReportBean adminReport = new AdminDataReportBean(adc.getOdmAdminDataMap());
        adminReport.setODMVersion(odmVersion);
        adminReport.setOdmBean(mdc.getODMBean());
        adminReport.createChunkedOdmXml(Boolean.TRUE);

        sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        fId = createFileK(ODMXMLFileName, generalFileDir, adminReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false,
                zipped, deleteOld, userBean);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 = createFileK(ODMXMLFileName, generalFileDirCopy, adminReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE,
                    false, zipped, deleteOld, userBean);
        }

        //////////////////////////////////////////
        ////////// ClinicalData Extraction ///////

        DatasetDAO dsdao = new DatasetDAO(dataSource);
        String sql = eb.getDataset().getSQLStatement();
        String st_sed_in = dsdao.parseSQLDataset(sql, true, true);
        String st_itemid_in = dsdao.parseSQLDataset(sql, false, true);
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = dsdao.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = dsdao.getItemDataStatusConstraint(datasetItemStatusId);

        Iterator<OdmStudyBase> it = cdc.getStudyBaseMap().values().iterator();
        while (it.hasNext()) {
            JobTerminationMonitor.check();

            OdmStudyBase u = it.next();
            ArrayList newRows = dsdao.selectStudySubjects(u.getStudy().getStudyId(), 0, st_sed_in, st_itemid_in, dsdao.genDatabaseDateConstraint(eb),
                    ecStatusConstraint, itStatusConstraint);

            ///////////////
            int fromIndex = 0;
            boolean firstIteration = true;
            while (fromIndex < newRows.size()) {
                JobTerminationMonitor.check();

                int toIndex = fromIndex + ssNumber < newRows.size() ? fromIndex + ssNumber : newRows.size() - 1;
                List x = newRows.subList(fromIndex, toIndex + 1);
                fromIndex = toIndex + 1;
                String studySubjectIds = "";
                for (int i = 0; i < x.size(); i++) {
                    StudySubjectBean sub = new StudySubjectBean();
                    sub = (StudySubjectBean) x.get(i);
                    studySubjectIds += "," + sub.getId();
                } // for
                studySubjectIds = studySubjectIds.replaceFirst(",", "");

                ClinicalDataUnit cdata = new ClinicalDataUnit(dataSource, datasetBean, cdc.getOdmbean(), u.getStudy(), cdc.getCategory(), studySubjectIds,permissionTagsString,edcSet, studyDao);
                cdata.setCategory(cdc.getCategory());
                cdata.collectOdmClinicalData();

                FullReportBean report = new FullReportBean();
                report.setStudyDao(studyDao);
                report.setClinicalData(cdata.getOdmClinicalData());
                report.setOdmStudyMap(mdc.getOdmStudyMap());
                report.setODMVersion(odmVersion);
                // report.setOdmStudy(mdc.getOdmStudy());
                report.setOdmBean(mdc.getODMBean());
                if (firstIteration && fromIndex >= newRows.size()) {
                    report.createChunkedOdmXml(Boolean.TRUE, true, true, this.dataSource, userBean, odmFilter, permissionTagsStringArray);
                    firstIteration = false;
                } else if (firstIteration) {
                    report.createChunkedOdmXml(Boolean.TRUE, true, false, this.dataSource, userBean, odmFilter, permissionTagsStringArray);
                    firstIteration = false;
                } else if (fromIndex >= newRows.size()) {
                    report.createChunkedOdmXml(Boolean.TRUE, false, true, this.dataSource, userBean, odmFilter, permissionTagsStringArray);
                } else {
                    report.createChunkedOdmXml(Boolean.TRUE, false, false, this.dataSource, userBean, odmFilter, permissionTagsStringArray);
                }
                fId = createFileK(ODMXMLFileName, generalFileDir, report.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false,
                        zipped, deleteOld, userBean);
                if (!"".equals(generalFileDirCopy)) {
                    int fId2 = createFileK(ODMXMLFileName, generalFileDirCopy, report.getXmlOutput().toString(), datasetBean, sysTimeEnd,
                            ExportFormatBean.XMLFILE, false, zipped, deleteOld, userBean);
                }
            }
        }

        sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        fId = createFileK(ODMXMLFileName, generalFileDir, "</ODM>", datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, saveToDB, zipped, deleteOld, userBean);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 = createFileK(ODMXMLFileName, generalFileDirCopy, "</ODM>", datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false, zipped, deleteOld,
                    userBean);
        }

        //////////////////////////////////////////
        ////////// pre pagination extraction /////
        /*
         * mdc.collectFileData();
         * adc.collectOdmAdminDataMap();
         * cdc.collectOdmClinicalDataMap();
         * FullReportBean report = new FullReportBean();
         * report.setClinicalDataMap(cdc.getOdmClinicalDataMap());
         * report.setAdminDataMap(adc.getOdmAdminDataMap());
         * report.setOdmStudyMap(mdc.getOdmStudyMap());
         * report.setOdmBean(mdc.getODMBean());
         * report.setODMVersion(odmVersion);
         * report.createOdmXml(Boolean.TRUE);
         * long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
         * String ODMXMLFileName = mdc.getODMBean().getFileOID() + ".xml";
         * int fId = this.createFile(ODMXMLFileName, generalFileDir, report.getXmlOutput().toString(), datasetBean,
         * sysTimeEnd, ExportFormatBean.XMLFILE, true);
         * if (!"".equals(generalFileDirCopy)) {
         * int fId2 = this.createFile(ODMXMLFileName, generalFileDirCopy, report.getXmlOutput().toString(), datasetBean,
         * sysTimeEnd, ExportFormatBean.XMLFILE, false);
         * }
         */
        HashMap answerMap = new HashMap<String, Integer>();
        // JN: Zipped in the next stage as thats where the ODM file is named and copied over in default categories.
        // if(zipped)
        // { try {
        // zipFile(ODMXMLFileName,generalFileDir);
        //
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // logger.error(e.getMessage());
        // e.printStackTrace();
        // }
        //
        // } // return ODMXMLFileName;

        answerMap.put(ODMXMLFileName, new Integer(fId));
        // if(deleteOld && files!=null &&oldFiles!=null) setOldFiles(oldFiles);
        return answerMap;
    }

    public int createFileK(String name, String dir, String content, DatasetBean datasetBean, long time, ExportFormatBean efb, boolean saveToDB, boolean zipped,
            boolean deleteOld, UserAccountBean userBean) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        // >> tbh 04/2010 #4915 replace all names' spaces with underscores
        name = name.replaceAll(" ", "_");
        fbFinal.setId(0);
        BufferedWriter w = null;
        try {

            File complete = new File(dir);
            if (!complete.isDirectory()) {
                complete.mkdirs();
            }

            // else if(deleteOld)// so directory exists check if the files are there
            // {
            // deleteDirectory(complete);
            // }

            // File newFile = new File(complete, name);
            // newFile.setLastModified(System.currentTimeMillis());

            File oldFile = new File(complete, name);
            File newFile = null;
            if (oldFile.exists()) {
                newFile = oldFile;
                if (oldFiles != null || !oldFiles.isEmpty()) {
                    oldFiles.remove(oldFile);
                }
            } else {
                newFile = new File(complete, name);
            }

            // File
            newFile.setLastModified(System.currentTimeMillis());

            w = new BufferedWriter(new FileWriter(newFile, true));
            w.write(content);
            w.close();
            LOG.info("finished writing the text file..." + dir + "#######file:" + name);
            // set up the zip to go into the database
            if (saveToDB) {
                ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
                if (zipped) {
                    fb.setName(name + ".zip");
                    fb.setFileReference(dir + name + ".zip");
                } else {
                    fb.setName(name);
                    fb.setFileReference(dir + name);
                }
                // logger.info("ODM filename: " + name + ".zip");

                // logger.info("ODM fileReference: " + dir + name + ".zip");
                // current location of the file on the system
                fb.setFileSize((int) newFile.length());
                // logger.info("ODM setFileSize: " + (int)newFile.length() );
                // set the above to compressed size?
                fb.setRunTime((int) time);
                // logger.info("ODM setRunTime: " + (int)time );
                // need to set this in milliseconds, get it passed from above
                // methods?
                fb.setDatasetId(datasetBean.getId());
                // logger.info("ODM setDatasetid: " + ds.getId() );
                fb.setExportFormatBean(efb);
                // logger.info("ODM setExportFormatBean: success" );
                fb.setExportFormatId(efb.getId());
                // logger.info("ODM setExportFormatId: " + efb.getId());
                fb.setOwner(userBean);
                // logger.info("ODM setOwner: " + sm.getUserBean());
                fb.setOwnerId(userBean.getId());
                // logger.info("ODM setOwnerId: " + sm.getUserBean().getId() );
                fb.setDateCreated(new Date(System.currentTimeMillis()));
                boolean write = true;
                ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(dataSource);
                // eliminating all checks so that we create multiple files, tbh 6-7
                if (write) {
                    fbFinal = (ArchivedDatasetFileBean) asdfDAO.create(fb);
                } else {
                    LOG.info("duplicate found: " + fb.getName());
                }
            }
            // created in database!

        } catch (Exception e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        } finally {
            if (w != null)
                try {
                    w.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return fbFinal.getId();
    }

    private Integer getStudySubjectNumber(String studySubjectNumber) {
        try {
            Integer value = Integer.valueOf(studySubjectNumber);
            return value > 0 ? value : 99;
        } catch (NumberFormatException e) {
            return 99;
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public RuleSetRuleDao getRuleSetRuleDao() {
        return ruleSetRuleDao;
    }

    public void setRuleSetRuleDao(RuleSetRuleDao ruleSetRuleDao) {
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public StudyDao getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

}
