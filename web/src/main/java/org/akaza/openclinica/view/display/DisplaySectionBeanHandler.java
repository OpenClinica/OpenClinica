package org.akaza.openclinica.view.display;

import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.form.FormBeanUtil;
import org.akaza.openclinica.view.form.ViewPersistanceHandler;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the responsibility for generating a List of
 * DisplaySectionBeans for a form, such as for a CRF that will be printed. The
 * class is used by PrintCRFServlet and PrintDataEntryServlet.
 */
public class DisplaySectionBeanHandler {
    private boolean hasStoredData = false;
    private int crfVersionId;
    private int eventCRFId;
    private List<DisplaySectionBean> displaySectionBeans;
    private ServletContext context;
    private DataSource dataSource;

    public DisplaySectionBeanHandler(boolean dataEntry) {
        this.hasStoredData = dataEntry;
    }

    public DisplaySectionBeanHandler(boolean dataEntry, DataSource dataSource, ServletContext context) {
        this(dataEntry);
        if (dataSource != null) {
            this.setDataSource(dataSource);
        }
        if (context != null) {
            this.context = context;
        }
    }

    public int getCrfVersionId() {
        return crfVersionId;
    }

    public void setCrfVersionId(int crfVersionId) {
        this.crfVersionId = crfVersionId;
    }

    public int getEventCRFId() {
        return eventCRFId;
    }

    public void setEventCRFId(int eventCRFId) {
        this.eventCRFId = eventCRFId;
    }

    /**
     * This method creates a List of DisplaySectionBeans, returning them in the
     * order that the sections appear in a CRF. This List is "lazily"
     * initialized the first time it is requested.
     *
     * @return A List of DisplaySectionBeans.
     * @see org.akaza.openclinica.control.managestudy.PrintCRFServlet
     * @see org.akaza.openclinica.control.managestudy.PrintDataEntryServlet
     */
    public List<DisplaySectionBean> getDisplaySectionBeans() {
        FormBeanUtil formBeanUtil;
        ViewPersistanceHandler persistanceHandler;
        ArrayList<SectionBean> allCrfSections;
        // DAO classes for getting item definitions
        SectionDAO sectionDao;
        CRFVersionDAO crfVersionDao;

        if (displaySectionBeans == null) {
            displaySectionBeans = new ArrayList<DisplaySectionBean>();
            formBeanUtil = new FormBeanUtil();
            if (hasStoredData)
                persistanceHandler = new ViewPersistanceHandler();

            // We need a CRF version id to populate the form display
            if (this.crfVersionId == 0) {
                return displaySectionBeans;
            }

            sectionDao = new SectionDAO(dataSource);
            allCrfSections = (ArrayList) sectionDao.findByVersionId(this.crfVersionId);

            // for the purposes of null values, try to obtain a valid
            // eventCrfDefinition id
            EventDefinitionCRFBean eventDefBean = null;
            EventCRFBean eventCRFBean = new EventCRFBean();
            if (eventCRFId > 0) {
                EventCRFDAO ecdao = new EventCRFDAO(dataSource);
                eventCRFBean = (EventCRFBean) ecdao.findByPK(eventCRFId);
                StudyEventDAO sedao = new StudyEventDAO(dataSource);
                StudyEventBean studyEvent = (StudyEventBean) sedao.findByPK(eventCRFBean.getStudyEventId());

                EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(dataSource);
                StudyDAO sdao = new StudyDAO(dataSource);
                StudyBean study = sdao.findByStudySubjectId(eventCRFBean.getStudySubjectId());
                eventDefBean = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(study, studyEvent.getId(), this.crfVersionId);
            }
            eventDefBean = eventDefBean == null ? new EventDefinitionCRFBean() : eventDefBean;
            // Create an array or List of DisplaySectionBeans representing each
            // section
            // for printing
            DisplaySectionBean displaySectionBean;
            for (SectionBean sectionBean : allCrfSections) {
                displaySectionBean = formBeanUtil.createDisplaySectionBWithFormGroupsForPrint(sectionBean.getId(), 
                        this.crfVersionId, dataSource, eventDefBean.getId(), eventCRFBean, context);
                displaySectionBeans.add(displaySectionBean);
            }
        }
        return displaySectionBeans;
    }

    public void setDisplaySectionBeans(List<DisplaySectionBean> displaySectionBeans) {
        this.displaySectionBeans = displaySectionBeans;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
