package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.StudyStatisticsView;
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

public class StudyStatisticsTableFactory extends AbstractTableFactory {

    private StudyDAO studyDao;
    private StudySubjectDAO studySubjectDao;
    private StudyBean currentStudy;
    private ResourceBundle reswords = ResourceBundleProvider.getWordsBundle();
    
    @Override
    protected String getTableName() {
        return "studyStatistics";
    }

    @Override
    public void configureTableFacadeCustomView(TableFacade tableFacade) {
        tableFacade.setView(new StudyStatisticsView(getLocale()));
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("name", "enrolled", "expectedTotalEnrollment", "percentage");
        ((HtmlTableRenderer) tableFacade.getTable().getTableRenderer()).setWidth("363px");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("name"), reswords.getString("study"), null, null, false, true);
        configureColumn(row.getColumn("enrolled"), reswords.getString("enrolled"), null, null, false, true);
        configureColumn(row.getColumn("expectedTotalEnrollment"), reswords.getString("expected_enrollment"), null, null, false, true);
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
            int totalRows = 1;
            tableFacade.setTotalRows(totalRows);
        }

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        // Get number of subjects enrolled at a specific study or site
        Integer countofStudySubjectsAtStudy = studySubjectDao.getCountofStudySubjectsAtStudy(currentStudy);
        Integer expectedTotalEnrollment = currentStudy.getExpectedTotalEnrollment();
        Long percentage =
            expectedTotalEnrollment == 0 ? 0 : Math.round((countofStudySubjectsAtStudy.doubleValue() / expectedTotalEnrollment.doubleValue()) * 100);

        HashMap<Object, Object> theItem = new HashMap<Object, Object>();
        theItem.put("name", currentStudy.getName());
        theItem.put("enrolled", countofStudySubjectsAtStudy);
        theItem.put("expectedTotalEnrollment", currentStudy.getExpectedTotalEnrollment());
        theItem.put("percentage", String.valueOf(percentage) + "%");
        theItems.add(theItem);

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
