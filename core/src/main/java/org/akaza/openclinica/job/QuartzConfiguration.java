package org.akaza.openclinica.job;
  
import static org.akaza.openclinica.service.extract.XsltTriggerService.COUNT;
import static org.akaza.openclinica.service.extract.XsltTriggerService.DATASET_ID;
import static org.akaza.openclinica.service.extract.XsltTriggerService.DELETE_OLD;
import static org.akaza.openclinica.service.extract.XsltTriggerService.EMAIL;
import static org.akaza.openclinica.service.extract.XsltTriggerService.EP_BEAN;
import static org.akaza.openclinica.service.extract.XsltTriggerService.EXTRACT_PROPERTY;
import static org.akaza.openclinica.service.extract.XsltTriggerService.FAILURE_MESSAGE;
import static org.akaza.openclinica.service.extract.XsltTriggerService.LOCALE;
import static org.akaza.openclinica.service.extract.XsltTriggerService.POST_FILE_NAME;
import static org.akaza.openclinica.service.extract.XsltTriggerService.POST_FILE_PATH;
import static org.akaza.openclinica.service.extract.XsltTriggerService.POST_PROC_DELETE_OLD;
import static org.akaza.openclinica.service.extract.XsltTriggerService.POST_PROC_EXPORT_NAME;
import static org.akaza.openclinica.service.extract.XsltTriggerService.POST_PROC_LOCATION;
import static org.akaza.openclinica.service.extract.XsltTriggerService.POST_PROC_ZIP;
import static org.akaza.openclinica.service.extract.XsltTriggerService.STUDY_ID;
import static org.akaza.openclinica.service.extract.XsltTriggerService.SUCCESS_MESSAGE;
import static org.akaza.openclinica.service.extract.XsltTriggerService.TENANT_SCHEMA;
import static org.akaza.openclinica.service.extract.XsltTriggerService.USER_ID;
import static org.akaza.openclinica.service.extract.XsltTriggerService.XML_FILE_PATH;
import static org.akaza.openclinica.service.extract.XsltTriggerService.XSLT_PATH;
import static org.akaza.openclinica.service.extract.XsltTriggerService.XSL_FILE_PATH;
import static org.akaza.openclinica.service.extract.XsltTriggerService.ZIPPED;

import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.quartz.JobDataMap;
import org.quartz.SimpleTrigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
@EnableAspectJAutoProxy
public class QuartzConfiguration {
	@Bean
	@Scope("prototype")
    @Lazy
    public SimpleTriggerFactoryBean simpleTriggerFactoryBean(String xslFile, String xmlFile, String endFilePath,
            String endFile, int datasetId, ExtractPropertyBean epBean, UserAccountBean userAccountBean, String locale,int cnt, String xsltPath,
            StudyBean currentPublicStudy, StudyBean currentStudy){
		SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();

		triggerFactoryBean.setBeanName("trigger1");
		triggerFactoryBean.setGroup("group1");
		triggerFactoryBean.setRepeatInterval(1);
		triggerFactoryBean.setRepeatCount(0);

		//triggerFactoryBean.setStartTime(startDateTime);
		triggerFactoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

		// set job data map
		JobDataMap jobDataMap = new JobDataMap();

		jobDataMap.put(XSL_FILE_PATH, xslFile);
		jobDataMap.put(XML_FILE_PATH, endFilePath);
		jobDataMap.put(POST_FILE_PATH, endFilePath);
		jobDataMap.put(POST_FILE_NAME, endFile);

		jobDataMap.put(EXTRACT_PROPERTY, epBean.getId());
		jobDataMap.put(USER_ID, userAccountBean.getId());
        jobDataMap.put(STUDY_ID, currentStudy.getId());
        jobDataMap.put(TENANT_SCHEMA, currentPublicStudy.getSchemaName());
		jobDataMap.put(LOCALE, locale);
		jobDataMap.put(DATASET_ID, datasetId);
		jobDataMap.put(EMAIL, userAccountBean.getEmail());
		jobDataMap.put(ZIPPED,epBean.getZipFormat());
		jobDataMap.put(DELETE_OLD,epBean.getDeleteOld());
		jobDataMap.put(SUCCESS_MESSAGE,epBean.getSuccessMessage());
		jobDataMap.put(FAILURE_MESSAGE,epBean.getFailureMessage());

		jobDataMap.put(POST_PROC_DELETE_OLD, epBean.getPostProcDeleteOld());
		jobDataMap.put(POST_PROC_ZIP, epBean.getPostProcZip());
		jobDataMap.put(POST_PROC_LOCATION, epBean.getPostProcLocation());
		jobDataMap.put(POST_PROC_EXPORT_NAME, epBean.getPostProcExportName());
		jobDataMap.put(COUNT,cnt);
		jobDataMap.put(XSLT_PATH,xsltPath);
		// jobDataMap.put(DIRECTORY, directory);
		// jobDataMap.put(ExampleSpringJob.LOCALE, locale);
		jobDataMap.put(EP_BEAN, epBean);
        triggerFactoryBean.setJobDataMap(jobDataMap);
        return triggerFactoryBean;
	}

    @Bean
    @Scope("prototype")
    @Lazy
    public JobDetailFactoryBean getJobDetailFactoryBean(SimpleTrigger simpleTrigger, String triggerGroupName) {
        JobDetailFactoryBean jobDetailFactoryBean;
        jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setGroup(triggerGroupName);
        jobDetailFactoryBean.setName(simpleTrigger.getKey().getName()+System.currentTimeMillis());
        jobDetailFactoryBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
        jobDetailFactoryBean.setJobDataMap(simpleTrigger.getJobDataMap());
        jobDetailFactoryBean.setDurability(true); // need durability? YES - we will want to see if it's finished
        return jobDetailFactoryBean;
    }

}  
 