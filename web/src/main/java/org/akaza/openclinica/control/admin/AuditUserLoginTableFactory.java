package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginFilter;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginSort;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
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
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletResponse;

public class AuditUserLoginTableFactory extends AbstractTableFactory {

    private AuditUserLoginDao auditUserLoginDao;
    private ResourceBundle resword;

    @Override
    protected String getTableName() {
        return "userLogins";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("userName", "loginAttemptDate", "loginStatus", "detail", "actions");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("userName"), "User Name", null, null);
        configureColumn(row.getColumn("loginAttemptDate"), "Attempt Date", new DateCellEditor("yyyy-MM-dd HH:mm:ss"), null);
        configureColumn(row.getColumn("loginStatus"), "Status", null, new AvailableDroplistFilterEditor());
        configureColumn(row.getColumn("detail"), "Details", null, null);
        String actionsHeader = resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);

    }

    @Override
    protected void configureExportColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties("userName", "loginAttemptDate", "loginStatus", "detail");
        Row row = tableFacade.getTable().getRow();
        configureColumn(row.getColumn("userName"), "User Name", null, null);
        configureColumn(row.getColumn("loginAttemptDate"), "Attempt Date", new DateCellEditor("yyyy-MM-dd HH:mm:ss"), null);
        configureColumn(row.getColumn("loginStatus"), "Status", null, new AvailableDroplistFilterEditor());
        configureColumn(row.getColumn("detail"), "Details", null, null);
    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        tableFacade.addFilterMatcher(new MatcherKey(Date.class, "loginAttemptDate"), new DateFilterMatcher("yyyy-MM-dd HH:mm"));
        tableFacade.addFilterMatcher(new MatcherKey(LoginStatus.class, "loginStatus"), new AvailableFilterMatcher());
    }

    @Override
    public int getSize(Limit limit) {
        return getAuditUserLoginDao().getCountWithFilter(new AuditUserLoginFilter());
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n 
        resword = ResourceBundleProvider.getWordsBundle(getLocale());

        Limit limit = tableFacade.getLimit();
        AuditUserLoginFilter auditUserLoginFilter = getAuditUserLoginFilter(limit);

        /*
         * Because we are using the State feature (via stateAttr) we can do a
         * check to see if we have a complete limit already. See the State
         * feature for more details Very important to set the totalRow before
         * trying to get the row start and row end variables. Very important to
         * set the totalRow before trying to get the row start and row end
         * variables.
         */
        if (!limit.isComplete()) {
            int totalRows = getAuditUserLoginDao().getCountWithFilter(auditUserLoginFilter);
            tableFacade.setTotalRows(totalRows);
        }

        AuditUserLoginSort auditUserLoginSort = getAuditUserLoginSort(limit);
        if (auditUserLoginSort.getSorts().size() == 0) {
            auditUserLoginSort.addSort("loginAttemptDate", "desc");
        }
        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<AuditUserLoginBean> items = getAuditUserLoginDao().getWithFilterAndSort(auditUserLoginFilter, auditUserLoginSort, rowStart, rowEnd);
        tableFacade.setItems(items); // Do not forget to set the items back on
        // the tableFacade.

    }

    /**
     * A very custom way to filter the items. The AuditUserLoginFilter acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * filter the rows.
     * 
     * @param limit
     *            The Limit to use.
     */
    protected AuditUserLoginFilter getAuditUserLoginFilter(Limit limit) {
        AuditUserLoginFilter auditUserLoginFilter = new AuditUserLoginFilter();
        FilterSet filterSet = limit.getFilterSet();
        if (filterSet != null) {
            Collection<Filter> filters = filterSet.getFilters();
            for (Filter filter : filters) {
                String property = filter.getProperty();
                String value = filter.getValue();
                auditUserLoginFilter.addFilter(property, value);
            }
        }

        return auditUserLoginFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * sort the rows.
     * 
     * @param limit
     *            The Limit to use.
     */
    protected AuditUserLoginSort getAuditUserLoginSort(Limit limit) {
        AuditUserLoginSort auditUserLoginSort = new AuditUserLoginSort();
        SortSet sortSet = limit.getSortSet();
        if (sortSet != null) {
            Collection<Sort> sorts = sortSet.getSorts();
            for (Sort sort : sorts) {
                String property = sort.getProperty();
                String order = sort.getOrder().toParam();
                auditUserLoginSort.addSort(property, order);
            }
        }

        return auditUserLoginSort;
    }

    public AuditUserLoginDao getAuditUserLoginDao() {
        return auditUserLoginDao;
    }

    public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
        this.auditUserLoginDao = auditUserLoginDao;
    }

    private class AvailableDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (LoginStatus loginStatus : LoginStatus.values()) {
                options.add(new Option(loginStatus.name(), loginStatus.toString()));
            }
            return options;
        }
    }

    private class AvailableFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {

            LoginStatus filter = LoginStatus.getByName(String.valueOf(filterValue));
            LoginStatus item = (LoginStatus) itemValue;

            if (item.equals(filter)) {
                return true;
            }

            return false;
        }
    }

    private class ActionsCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            Integer userAccountId = (Integer) new BasicCellEditor().getValue(item, "userAccountId", rowcount);
            if (userAccountId != null) {
                StringBuilder url = new StringBuilder();
                url
                        .append("<a onmouseup=\"javascript:setImage('bt_View1','images/bt_View.gif');\" onmousedown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\" href=\"ViewUserAccount?userId=");
                url.append(userAccountId.toString());
                url
                        .append("&amp;viewFull=yes\"><img hspace=\"6\" border=\"0\" align=\"left\" title=\"View\" alt=\"View\" src=\"images/bt_View.gif\" name=\"bt_View1\"/></a>");
                value = url.toString();
            }
            return value;
        }

    }

}
