package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.ListSubjectFilter;
import org.akaza.openclinica.dao.submit.ListSubjectSort;
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
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

public class ListSubjectTableFactory extends AbstractTableFactory {

    private AuditUserLoginDao auditUserLoginDao;
    private StudySubjectDAO studySubjectDao;
    private UserAccountDAO userAccountDao;
    private StudyDAO studyDao;
    private SubjectDAO subjectDao;
    private StudyBean currentStudy;
    private ResourceBundle resword;
    private ResourceBundle resformat;

    @Override
    protected String getTableName() {
        return "listSubjects";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("subject.uniqueIdentifier", "studySubjectIdAndStudy", "subject.gender", "subject.createdDate", "subject.owner",
                "subject.updatedDate", "subject.updater", "subject.status", "actions");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("subject.uniqueIdentifier"), resword.getString("person_ID"), null, null);
        configureColumn(row.getColumn("studySubjectIdAndStudy"), resword.getString("Protocol_Study_subject_IDs"), null, null, true, false);
        configureColumn(row.getColumn("subject.gender"), resword.getString("gender"), null, null);
        configureColumn(row.getColumn("subject.createdDate"), resword.getString("date_created"), new DateCellEditor(getDateFormat()), null);
        configureColumn(row.getColumn("subject.owner"), resword.getString("owner"), new OwnerCellEditor(), null, true, false);
        configureColumn(row.getColumn("subject.updatedDate"), resword.getString("date_updated"), new DateCellEditor(getDateFormat()), null);
        configureColumn(row.getColumn("subject.updater"), resword.getString("last_updated_by"), new UpdaterCellEditor(), null, true, false);
        configureColumn(row.getColumn("subject.status"), resword.getString("status"), new StatusCellEditor(), new StatusDroplistFilterEditor());
        configureColumn(
                row.getColumn("actions"),
                resword.getString("actions")
                // +
                // "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                    + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;",
                new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);
        // >> tbh #4003, 08/2009

    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        tableFacade.addFilterMatcher(new MatcherKey(Status.class, "subject.status"), new StatusFilterMatecher());
        tableFacade.addFilterMatcher(new MatcherKey(UserAccountBean.class, "subject.owner"), new GenericFilterMatecher());
        tableFacade.addFilterMatcher(new MatcherKey(UserAccountBean.class, "subject.updater"), new GenericFilterMatecher());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n
        resword = ResourceBundleProvider.getWordsBundle(getLocale());
        resformat = ResourceBundleProvider.getFormatBundle(getLocale());

        Limit limit = tableFacade.getLimit();
        ListSubjectFilter listSubjectFilter = getListSubjectFilter(limit);

        if (!limit.isComplete()) {
            int totalRows = getSubjectDao().getCountWithFilter(listSubjectFilter, getCurrentStudy());
            tableFacade.setTotalRows(totalRows);
        }

        ListSubjectSort listSubjectSort = getListSubjectSort(limit);
        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();

        Collection<SubjectBean> items = getSubjectDao().getWithFilterAndSort(getCurrentStudy(), listSubjectFilter, listSubjectSort, rowStart, rowEnd);
        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();

