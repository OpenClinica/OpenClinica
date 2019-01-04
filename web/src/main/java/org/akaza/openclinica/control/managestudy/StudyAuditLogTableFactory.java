package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyAuditLogFilter;
import org.akaza.openclinica.dao.managestudy.StudyAuditLogSort;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.i18n.util.I18nFormatUtil;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.core.filter.DateFilterMatcher;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

public class StudyAuditLogTableFactory extends AbstractTableFactory {

    private AuditUserLoginDao auditUserLoginDao;
    private StudySubjectDAO studySubjectDao;
    private UserAccountDAO userAccountDao;
    private SubjectDAO subjectDao;
    private StudyBean currentStudy;
    private ResourceBundle resword;
    private ResourceBundle resformat;

    @Override
    protected String getTableName() {
        return "studyAuditLogs";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("studySubject.label",
                 "studySubject.owner", "studySubject.status", "actions");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("studySubject.label"), resword.getString("study_subject_ID"), null, null);
        configureColumn(row.getColumn("studySubject.owner"), resword.getString("created_by"), new OwnerCellEditor(), null, true, false);
        configureColumn(row.getColumn("studySubject.status"), resword.getString("status"), new StatusCellEditor(), new StatusDroplistFilterEditor());
        String actionsHeader = resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        tableFacade.addFilterMatcher(new MatcherKey(Status.class, "studySubject.status"), new GenericFilterMatecher());
        tableFacade.addFilterMatcher(new MatcherKey(UserAccountBean.class, "studySubject.owner"), new GenericFilterMatecher());
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n
        resword = ResourceBundleProvider.getWordsBundle(getLocale());
        resformat = ResourceBundleProvider.getFormatBundle(getLocale());

        // https://jira.openclinica.com/browse/OC-9952
        tableFacade.setMaxRows(50);
        Limit limit = tableFacade.getLimit();
        StudyAuditLogFilter auditLogStudyFilter = getAuditLogStudyFilter(limit);

        if (!limit.isComplete()) {
            int totalRows = getStudySubjectDao().getCountWithFilter(auditLogStudyFilter, getCurrentStudy());
            tableFacade.setTotalRows(totalRows);
        }

        StudyAuditLogSort auditLogStudySort = getAuditLogStudySort(limit);
        /*
         * if (auditLogStudySort.getSorts().size() == 0) {
         * auditLogStudySort.addSort("loginAttemptDate", "desc"); }
         */
        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        Collection<StudySubjectBean> items =
            getStudySubjectDao().getWithFilterAndSort(getCurrentStudy(), auditLogStudyFilter, auditLogStudySort, rowStart, rowEnd);
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        for (StudySubjectBean studySubjectBean : items) {
            SubjectBean subject = (SubjectBean) getSubjectDao().findByPK(studySubjectBean.getSubjectId());
            UserAccountBean owner = (UserAccountBean) getUserAccountDao().findByPK(studySubjectBean.getOwnerId());
            HashMap<Object, Object> h = new HashMap<Object, Object>();
            h.put("studySubject", studySubjectBean);
            h.put("studySubject.label", studySubjectBean.getLabel());
            h.put("studySubject.owner", owner);
            h.put("studySubject.status", studySubjectBean.getStatus());
            h.put("subject", subject);
            h.put("subject.uniqueIdentifier", subject.getUniqueIdentifier());

            theItems.add(h);
        }

        tableFacade.setItems(theItems);
    }

    /**
     * A very custom way to filter the items. The AuditUserLoginFilter acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * filter the rows.
     *
     * @param limit
     *            The Limit to use.
     */
    protected StudyAuditLogFilter getAuditLogStudyFilter(Limit limit) {
        StudyAuditLogFilter auditLogStudyFilter = new StudyAuditLogFilter(getDateFormat());
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            if("studySubject.status".equalsIgnoreCase(property)) {
                value = Status.getByName(value).getId()+"";
            }
            auditLogStudyFilter.addFilter(property, value);
        }

        return auditLogStudyFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * sort the rows.
     *
     * @param limit
     *            The Limit to use.
     */
    protected StudyAuditLogSort getAuditLogStudySort(Limit limit) {
        StudyAuditLogSort auditLogStudySort = new StudyAuditLogSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            auditLogStudySort.addSort(property, order);
        }

        return auditLogStudySort;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (Object status : Status.toActiveArrayList()) {
                options.add(new Option(((Status) status).getName(), ((Status) status).getName()));
            }
            return options;
        }
    }

    private class GenericFilterMatecher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            return true;
        }
    }

    private class StatusCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            Status status = (Status) ((HashMap<Object, Object>) item).get("studySubject.status");

            if (status != null) {
                value = status.getName();
            }
            return value;
        }
    }

    private class OwnerCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            UserAccountBean user = (UserAccountBean) ((HashMap<Object, Object>) item).get("studySubject.owner");

            if (user != null) {
                value = user.getName();
            }
            return value;
        }
    }

    private class ActionsCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            StudySubjectBean studySubjectBean = (StudySubjectBean) ((HashMap<Object, Object>) item).get("studySubject");
            Integer studySubjectId = studySubjectBean.getId();
            if (studySubjectBean != null) {
                StringBuilder url = new StringBuilder();
                url.append("<a onmouseup=\"javascript:setImage('bt_View1','icon icon-search');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-search');\" href=\"javascript:openDocWindow('ViewStudySubjectAuditLog?id=");
                url.append(studySubjectId);
                url.append("')\"><span hspace=\"6\" border=\"0\" align=\"left\" title=\"View\" alt=\"View\" class=\"icon icon-search\" name=\"bt_View1\"/></a>");
                value = url.toString();
            }
            return value;
        }
    }

    private String formatDate(Date date) {
        String format = resformat.getString("date_format_string");
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    private String getDateFormat() {
        return resformat.getString("date_format_string");
    }

    public StudySubjectDAO getStudySubjectDao() {
        return studySubjectDao;
    }

    public void setStudySubjectDao(StudySubjectDAO studySubjectDao) {
        this.studySubjectDao = studySubjectDao;
    }

    public SubjectDAO getSubjectDao() {
        return subjectDao;
    }

    public void setSubjectDao(SubjectDAO subjectDao) {
        this.subjectDao = subjectDao;
    }

    public StudyBean getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(StudyBean currentStudy) {
        this.currentStudy = currentStudy;
    }

    public UserAccountDAO getUserAccountDao() {
        return userAccountDao;
    }

    public void setUserAccountDao(UserAccountDAO userAccountDao) {
        this.userAccountDao = userAccountDao;
    }

    private String resolveBirthDay(Date birthDate, boolean isDobCollected, Locale locale) {
        if(birthDate == null) {
            return "";
        }else {
            if(isDobCollected) {
                return I18nFormatUtil.getDateFormat(locale).format(birthDate);
            } else {
                Calendar c = Calendar.getInstance(locale);
                c.setTime(birthDate);
                return c.get(Calendar.YEAR)+"";
            }
        }
    }
}
