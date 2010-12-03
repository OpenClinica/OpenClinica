package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.DisplayItemHeaderBean;
import org.akaza.openclinica.bean.extract.ExportFormatBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.SPSSReportBean;
import org.akaza.openclinica.bean.extract.SPSSVariableNameValidationBean;
import org.akaza.openclinica.bean.extract.TabReportBean;
import org.akaza.openclinica.bean.extract.odm.AdminDataReportBean;
import org.akaza.openclinica.bean.extract.odm.FullReportBean;
import org.akaza.openclinica.bean.extract.odm.MetaDataReportBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.odmExport.AdminDataCollector;
import org.akaza.openclinica.logic.odmExport.ClinicalDataCollector;
import org.akaza.openclinica.logic.odmExport.ClinicalDataUnit;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.logic.odmExport.OdmStudyBase;
import org.apache.poi.util.TempFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

public class GenerateExtractFileService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final DataSource ds;
    private HttpServletRequest request;
    private DatasetDAO datasetDao;
    public static ResourceBundle resword;
    private final UserAccountBean userBean;
    private final CoreResources coreResources;

	 private static File files[]=null;
    private static List<File> oldFiles = new LinkedList<File>();
    private final RuleSetRuleDao ruleSetRuleDao;

    public GenerateExtractFileService(DataSource ds, HttpServletRequest request, UserAccountBean userBean,CoreResources coreResources,RuleSetRuleDao ruleSetRuleDao) {
        this.ds = ds;
        this.request = request;
        this.userBean = userBean;
        this.coreResources = coreResources;
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public GenerateExtractFileService(DataSource ds, UserAccountBean userBean, CoreResources coreResources,RuleSetRuleDao ruleSetRuleDao) {
        this.ds = ds;
        this.userBean = userBean;
        this.coreResources = coreResources;
        this.ruleSetRuleDao = ruleSetRuleDao;
    }

    public void setUpResourceBundles() {
        Locale locale;
        try {
            locale = request.getLocale();
        } catch (NullPointerException ne) {
            locale = new Locale("en-US");
        }

        ResourceBundleProvider.updateLocale(locale);
        resword = ResourceBundleProvider.getWordsBundle(locale);
    }

    /**
     * createTabFile, added by tbh, 01/2009
     */
    public HashMap<String, Integer> createTabFile(ExtractBean eb, long sysTimeBegin, String generalFileDir, DatasetBean datasetBean, int activeStudyId,
            int parentStudyId, String generalFileDirCopy) {

        TabReportBean answer = new TabReportBean();

        DatasetDAO dsdao = new DatasetDAO(ds);
        // create the extract bean here, tbh
        eb = dsdao.getDatasetData(eb, activeStudyId, parentStudyId);
        eb.getMetadata();
        eb.computeReport(answer);

        long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        String TXTFileName = datasetBean.getName() + "_tab.xls";

        int fId = this.createFile(TXTFileName, generalFileDir, answer.toString(), datasetBean, sysTimeEnd, ExportFormatBean.TXTFILE, true);
        if (!"".equals(generalFileDirCopy)) {
        	int fId2 = this.createFile(TXTFileName, generalFileDirCopy, answer.toString(), datasetBean, sysTimeEnd, ExportFormatBean.TXTFILE, false);
        }
        logger.info("created txt file");
        // return TXTFileName;
        HashMap answerMap = new HashMap<String, Integer>();
        answerMap.put(TXTFileName, new Integer(fId));
        return answerMap;
    }
    
    private Integer getStudySubjectNumber(String studySubjectNumber){
        try{
        Integer value = Integer.valueOf(studySubjectNumber);
        return value > 0 ? value : 99;
        }catch (NumberFormatException e) {
            return 99;
        }
    }
    
    /**
     * createODMFile, added by tbh, 09/2010 - note that this is created to be backwards-compatible with previous versions of OpenClinica-web.
     * i.e. we remove the boolean zipped variable.
     */
    public HashMap<String, Integer> createODMFile(String odmVersion, long sysTimeBegin, String generalFileDir, DatasetBean datasetBean, 
            StudyBean currentStudy, String generalFileDirCopy,ExtractBean eb, 
            Integer currentStudyId, Integer parentStudyId, String studySubjectNumber) {
        // default zipped - true
        return createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean, 
                currentStudy, generalFileDirCopy, eb, currentStudyId, parentStudyId, studySubjectNumber, true, true, true);
    }
    /**
     * createODMfile, added by tbh, 01/2009
     * @param deleteOld TODO
     */

    public HashMap<String, Integer> createODMFile(String odmVersion, long sysTimeBegin, String generalFileDir, DatasetBean datasetBean, 
    		StudyBean currentStudy, String generalFileDirCopy,ExtractBean eb, 
    		Integer currentStudyId, Integer parentStudyId, String studySubjectNumber, boolean zipped, boolean saveToDB, boolean deleteOld) {
        
        Integer ssNumber = getStudySubjectNumber(studySubjectNumber);
        MetaDataCollector mdc = new MetaDataCollector(ds, datasetBean, currentStudy,ruleSetRuleDao);
        AdminDataCollector adc = new AdminDataCollector(ds, datasetBean, currentStudy);
        ClinicalDataCollector cdc = new ClinicalDataCollector(ds, datasetBean, currentStudy);
       
        MetaDataCollector.setTextLength(200);
        if(deleteOld){
        	File file = new File(generalFileDir);
        	if(file.isDirectory())
        	{
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
                //odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.2 OpenClinica-ODM1-2-1.xsd");
                odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.2 OpenClinica-ODM1-2-1-OC1.xsd");
                ArrayList<String> xmlnsList = new ArrayList<String>();
                xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.2\"");
                //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.2\"");
                xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v121/v3.1\"");
                xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
                odmb.setXmlnsList(xmlnsList);
                odmb.setODMVersion("oc1.2");
                mdc.setODMBean(odmb);
                adc.setOdmbean(odmb);
                cdc.setODMBean(odmb);
            } else if ("oc1.3".equals(odmVersion)) {
                ODMBean odmb = mdc.getODMBean();
                //odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0.xsd");
                //odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC1.xsd");
                odmb.setSchemaLocation("http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC2-0.xsd");
                ArrayList<String> xmlnsList = new ArrayList<String>();
                xmlnsList.add("xmlns=\"http://www.cdisc.org/ns/odm/v1.3\"");
                //xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/openclinica_odm/v1.3\"");
                xmlnsList.add("xmlns:OpenClinica=\"http://www.openclinica.org/ns/odm_ext_v130/v3.1\"");
                xmlnsList.add("xmlns:OpenClinicaRules=\"http://www.openclinica.org/ns/rules/v3.1\"");
                odmb.setXmlnsList(xmlnsList);
                odmb.setODMVersion("oc1.3");
                mdc.setODMBean(odmb);
                adc.setOdmbean(odmb);
                cdc.setODMBean(odmb);
            }
        }
        
        //////////////////////////////////////////
        ////////// MetaData Extraction //////////
        mdc.collectFileData();
        MetaDataReportBean metaReport = new MetaDataReportBean(mdc.getOdmStudyMap(),coreResources);
        metaReport.setODMVersion(odmVersion);
        metaReport.setOdmBean(mdc.getODMBean());
        metaReport.createChunkedOdmXml(Boolean.TRUE);
        
        
        
        
        long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        String ODMXMLFileName = mdc.getODMBean().getFileOID() + ".xml";
        int fId =
            this.createFileK(ODMXMLFileName, generalFileDir, metaReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false, zipped, deleteOld);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 =
                this.createFileK(ODMXMLFileName, generalFileDirCopy, metaReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE,
                        false, zipped, deleteOld);
        }
        //////////////////////////////////////////
        ////////// AdminData Extraction //////////
        
        adc.collectFileData();
        AdminDataReportBean adminReport = new AdminDataReportBean(adc.getOdmAdminDataMap());
        adminReport.setODMVersion(odmVersion);
        adminReport.setOdmBean(mdc.getODMBean());
        adminReport.createChunkedOdmXml(Boolean.TRUE);
      
        
        
        sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        fId =
            this.createFileK(ODMXMLFileName, generalFileDir, adminReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false, zipped, deleteOld);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 =
                this.createFileK(ODMXMLFileName, generalFileDirCopy, adminReport.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE,
                        false, zipped, deleteOld);
        }
        
        //////////////////////////////////////////
        ////////// ClinicalData Extraction ///////

        DatasetDAO dsdao = new DatasetDAO(ds);
        String sql = eb.getDataset().getSQLStatement();
        String st_sed_in = dsdao.parseSQLDataset(sql, true, true);
        String st_itemid_in = dsdao.parseSQLDataset(sql, false, true);
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = dsdao.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = dsdao.getItemDataStatusConstraint(datasetItemStatusId);

        Iterator<OdmStudyBase> it = cdc.getStudyBaseMap().values().iterator();
        while (it.hasNext()) {
            OdmStudyBase u = it.next();
            ArrayList newRows =
                dsdao.selectStudySubjects(u.getStudy().getId(), 0, st_sed_in, st_itemid_in, dsdao.genDatabaseDateConstraint(eb), ecStatusConstraint,
                        itStatusConstraint);

            ///////////////
            int fromIndex = 0;
            boolean firstIteration = true;
            while (fromIndex < newRows.size()) {
                int toIndex = fromIndex + ssNumber < newRows.size() ? fromIndex + ssNumber : newRows.size() - 1;
                List x = newRows.subList(fromIndex, toIndex + 1);
                fromIndex = toIndex + 1;
                String studySubjectIds = "";
                for (int i = 0; i < x.size(); i++) {
                    StudySubjectBean sub = new StudySubjectBean();
                    sub = (StudySubjectBean) x.get(i);
                    studySubjectIds += "," + sub.getId();
                }//for
                studySubjectIds = studySubjectIds.replaceFirst(",", "");

                ClinicalDataUnit cdata = new ClinicalDataUnit(ds, datasetBean, cdc.getOdmbean(), u.getStudy(), cdc.getCategory(), studySubjectIds);
                cdata.setCategory(cdc.getCategory());
                cdata.collectOdmClinicalData();

                FullReportBean report = new FullReportBean();
                report.setClinicalData(cdata.getOdmClinicalData());
                report.setOdmStudyMap(mdc.getOdmStudyMap());
                report.setODMVersion(odmVersion);
                //report.setOdmStudy(mdc.getOdmStudy());
                report.setOdmBean(mdc.getODMBean());
                if (firstIteration && fromIndex >= newRows.size()) {
                    report.createChunkedOdmXml(Boolean.TRUE, true, true);
                    firstIteration = false;
                } else if (firstIteration) {
                    report.createChunkedOdmXml(Boolean.TRUE, true, false);
                    firstIteration = false;
                } else if (fromIndex >= newRows.size()) {
                    report.createChunkedOdmXml(Boolean.TRUE, false, true);
                } else {
                    report.createChunkedOdmXml(Boolean.TRUE, false, false);
                }
                fId =
                    this
                            .createFileK(ODMXMLFileName, generalFileDir, report.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE,
                                    false, zipped, deleteOld);
                if (!"".equals(generalFileDirCopy)) {
                    int fId2 =
                        this.createFileK(ODMXMLFileName, generalFileDirCopy, report.getXmlOutput().toString(), datasetBean, sysTimeEnd,
                                ExportFormatBean.XMLFILE, false, zipped, deleteOld);
                }
            }
        }

        sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        fId = this.createFileK(ODMXMLFileName, generalFileDir, "</ODM>", datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, saveToDB, zipped, deleteOld);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 = this.createFileK(ODMXMLFileName, generalFileDirCopy, "</ODM>", datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false, zipped, deleteOld);
        }
        
        
        //////////////////////////////////////////
        ////////// pre pagination extraction /////
        /*
        mdc.collectFileData();
        adc.collectOdmAdminDataMap();
        cdc.collectOdmClinicalDataMap();
        FullReportBean report = new FullReportBean();
        report.setClinicalDataMap(cdc.getOdmClinicalDataMap());
        report.setAdminDataMap(adc.getOdmAdminDataMap());
        report.setOdmStudyMap(mdc.getOdmStudyMap());
        report.setOdmBean(mdc.getODMBean());
        report.setODMVersion(odmVersion);
        report.createOdmXml(Boolean.TRUE);
        long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        String ODMXMLFileName = mdc.getODMBean().getFileOID() + ".xml";
        int fId = this.createFile(ODMXMLFileName, generalFileDir, report.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, true);
        if (!"".equals(generalFileDirCopy)) {
        	int fId2 = this.createFile(ODMXMLFileName, generalFileDirCopy, report.getXmlOutput().toString(), datasetBean, sysTimeEnd, ExportFormatBean.XMLFILE, false);
        } */
    	HashMap answerMap = new HashMap<String, Integer>();
		//JN: Zipped in the next stage as thats where the ODM file is named and copied over in default categories.
