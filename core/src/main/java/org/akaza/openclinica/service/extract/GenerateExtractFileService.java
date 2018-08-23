package org.akaza.openclinica.service.extract;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
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

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.DisplayItemHeaderBean;
import org.akaza.openclinica.bean.extract.ExportFormatBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.SPSSReportBean;
import org.akaza.openclinica.bean.extract.SPSSVariableNameValidator;
import org.akaza.openclinica.bean.extract.TabReportBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateExtractFileService {

    private static final Logger logger = LoggerFactory.getLogger(GenerateExtractFileService.class);
    private final DataSource ds;
    private HttpServletRequest request;
    public static ResourceBundle resword;
    private final CoreResources coreResources;

    private static File files[]=null;
    private static List<File> oldFiles = new LinkedList<File>();
    private final RuleSetRuleDao ruleSetRuleDao;
    private PermissionService permissionService;


    public GenerateExtractFileService(DataSource ds, HttpServletRequest request, CoreResources coreResources,
            RuleSetRuleDao ruleSetRuleDao ,PermissionService permissionService) {
        this.ds = ds;
        this.request = request;
        this.coreResources = coreResources;
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.permissionService=permissionService;
    }

    public GenerateExtractFileService(DataSource ds, CoreResources coreResources,RuleSetRuleDao ruleSetRuleDao,PermissionService permissionService) {
        this.ds = ds;
        this.coreResources = coreResources;
        this.ruleSetRuleDao = ruleSetRuleDao;
        this.permissionService=permissionService;
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
            int parentStudyId, String generalFileDirCopy, UserAccountBean userBean) {

        TabReportBean answer = new TabReportBean();

        DatasetDAO dsdao = new DatasetDAO(ds);
        // create the extract bean here, tbh
        eb = dsdao.getDatasetData(eb, activeStudyId, parentStudyId);
        eb.getMetadata();
        eb.computeReport(answer);

        long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
        String TXTFileName = datasetBean.getName() + "_tab.xls";

        int fId = this.createFile(TXTFileName, generalFileDir, answer.toString(), datasetBean, sysTimeEnd, ExportFormatBean.TXTFILE, true, userBean);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 = this.createFile(TXTFileName, generalFileDirCopy, answer.toString(), datasetBean, sysTimeEnd, ExportFormatBean.TXTFILE, false, userBean);
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
            Integer currentStudyId, Integer parentStudyId, String studySubjectNumber, UserAccountBean userBean) {
        // default zipped - true
        return createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean,
                currentStudy, generalFileDirCopy, eb, currentStudyId, parentStudyId, studySubjectNumber, true, true, true, null, userBean);
    }
    /**
     * createODMfile, added by tbh, 01/2009
     * @param deleteOld TODO
     * @param odmType TODO
     * @deprecated Use {@link OdmFileCreation#createODMFile} instead
     */
    @Deprecated
    public HashMap<String, Integer> createODMFile(String odmVersion, long sysTimeBegin, String generalFileDir, DatasetBean datasetBean,
            StudyBean currentStudy, String generalFileDirCopy,ExtractBean eb,
            Integer currentStudyId, Integer parentStudyId, String studySubjectNumber, boolean zipped, boolean saveToDB, boolean deleteOld, String odmType, UserAccountBean userBean){

        String permissionTagsString =permissionService.getPermissionTagsString(currentStudy,request);
        String[] permissionTagsStringArray =permissionService.getPermissionTagsStringArray(currentStudy,request);

        return new OdmFileCreation().createODMFile(odmVersion, sysTimeBegin, generalFileDir, datasetBean,
                currentStudy, generalFileDirCopy, eb,
                currentStudyId, parentStudyId, studySubjectNumber, zipped, saveToDB, deleteOld, odmType, userBean,permissionTagsString,permissionTagsStringArray);
    }

    public List<File> getOldFiles(){
        return oldFiles;
    }

    /**
     * createSPSSFile, added by tbh, 01/2009
     *
     * @param db

     * @return
     */
    public HashMap<String, Integer> createSPSSFile(DatasetBean db, ExtractBean eb2, StudyBean currentStudy, StudyBean parentStudy, long sysTimeBegin,
            String generalFileDir, SPSSReportBean answer, String generalFileDirCopy, UserAccountBean userBean) {
        setUpResourceBundles();

        String SPSSFileName = db.getName() + "_data_spss.dat";
        String DDLFileName = db.getName() + "_ddl_spss.sps";
        String ZIPFileName = db.getName() + "_spss";

        SPSSVariableNameValidator svnv = new SPSSVariableNameValidator();
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
            generatedReports.add(answer.getMetadataFile(svnv, eb2).toString());
            generatedReports.add(answer.getDataFile().toString());
            // YW >>
        } catch (IndexOutOfBoundsException i) {
            generatedReports.add(answer.getMetadataFile(svnv, eb2).toString());
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
        int fId = this.createFile(ZIPFileName, titles, generalFileDir, generatedReports, db, sysTimeEnd, ExportFormatBean.TXTFILE, true, userBean);
        if (!"".equals(generalFileDirCopy)) {
            int fId2 = this.createFile(ZIPFileName, titles, generalFileDirCopy, generatedReports, db, sysTimeEnd, ExportFormatBean.TXTFILE, false, userBean);
        }
        // return DDLFileName;
        HashMap answerMap = new HashMap<String, Integer>();
        answerMap.put(DDLFileName, new Integer(fId));
        return answerMap;
    }

    public int createFile(String zipName, ArrayList names, String dir, ArrayList contents, DatasetBean datasetBean, long time,
            ExportFormatBean efb, boolean saveToDB, UserAccountBean userBean) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        // >> tbh #4915
        zipName = zipName.replaceAll(" ", "_");
        fbFinal.setId(0);
        BufferedWriter w = null;
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

                 w = new BufferedWriter(new FileWriter(newFile));
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
            e.printStackTrace();
        }
        finally{
            if(w!=null)
                try {
                    w.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return fbFinal.getId();
    }

    public int createFileK(String name, String dir, String content,
            DatasetBean datasetBean, long time, ExportFormatBean efb,
            boolean saveToDB, boolean zipped, boolean deleteOld, UserAccountBean userBean) {
        ArchivedDatasetFileBean fbFinal = new ArchivedDatasetFileBean();
        // >> tbh 04/2010 #4915 replace all names' spaces with underscores
        name = name.replaceAll(" ", "_");
        fbFinal.setId(0);
        BufferedWriter w =null;
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

            w = new BufferedWriter(new FileWriter(newFile, true));
            w.write(content);
            w.close();
            logger.info("finished writing the text file...");
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
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        finally{
            if(w!=null)
                try {
                    w.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
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

    public int createFile(String name, String dir, String content, DatasetBean datasetBean, long time,
            ExportFormatBean efb, boolean saveToDB, UserAccountBean userBean) {
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
            logger.error("-- exception thrown at createFile: " + e.getMessage());
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
