/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.CommaReportBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExportFormatBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.SPSSReportBean;
import org.akaza.openclinica.bean.extract.TabReportBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.PermissionService;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.bean.ArchivedDatasetFileRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.job.XalanTriggerService;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdScheduler;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * Take a dataset and show it in different formats,<BR/> Detect whether or not
 * files exist in the system or database,<BR/> Give the user the option of
 * showing a stored dataset, or refresh the current one.
 * </P>
 *
 * TODO eventually allow for a thread to be split off, so that exporting can run
 * seperately from the servlet and be retrieved at a later time.
 *
 * @author thickerson
 *
 *
 */
public class ExportDatasetServlet extends SecureController {

    public static String getLink(int dsId) {
        return "ExportDataset?datasetId=" + dsId;
    }

    private StdScheduler scheduler;

    private static String SCHEDULER = "schedulerFactoryBean";
    private static final String DATASET_DIR = SQLInitServlet.getField("filePath") + "datasets" + File.separator;

    private static String WEB_DIR = "/WEB-INF/datasets/";
    // may not use the above, security issue
    public File SASFile;
    public String SASFilePath;
    public File SPSSFile;
    public String SPSSFilePath;
    public File TXTFile;
    public String TXTFilePath;
    public File CSVFile;
    public String CSVFilePath;
    public ArrayList fileList;

    public StudyBean getPublicStudy(String uniqueId) {
        StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
        String studySchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        StudyBean study = studyDAO.findByUniqueIdentifier(uniqueId);
        CoreResources.setRequestSchema(studySchema);
        return study;
    }

    @Override
    public void processRequest() throws Exception {
        DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
        ArchivedDatasetFileDAO asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());
        FormProcessor fp = new FormProcessor(request);

