package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.*;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.control.SpringServletAccess;

import javax.servlet.ServletContext;

public class JdbcService {

    public static StudyEventDAO getStudyEventDao(ServletContext servletContext){
        return (StudyEventDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("studyEventJDBCDao");
    }

    public static EventCRFDAO getEventCrfDao(ServletContext servletContext){
        return (EventCRFDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("eventCRFJDBCDao");
    }
    public static CRFDAO getCrfDao(ServletContext servletContext){
        return (CRFDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("crfJDBCDao");
    }

    public static CRFVersionDAO getCRFVersionDao(ServletContext servletContext){
        return (CRFVersionDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("crfVersionJDBCDao");
    }

    public static StudyEventDefinitionDAO getStudyEventDefinitionDao(ServletContext servletContext){
        return (StudyEventDefinitionDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("studyEventDefinitionJDBCDao");
    }

    public static EventDefinitionCRFDAO getEventDefinitionCRFDao(ServletContext servletContext){
        return (EventDefinitionCRFDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("eventDefinitionCrfJDBCDao");
    }

    public static ItemDataDAO getItemDataDao(ServletContext servletContext){
        return (ItemDataDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("itemDataJDBCDao");
    }

    public static StudySubjectDAO getStudySubjectDao(ServletContext servletContext){
        return (StudySubjectDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("studySubjectJDBCDao");
    }

    public static DiscrepancyNoteDAO getDiscrepancyNoteDao(ServletContext servletContext){
        return (DiscrepancyNoteDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("discrepancyNoteJDBCDao");
    }

    public static FormLayoutDAO getFormLayoutDao(ServletContext servletContext){
        return (FormLayoutDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("formLayoutJDBCDao");
    }

    public static UserAccountDAO getUserAccountDao(ServletContext servletContext){
        return (UserAccountDAO) SpringServletAccess.getApplicationContext(servletContext).getBean("userAccountDAO");
    }
}
