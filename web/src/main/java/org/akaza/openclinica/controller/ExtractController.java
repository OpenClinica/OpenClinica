package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
// import org.akaza.openclinica.control.extract.StdScheduler;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.akaza.openclinica.web.SQLInitServlet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdScheduler;
import org.springframework.scheduling.quartz.JobDetailBean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

@Controller("extractController")
@RequestMapping("/extract")
public class ExtractController {
    @Autowired
    @Qualifier("sidebarInit")
    private SidebarInit sidebarInit;

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;
    
    private DatasetDAO datasetDao;
    
    private StudyDAO studyDao;
    
    private GenerateExtractFileService generateFileService;
    
    private StdScheduler scheduler;

    private static String SCHEDULER = "schedulerFactoryBean";
    
    public ExtractController() {
        
    }
    
    /**
     * process the page from whence you came, i.e. extract a dataset
     * @param id, the id of the extract properties bean, gained from Core Resources
     * @param datasetId, the id of the dataset, found through DatasetDAO
     * @param request, http request
     * @return model map, but more importantly, creates a quartz job which runs right away and generates all output there
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelMap processSubmit(@RequestParam("id") String id, @RequestParam("datasetId") String datasetId, HttpServletRequest request) {
        ModelMap map = new ModelMap();
        // String datasetId = (String)request.getAttribute("datasetId");
        // String id = (String)request.getAttribute("id");
        System.out.println("found both id " + id + " and dataset " + datasetId);
        // get extract id
        // get dataset id
        // if id is a number and dataset id is a number ...
        datasetDao = new DatasetDAO(dataSource);
        studyDao = new StudyDAO(dataSource);
        
        ExtractPropertyBean epBean = CoreResources.findExtractPropertyBeanById(new Integer(id).intValue());
        
        DatasetBean dsBean = (DatasetBean)datasetDao.findByPK(new Integer(datasetId).intValue());
        // set the job in motion
        
        XsltTriggerService xsltService = new XsltTriggerService();
        // TODO get a user bean somehow?
        generateFileService = new GenerateExtractFileService(dataSource, request, new UserAccountBean());
        String generalFileDir = SQLInitServlet.getField("filePath");
        long sysTimeBegin = System.currentTimeMillis();
        
        UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
        
        StudyBean currentStudy = (StudyBean)studyDao.findByPK(userBean.getActiveStudyId());
        StudyBean parentStudy = (StudyBean)studyDao.findByPK(currentStudy.getParentStudyId());
        // TODO need to get the current study and parent study, somehow
        ExtractBean eb = generateFileService.generateExtractBean(dsBean, currentStudy, parentStudy);
        
        // generate file directory for file service
        String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS" + File.separator;
        SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
        generalFileDir = generalFileDir + "datasets" + File.separator + dsBean.getId() + File.separator + sdfDir.format(new java.util.Date());
        
        dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
        
        HashMap answerMap = generateFileService.createODMFile("oc1.3", sysTimeBegin, generalFileDir, dsBean, 
                currentStudy, "", eb, currentStudy.getId(), currentStudy.getParentStudyId(), "99", false);
        // won't be a zipped file, so that we can submit it for transformation
        String ODMXMLFileName = "";
        int fId = 0;
        for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            ODMXMLFileName = (String) key;
            Integer fileID = (Integer) value;
            fId = fileID.intValue();
        }
        // need to set the dataset path here, tbh
        System.out.println("found odm xml file name " + generalFileDir + ODMXMLFileName);
        // next, can already run jobs, translations, and then add a message to be notified later
        String xsltPath = SQLInitServlet.getField("filePath") + "xslt" + File.separator + epBean.getFileName();
        // also need to add the status fields discussed w/ cc:
        // result code, user message, optional URL, archive message, log file message
        // asdf table: sort most recent at top
        System.out.println("found xslt file name " + xsltPath);
        String xmlFilePath = generalFileDir + ODMXMLFileName;
        SimpleTrigger simpleTrigger = xsltService.generateXsltTrigger(xsltPath, 
                xmlFilePath, // next, generate output file path then name
                generalFileDir + epBean.getFileLocation() + File.separator, 
                resolveExportFilePath(epBean), 
                dsBean.getId(), 
                epBean, userBean);
        scheduler = getScheduler(request);
        
        JobDetailBean jobDetailBean = new JobDetailBean();
        jobDetailBean.setGroup(xsltService.TRIGGER_GROUP_NAME);
        jobDetailBean.setName(simpleTrigger.getName());
        jobDetailBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
        jobDetailBean.setJobDataMap(simpleTrigger.getJobDataMap());
        jobDetailBean.setDurability(true); // need durability? YES - we will want to see if it's finished
        jobDetailBean.setVolatility(false);
        
        try {
            Date dateStart = scheduler.scheduleJob(jobDetailBean, simpleTrigger);
            System.out.println("== found job date: " + dateStart.toString());
            
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
        // set the job name here in the user's session, so that we can ping the scheduler to pull it out later
        request.getSession().setAttribute("jobName", jobDetailBean.getName());
        request.getSession().setAttribute("groupName", simpleTrigger.getGroup());
        request.getSession().setAttribute("datasetId", new Integer(dsBean.getId()));
        return map;
    }
    
    private void setUpSidebar(HttpServletRequest request) {
        if (sidebarInit.getAlertsBoxSetup() == SidebarEnumConstants.OPENALERTS) {
            request.setAttribute("alertsBoxSetup", true);
        }

        if (sidebarInit.getInfoBoxSetup() == SidebarEnumConstants.OPENINFO) {
            request.setAttribute("infoBoxSetup", true);
        }
        if (sidebarInit.getInstructionsBoxSetup() == SidebarEnumConstants.OPENINSTRUCTIONS) {
            request.setAttribute("instructionsBoxSetup", true);
        }

        if (!(sidebarInit.getEnableIconsBoxSetup() == SidebarEnumConstants.DISABLEICONS)) {
            request.setAttribute("enableIconsBoxSetup", true);
        }
    }

    public SidebarInit getSidebarInit() {
        return sidebarInit;
    }

    public void setSidebarInit(SidebarInit sidebarInit) {
        this.sidebarInit = sidebarInit;
    }
    
    private StdScheduler getScheduler(HttpServletRequest request) {
        scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(request.getSession().getServletContext()).getBean(SCHEDULER);
        return scheduler;
    }
    
    private String resolveExportFilePath(ExtractPropertyBean epBean) {
        // String retMe = "";
        String epBeanFileName = epBean.getExportFileName();
        // important that this goes first, tbh
        if (epBean.getExportFileName().contains("$datetime")) {
            String dateTimeFilePattern = "yyyy-MM-dd-HHmmssSSS";
            SimpleDateFormat sdfDir = new SimpleDateFormat(dateTimeFilePattern);
            epBeanFileName = epBeanFileName.replace("$datetime", sdfDir.format(new java.util.Date()));
        } else if (epBean.getExportFileName().contains("$date")) {
            String dateFilePattern = "yyyy-MM-dd";
            SimpleDateFormat sdfDir = new SimpleDateFormat(dateFilePattern);
            epBeanFileName = epBeanFileName.replace("$date", sdfDir.format(new java.util.Date()));
            // sdfDir.format(new java.util.Date())
            // retMe = epBean.getFileLocation() + File.separator + epBean.getExportFileName() + "." + epBean.getPostProcessing().getFileType();
        } else {
            // retMe = epBean.getFileLocation() + File.separator + epBean.getExportFileName() + "." + epBean.getPostProcessing().getFileType();
        }
        return epBeanFileName;// + "." + epBean.getPostProcessing().getFileType();// not really the case - might be text to pdf
        // return retMe;
    }
}