        GenerateExtractFileService generateFileService = new GenerateExtractFileService(sm.getDataSource(),request,
                (CoreResources) SpringServletAccess.getApplicationContext(context).getBean("coreResources"),
                (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean("ruleSetRuleDao"),
                (PermissionService) SpringServletAccess.getApplicationContext(context).getBean("permissionService"));
        String action = fp.getString("action");
        int datasetId = fp.getInt("datasetId");
        int adfId = fp.getInt("adfId");
        if (datasetId == 0) {
            try {
                DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
                datasetId = dsb.getId();
                logger.info("dataset id was zero, trying session: " + datasetId);
            } catch (NullPointerException e) {

                e.printStackTrace();
                logger.info("tripped over null pointer exception");
            }
        }
        DatasetBean db = (DatasetBean) dsdao.findByPK(datasetId);
        StudyDAO sdao = new StudyDAO(sm.getDataSource());
        StudyBean study = (StudyBean)sdao.findByPK(db.getStudyId());
        checkRoleByUserAndStudy(ub, study, sdao);

        //Checks if the study is current study or child of current study
        if (study.getId() != currentStudy.getId() && study.getParentStudyId() != currentStudy.getId()) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study")
                    + " " + respage.getString("change_active_study_or_contact"));
            forwardPage(Page.MENU_SERVLET);
            return;
        }
        /**
         * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION get study_id and
         *      parentstudy_id int currentstudyid = currentStudy.getId(); int
         *      parentstudy = currentStudy.getParentStudyId(); if (parentstudy >
         *      0) { // is OK } else { // same parentstudy = currentstudyid; } //
         */
        int currentstudyid = currentStudy.getId();
        // YW 11-09-2008 << modified logic here.
        int parentstudy = currentstudyid;
        // YW 11-09-2008 >>

        StudyBean parentStudy = new StudyBean();
        if (currentStudy.getParentStudyId() > 0) {
            //StudyDAO sdao = new StudyDAO(sm.getDataSource());
            parentStudy = (StudyBean) sdao.findByPK(currentStudy.getParentStudyId());
        }

        ExtractBean eb = generateFileService.generateExtractBean(db, currentStudy, parentStudy);

        // new ExtractBean(sm.getDataSource());
        // eb.setDataset(db);
        // eb.setShowUniqueId(SQLInitServlet.getField("show_unique_id"));
        // eb.setStudy(currentStudy);
        // eb.setParentStudy(parentStudy);
        // eb.setDateCreated(new java.util.Date());

        if (StringUtil.isBlank(action)) {
            loadList(db, asdfdao, datasetId, fp, eb);
            forwardPage(Page.EXPORT_DATASETS);
        } else if ("delete".equalsIgnoreCase(action) && adfId > 0) {
            boolean success = false;
            ArchivedDatasetFileBean adfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(adfId);
            File file = new File(adfBean.getFileReference());
            if (!file.canWrite()) {
                addPageMessage(respage.getString("write_protected"));
            } else {
                success = file.delete();
                if (success) {
                    asdfdao.deleteArchiveDataset(adfBean);
                    addPageMessage(respage.getString("file_removed"));
                } else {
                    addPageMessage(respage.getString("error_removing_file"));
                }
            }
            loadList(db, asdfdao, datasetId, fp, eb);
            forwardPage(Page.EXPORT_DATASETS);
        } else {
            logger.info("**** found action ****: " + action);
            String generateReport = "";
            // generate file, and show screen export
            // String generalFileDir = DATASET_DIR + db.getId() +
            // File.separator;
            // change this up, so that we don't overwrite anything
            String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
            SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
            String generalFileDir = DATASET_DIR + db.getId() + File.separator + sdfDir.format(new java.util.Date());
            String fileName = "";

            db.setName(db.getName().replaceAll(" ", "_"));
            Page finalTarget = Page.GENERATE_DATASET;
            finalTarget = Page.EXPORT_DATA_CUSTOM;

            // now display report according to format specified

            // TODO revise final target to set to fileReference????
            long sysTimeBegin = System.currentTimeMillis();
            int fId = 0;
            if ("sas".equalsIgnoreCase(action)) {
                // generateReport =
                // dsdao.generateDataset(db,
                // ExtractBean.SAS_FORMAT,
                // currentStudy,
                // parentStudy);
                long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
                String SASFileName = db.getName() + "_sas.sas";
                // logger.info("found data set: "+generateReport);
                generateFileService.createFile(SASFileName, generalFileDir, generateReport, db, sysTimeEnd, ExportFormatBean.TXTFILE, true, ub);
                logger.info("created sas file");
                request.setAttribute("generate", generalFileDir + SASFileName);
                finalTarget.setFileName(generalFileDir + SASFileName);
                fileName = SASFileName;
                // won't work since page creator is private
            } else if ("odm".equalsIgnoreCase(action)) {
                String odmVersion = fp.getString("odmVersion");
                String ODMXMLFileName = "";
                // DRY
                // HashMap answerMap = generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, db, this.currentStudy, "");
                HashMap answerMap = generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, db, this.currentStudy, "", eb, currentStudy.getId(), currentStudy.getParentStudyId(), "99", true, true, true, null, ub);

                for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    ODMXMLFileName = (String) key;
                    Integer fileID = (Integer) value;
                    fId = fileID.intValue();
                }
                fileName = ODMXMLFileName;
                request.setAttribute("generate", generalFileDir + ODMXMLFileName);
                logger.debug("+++ set the following: " + generalFileDir + ODMXMLFileName);
                // >> tbh #xslt working group
                // put an extra flag here, where we generate the XML, and then find the XSL, run a job and
                // send a link with the SQL file? put the generated SQL file with the dataset?
                if (fp.getString("xalan") != null) {
                    XalanTriggerService xts = new XalanTriggerService();

                    String propertiesPath = SQLInitServlet.getField("filePath");

                    // the trick there, we need to open up the zipped file and get at the XML
                    openZipFile(generalFileDir + ODMXMLFileName + ".zip");
                    // need to find out how to copy this xml file from /bin to the generalFileDir
                    SimpleTrigger simpleTrigger = xts.generateXalanTrigger(propertiesPath + File.separator + "ODMReportStylesheet.xsl",
                            ODMXMLFileName,
                            generalFileDir + "output.sql", db.getId());
                    scheduler = getScheduler();

                    JobDetailFactoryBean JobDetailFactoryBean = new JobDetailFactoryBean();
                    JobDetailFactoryBean.setGroup(xts.TRIGGER_GROUP_NAME);
                    JobDetailFactoryBean.setName(simpleTrigger.getKey().getName());
                    JobDetailFactoryBean.setJobClass(org.akaza.openclinica.web.job.XalanStatefulJob.class);
                    JobDetailFactoryBean.setJobDataMap(simpleTrigger.getJobDataMap());
                    JobDetailFactoryBean.setDurability(true); // need durability?


                    try {
                        Date dateStart = scheduler.scheduleJob(JobDetailFactoryBean.getObject(), simpleTrigger);
                        logger.info("== found job date: " + dateStart.toString());
                    } catch (SchedulerException se) {
                        se.printStackTrace();
                    }
                }
            } else if ("txt".equalsIgnoreCase(action)) {
                // generateReport =
                // dsdao.generateDataset(db,
                // ExtractBean.TXT_FORMAT,
                // currentStudy,
                // parentStudy);
                // eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);
                String TXTFileName = "";
                HashMap answerMap = generateFileService.createTabFile(eb, sysTimeBegin, generalFileDir, db,
                        currentstudyid, parentstudy, "", ub);
                // the above gets us the best of both worlds - the file name,
                // together with the file id which we can then
                // push out to the browser. Shame that it is a long hack,
                // though. need to pare it down later, tbh
                // and of course DRY
                for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    TXTFileName = (String) key;
                    Integer fileID = (Integer) value;
                    fId = fileID.intValue();
                }
                fileName = TXTFileName;
                request.setAttribute("generate", generalFileDir + TXTFileName);
                // finalTarget.setFileName(generalFileDir+TXTFileName);
                logger.debug("+++ set the following: " + generalFileDir + TXTFileName);
            } else if ("html".equalsIgnoreCase(action)) {
                // html based dataset browser
                TabReportBean answer = new TabReportBean();

                eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);
                eb.getMetadata();
                eb.computeReport(answer);
                request.setAttribute("dataset", db);
                request.setAttribute("extractBean", eb);
                finalTarget = Page.GENERATE_DATASET_HTML;

            } else if ("spss".equalsIgnoreCase(action)) {
                SPSSReportBean answer = new SPSSReportBean();

                // removed three lines here and put them in generate file
                // service, createSPSSFile method. tbh 01/2009
                eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);

                eb.getMetadata();

                eb.computeReport(answer);
                // System.out.println("*** isShowCRFversion:
                // "+db.isShowCRFversion());

                // TODO in the spirit of DRY, if this works we need to remove
                // lines 443-776 in this servlet, tbh 01/2009
                String DDLFileName = "";
                HashMap answerMap = generateFileService.createSPSSFile(db, eb, currentStudy, parentStudy, sysTimeBegin, generalFileDir, answer, "", ub);
                // String DDLFileName = createSPSSFile(db, eb, currentstudyid,
                // parentstudy);
                /*
                 * String dataReport = (String) generatedReports.get(0);
                 * this.createFile(SPSSFileName,generalFileDir, dataReport, db,
                 * sysTimeEnd, ExportFormatBean.TXTFILE); logger.info("*** just
                 * created test spss data file: " + SPSSFileName);
                 *
                 * String ddlReport = (String)generatedReports.get(1);
                 * this.createFile(DDLFileName,generalFileDir,ddlReport,db,
                 * sysTimeEnd, ExportFormatBean.TXTFILE); logger.info("*** just
                 * created test spss ddl file: " + DDLFileName);
                 */

                /*
                 * at this point, we want to redirect to the main page and add a
                 * message that the two files are below, available for download
                 */
                // hmm, DRY?
                for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
                    java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    DDLFileName = (String) key;
                    Integer fileID = (Integer) value;
                    fId = fileID.intValue();
                }
                request.setAttribute("generate", generalFileDir + DDLFileName);
                logger.debug("+++ set the following: " + generalFileDir + DDLFileName);
            } else if ("csv".equalsIgnoreCase(action)) {
                CommaReportBean answer = new CommaReportBean();
                eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);
                eb.getMetadata();
                eb.computeReport(answer);
                long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
                // logger.info("found data set: "+generateReport);
                String CSVFileName = db.getName() + "_comma.txt";
                fId = generateFileService.createFile(CSVFileName, generalFileDir, answer.toString(), db, sysTimeEnd, ExportFormatBean.CSVFILE, true, ub);
                fileName = CSVFileName;
                logger.info("just created csv file");
                request.setAttribute("generate", generalFileDir + CSVFileName);
                // finalTarget.setFileName(generalFileDir+CSVFileName);
            } else if ("excel".equalsIgnoreCase(action)) {
                // HSSFWorkbook excelReport = dsdao.generateExcelDataset(db,
                // ExtractBean.XLS_FORMAT,
                // currentStudy,
                // parentStudy);
                long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
                // TODO this will change and point to a created excel
                // spreadsheet, tbh
                String excelFileName = db.getName() + "_excel.xls";
                // fId = this.createFile(excelFileName,
                // generalFileDir,
                // excelReport,
                // db, sysTimeEnd,
                // ExportFormatBean.EXCELFILE);
                // logger.info("just created csv file, for excel output");
                // response.setHeader("Content-disposition","attachment;
                // filename="+CSVFileName);
                // logger.info("csv file name: "+CSVFileName);

                finalTarget = Page.GENERATE_EXCEL_DATASET;

                // response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment; filename=" + db.getName() + "_excel.xls");
                request.setAttribute("generate", generalFileDir + excelFileName);
                logger.info("set 'generate' to :" + generalFileDir + excelFileName);
                fileName = excelFileName;
                // excelReport.write(stream);
                // stream.flush();
                // stream.close();
                // finalTarget.setFileName(WEB_DIR+db.getId()+"/"+excelFileName);
            }
            // request.setAttribute("generate",generateReport);
            // TODO might not set the above to request and instead aim the
            // user directly to the generated file?
            // logger.info("*** just set generated report to request: "+action);
            // create the equivalent to:
            // <%@page contentType="application/vnd.ms-excel"%>
            if (!finalTarget.equals(Page.GENERATE_EXCEL_DATASET) && !finalTarget.equals(Page.GENERATE_DATASET_HTML)) {
                // to catch all the others and try to set a new path for file
                // capture
                // tbh, 4-18-05
                // request.setAttribute("generate",finalTarget.getFileName());
                // TODO changing path to show refresh page, then window with
                // link to download file, tbh 06-08-05
                // finalTarget.setFileName(
                // "/WEB-INF/jsp/extract/generatedFileDataset.jsp");
                finalTarget.setFileName("" + "/WEB-INF/jsp/extract/generateMetadataCore.jsp");
                // also set up table here???
                asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());

                ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fId);
                // *** do we need this below? tbh
                ArrayList newFileList = new ArrayList();
                newFileList.add(asdfBean);
                // request.setAttribute("filelist",newFileList);

                ArrayList filterRows = ArchivedDatasetFileRow.generateRowsFromBeans(newFileList);
                EntityBeanTable table = fp.getEntityBeanTable();
                table.setSortingIfNotExplicitlySet(3, false);// sort by date
                String[] columns =
                    { resword.getString("file_name"), resword.getString("run_time"), resword.getString("file_size"), resword.getString("created_date"),
                        resword.getString("created_by") };

                table.setColumns(new ArrayList(Arrays.asList(columns)));
                table.hideColumnLink(0);
                table.hideColumnLink(1);
                table.hideColumnLink(2);
                table.hideColumnLink(3);
                table.hideColumnLink(4);

                // table.setQuery("ExportDataset?datasetId=" +db.getId(), new
                // HashMap());
                // trying to continue...
                // session.setAttribute("newDataset",db);
                request.setAttribute("dataset", db);
                request.setAttribute("file", asdfBean);
                table.setRows(filterRows);

                table.computeDisplay();

                request.setAttribute("table", table);
                // *** do we need this above? tbh
            }
            logger.info("set first part of 'generate' to :" + generalFileDir);
            logger.info("found file name: " + finalTarget.getFileName());

