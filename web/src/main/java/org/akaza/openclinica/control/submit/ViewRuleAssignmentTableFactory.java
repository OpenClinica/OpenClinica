package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.control.OCTableFacadeImpl;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentFilter;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentSort;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionType;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.jmesa.core.filter.DateFilterMatcher;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.limit.ExportType;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.editor.DroplistFilterEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ViewRuleAssignmentTableFactory extends AbstractTableFactory {

    private RuleSetServiceInterface ruleSetService;
    private StudyBean currentStudy;
    private ResourceBundle resword;
    private final boolean showMoreLink;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private List<Integer> ruleSetRuleIds;
    private String[] columnNames = new String[] {};

    public ViewRuleAssignmentTableFactory(boolean showMoreLink) {
        this.showMoreLink = showMoreLink;
    }

    @Override
    public TableFacade getTableFacadeImpl(HttpServletRequest request, HttpServletResponse response) {
        return new OCTableFacadeImpl(getTableName(), request, response, "xxx");
    }

    @Override
    protected String getTableName() {
        return "ruleAssignments";
    }

    @Override
    protected void configureColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        int index = 0;
        configureColumn(row.getColumn(columnNames[index++]), "Target", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Study Event", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "CRF", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Version", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Group", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Item Name", new ItemCellEditor(), null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule Name", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule OID", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule Status", new StatusCellEditor(), new StatusDroplistFilterEditor());
        configureColumn(row.getColumn(columnNames[index++]), "Rule Description", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Expression", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "CRF Validations", new ValidationsValueCellEditor(false), null, false, false);
        configureColumn(row.getColumn(columnNames[index++]), "Execute On", new ExecuteOnCellEditor(false), new ExpressionEvaluatesToDroplistFilterEditor(),
                true, false);
        configureColumn(row.getColumn(columnNames[index++]), "Action Type", new ActionTypeCellEditor(false), new ActionTypeDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[index++]), "Action Summary", new ActionSummaryCellEditor(false), null, true, false);
        String actionsHeader =
            resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);

    }

    @Override
    protected void configureExportColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        int index = 0;
        configureColumn(row.getColumn(columnNames[index++]), "Target", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Study Event", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "CRF", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Version", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Group", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Item Name", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule Name", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule OID", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule Status", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Rule Description", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "Expression", null, null);
        configureColumn(row.getColumn(columnNames[index++]), "CRF <br/> Validations", new ValidationsValueCellEditor(true), null, false, false);
        configureColumn(row.getColumn(columnNames[index++]), "Execute On", new ExecuteOnCellEditor(true), new ExpressionEvaluatesToDroplistFilterEditor(),
                true, false);
        configureColumn(row.getColumn(columnNames[index++]), "Action Type", new ActionTypeCellEditor(true), new ActionTypeDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[index++]), "Action Summary", new ActionSummaryCellEditor(true), null, true, false);
    }

    @Override
    protected ExportType[] getExportTypes() {
        return new ExportType[] { ExportType.CSV, ExportType.EXCEL, ExportType.PDF };
    }

    @Override
    public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
        super.configureTableFacade(response, tableFacade);
        getColumnNamesMap();
        tableFacade.addFilterMatcher(new MatcherKey(Date.class, "loginAttemptDate"), new DateFilterMatcher("yyyy-MM-dd HH:mm"));
        tableFacade.addFilterMatcher(new MatcherKey(LoginStatus.class, "loginStatus"), new AvailableFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "actionExecuteOn"), new GenericFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "actionType"), new GenericFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "actionSummary"), new GenericFilterMatcher());
        tableFacade.addFilterMatcher(new MatcherKey(String.class, "ruleSetRuleStatus"), new GenericFilterMatcher());

    }

    public class GenericFilterMatcher implements FilterMatcher {
        public boolean evaluate(Object itemValue, String filterValue) {
            // No need to evaluate itemValue and filterValue.
            return true;
        }
    }

    @Override
    public void configureTableFacadePostColumnConfiguration(TableFacade tableFacade) {
        //Role r = currentRole.getRole();
        //boolean addSubjectLinkShow = studyBean.getStatus().isAvailable() && !r.equals(Role.MONITOR);

        tableFacade.setToolbar(new ViewRuleAssignmentTableToolbar(showMoreLink, ruleSetRuleIds));
    }

    @Override
    public void setDataAndLimitVariables(TableFacade tableFacade) {
        // initialize i18n 
        resword = ResourceBundleProvider.getWordsBundle(getLocale());

        Limit limit = tableFacade.getLimit();
        ViewRuleAssignmentFilter viewRuleAssignmentFilter = getViewRuleAssignmentFilter(limit);
        ViewRuleAssignmentSort viewRuleAssignmentSort = getViewRuleAssignmentSort(limit);
        viewRuleAssignmentFilter.addFilter("studyId", currentStudy.getId());
        if (viewRuleAssignmentSort.getSorts().size() == 0) {
            viewRuleAssignmentSort.addSort("itemName", "asc");
        }
        Boolean ruleSetRuleStatusFilterNotSelected = true;
        for (ViewRuleAssignmentFilter.Filter filter : viewRuleAssignmentFilter.getFilters()) {
            if (filter.getProperty().equals("ruleSetRuleStatus")) {
                ruleSetRuleStatusFilterNotSelected = false;
            }
        }
        if (ruleSetRuleStatusFilterNotSelected) {
            viewRuleAssignmentFilter.addFilter("ruleSetRuleStatus", "1");
        }

        /*
         * Because we are using the State feature (via stateAttr) we can do a
         * check to see if we have a complete limit already. See the State
         * feature for more details Very important to set the totalRow before
         * trying to get the row start and row end variables. Very important to
         * set the totalRow before trying to get the row start and row end
         * variables.
         */
        if (!limit.isComplete()) {
            int totalRows = getRuleSetService().getCountWithFilter(viewRuleAssignmentFilter);
            tableFacade.setTotalRows(totalRows);
        }

        int rowStart = limit.getRowSelect().getRowStart();
        int rowEnd = limit.getRowSelect().getRowEnd();
        Collection<RuleSetRuleBean> items = getRuleSetService().getWithFilterAndSort(viewRuleAssignmentFilter, viewRuleAssignmentSort, rowStart, rowEnd);
        HashMap<Integer, RuleSetBean> ruleSets = new HashMap<Integer, RuleSetBean>();

        Collection<HashMap<Object, Object>> theItems = new ArrayList<HashMap<Object, Object>>();
        ruleSetRuleIds = new ArrayList<Integer>();
        for (RuleSetRuleBean ruleSetRuleBean : items) {

            RuleSetBean ruleSetBean = null;
            ruleSetRuleIds.add(ruleSetRuleBean.getId());
            if (ruleSets.containsKey(ruleSetRuleBean.getRuleSetBean().getId())) {
                ruleSetBean = ruleSets.get(ruleSetRuleBean.getRuleSetBean().getId());
            } else {
                ruleSetBean = ruleSetRuleBean.getRuleSetBean();
                getRuleSetService().getObjects(ruleSetBean);
                ruleSets.put(ruleSetBean.getId(), ruleSetBean);
            }

            HashMap<Object, Object> theItem = new HashMap<Object, Object>();
            theItem.put("ruleSetId", ruleSetBean.getId());
            theItem.put("ruleSetRuleId", ruleSetRuleBean.getId());
            theItem.put("ruleId", ruleSetRuleBean.getRuleBean().getId());
            theItem.put("ruleSetRule", ruleSetRuleBean);
            theItem.put("targetValue", ruleSetBean.getTarget().getValue());
            theItem.put("studyEventDefinitionName", ruleSetBean.getStudyEventDefinitionName());
            theItem.put("crf", ruleSetBean.getCrf());
            theItem.put("crfVersion", ruleSetBean.getCrfVersion());
            theItem.put("item", ruleSetBean.getItem());
            theItem.put("crfName", ruleSetBean.getCrfName());
            theItem.put("crfVersionName", ruleSetBean.getCrfVersionName());
            theItem.put("groupLabel", ruleSetBean.getGroupLabel());
            theItem.put("itemName", ruleSetBean.getItemName());
            theItem.put("ruleSetRules", ruleSetBean.getRuleSetRules());
            theItem.put("ruleName", ruleSetRuleBean.getRuleBean().getName());
            theItem.put("ruleExpressionValue", ruleSetRuleBean.getRuleBean().getExpression().getValue());
            theItem.put("ruleOid", ruleSetRuleBean.getRuleBean().getOid());
            theItem.put("ruleDescription", ruleSetRuleBean.getRuleBean().getDescription());
            theItem.put("theActions", ruleSetRuleBean.getActions());
            theItem.put("ruleSetRuleStatus", "");
            theItem.put("validations", "");
            theItem.put("actionExecuteOn", "");
            theItem.put("actionType", "XXXXXXXXX");
            theItem.put("actionSummary", "");
            theItems.add(theItem);
        }

        // Do not forget to set the items back on the tableFacade.
        tableFacade.setItems(theItems);

    }

    private void getColumnNamesMap() {
        ArrayList<String> columnNamesList = new ArrayList<String>();
        columnNamesList.add("targetValue");
        columnNamesList.add("studyEventDefinitionName");
        columnNamesList.add("crfName");
        columnNamesList.add("crfVersionName");
        columnNamesList.add("groupLabel");
        columnNamesList.add("itemName");
        columnNamesList.add("ruleName");
        columnNamesList.add("ruleOid");
        columnNamesList.add("ruleSetRuleStatus");
        columnNamesList.add("ruleDescription");
        columnNamesList.add("ruleExpressionValue");
        columnNamesList.add("validations");
        columnNamesList.add("actionExecuteOn");
        columnNamesList.add("actionType");
        columnNamesList.add("actionSummary");
        columnNamesList.add("actions");
        columnNames = columnNamesList.toArray(columnNames);
    }

    /**
     * A very custom way to filter the items. The AuditUserLoginFilter acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * filter the rows.
     * 
     * @param limit
     *            The Limit to use.
     */
    protected ViewRuleAssignmentFilter getViewRuleAssignmentFilter(Limit limit) {
        ViewRuleAssignmentFilter viewRuleAssignmentFilter = new ViewRuleAssignmentFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            viewRuleAssignmentFilter.addFilter(property, value);
        }

        return viewRuleAssignmentFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a
     * command for the Hibernate criteria object. Take the Limit information and
     * sort the rows.
     * 
     * @param limit
     *            The Limit to use.
     */
    protected ViewRuleAssignmentSort getViewRuleAssignmentSort(Limit limit) {
        ViewRuleAssignmentSort viewRuleAssignmentSort = new ViewRuleAssignmentSort();
        SortSet sortSet = limit.getSortSet();
        Collection<Sort> sorts = sortSet.getSorts();
        for (Sort sort : sorts) {
            String property = sort.getProperty();
            String order = sort.getOrder().toParam();
            viewRuleAssignmentSort.addSort(property, order);
        }

        return viewRuleAssignmentSort;
    }

    public RuleSetServiceInterface getRuleSetService() {
        return ruleSetService;
    }

    public void setRuleSetService(RuleSetServiceInterface ruleSetService) {
        this.ruleSetService = ruleSetService;
    }

    public StudyBean getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(StudyBean currentStudy) {
        this.currentStudy = currentStudy;
    }

    public ItemFormMetadataDAO getItemFormMetadataDAO() {
        return itemFormMetadataDAO;
    }

    public void setItemFormMetadataDAO(ItemFormMetadataDAO itemFormMetadataDAO) {
        this.itemFormMetadataDAO = itemFormMetadataDAO;
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

            Status filter = Status.getByCode(Integer.valueOf(filterValue));
            Status item = (Status) itemValue;

            if (item.equals(filter)) {
                return true;
            }

            return false;
        }
    }

    private class ItemCellEditor implements CellEditor {
        ItemBean theItem;

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            String value = null;
            HtmlBuilder builder = new HtmlBuilder();
            theItem = (ItemBean) ((HashMap<Object, Object>) item).get("item");

            value =
                builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(theItem.getName()).aEnd()
                        .toString();
            return value;
        }
    }

    private class ValidationsValueCellEditor implements CellEditor {
        ItemBean theItem;
        CRFBean crf;
        CRFVersionBean crfVersion;
        String YES = "yes";
        String NO = "no";
        Boolean isExport;

        public ValidationsValueCellEditor(Boolean isExport) {
            this.isExport = isExport;
        }

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            return isExport ? renderExportValue(item, property, rowcount) : renderHtmlValue(item, property, rowcount);

        }

        public Object renderExportValue(Object item, String property, int rowcount) {

            String value = null;
            HtmlBuilder builder = new HtmlBuilder();
            theItem = (ItemBean) ((HashMap<Object, Object>) item).get("item");
            crf = (CRFBean) ((HashMap<Object, Object>) item).get("crf");
            crfVersion = (CRFVersionBean) ((HashMap<Object, Object>) item).get("crfVersion");

            if (crfVersion != null) {
                ItemFormMetadataBean ifm = getItemFormMetadataDAO().findByItemIdAndCRFVersionId(theItem.getId(), crfVersion.getId());
                if (ifm.getId() != 0 && ifm.getRegexp() != null && !ifm.getRegexp().equals("")) {
                    value = YES;
                } else {
                    value = NO;
                }
            } else if (crf != null) {
                ArrayList<ItemFormMetadataBean> itemFormMetadatas =
                    getItemFormMetadataDAO().findAllByCRFIdItemIdAndHasValidations(crf.getId(), theItem.getId());
                if (itemFormMetadatas.size() > 0) {
                    value =
                        builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(YES).aEnd().toString();
                } else {
                    value = NO;
                }
            } else {
                ArrayList<ItemFormMetadataBean> itemFormMetadatas = getItemFormMetadataDAO().findAllByItemIdAndHasValidations(theItem.getId());
                if (itemFormMetadatas.size() > 0) {
                    value =
                        builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(YES).aEnd().toString();
                } else {
                    value = NO;
                }

            }

            return value;
        }

        public Object renderHtmlValue(Object item, String property, int rowcount) {

            String value = null;
            HtmlBuilder builder = new HtmlBuilder();
            theItem = (ItemBean) ((HashMap<Object, Object>) item).get("item");
            crf = (CRFBean) ((HashMap<Object, Object>) item).get("crf");
            crfVersion = (CRFVersionBean) ((HashMap<Object, Object>) item).get("crfVersion");

            if (crfVersion != null) {
                ItemFormMetadataBean ifm = getItemFormMetadataDAO().findByItemIdAndCRFVersionId(theItem.getId(), crfVersion.getId());
                if (ifm.getId() != 0 && ifm.getRegexp() != null && !ifm.getRegexp().equals("")) {
                    value =
                        builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(YES).aEnd().toString();
                } else {
                    value = NO;
                }
            } else if (crf != null) {
                ArrayList<ItemFormMetadataBean> itemFormMetadatas =
                    getItemFormMetadataDAO().findAllByCRFIdItemIdAndHasValidations(crf.getId(), theItem.getId());
                if (itemFormMetadatas.size() > 0) {
                    value =
                        builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(YES).aEnd().toString();
                } else {
                    value = NO;
                }
            } else {
                ArrayList<ItemFormMetadataBean> itemFormMetadatas = getItemFormMetadataDAO().findAllByItemIdAndHasValidations(theItem.getId());
                if (itemFormMetadatas.size() > 0) {
                    value =
                        builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(YES).aEnd().toString();
                } else {
                    value = NO;
                }

            }

            return value;
        }
    }

    private class ExecuteOnCellEditor implements CellEditor {
        List<RuleActionBean> actions;
        Boolean isExport;

        public ExecuteOnCellEditor(Boolean isExport) {
            this.isExport = isExport;
            // TODO Auto-generated constructor stub
        }

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            if (isExport) {
                return renderExportValue(item, property, rowcount);
            } else {
                return renderHtmlValue(item, property, rowcount);
            }
        }

        public Object renderHtmlValue(Object item, String property, int rowcount) {

            HtmlBuilder builder = new HtmlBuilder();
            actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");

            //builder.table(1).close();
            for (RuleActionBean ruleAction : actions) {
                builder.append(ruleAction.getExpressionEvaluatesTo() + "<br/>");
                //builder.tr(1).close().td(1).close().append(ruleAction.getExpressionEvaluatesTo()).tdEnd().trEnd(1);
            }
            //builder.tableEnd(1);

            return builder.toString();
        }

        public Object renderExportValue(Object item, String property, int rowcount) {

            actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");
            String expressionEvaluatesTo = actions.size() > 0 ? String.valueOf(actions.get(0).getExpressionEvaluatesTo()) : "";
            for (int i = 1; i < actions.size(); i++) {
                expressionEvaluatesTo += " - " + actions.get(i).getExpressionEvaluatesTo();
            }
            return expressionEvaluatesTo;
        }

    }

    private class ActionTypeCellEditor implements CellEditor {
        List<RuleActionBean> actions;

        Boolean isExport;

        public ActionTypeCellEditor(Boolean isExport) {
            this.isExport = isExport;
            // TODO Auto-generated constructor stub
        }

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            if (isExport) {
                return renderExportValue(item, property, rowcount);
            } else {
                return renderHtmlValue(item, property, rowcount);
            }
        }

        public Object renderHtmlValue(Object item, String property, int rowcount) {

            HtmlBuilder builder = new HtmlBuilder();
            actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");

            //builder.table(1).close();
            for (RuleActionBean ruleAction : actions) {
                builder.append(ruleAction.getActionType().getDescription() + "<br/>");
                //builder.tr(1).close().td(1).close().append(ruleAction.getActionType().getDescription()).tdEnd().trEnd(1);
            }
            //builder.tableEnd(1);

            return builder.toString();
        }

        public Object renderExportValue(Object item, String property, int rowcount) {

            actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");
            String expressionEvaluatesTo = actions.size() > 0 ? String.valueOf(actions.get(0).getActionType().getDescription()) : "";
            for (int i = 1; i < actions.size(); i++) {
                expressionEvaluatesTo += " ; " + actions.get(i).getActionType().getDescription();
            }
            return expressionEvaluatesTo;
        }
    }

    private class ActionSummaryCellEditor implements CellEditor {
        List<RuleActionBean> actions;
        Boolean isExport;

        public ActionSummaryCellEditor(Boolean isExport) {
            this.isExport = isExport;
            // TODO Auto-generated constructor stub
        }

        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {

            if (isExport) {
                return renderExportValue(item, property, rowcount);
            } else {
                return renderHtmlValue(item, property, rowcount);
            }
        }

        public Object renderHtmlValue(Object item, String property, int rowcount) {

            HtmlBuilder builder = new HtmlBuilder();
            actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");

            builder.table(1).close();
            for (RuleActionBean ruleAction : actions) {
                for (Map.Entry<String, Object> entry : ruleAction.getPropertiesForDisplay().entrySet()) {
                    //builder.append("<i>" + resword.getString(entry.getKey()) + "</i>" + "&nbsp;&nbsp;" + entry.getValue());
                    builder.tr(1).close().td(1).close().append("<i>" + resword.getString(entry.getKey()) + "</i>").tdEnd().td(1).close().append(
                            entry.getValue()).tdEnd().trEnd(1);
                }

            }
            builder.tableEnd(1);

            return builder.toString();
        }

        public Object renderExportValue(Object item, String property, int rowcount) {

            actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");
            String expressionEvaluatesTo = actions.size() > 0 ? String.valueOf(actions.get(0).getSummary()) : "";
            for (int i = 1; i < actions.size(); i++) {
                expressionEvaluatesTo += " ; " + actions.get(i).getSummary();
            }
            return expressionEvaluatesTo;
        }

    }

    private class ActionTypeDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (ActionType actionTypes : ActionType.values()) {
                options.add(new Option(String.valueOf(actionTypes.getCode()), actionTypes.getDescription()));
            }
            return options;
        }
    }

    private class ExpressionEvaluatesToDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            options.add(new Option(String.valueOf(Boolean.TRUE), String.valueOf(Boolean.TRUE)));
            options.add(new Option(String.valueOf(Boolean.FALSE), String.valueOf(Boolean.FALSE)));
            return options;
        }
    }

    private class ActionsCellEditor implements CellEditor {
        @SuppressWarnings("unchecked")
        public Object getValue(Object item, String property, int rowcount) {
            String value = "";
            Integer ruleSetId = (Integer) ((HashMap<Object, Object>) item).get("ruleSetId");
            Integer ruleSetRuleId = (Integer) ((HashMap<Object, Object>) item).get("ruleSetRuleId");
            Integer ruleId = (Integer) ((HashMap<Object, Object>) item).get("ruleId");
            RuleSetRuleBean ruleSetRule = (RuleSetRuleBean) ((HashMap<Object, Object>) item).get("ruleSetRule");

            if (ruleSetRule.getStatus() != Status.DELETED) {
                value +=
                    viewLinkBuilder(ruleSetId) + executeLinkBuilder(ruleSetId, ruleId) + removeLinkBuilder(ruleSetRuleId, ruleSetId)
                        + extractXmlLinkBuilder(ruleSetRuleId) + testLinkBuilder(ruleSetRuleId);
            } else {
                value +=
                    viewLinkBuilder(ruleSetId) + restoreLinkBuilder(ruleSetRuleId, ruleSetId) + extractXmlLinkBuilder(ruleSetRuleId)
                        + testLinkBuilder(ruleSetRuleId);
            }
            return value;
        }
    }

    private class StatusCellEditor implements CellEditor {
        public Object getValue(Object item, String property, int rowcount) {
            RuleSetRuleBean ruleSetRule = (RuleSetRuleBean) new BasicCellEditor().getValue(item, "ruleSetRule", rowcount);
            return ruleSetRule.getStatus().getName();
        }
    }

    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            options.add(new Option(String.valueOf(Status.AVAILABLE.getCode()), Status.AVAILABLE.name()));
            options.add(new Option(String.valueOf(Status.DELETED.getCode()), Status.DELETED.name()));
            return options;
        }
    }

    private String viewLinkBuilder(Integer ruleSetId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("ViewRuleSet?ruleSetId=" + ruleSetId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
        actionLink.img().name("bt_View1").src("images/bt_View.gif").border("0").alt("View").title("View").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String executeLinkBuilder(Integer ruleSetId, Integer ruleId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("RunRuleSet?ruleSetId=" + ruleSetId + "&ruleId=" + ruleId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Run1','images/bt_ExexuteRules.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Run1','images/bt_ExexuteRules.gif');\"").close();
        actionLink.img().name("bt_Run1").src("images/bt_ExexuteRules.gif").border("0").alt("Run").title("Run").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String removeLinkBuilder(Integer ruleSetRuleId, Integer ruleSetId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("UpdateRuleSetRule?action=remove&ruleSetRuleId=" + ruleSetRuleId + "&ruleSetId=" + ruleSetId);
        actionLink.append("onClick='return confirm(\"" + resword.getString("rule_if_you_remove_this") + "\");'");
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Remove1','images/bt_Remove.gif');\"").close();
        actionLink.img().name("bt_Remove1").src("images/bt_Remove.gif").border("0").alt("Remove").title("Remove").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String restoreLinkBuilder(Integer ruleSetRuleId, Integer ruleSetId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("UpdateRuleSetRule?action=restore&ruleSetRuleId=" + ruleSetRuleId + "&ruleSetId=" + ruleSetId);
        actionLink.append("onClick='return confirm(\"" + resword.getString("rule_if_you_restore_this") + "\");'");
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Restore3','images/bt_Restore_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Restore3','images/bt_Restore.gif');\"").close();
        actionLink.img().name("bt_Restore3").src("images/bt_Restore.gif").border("0").alt("Restore").title("Restore").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String extractXmlLinkBuilder(Integer ruleSetRuleId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("DownloadRuleSetXml?ruleSetRuleIds=" + ruleSetRuleId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_Download','images/bt_Download_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_Download','images/bt_Download.gif');\"").close();
        actionLink.img().name("bt_Download").src("images/bt_Download.gif").border("0").alt("Download").title("Download").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String executeLinkBuilder(Integer ruleSetId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("RunRuleSet?ruleSetId=" + ruleSetId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_run','images/bt_ExexuteRules.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_run','images/bt_ExexuteRules.gif');\"").close();
        actionLink.img().name("Run").src("images/bt_ExexuteRules.gif").border("0").alt("Run").title("Run").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String testLinkBuilder(Integer ruleSetRuleId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("TestRule?ruleSetRuleId=" + ruleSetRuleId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_test','images/bt_EnterData_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_test','images/bt_EnterData.gif');\"").close();
        actionLink.img().name("bt_test").src("images/bt_EnterData.gif").border("0").alt("Test").title("Test").append("hspace=\"2\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

}
