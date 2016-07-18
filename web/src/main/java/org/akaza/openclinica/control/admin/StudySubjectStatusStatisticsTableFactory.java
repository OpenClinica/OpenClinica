package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.StudySubjectStatusView;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
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

public class StudySubjectStatusStatisticsTableFactory extends AbstractTableFactory {

    private StudyDAO studyDao;
    private StudySubjectDAO studySubjectDao;
    private StudyBean currentStudy;
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();

    @Override
    protected String getTableName() {
        return "studySubjectStatusStatistics";
    }

    @Override
    public void configureTableFacadeCustomView(TableFacade tableFacade) {
        tableFacade.setView(new StudySubjectStatusView(getLocale()));
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("status", "studySubjects", "percentage");
        ((HtmlTableRenderer) tableFacade.getTable().getTableRenderer()).setWidth("350px");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("status"), reswords.getString("study_subject_status"), null, null, false, true);
        configureColumn(row.getColumn("studySubjects"), reswords.getString("n_study_subjects"), null, null, false, true);
        configureColumn(row.getColumn("percentage"), reswords.getString("percentage"), new PercentageCellEditor(), null, false, true);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        tableFacade.addFilterMatcher(new MatcherKey(Date.class, "loginAttemptDate"), new DateFilterMatcher("yyyy-MM-dd hh:mm"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {

        Limit limit = tableFacade.getLimit();
        Status[] statuses = { Status.AVAILABLE, Status.SIGNED, Status.DELETED };
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
            int totalRows = statuses.length;
            tableFacade.setTotalRows(totalRows);
        }

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        Integer totalStudySubjects = studySubjectDao.getCountofStudySubjects(currentStudy);

        for (Status status : statuses) {
            Integer totalStudySubjectsByStatus = studySubjectDao.getCountofStudySubjectsBasedOnStatus(currentStudy, status);

            Long percentage = totalStudySubjects == 0 ? 0 : Math.round((totalStudySubjectsByStatus.doubleValue() / totalStudySubjects.doubleValue()) * 100);

            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("status", status.getName());
            theItem.put("studySubjects", totalStudySubjectsByStatus);
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