//            String del = CoreResources.getField("dataset_file_delete");
//            if (del.equalsIgnoreCase("true") || del.equals("")) {
//                File deleteFile = new File(generalFileDir + fileName);
//                deleteFile.delete();
//            }

            forwardPage(finalTarget);
        }
    }

    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
            || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.MONITOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU, resexception.getString("not_allowed_access_extract_data_servlet"), "1");

    }

    public ArchivedDatasetFileBean generateFileBean(File datasetFile, String relativePath, int formatId) {
        ArchivedDatasetFileBean adfb = new ArchivedDatasetFileBean();
        adfb.setName(datasetFile.getName());
        if (datasetFile.canRead()) {
            logger.info("File can be read");
        } else {
            logger.info("File CANNOT be read");
        }
        logger.info("Found file length: " + datasetFile.length());
        logger.info("Last Modified: " + datasetFile.lastModified());
        adfb.setFileSize(new Long(datasetFile.length()).intValue());
        adfb.setExportFormatId(formatId);
        adfb.setWebPath(relativePath);
        adfb.setDateCreated(new java.util.Date(datasetFile.lastModified()));
        return adfb;
    }

    private void openZipFile(String fileName) {
        try {
            ZipFile zipFile = new ZipFile(fileName);

            java.util.Enumeration entries = zipFile.entries();

            while(entries.hasMoreElements()) {
              ZipEntry entry = (ZipEntry)entries.nextElement();

              if(entry.isDirectory()) {
                // Assume directories are stored parents first then children.
                logger.debug("Extracting directory: " + entry.getName());
                // This is not robust, just for demonstration purposes.
                (new File(entry.getName())).mkdir();
                // no dirs necessary?
                continue;
              }

              logger.debug("Extracting file: " + entry.getName());
              // System.out.println("Writing to dir " + targetDir);
              copyInputStream(zipFile.getInputStream(entry),
                 new java.io.BufferedOutputStream(new java.io.FileOutputStream(entry.getName())));
            }

            zipFile.close();
          } catch (java.io.IOException ioe) {
        	  logger.error("Unhandled exception:");
            ioe.printStackTrace();
            return;
          }
    }

    public void loadList(DatasetBean db, ArchivedDatasetFileDAO asdfdao, int datasetId, FormProcessor fp, ExtractBean eb) {
        logger.info("action is blank");
        request.setAttribute("dataset", db);
        logger.info("just set dataset to request");
        request.setAttribute("extractProperties", CoreResources.getExtractProperties());
        // find out if there are any files here:
        File currentDir = new File(DATASET_DIR + db.getId() + File.separator);

        //JN: Commenting out this, as its creating directories without any reason. TODO: Check why was this added.
       // if (!currentDir.isDirectory()) {
      //      currentDir.mkdirs();
      //  }

        ArrayList fileListRaw = new ArrayList();
        fileListRaw = asdfdao.findByDatasetId(datasetId);
        fileList = new ArrayList();
        Iterator fileIterator = fileListRaw.iterator();
        while (fileIterator.hasNext()) {
            ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) fileIterator.next();
            // set the correct webPath in each bean here
            // changed here, tbh, 4-18
            // asdfBean.setWebPath(WEB_DIR+db.getId()+"/"+asdfBean.getName());
            // asdfBean.setWebPath(DATASET_DIR+db.getId()+File.separator+
            // asdfBean.getName());
            asdfBean.setWebPath(asdfBean.getFileReference());
            if (new File(asdfBean.getFileReference()).isFile()) {
                // logger.warn(asdfBean.getFileReference()+" is a
                // file!");
                fileList.add(asdfBean);
            } else {
                logger.warn(asdfBean.getFileReference() + " is NOT a file!");
            }
        }

        logger.warn("");
        logger.warn("file list length: " + fileList.size());
        request.setAttribute("filelist", fileList);

        ArrayList filterRows = ArchivedDatasetFileRow.generateRowsFromBeans(fileList);
        EntityBeanTable table = fp.getEntityBeanTable();
        table.setSortingIfNotExplicitlySet(3, false);// sort by date
        String[] columns =
            { resword.getString("file_name"), resword.getString("run_time"), resword.getString("file_size"), resword.getString("created_date"),
                resword.getString("created_by"), resword.getString("action") };
        table.setColumns(new ArrayList(Arrays.asList(columns)));
        table.hideColumnLink(0);
        table.hideColumnLink(1);
        table.hideColumnLink(2);
        table.hideColumnLink(3);
        table.hideColumnLink(4);
        table.hideColumnLink(5);

        table.setQuery("ExportDataset?datasetId=" + db.getId(), new HashMap());
        // trying to continue...
        session.setAttribute("newDataset", db);
        table.setRows(filterRows);
        table.computeDisplay();

        request.setAttribute("table", table);
        // for the side info bar
        TabReportBean answer = new TabReportBean();

        resetPanel();
        panel.setStudyInfoShown(false);
        setToPanel(resword.getString("study_name"), eb.getStudy().getName());
        setToPanel(resword.getString("protocol_ID"), eb.getStudy().getIdentifier());
        setToPanel(resword.getString("dataset_name"), db.getName());
        setToPanel(resword.getString("created_date"), local_df.format(db.getCreatedDate()));
        setToPanel(resword.getString("dataset_owner"), db.getOwner().getName());
        try {
            // do we not set this or is it null b/c we come to the page with no session?
            setToPanel(resword.getString("date_last_run"), local_df.format(db.getDateLastRun()));
        } catch (NullPointerException npe) {
            logger.error("exception: " + npe.getMessage());
        }

        logger.warn("just set file list to request, sending to page");

    }

    private StdScheduler getScheduler() {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(context).getBean(SCHEDULER);
        return scheduler;
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
    throws IOException
    {
      byte[] buffer = new byte[1024];
      int len = 0;

      while((len = in.read(buffer)) > 0)
        out.write(buffer, 0, len);

      in.close();
      out.close();
    }
}
