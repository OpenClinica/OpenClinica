package org.akaza.openclinica.service.rule;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.patterns.ocobserver.Listener;
import org.akaza.openclinica.patterns.ocobserver.Observer;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventJDBCBeanChanged;
import org.springframework.context.ApplicationListener;

public class StudyEventBeanListener implements Observer {

	private StudyEventDAO studyEventDao;
	private DataSource dataSource;


	public StudyEventBeanListener(StudyEventDAO seDAO){
		this.studyEventDao = seDAO;
		studyEventDao.setObserver(this);
	}
	@Override
	public void update(Listener lstnr) {
	System.out.println("Triggering the rules based on event updates");
		//ADD LOGIC
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	

}
