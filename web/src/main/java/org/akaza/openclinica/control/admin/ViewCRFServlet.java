/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;
import static org.jmesa.limit.ExportType.CSV;
import static org.jmesa.limit.ExportType.JEXCEL;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.table.sdv.SDVUtil;

import org.jmesa.facade.TableFacade;
import org.jmesa.limit.Limit;
import org.jmesa.view.component.Column;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.editor.BasicCellEditor;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author jxu
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ViewCRFServlet extends SecureController {

    private static String CRF_ID = "crfId";
    private static String CRF = "crf";
    private RuleSetServiceInterface ruleSetService;

    /**
     *
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        resetPanel();
        panel.setStudyInfoShown(false);
        panel.setOrderedData(true);
        panel.setSubmitDataModule(false);
        panel.setExtractData(false);
        panel.setCreateDataset(false);

        setToPanel(resword.getString("create_CRF"), respage.getString("br_create_new_CRF_entering"));
        setToPanel(resword.getString("create_CRF_version"), respage.getString("br_create_new_CRF_uploading"));
        setToPanel(resword.getString("revise_CRF_version"), respage.getString("br_if_you_owner_CRF_version"));
        setToPanel(resword.getString("CRF_spreadsheet_template"), respage.getString("br_download_blank_CRF_spreadsheet_from"));
        setToPanel(resword.getString("example_CRF_br_spreadsheets"), respage.getString("br_download_example_CRF_instructions_from"));

        FormProcessor fp = new FormProcessor(request);

        // checks which module the requests are from, manage or admin
        String module = fp.getString(MODULE);
        request.setAttribute(MODULE, module);

        int crfId = fp.getInt(CRF_ID);
        List<StudyBean> studyBeans = null;
        if (crfId == 0) {
            addPageMessage(respage.getString("please_choose_a_CRF_to_view"));
            forwardPage(Page.CRF_LIST);
        } else {
            CRFDAO cdao = new CRFDAO(sm.getDataSource());
            CRFVersionDAO vdao = new CRFVersionDAO(sm.getDataSource());
            CRFBean crf = (CRFBean) cdao.findByPK(crfId);
            ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) vdao.findAllByCRF(crfId);
            crf.setVersions(versions);
            if("admin".equalsIgnoreCase(module)){
                //BWP 3279: generate a table showing a list of studies associated with the CRF>>
                StudyDAO studyDAO = new StudyDAO(sm.getDataSource());

                /*ArrayList<StudyEventDefinitionBean> studyEventDefinitionBeans = ( ArrayList<StudyEventDefinitionBean>)
                  studyEventDefinitionDAO.findAll();

                studyBeans = findStudiesForCRF(studyEventDefinitionBeans,crfId,
                  cdao,studyDAO);*/

                studyBeans = findStudiesForCRFId(crfId,studyDAO);
                //Create the Jmesa table for the studies associated with the CRF
                String studyHtml = renderStudiesTable(studyBeans);
                request.setAttribute("studiesTableHTML",studyHtml);
                //>>
            }
            // Collection<RuleSetBean> items =
            // getRuleSetService().getRuleSetsByCrf(crf);
            Collection<TableColumnHolder> items = populate(crf, versions);
            TableFacade tableFacade = createTableFacade("rules", request);
            tableFacade.setItems(items); // set the items
            tableFacade.setExportTypes(response, CSV, JEXCEL);
            tableFacade.setStateAttr("restore");

            Limit limit = tableFacade.getLimit();
            if (limit.isExported()) {
                export(tableFacade);
                // return null; // In Spring returning null tells the controller
                // not to do anything.
            }

            String html = html(tableFacade);
            request.setAttribute("rules", html); // Set the Html in the
            // request for the JSP.

            request.setAttribute(CRF, crf);
            forwardPage(Page.VIEW_CRF);

        }
    }
    /*
    Create a JMesa-based table for showing the studies associated with a CRF.
     */
    private String renderStudiesTable(List<StudyBean> studyBeans)  {

        Collection<StudyRowContainer> items = getStudyRows(studyBeans);
        TableFacade tableFacade = createTableFacade("studies", request);
        tableFacade.setColumnProperties("name","uniqueProtocolid","actions");

        tableFacade.setItems(items);
        //Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();
        //i18n caption; TODO: convert to Spring messages
        ResourceBundle  resourceBundle = ResourceBundle.getBundle(
          "org.akaza.openclinica.i18n.words",request.getLocale());
        String captionText = resourceBundle.getString("studies_using_crf");
        if(captionText == null || "".equalsIgnoreCase(captionText)){
            captionText = "Studies Using this CRF for Data Entry";
        }
        table.setCaption(captionText);
        HtmlRow row = table.getRow();
        SDVUtil  sDVUtil = new SDVUtil();

        String[] colNames = new String[]{"name","uniqueProtocolid","actions"};
        sDVUtil.setHtmlCellEditors(tableFacade,colNames,true);

        HtmlColumn firstName = row.getColumn("name");
        firstName.setTitle("Study Name");


        HtmlColumn protocol = row.getColumn("uniqueProtocolid");
        protocol.setTitle("Unique Protocol Id");


        HtmlColumn actions = row.getColumn("actions");
         actions.setTitle("Actions");


        return tableFacade.render();
    }
    /*
    Generate the rows for the study table. Each row represents a StudyBean domain object.
     */
    private Collection<StudyRowContainer> getStudyRows(List<StudyBean> studyBeans)  {

        Collection<StudyRowContainer> allRows = new ArrayList<StudyRowContainer>();
        StudyRowContainer tempBean = null;
        StringBuilder actions = new StringBuilder("");
        for(StudyBean studBean : studyBeans) {
            tempBean = new StudyRowContainer();
            tempBean.setName(studBean.getName());
            tempBean.setUniqueProtocolid(studBean.getIdentifier());
            tempBean.setStudyBean(studBean);
            actions.append(StudyRowContainer.VIEW_STUDY_DETAILS_URL).append(studBean.getId()).
              append(StudyRowContainer.VIEW_STUDY_DETAILS_SUFFIX);
            tempBean.setActions(actions.toString());
            allRows.add(tempBean);

            actions = new StringBuilder("");
        }

        return allRows;
    }
     /*
     Fetch the studies associated with a CRF, via an event definition that uses the CRF.
      */
    private List<StudyBean> findStudiesForCRFId(int crfId,
                                                StudyDAO studyDao){
        List<StudyBean> studyBeans = new ArrayList<StudyBean>();
        if(crfId == 0 || studyDao == null) {
            return studyBeans;
        }

        ArrayList<Integer> studyIds = studyDao.getStudyIdsByCRF(crfId);
        StudyBean tempBean = new StudyBean();

        for(Integer id : studyIds){
            tempBean = (StudyBean) studyDao.findByPK(id);
            studyBeans.add(tempBean);

        }
        return studyBeans;
    }

    private Collection<TableColumnHolder> populate(CRFBean crf,
                                                   ArrayList<CRFVersionBean> versions) {
        HashMap<CRFVersionBean, ArrayList<TableColumnHolder>> hm = new
          HashMap<CRFVersionBean, ArrayList<TableColumnHolder>>();
        List<TableColumnHolder> tableColumnHolders = new ArrayList<TableColumnHolder>();
        for (CRFVersionBean versionBean : versions) {
            hm.put(versionBean, new ArrayList<TableColumnHolder>());
        }
        List<RuleSetBean> ruleSets = getRuleSetService().getRuleSetsByCrfAndStudy(crf, currentStudy);
        ruleSets = getRuleSetService().filterByStatusEqualsAvailable(ruleSets);
        for (RuleSetBean ruleSetBean : ruleSets) {
            if (ruleSetBean.getCrfVersion() == null) {
                for (CRFVersionBean key : hm.keySet()) {
                    hm.get(key).addAll(createFromRuleSet(ruleSetBean, key));
                }
            }
            if (ruleSetBean.getCrfVersion() != null) {
                hm.get(ruleSetBean.getCrfVersion()).addAll(createFromRuleSet(ruleSetBean, ruleSetBean.getCrfVersion()));
            }
        }
        for (ArrayList<TableColumnHolder> list : hm.values()) {
            tableColumnHolders.addAll(list);
        }
        return tableColumnHolders;
    }

    private List<TableColumnHolder> createFromRuleSet(RuleSetBean ruleSet, CRFVersionBean crfVersion) {
        List<TableColumnHolder> tchs = new ArrayList<TableColumnHolder>();
        for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
            String ruleExpression = ruleSetRule.getRuleBean().getExpression().getValue();
            String ruleName = ruleSetRule.getRuleBean().getName();
            TableColumnHolder tch =
              new TableColumnHolder(crfVersion.getName(), crfVersion.getId(), ruleName, ruleExpression, ruleSetRule.getActions(), ruleSetRule.getId());
            tchs.add(tch);

        }
        return tchs;
    }

    private String html(TableFacade tableFacade) {

        // set the column properties
        tableFacade.setColumnProperties("versionName", "ruleName", "ruleExpression", "executeOnPlaceHolder", "actionTypePlaceHolder",
          "actionSummaryPlaceHolder", "link");

        HtmlTable table = (HtmlTable) tableFacade.getTable();
        table.setCaption("Rules");
        table.getTableRenderer().setWidth("800px");

        HtmlRow row = table.getRow();

        HtmlColumn versionName = row.getColumn("versionName");
        versionName.setTitle("CRF Version");

        HtmlColumn ruleName = row.getColumn("ruleName");
        ruleName.setTitle("Rule Name");

        HtmlColumn career = row.getColumn("ruleExpression");
        career.setWidth("100px");
        career.setTitle("Expression");

        HtmlColumn executeOn = row.getColumn("executeOnPlaceHolder");
        executeOn.setSortable(false);
        executeOn.setFilterable(false);
        executeOn.setTitle("Execute On");
        executeOn.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String value = "";
                List<RuleActionBean> ruleActions = (List<RuleActionBean>) new BasicCellEditor().getValue(item, "actions", rowcount);
                for (int i = 0; i < ruleActions.size(); i++) {
                    value += ruleActions.get(i).getExpressionEvaluatesTo();
                    // Do not add horizontal line after last Summary
                    if (i != ruleActions.size() - 1) {
                        value += "<hr>";
                    }
                }
                return value;
            }
        });

        HtmlColumn actionTypePlaceHolder = row.getColumn("actionTypePlaceHolder");
        actionTypePlaceHolder.setSortable(false);
        actionTypePlaceHolder.setFilterable(false);
        actionTypePlaceHolder.setTitle("Action Type");
        actionTypePlaceHolder.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String value = "";
                List<RuleActionBean> ruleActions = (List<RuleActionBean>) new BasicCellEditor().getValue(item, "actions", rowcount);
                for (int i = 0; i < ruleActions.size(); i++) {
                    value += ruleActions.get(i).getActionType().name();
                    // Do not add horizontal line after last Summary
                    if (i != ruleActions.size() - 1) {
                        value += "<hr>";
                    }
                }
                return value;
            }
        });

        HtmlColumn actionSummaryPlaceHolder = row.getColumn("actionSummaryPlaceHolder");
        actionSummaryPlaceHolder.setSortable(false);
        actionSummaryPlaceHolder.setFilterable(false);
        actionSummaryPlaceHolder.setTitle("Action Summary");
        actionSummaryPlaceHolder.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String value = "";
                List<RuleActionBean> ruleActions = (List<RuleActionBean>) new BasicCellEditor().getValue(item, "actions", rowcount);
                for (int i = 0; i < ruleActions.size(); i++) {
                    value += ruleActions.get(i).getSummary();
                    // Do not add horizontal line after last Summary
                    if (i != ruleActions.size() - 1) {
                        value += "<hr>";
                    }
                }
                return value;
            }
        });

        HtmlColumn link = row.getColumn("link");
        link.setSortable(false);
        link.setFilterable(false);
        link.setTitle("Action");
        link.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String param1 = (String) new BasicCellEditor().getValue(item, "ruleSetRuleId", rowcount);
                String param2 = (String) new BasicCellEditor().getValue(item, "versionId", rowcount);
                HtmlBuilder html = new HtmlBuilder();
                html.a().href().quote().append(request.getContextPath() + "/RunRule?ruleSetRuleId=" + param1 + "&versionId=" + param2 + "&action=dryRun")
                  .quote().close();
                html.img().name("bt_View1").src("images/bt_ExexuteRules.gif").border("0").end();
                html.aEnd();
                return html.toString();
            }
        });

        return tableFacade.render(); // Return the Html.
    }

    private void export(TableFacade tableFacade) {
        // set the column properties
        tableFacade.setColumnProperties("versionName", "ruleName", "ruleExpression", "executeOnPlaceHolder", "actionTypePlaceHolder",
          "actionSummaryPlaceHolder");
        Table table = tableFacade.getTable();
        table.setCaption("Rules");

        Row row = table.getRow();

        Column executeOn = row.getColumn("executeOnPlaceHolder");
        executeOn.setTitle("Execute On");
        executeOn.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String value = "";
                List<RuleActionBean> ruleActions = (List<RuleActionBean>) new BasicCellEditor().getValue(item, "actions", rowcount);
                for (int i = 0; i < ruleActions.size(); i++) {
                    value += ruleActions.get(i).getExpressionEvaluatesTo();
                    // Do not add horizontal line after last Summary
                    if (i != ruleActions.size() - 1) {
                        value += " | ";
                    }
                }
                return value;
            }
        });

        Column actionTypePlaceHolder = row.getColumn("actionTypePlaceHolder");
        actionTypePlaceHolder.setTitle("Action Type");
        actionTypePlaceHolder.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String value = "";
                List<RuleActionBean> ruleActions = (List<RuleActionBean>) new BasicCellEditor().getValue(item, "actions", rowcount);
                for (int i = 0; i < ruleActions.size(); i++) {
                    value += ruleActions.get(i).getActionType().name();
                    // Do not add horizontal line after last Summary
                    if (i != ruleActions.size() - 1) {
                        value += " | ";
                    }
                }
                return value;
            }
        });

        Column actionSummaryPlaceHolder = row.getColumn("actionSummaryPlaceHolder");
        actionSummaryPlaceHolder.setTitle("Action Summary");
        actionSummaryPlaceHolder.getCellRenderer().setCellEditor(new CellEditor() {
            @SuppressWarnings("unchecked")
            public Object getValue(Object item, String property, int rowcount) {
                String value = "";
                List<RuleActionBean> ruleActions = (List<RuleActionBean>) new BasicCellEditor().getValue(item, "actions", rowcount);
                for (int i = 0; i < ruleActions.size(); i++) {
                    value += ruleActions.get(i).getSummary();
                    // Do not add horizontal line after last Summary
                    if (i != ruleActions.size() - 1) {
                        value += " | ";
                    }
                }
                return value;
            }
        });

        tableFacade.render();
    }

    @Override
    protected String getAdminServlet() {
        if (ub.isSysAdmin()) {
            return SecureController.ADMIN_SERVLET_CODE;
        } else {
            return "";
        }
    }

    private RuleSetServiceInterface getRuleSetService() {
        ruleSetService =
          this.ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess.getApplicationContext(context).getBean("ruleSetService");

        /*ruleSetService =
            this.ruleSetService != null ? ruleSetService : new RuleSetService(sm.getDataSource(), getRequestURLMinusServletPath(), getContextPath());*/
        return ruleSetService;
    }

}
