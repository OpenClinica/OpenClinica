package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.EventStatusView;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.DateFilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Limit;
import org.jmesa.util.ItemUtils;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.renderer.HtmlTableRenderer;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class EventStatusStatisticsTableFactory extends AbstractTableFactory {

    private StudyDAO studyDao;
    private StudySubjectDAO studySubjectDao;
    private StudyEventDAO studyEventDao;
    private StudyBean currentStudy;
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();

    @Override
    protected String getTableName() {
        return "subjectEventStatusStatistics";
    }

    @Override
    public void configureTableFacadeCustomView(TableFacade tableFacade) {
        tableFacade.setView(new EventStatusView(getLocale()));
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("status", "studySubjects", "percentage");
        ((HtmlTableRenderer) tableFacade.getTable().getTableRenderer()).setWidth("375px");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("status"), reswords.getString("event_status"), null, null, false, true);
        configureColumn(row.getColumn("studySubjects"), reswords.getString("n_events"), null, null, false, true);
        configureColumn(row.getColumn("percentage"), reswords.getString("percentage"), new PercentageCellEditor(), null, false, true);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        tableFacade.addFilterMatcher(new MatcherKey(Date.class, "loginAttemptDate"), new DateFilterMatcher("yyyy-MM-dd hh:mm"));
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {

        Limit limit = tableFacade.getLimit();
        SubjectEventStatus[] subjectEventStatuses =
            { SubjectEventStatus.SCHEDULED, SubjectEventStatus.DATA_ENTRY_STARTED, SubjectEventStatus.COMPLETED, SubjectEventStatus.SIGNED,
                SubjectEventStatus.LOCKED, SubjectEventStatus.SKIPPED, SubjectEventStatus.STOPPED };

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        /*
         * Because we are using the State feature (via stateAttr) we can do a
         * check to see if we have a complete limit already. See the State
         * feature for more details Very important to set the totalRow before
         * trying to get the row start and row end variables. Very important to
         * set the totalRow before trying to get the row start and row end
         * variables.
         */
        if (!limit.isComplete()) {
            int totalRows = subjectEventStatuses.length;
            tableFacade.setTotalRows(totalRows);
        }

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        for (SubjectEventStatus subjectEventStatus : subjectEventStatuses) {

            Integer totalEventsByEventStatus = studyEventDao.getCountofEventsBasedOnEventStatus(currentStudy, subjectEventStatus);
            Integer totalEvents = studyEventDao.getCountofEvents(currentStudy);

            Long percentage = totalEvents == 0 ? 0 : Math.round((totalEventsByEventStatus.doubleValue() / totalEvents.doubleValue()) * 100);

            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("status", subjectEventStatus.getName());
            theItem.put("studySubjects", totalEventsByEventStatus);
            theItem.put("percentage", String.valueOf(percentage) + "%");
            theItems.add(theItem);

        }
        tableFacade.setItems(theItems);
    }

    public StudyDAO getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDAO studyDao) {
        this.studyDao = studyDao;
    }

    public StudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(StudySubjectDAO studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public StudyBean getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(StudyBean currentStudy) {
        this.currentStudy = currentStudy;
    }

    public StudyEventDAO getStudyEventDao() {
        return studyEventDao;
    }

    public void setStudyEventDao(StudyEventDAO studyEventDao) {
        this.studyEventDao = studyEventDao;
    }

    private class PercentageCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            Object value = ItemUtils.getItemValue(item, property);
            HtmlBuilder html = new HtmlBuilder();
            html.div().styleClass("graph").close();
            html.div().styleClass("bar").style("width: " + value).close().append(value).divEnd();
            html.divEnd();
            return html.toString();
        }
    }

}
