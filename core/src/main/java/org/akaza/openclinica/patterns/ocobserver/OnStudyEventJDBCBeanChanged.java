package org.akaza.openclinica.patterns.ocobserver;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.springframework.context.ApplicationEvent;

/**
 * Event class for JDBC(older) dao beans change, to implement publish/subscribe pattern
 * @author jnyayapathi
 *
 */
public class OnStudyEventJDBCBeanChanged  extends ApplicationEvent  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StudyEventBean studyEventBean;
	
	public OnStudyEventJDBCBeanChanged(StudyEventBean source) {
		super(source);
		this.setStudyEventBean(source);
	}

	public StudyEventBean getStudyEventBean() {
		return studyEventBean;
	}

	public void setStudyEventBean(StudyEventBean studyEventBean) {
		this.studyEventBean = studyEventBean;
	}

}