//        if(zipped)
//        {	try {
//				zipFile(ODMXMLFileName,generalFileDir);
//				
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				logger.error(e.getMessage());
//				e.printStackTrace();
//			}
//		
//        }   // return ODMXMLFileName;
        
        answerMap.put(ODMXMLFileName, new Integer(fId));
    //    if(deleteOld && files!=null &&oldFiles!=null) setOldFiles(oldFiles);
        return answerMap;
    }
    
    

    
    public List<File> getOldFiles(){
    	return oldFiles;
    }

	/**
     * createSPSSFile, added by tbh, 01/2009
     * 
     * @param db
     * @param eb
     * @param currentstudyid
     * @param parentstudy
     * @return
     */
    public HashMap<String, Integer> createSPSSFile(DatasetBean db, ExtractBean eb2, StudyBean currentStudy, StudyBean parentStudy, long sysTimeBegin,
            String generalFileDir, SPSSReportBean answer, String generalFileDirCopy) {
        setUpResourceBundles();

        String SPSSFileName = db.getName() + "_data_spss.dat";
        String DDLFileName = db.getName() + "_ddl_spss.sps";
        String ZIPFileName = db.getName() + "_spss";

        SPSSVariableNameValidationBean svnvbean = new SPSSVariableNameValidationBean();
        answer.setDatFileName(SPSSFileName);
        // DatasetDAO dsdao = new DatasetDAO(ds);

        // create the extract bean here, tbh
        // ExtractBean eb = this.generateExtractBean(db, currentStudy,
        // parentStudy);

        // eb = dsdao.getDatasetData(eb, currentStudy.getId(),
        // parentStudy.getId());

        // eb.getMetadata();

        // eb.computeReport(answer);

        answer.setItems(eb2.getItemNames());// set up items here to get
        // itemMetadata

        // set up response sets for each item here
        ItemDAO itemdao = new ItemDAO(ds);
        ItemFormMetadataDAO imfdao = new ItemFormMetadataDAO(ds);
        ArrayList items = answer.getItems();
        for (int i = 0; i < items.size(); i++) {
            DisplayItemHeaderBean dih = (DisplayItemHeaderBean) items.get(i);
            ItemBean item = dih.getItem();
            ArrayList metas = imfdao.findAllByItemId(item.getId());
            // for (int h = 0; h < metas.size(); h++) {
            // ItemFormMetadataBean ifmb = (ItemFormMetadataBean)
            // metas.get(h);
            // logger.info("group name found:
            // "+ifmb.getGroupLabel());
            // }
            // logger.info("crf versionname" +
            // meta.getCrfVersionName());
            item.setItemMetas(metas);

        }

        HashMap eventDescs = new HashMap<String, String>();

        eventDescs = eb2.getEventDescriptions();

        eventDescs.put("SubjID", resword.getString("study_subject_ID"));
        eventDescs.put("ProtocolID", resword.getString("protocol_ID_site_ID"));
        eventDescs.put("DOB", resword.getString("date_of_birth"));
        eventDescs.put("YOB", resword.getString("year_of_birth"));
        eventDescs.put("Gender", resword.getString("gender"));
        answer.setDescriptions(eventDescs);

        ArrayList generatedReports = new ArrayList<String>();
        try {
        	// YW <<
        	generatedReports.add(answer.getMetadataFile(svnvbean, eb2).toString());
        	generatedReports.add(answer.getDataFile().toString());
        	// YW >>
        } catch (IndexOutOfBoundsException i) {
        	generatedReports.add(answer.getMetadataFile(svnvbean, eb2).toString());
        	logger.debug("throw the error here");
        }

        long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;

        ArrayList titles = new ArrayList();
        // YW <<
        titles.add(DDLFileName);
        titles.add(SPSSFileName);
        // YW >>

        // create new createFile method that accepts array lists to
        // put into zip files
        int fId = this.createFile(ZIPFileName, titles, generalFileDir, generatedReports, db, sysTimeEnd, ExportFormatBean.TXTFILE, true);
        if (!"".equals(generalFileDirCopy)) {
        	int fId2 = this.createFile(ZIPFileName, titles, generalFileDirCopy, generatedReports, db, sysTimeEnd, ExportFormatBean.TXTFILE, false);
        }
        // return DDLFileName;
        HashMap answerMap = new HashMap<String, Integer>();
        answerMap.put(DDLFileName, new Integer(fId));
        return answerMap;
    }

    public int createFile(String zipName, ArrayList names, String dir, ArrayList contents, DatasetBean datasetBean, long time, 
    		ExportFormatBean efb, boolean saveToDB) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        // >> tbh #4915
        zipName = zipName.replaceAll(" ", "_");
        fbFinal.setId(0);
        try {
            File complete = new File(dir);
            if (!complete.isDirectory()) {
                complete.mkdirs();
            }
            int totalSize = 0;
            ZipOutputStream z = new ZipOutputStream(new FileOutputStream(new File(complete, zipName + ".zip")));
            FileInputStream is = null;
            for (int i = 0; i < names.size(); i++) {
                String name = (String) names.get(i);
                // >> tbh #4915
                name = name.replaceAll(" ", "_");
                String content = (String) contents.get(i);
                File newFile = new File(complete, name);
                // totalSize = totalSize + (int)newFile.length();
                newFile.setLastModified(System.currentTimeMillis());

                BufferedWriter w = new BufferedWriter(new FileWriter(newFile));
                w.write(content);
                w.close();
                logger.info("finished writing the text file...");
                // now, we write the file to the zip file
                is = new FileInputStream(newFile);

                logger.info("created zip output stream...");

                z.putNextEntry(new java.util.zip.ZipEntry(name));

                int bytesRead;
                byte[] buff = new byte[512];

                while ((bytesRead = is.read(buff)) != -1) {
                    z.write(buff, 0, bytesRead);
                    totalSize += 512;
                }
                z.closeEntry();
                //A. Hamid. 4910 
                is.close();
                if(CoreResources.getField("dataset_file_delete").equalsIgnoreCase("true")
                        || CoreResources.getField("dataset_file_delete").equals("")){
                    newFile.delete();
                }




            }
            logger.info("writing buffer...");
            // }
            z.flush();
            z.finish();
            z.close();

            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException ie) {
                    ie.printStackTrace();
                }
            }
            logger.info("finished zipping up file...");
            // set up the zip to go into the database
            if (saveToDB) {
	            ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
	            fb.setName(zipName + ".zip");
	            fb.setFileReference(dir + zipName + ".zip");
	            // current location of the file on the system
	            fb.setFileSize(totalSize);
	            // set the above to compressed size?
	            fb.setRunTime((int) time);
	            // need to set this in milliseconds, get it passed from above
	            // methods?
	            fb.setDatasetId(datasetBean.getId());
	            fb.setExportFormatBean(efb);
	            fb.setExportFormatId(efb.getId());
	            fb.setOwner(userBean);
	            fb.setOwnerId(userBean.getId());
	            fb.setDateCreated(new Date(System.currentTimeMillis()));
	
	            boolean write = true;
	            ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(ds);
	
	            if (write) {
	                fbFinal = (ArchivedDatasetFileBean) asdfDAO.create(fb);
	                logger.info("Created ADSFile!: " + fbFinal.getId() + " for " + zipName + ".zip");
	            } else {
	                logger.info("duplicate found: " + fb.getName());
	            }
            }
            // created in database!

        } catch (Exception e) {
            logger.warn(e.getMessage());
            System.out.println("-- exception at create file: " + e.getMessage());
            e.printStackTrace();
        }
        return fbFinal.getId();
    }
    
    public int createFileK(String name, String dir, String content, 
            DatasetBean datasetBean, long time, ExportFormatBean efb, 
            boolean saveToDB, boolean zipped, boolean deleteOld) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        // >> tbh 04/2010 #4915 replace all names' spaces with underscores
        name = name.replaceAll(" ", "_");
        fbFinal.setId(0);
        try {
          
        	
        	
        	File complete = new File(dir);
            if (!complete.isDirectory()) {
                complete.mkdirs();
            }
            
//            else  if(deleteOld)// so directory exists check if the files are there
//            {
//            	deleteDirectory(complete);
//            }
            
            //File newFile = new File(complete, name);
            //newFile.setLastModified(System.currentTimeMillis());

            File oldFile = new File(complete, name);
            File newFile = null;
            if (oldFile.exists()) {
                newFile = oldFile;
                if(oldFiles!=null || !oldFiles.isEmpty() )
                oldFiles.remove(oldFile);
            } else {
                newFile = new File(complete, name);
            }

            //File 
            newFile.setLastModified(System.currentTimeMillis());

            BufferedWriter w = new BufferedWriter(new FileWriter(newFile, true));
            w.write(content);
            w.close();
            logger.info("finished writing the text file...");
/*            if (zipped) {
                // now, we write the file to the zip file
                FileInputStream is = new FileInputStream(newFile);
                ZipOutputStream z = new ZipOutputStream(new FileOutputStream(new File(complete, name + ".zip")));
                if(oldFiles!=null || !oldFiles.isEmpty())
                {
                	
                	if(oldFiles.contains(new File(complete, name + ".zip")))
                	{
                		oldFiles.remove(new File(complete, name + ".zip"));//Dont delete the files which u r just creating
                	}
                }
                logger.info("created zip output stream...");
                // we write over the content no matter what
                // we then check to make sure there are no duplicates
                // TODO need to change the above -- save all content!
                // z.write(content);
                z.putNextEntry(new java.util.zip.ZipEntry(name));
                // int length = (int) newFile.length();
                int bytesRead;
                byte[] buff = new byte[512];
                // read from buffered input stream and put into zip file
                // while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
                while ((bytesRead = is.read(buff)) != -1) {
                    z.write(buff, 0, bytesRead);
                }
                logger.info("writing buffer...");
                // }
                z.closeEntry();
                z.finish();
                // newFile = new File(complete, name+".zip");
                // newFile.setLastModified(System.currentTimeMillis());
                //
                // BufferedWriter w2 = new BufferedWriter(new FileWriter(newFile));
                // w2.write(newOut.toString());
                // w2.close();
                if (is != null) {
                    try {
                        is.close();
                    } catch (java.io.IOException ie) {
                        ie.printStackTrace();
                    }
                }
                logger.info("finished zipping up file...");
            }*/
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
                ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(ds);
                // eliminating all checks so that we create multiple files, tbh 6-7
                if (write) {
                    fbFinal = (ArchivedDatasetFileBean) asdfDAO.create(fb);
                } else {
                    logger.info("duplicate found: " + fb.getName());
                }
            }
            // created in database!

        } catch (Exception e) {
            logger.warn(e.getMessage());
            System.out.println("-- exception thrown at createFile: " + e.getMessage());
            logger.info("-- exception thrown at createFile: " + e.getMessage());
            e.printStackTrace();
        }

        return fbFinal.getId();
    }

    private void deleteOldFiles(List oldFiles2) {
    	
    		//File[] files = complete.listFiles();
    	
    		Iterator<File> fileIt = oldFiles2.iterator();
    		while(fileIt.hasNext())
    		{
    			fileIt.next().delete();
    		}
    	
		
	}

	public int createFile(String name, String dir, String content, DatasetBean datasetBean, long time, ExportFormatBean efb, boolean saveToDB) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        // >> tbh 04/2010 #4915 replace all names' spaces with underscores
        name = name.replaceAll(" ", "_");
        fbFinal.setId(0);
        try {
            File complete = new File(dir);
            if (!complete.isDirectory()) {
                complete.mkdirs();
            }
            File newFile = new File(complete, name);
            newFile.setLastModified(System.currentTimeMillis());

            BufferedWriter w = new BufferedWriter(new FileWriter(newFile));
            w.write(content);
            w.close();
            logger.info("finished writing the text file...");
            // now, we write the file to the zip file
            FileInputStream is = new FileInputStream(newFile);
            ZipOutputStream z = new ZipOutputStream(new FileOutputStream(new File(complete, name + ".zip")));
            logger.info("created zip output stream...");
            // we write over the content no matter what
            // we then check to make sure there are no duplicates
            // TODO need to change the above -- save all content!
            // z.write(content);
            z.putNextEntry(new java.util.zip.ZipEntry(name));
            // int length = (int) newFile.length();
            int bytesRead;
            byte[] buff = new byte[512];
            // read from buffered input stream and put into zip file
            // while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            while ((bytesRead = is.read(buff)) != -1) {
                z.write(buff, 0, bytesRead);
            }
            logger.info("writing buffer...");
            // }
            z.closeEntry();
            z.finish();
            // newFile = new File(complete, name+".zip");
            // newFile.setLastModified(System.currentTimeMillis());
            //
            // BufferedWriter w2 = new BufferedWriter(new FileWriter(newFile));
            // w2.write(newOut.toString());
            // w2.close();
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException ie) {
                    ie.printStackTrace();
                }
            }
            logger.info("finished zipping up file...");
            // set up the zip to go into the database
            if (saveToDB) {
	            ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
	            fb.setName(name + ".zip");
	            // logger.info("ODM filename: " + name + ".zip");
	            fb.setFileReference(dir + name + ".zip");
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
	            ArchivedDatasetFileDAO asdfDAO = new ArchivedDatasetFileDAO(ds);
	            // eliminating all checks so that we create multiple files, tbh 6-7
	            if (write) {
	                fbFinal = (ArchivedDatasetFileBean) asdfDAO.create(fb);
	            } else {
	                logger.info("duplicate found: " + fb.getName());
	            }
            }
            // created in database!

        } catch (Exception e) {
            logger.warn(e.getMessage());
            System.out.println("-- exception thrown at createFile: " + e.getMessage());
            logger.info("-- exception thrown at createFile: " + e.getMessage());
            e.printStackTrace();
        }

        return fbFinal.getId();
    }

    public ExtractBean generateExtractBean(DatasetBean dsetBean, StudyBean currentStudy, StudyBean parentStudy) {
        ExtractBean eb = new ExtractBean(ds);
        eb.setDataset(dsetBean);
        eb.setShowUniqueId(CoreResources.getField("show_unique_id"));
        eb.setStudy(currentStudy);
        eb.setParentStudy(parentStudy);
        eb.setDateCreated(new java.util.Date());
        return eb;
    }

    /**
     * To zip the xml files and delete the intermediate files.
     * @param name
     * @param dir
     * @throws IOException
     */
    
    public void zipFile(String name, String dir) throws IOException
    {
        //if (zipped) {
    	String zipFileName = null;
    	File complete = new File(dir);
        if (!complete.isDirectory()) {
            complete.mkdirs();
        }
        
        File[] interXMLS = complete.listFiles();
        List<File> temp  = new LinkedList<File>(Arrays.asList(interXMLS));
      
        	
        File oldFile = new File(complete, name);
       
        File newFile = null;
        if (oldFile.exists()) {
            newFile = oldFile;
           
        } else {
            newFile = new File(complete, name);
        }
            // now, we write the file to the zip file
            FileInputStream is = new FileInputStream(newFile);
            ZipOutputStream z = new ZipOutputStream(new FileOutputStream(new File(complete, name + ".zip")));
            if(oldFiles!=null || !oldFiles.isEmpty())
            {
            	
            	if(oldFiles.contains(new File(complete, name + ".zip")))
            	{
            		oldFiles.remove(new File(complete, name + ".zip"));//Dont delete the files which u r just creating
            		
            	}
            }
            logger.info("created zip output stream...");
            // we write over the content no matter what
            // we then check to make sure there are no duplicates
            // TODO need to change the above -- save all content!
            // z.write(content);
            z.putNextEntry(new java.util.zip.ZipEntry(name));
            // int length = (int) newFile.length();
            int bytesRead;
            byte[] buff = new byte[512];
            // read from buffered input stream and put into zip file
            // while (-1 != (bytesRead = bis.read(buff, 0, buff.length))) {
            while ((bytesRead = is.read(buff)) != -1) {
                z.write(buff, 0, bytesRead);
            }
            logger.info("writing buffer...");
            // }
            
            z.closeEntry();
            z.finish();
            if(z!=null)z.close();
            // newFile = new File(complete, name+".zip");
            // newFile.setLastModified(System.currentTimeMillis());
            //
            // BufferedWriter w2 = new BufferedWriter(new FileWriter(newFile));
            // w2.write(newOut.toString());
            // w2.close();
            if (is != null) {
                try {
                    is.close();
                } catch (java.io.IOException ie) {
                    ie.printStackTrace();
                }
            }
           //Adding the logic to delete the intermediate xmls
           oldFiles = temp;
            logger.info("finished zipping up file...");
       // }
    }
}
