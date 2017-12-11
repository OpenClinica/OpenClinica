package org.akaza.openclinica.control.submit;

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

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
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
import org.akaza.openclinica.domain.rule.action.EventActionBean;
import org.akaza.openclinica.domain.rule.action.HideActionBean;
import org.akaza.openclinica.domain.rule.action.InsertActionBean;
import org.akaza.openclinica.domain.rule.action.RandomizeActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
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


public class ViewRuleAssignmentTableFactory extends AbstractTableFactory {

    private RuleSetServiceInterface ruleSetService;
    private StudyBean currentStudy;
    private ResourceBundle resword;
    private final boolean showMoreLink;
    private final boolean isDesignerRequest;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private List<Integer> ruleSetRuleIds;
    private final String designerURL;
    private String[] columnNames = new String[] {};
    private UserAccountBean currentUser;

    private String designerLink;
    public UserAccountBean getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserAccountBean currentUser) {
        this.currentUser = currentUser;
    }
    public ViewRuleAssignmentTableFactory(boolean showMoreLink, String designerURL, boolean isDesignerRequest) {
        this.showMoreLink = showMoreLink;
        this.designerURL = designerURL;
        this.isDesignerRequest = isDesignerRequest;
    }

    @Override
    public TableFacade getTableFacadeImpl(HttpServletRequest request, HttpServletResponse response) {
        return new OCTableFacadeImpl(getTableName(), request, response, "rules" + currentStudy.getOid() + "-");
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
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_run_schedule"), null, new RunOnScheduleDroplistFilterEditor());
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_run_time"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_target"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_study_event"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_crf") + "&#160;&#160;&#160;&#160;&#160;", null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_version"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_group"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_item_name"), new ItemCellEditor(), null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_name"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_oid"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_status"), new StatusCellEditor(),
                new StatusDroplistFilterEditor());
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_description"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_expression"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_crf_br_validations"),
                new ValidationsValueCellEditor(false), null, false, false);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_execute_on"), new ExecuteOnCellEditor(false),
                new ExpressionEvaluatesToDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_action_type"), new ActionTypeCellEditor(false),
                new ActionTypeDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_action_summary"), new ActionSummaryCellEditor(false),
                null, true, false);
        String actionsHeader =
            resword.getString("actions") + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"
                + "&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;";
        configureColumn(row.getColumn("actions"), actionsHeader, new ActionsCellEditor(), new DefaultActionsEditor(locale), true, false);

    }

    @Override
    protected void configureExportColumns(TableFacade tableFacade, Locale locale) {
        tableFacade.setColumnProperties(columnNames);
        Row row = tableFacade.getTable().getRow();
        int index = 0;
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_run_schedule"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_run_time"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_target"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_study_event"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_crf") + "&#160;&#160;&#160;&#160;&#160;", null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_version"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_group"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_item_name"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_name"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_oid"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_status"), new StatusCellEditor(),
                new StatusDroplistFilterEditor());
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_rule_description"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_expression"), null, null);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_crf_validations"), new ValidationsValueCellEditor(true),
                null, false, false);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_execute_on"), new ExecuteOnCellEditor(true),
                new ExpressionEvaluatesToDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_action_type"), new ActionTypeCellEditor(true),
                new ActionTypeDroplistFilterEditor(), true, false);
        configureColumn(row.getColumn(columnNames[index++]), resword.getString("view_rule_assignment_action_summary"), new ActionSummaryCellEditor(true), null,
                true, false);
    }

    @Override
    protected ExportType[] getExportTypes() {
        if (isDesignerRequest) {
            return new ExportType[] {};
        }
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
        // Role r = currentRole.getRole();
        // boolean addSubjectLinkShow = studyBean.getStatus().isAvailable() && !r.equals(Role.MONITOR);

        tableFacade.setToolbar(new ViewRuleAssignmentTableToolbar(ruleSetRuleIds, showMoreLink, isDesignerRequest));
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
         * Because we are using the State feature (via stateAttr) we can do a check to see if we have a complete limit
         * already. See the State feature for more details Very important to set the totalRow before trying to get the row
         * start and row end variables. Very important to set the totalRow before trying to get the row start and row end
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
            theItem.put("ruleSetRunSchedule", ruleSetBean.isRunSchedule());
            theItem.put("ruleSetRunTime", ruleSetBean.getRunTime());
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
        columnNamesList.add("ruleSetRunSchedule");
        columnNamesList.add("ruleSetRunTime");
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
     * A very custom way to filter the items. The AuditUserLoginFilter acts as a command for the Hibernate criteria object.
     * Take the Limit information and filter the rows.
     *
     * @param limit The Limit to use.
     */
    protected ViewRuleAssignmentFilter getViewRuleAssignmentFilter(Limit limit) {
        ViewRuleAssignmentFilter viewRuleAssignmentFilter = new ViewRuleAssignmentFilter();
        FilterSet filterSet = limit.getFilterSet();
        Collection<Filter> filters = filterSet.getFilters();
        for (Filter filter : filters) {
            String property = filter.getProperty();
            String value = filter.getValue();
            if("ruleSetRuleStatus".equals(property)) {
                Status s = Status.getByI18nDescription(value, locale);
                int code = s!=null ? s.getCode() : -1;
                value = code>0 ? Status.getByCode(code).getCode()+"" : "0";
            } else if("actionType".equals(property)) {
                ActionType a = ActionType.getByDescription(value);
                value = a != null? a.getCode()+"":value;
            }
            viewRuleAssignmentFilter.addFilter(property, value);
        }

        return viewRuleAssignmentFilter;
    }

    /**
     * A very custom way to sort the items. The AuditUserLoginSort acts as a command for the Hibernate criteria object. Take
     * the Limit information and sort the rows.
     *
     * @param limit The Limit to use.
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
            String mouseOver = "this.style.textDecoration='underline';";
            String mouseOut = "this.style.textDecoration='none';";
            theItem = (ItemBean) ((HashMap<Object, Object>) item).get("item");

            value =
                builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" +(theItem!=null?theItem.getId():"" ) + "')").style("color: #789EC5;text-decoration: none;")
                        .onmouseover(mouseOver).onmouseout(mouseOut).close().append(theItem!=null?theItem.getName():"").aEnd().toString();

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
                ItemFormMetadataBean ifm = getItemFormMetadataDAO().findByItemIdAndCRFVersionId(theItem!=null?theItem.getId():0, crfVersion.getId());
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
            } else if(theItem !=null) {
                ArrayList<ItemFormMetadataBean> itemFormMetadatas = getItemFormMetadataDAO().findAllByItemIdAndHasValidations(theItem.getId());
                if (itemFormMetadatas.size() > 0) {
                    value =
                        builder.a().href("javascript: openDocWindow('ViewItemDetail?itemId=" + theItem.getId() + "')").close().append(YES).aEnd().toString();
                } else {
                    value = NO;
                }

            }else{
                value =null;
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
                ArrayList<ItemFormMetadataBean> itemFormMetadatas = getItemFormMetadataDAO().findAllByItemIdAndHasValidations(theItem!=null?theItem.getId():0);
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

            // builder.table(1).close();
            for (RuleActionBean ruleAction : actions) {
                builder.append(ruleAction.getExpressionEvaluatesTo() + "<br/>");
                // builder.tr(1).close().td(1).close().append(ruleAction.getExpressionEvaluatesTo()).tdEnd().trEnd(1);
            }
            // builder.tableEnd(1);

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

            // builder.table(1).close();
            for (RuleActionBean ruleAction : actions) {
                builder.append(ruleAction.getActionType().getDescription() + "<br/>");
                // builder.tr(1).close().td(1).close().append(ruleAction.getActionType().getDescription()).tdEnd().trEnd(1);
            }
            // builder.tableEnd(1);

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
                    // builder.append("<i>" + resword.getString(entry.getKey()) + "</i>" + "&nbsp;&nbsp;" + entry.getValue());
                    builder.tr(1).close().td(1).close().append("<i>" + resword.getString(entry.getKey()) + "</i>").tdEnd().td(1).close()
                            .append(entry.getValue()).tdEnd().trEnd(1);
                }
           
            String targetValue =ruleAction.getRuleSetRule().getRuleSetBean().getOriginalTarget().getValue(); 
 
           if(targetValue.startsWith(ExpressionService.STUDY_EVENT_OID_START_KEY)&& (targetValue.endsWith(ExpressionService.STARTDATE)|| targetValue.endsWith(ExpressionService.STATUS)))
           	{
             if (ruleAction.getActionType().getCode()!=7)
        	   appendRunOnForEventAction(builder,ruleAction);
          	}else{
                appendRunOn(builder,ruleAction);
           	}                
                
                appendDest(builder,ruleAction);
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

        public void appendRunOn(HtmlBuilder builder, RuleActionBean ruleAction) {
            String s = "";
            RuleActionRunBean ruleActionRun = ruleAction.getRuleActionRun();
            
            if(ruleActionRun.getInitialDataEntry()!=null &&ruleActionRun.getInitialDataEntry()) s+=resword.getString("IDE_comma")+" ";
            if(ruleActionRun.getDoubleDataEntry()!=null&&ruleActionRun.getDoubleDataEntry()) s+=resword.getString("DDE_comma")+" ";
            if(ruleActionRun.getAdministrativeDataEntry()!=null && ruleActionRun.getAdministrativeDataEntry()) s+=resword.getString("ADE_comma")+" ";
            if (ruleActionRun.getImportDataEntry()!=null && ruleActionRun.getImportDataEntry()) s += resword.getString("import_comma") + " ";
            if(ruleActionRun.getBatch()!=null && ruleActionRun.getBatch()) s+=resword.getString("batch_comma")+" ";

            if(s.length()>0){
            	s = s.trim(); s = s.substring(0,s.length()-1);
                    builder.tr(1).close().td(1).close().append("<i>" + resword.getString("run_on_colon") + "</i>").tdEnd().td(1).close().append(s).tdEnd().trEnd(1);
              }
            }

        public void appendRunOnForEventAction(HtmlBuilder builder, RuleActionBean ruleAction) {
            String s = "";
            RuleActionRunBean ruleActionRun = ruleAction.getRuleActionRun();
            
            if(ruleActionRun.getNot_started()!=null && ruleActionRun.getNot_started()==true) s+=resword.getString("not_scheduled_comma")+" ";
            if(ruleActionRun.getScheduled()!=null && ruleActionRun.getScheduled()==true) s+=resword.getString("scheduled_comma")+" ";
            if(ruleActionRun.getData_entry_started()!=null && ruleActionRun.getData_entry_started()==true) s+=resword.getString("data_entry_started_comma")+" ";
            if(ruleActionRun.getComplete()!=null && ruleActionRun.getComplete()==true) s+=resword.getString("completed_comma")+" ";
            if(ruleActionRun.getSkipped()!=null && ruleActionRun.getSkipped()==true) s+=resword.getString("skipped_comma")+" ";
            if(ruleActionRun.getStopped()!=null && ruleActionRun.getStopped()==true) s+=resword.getString("stopped_comma")+" ";
            
            if(s.length()>0){
                s = s.trim(); s = s.substring(0,s.length()-1);
                builder.tr(1).close().td(1).close().append("<i>" + resword.getString("run_on_colon") + "</i>").tdEnd().td(1).close().append(s).tdEnd().trEnd(1);
        }
        }

        
        public void appendDest(HtmlBuilder builder, RuleActionBean ruleAction) {
            ActionType actionType = ruleAction.getActionType();
            if(actionType==ActionType.RANDOMIZE) {
                RandomizeActionBean a = (RandomizeActionBean)ruleAction;
                appendDestProps(builder,a.getProperties());
                appendStratificationFactors(builder, a.getStratificationFactors());
            }
            if(actionType==ActionType.INSERT) {
                InsertActionBean a = (InsertActionBean)ruleAction;
                appendDestProps(builder,a.getProperties());
            }
            if(actionType==ActionType.EVENT) {
                EventActionBean a = (EventActionBean)ruleAction;
                appendDestProps(builder,a.getProperties());
            }
            if(actionType==ActionType.SHOW) {
                ShowActionBean a = (ShowActionBean)ruleAction;
                appendDestProps(builder,a.getProperties());
            }
            if(actionType==ActionType.HIDE) {
                HideActionBean a = (HideActionBean)ruleAction;
                appendDestProps(builder,a.getProperties());
            }
        }
        private void appendDestProps(HtmlBuilder builder,
                List<org.akaza.openclinica.domain.rule.action.PropertyBean> propertyBeans) {
            if(propertyBeans!=null && propertyBeans.size()>0) {
                String s = "";
                for(org.akaza.openclinica.domain.rule.action.PropertyBean p : propertyBeans) {
                	if(p.getOid()!=null)
                	{
                		s += p.getOid().trim() + ", ";
                	}
                	else if(p.getProperty()!=null){
                		s +=p.getProperty().trim()+", ";
                	}
                		
                }
                s = s.trim(); 
                
                if(s.length()>0)
                s = s.substring(0,s.length()-1);
                builder.tr(1).close().td(1).close().append("<i>" + resword.getString("dest_prop_colon") + "</i>").tdEnd()
                .td(1).close().append(s).tdEnd().td(1).close().tdEnd();
                builder.trEnd(1);
                builder.tr(1).close().td(1).close().tdEnd().trEnd(1);
                builder.tr(1).close().td(1).close().tdEnd().trEnd(1);
            }
        }


        private void appendStratificationFactors(HtmlBuilder builder,
                List<org.akaza.openclinica.domain.rule.action.StratificationFactorBean> factorBeans) {
            if(factorBeans!=null && factorBeans.size()>0) {
                String s = "";
                for(org.akaza.openclinica.domain.rule.action.StratificationFactorBean p : factorBeans) {
                   if(p.getStratificationFactor()!=null){
                        s +=p.getStratificationFactor().getValue()+", ";
                    }
                        
                }
                s = s.trim(); 
                
                if(s.length()>0)
                s = s.substring(0,s.length()-1);
                builder.tr(1).close().td(1).close().append("<i>" + resword.getString("stratification_factor_colon") + "</i>").tdEnd()
                .td(1).close().append(s).tdEnd().td(1).close().tdEnd();
                builder.trEnd(1);
                builder.tr(1).close().td(1).close().tdEnd().trEnd(1);
                builder.tr(1).close().td(1).close().tdEnd().trEnd(1);
            }
        }


    }

    private class ActionTypeDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            for (ActionType actionTypes : ActionType.values()) {
                options.add(new Option(actionTypes.getDescription(), actionTypes.getDescription()));
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
            String target = (String) ((HashMap<Object, Object>) item).get("targetValue");
            String ruleOid = (String) ((HashMap<Object, Object>) item).get("ruleOid");
            String runTime = (String) ((HashMap<Object, Object>) item).get("ruleSetRunTime");
            List<RuleActionBean> actions = (List<RuleActionBean>) ((HashMap<Object, Object>) item).get("theActions");
            String message = actions.get(0).getSummary();
        //    if (isDesignerRequest)
          //  {
                value += testEditByDesignerBuilder(target, ruleOid, runTime, message);
            //} else
                if (ruleSetRule.getStatus() != Status.DELETED) {
                value +=
                    viewLinkBuilder(ruleSetId) + executeLinkBuilder(ruleSetId, ruleId , target) + removeLinkBuilder(ruleSetRuleId, ruleSetId)
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
            //return ruleSetRule.getStatus().getDescription();
            return ruleSetRule.getStatus().getI18nDescription(locale);
        }
    }
    
    private class RunOnScheduleDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            options.add(new Option(Boolean.TRUE.toString(), "true"));
            options.add(new Option(Boolean.FALSE.toString(), "false"));
            return options;
        }
    }


    private class StatusDroplistFilterEditor extends DroplistFilterEditor {
        @Override
        protected List<Option> getOptions() {
            List<Option> options = new ArrayList<Option>();
            //options.add(new Option(Status.AVAILABLE.toString(), Status.AVAILABLE.toString()));
            //options.add(new Option(Status.DELETED.toString(), Status.DELETED.toString()));
            options.add(new Option(Status.AVAILABLE.getI18nDescription(locale), Status.AVAILABLE.getI18nDescription(locale)));
            options.add(new Option(Status.DELETED.getI18nDescription(locale), Status.DELETED.getI18nDescription(locale)));
            return options;
        }
    }

    private String viewLinkBuilder(Integer ruleSetId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("ViewRuleSet?ruleSetId=" + ruleSetId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_View1','images/bt_View_d.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_View1','images/bt_View.gif');\"").close();
        actionLink.append("<span hspace=\"2\" border=\"0\" title=\"View\" alt=\"View\" class=\"icon icon-search\" name=\"bt_View\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String executeLinkBuilder(Integer ruleSetId, Integer ruleId , String targetValue) {
        HtmlBuilder actionLink = new HtmlBuilder();
       
        if  (!(targetValue.startsWith(ExpressionService.STUDY_EVENT_OID_START_KEY)&& (targetValue.endsWith(ExpressionService.STARTDATE)|| targetValue.endsWith(ExpressionService.STATUS))))
        {   
            actionLink.a().href("RunRuleSet?ruleSetId=" + ruleSetId + "&ruleId=" + ruleId);
            actionLink.append("onMouseDown=\"javascript:setImage('bt_Run1','images/bt_ExexuteRules.gif');\"");
            actionLink.append("onMouseUp=\"javascript:setImage('bt_Run1','images/bt_ExexuteRules.gif');\"").close();
        }
        actionLink.append("<span hspace=\"2\" border=\"0\" title=\"Run\" alt=\"Run\" class=\"icon icon-plus\" name=\"bt_Run\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        
        return actionLink.toString();

    }

    private String removeLinkBuilder(Integer ruleSetRuleId, Integer ruleSetId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append("<a onmouseup=\"javascript:setImage('bt_View1','icon icon-cancel');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-cancel');\" href=\"UpdateRuleSetRule?action=remove&ruleSetRuleId="+ruleSetRuleId+"&ruleSetId="+ruleSetId);
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Remove\" alt=\"View\" class=\"icon icon-cancel\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();

    }

    private String restoreLinkBuilder(Integer ruleSetRuleId, Integer ruleSetId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append("<a onmouseup=\"javascript:setImage('bt_View1','icon icon-ccw');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-ccw');\" href=\"UpdateRuleSetRule?action=restore&ruleSetRuleId="+ruleSetRuleId+"&ruleSetId"+ruleSetId);
        builder.append("\"><span hspace=\"2\" border=\"0\" title=\"Restore\" alt=\"Restore\" class=\"icon icon-ccw\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();

    }

    private String extractXmlLinkBuilder(Integer ruleSetRuleId) {
        HtmlBuilder builder = new HtmlBuilder();
        builder.append("<a onmouseup=\"javascript:setImage('bt_View1','icon icon-download');\" onmousedown=\"javascript:setImage('bt_View1','icon icon-download');\" href=\"javascript:openDocWindow('DownloadRuleSetXml?ruleSetRuleIds="+ruleSetRuleId);
        builder.append("')\"><span hspace=\"2\" border=\"0\" title=\"Download\" alt=\"Download\" class=\"icon icon-download\" name=\"bt_Reassign1\"/></a>");
        builder.append("&nbsp;&nbsp;&nbsp;");
        return builder.toString();

    }

    private String executeLinkBuilder(Integer ruleSetId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        actionLink.a().href("RunRuleSet?ruleSetId=" + ruleSetId);
        actionLink.append("onMouseDown=\"javascript:setImage('bt_run','images/bt_ExexuteRules.gif');\"");
        actionLink.append("onMouseUp=\"javascript:setImage('bt_run','images/bt_ExexuteRules.gif');\"").close();
        actionLink.append("<span hspace=\"2\" border=\"0\" title=\"Run\" alt=\"Run\" class=\"icon icon-plus\" name=\"bt_Run\"").end().aEnd();
        actionLink.append("&nbsp;&nbsp;&nbsp;");
        return actionLink.toString();

    }

    private String testLinkBuilder(Integer ruleSetRuleId) {
        HtmlBuilder actionLink = new HtmlBuilder();
        return actionLink.toString();

    }

    private String testEditByDesignerBuilder(String target, String ruleOid, String runTime, String message) {
        HtmlBuilder actionLink = new HtmlBuilder();
        return actionLink.toString();

    }

    public String getDesingerLink(){
        return designerLink;
    }
    public void setDesignerLink(String designerLink)
    {
        this.designerLink = designerLink;
    }

    private String convertMessage(String message) {
        message = message.replace("\n","-0-");
        message = message.replace(" ","-1-");
        return message;
    }
}