        for (SubjectBean subject : items) {
            UserAccountBean owner = (UserAccountBean) getUserAccountDao().findByPK(subject.getOwnerId());
            UserAccountBean updater = subject.getUpdaterId() == 0 ? null : (UserAccountBean) getUserAccountDao().findByPK(subject.getUpdaterId());
            HashMap<Object, Object> h = new HashMap<Object, Object>();
            String studySubjectIdAndStudy = "";
            List<StudySubjectBean> studySubjects = getStudySubjectDao().findAllBySubjectId(subject.getId());
            for (StudySubjectBean studySubjectBean : studySubjects) {
                StudyBean study = (StudyBean) getStudyDao().findByPK(studySubjectBean.getStudyId());
                studySubjectIdAndStudy += studySubjectIdAndStudy.length() == 0 ? "" : ",";
                studySubjectIdAndStudy += study.getIdentifier() + "-" + studySubjectBean.getLabel();

            }

            h.put("studySubjectIdAndStudy", studySubjectIdAndStudy);
            h.put("subject", subject);
            h.put("subject.uniqueIdentifier", subject.getUniqueIdentifier());
            h.put("subject.gender", subject.getGender());
            Date tempDate = subject.getCreatedDate();
            String tempDateStr = tempDate == null ? "" : I18nFormatUtil.getDateFormat(getLocale()).format(tempDate);
            h.put("subject.createdDate", tempDateStr);
            h.put("subject.owner", owner);
            tempDate = subject.getUpdatedDate();
            tempDateStr = tempDate == null ? "" : I18nFormatUtil.getDateFormat(getLocale()).format(tempDate);
            h.put("subject.updatedDate", tempDateStr);
            h.put("subject.updater", updater);
            h.put("subject.status", subject.getStatus());

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
    protected ListSubjectFilter getListSubjectFilter(Limit limit) {
        ListSubjectFilter listSubjectFilter = new ListSubjectFilter(getDateFormat());
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            listSubjectFilter.addFilter(property, value);
        }

        return listSubjectFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * sort the rows.
     *
     * @param limit
     *            The Limit to use.
     */
    protected ListSubjectSort getListSubjectSort(Limit limit) {
        ListSubjectSort listSubjectSort = new ListSubjectSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            listSubjectSort.addSort(property, order);
        }

        return listSubjectSort;
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
            for (Object status : Status.toSubjectDropDownArrayList()) {
                //options.add(new Option(String.valueOf(((Status) status).getId()), ((Status) status).getName()));
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

    private class StatusFilterMatecher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            int itemStatusId = ((Status)itemValue).getId();
            return itemStatusId == Status.getByName(filterValue).getId();
        }
    }

    private class StatusCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            Status status = (Status) ((HashMap<Object, Object>) item).get("subject.status");

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
            UserAccountBean user = (UserAccountBean) ((HashMap<Object, Object>) item).get("subject.owner");

            if (user != null) {
                value = user.getName();
            }
            return value;
        }
    }

    private class UpdaterCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            UserAccountBean user = (UserAccountBean) ((HashMap<Object, Object>) item).get("subject.updater");

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
            SubjectBean subjectBean = (SubjectBean) ((HashMap<Object, Object>) item).get("subject");
            Integer subjectId = subjectBean.getId();
            if (subjectBean != null) {
                value += viewSubjectLink(subjectId);
                if (subjectBean.getStatus() != Status.DELETED) {
                    value += updateSubjectLink(subjectId);
                    value += removeSubjectLink(subjectId);
                } else {
                    value += restoreSubjectLink(subjectId);
                }
            }
            return value;
        }
    }

    private String updateSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("UpdateSubject?action=show&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');");
        builder.onmouseover("javascript:setImage('bt_Edit1','images/bt_Edit.gif');");
        builder.close();
        builder.img().name("bt_Edit1").src("images/bt_Edit.gif").border("0").alt(resword.getString("edit")).title(resword.getString("edit")).align("left").append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
    }

    private String removeSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("RemoveSubject?action=confirm&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');");
        builder.onmouseover("javascript:setImage('bt_Remove1','images/bt_Remove.gif');");
        builder.close();
        builder.img().name("bt_Remove1").src("images/bt_Remove.gif").border("0").alt(resword.getString("remove")).title(resword.getString("remove")).append("hspace=\"2\"").close();
        builder.aEnd();
        return builder.toString();
    }

    private String viewSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("ViewSubject?action=show&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_View1','images/bt_View_d.gif');");
        builder.onmouseover("javascript:setImage('bt_View1','images/bt_View.gif');");
        builder.close();
        builder.img().name("bt_View1").src("images/bt_View.gif").border("0").alt(resword.getString("view")).title(resword.getString("view")).align("left").append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
    }

    private String restoreSubjectLink(Integer subjectId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.a().href("RestoreSubject?action=confirm&id=" + subjectId);
        builder.onmouseout("javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');");
        builder.onmouseover("javascript:setImage('bt_Restore3','images/bt_Restore.gif');");
        builder.close();
        builder.img().name("bt_Restore3").src("images/bt_Restore.gif").border("0").alt(resword.getString("restore")).title(resword.getString("restore")).align("left").append("hspace=\"6\"").close();
        builder.aEnd();
        return builder.toString();
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

    public StudyDAO getStudyDao() {
        return studyDao;
    }

    public void setStudyDao(StudyDAO studyDao) {
        this.studyDao = studyDao;
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
}
