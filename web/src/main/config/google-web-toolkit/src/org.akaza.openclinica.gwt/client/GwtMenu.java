package org.akaza.openclinica.gwt.client;

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Window;

/**
 * A component class for the top-level navigation menu.
 */
public class GwtMenu implements EntryPoint {
    //The object containing the top-level menu
    private final Grid menuGrid = new Grid(1,5);
    //The object containing the second row of the menu
    private final HorizontalPanel horizontalPanel = new HorizontalPanel();

    public static String COORDS = "menupos";
    public void onModuleLoad() {
        MenuConstants menuConstants = GWT.create(MenuConstants.class);
        URLConstants urlConstants = GWT.create(URLConstants.class);
        //Get the selected menu from the URL parameter; will be a number from 1-5
        String selectedMenu = Window.Location.getParameter(COORDS) == null ? "1" :
          Window.Location.getParameter(COORDS);
        //Window.alert(selectedMenu);
        //Link the top-level menu section to its position on the menu
        HashMap<String,String> menuPosition = new HashMap<String,String>();
        menuPosition.put("1","home");
        menuPosition.put("2","submit_data");
        menuPosition.put("3","extract_data");
        menuPosition.put("4","manage_study");
        menuPosition.put("5","business_admin");


        //Top-level menu: Home, SubmitData, ExtractData, ManageStudy, Business Admin

        final HTML homeLab =
          createHTML("<div><div><div><a href=\""+urlConstants.MainMenu()+
          "\">"+menuConstants.home()+
            "</a></div></div></div>",
            "bt_Home", true);
        homeLab.setStyleName(selectedMenu.equalsIgnoreCase("1") ? "nav_bt_current" : "nav_bt");
        menuGrid.setWidget(0, 0, homeLab);

        final HTML submitDataLab =
          createHTML("<div><div><div><a href=\""+urlConstants.ListStudySubjectsSubmit()+"\">"+
            menuConstants.submit_data()+"</a></div></div></div>",
            "bt_SubmitData", true);
        submitDataLab.setStyleName(selectedMenu.equalsIgnoreCase("2") ? "nav_bt_current" : "nav_bt");
        menuGrid.setWidget(0, 1, submitDataLab);

        final HTML extractDataLab =
          createHTML("<div><div><div><a href=\""+urlConstants.ExtractDatasetsMain()+"\">"+
            menuConstants.extract_data()+"</a></div></div></div>",
            "bt_ExtractData", true);
        extractDataLab.setStyleName(selectedMenu.equalsIgnoreCase("3") ? "nav_bt_current" : "nav_bt");
        menuGrid.setWidget(0, 2, extractDataLab);

        final HTML manageStudyLab =
          createHTML("<div><div><div><a href=\""+urlConstants.ManageStudy()+"\">"+
            menuConstants.manage_study()+"</a></div></div></div>",
            "bt_ManageStudy", true);
        manageStudyLab.setStyleName(selectedMenu.equalsIgnoreCase("4") ? "nav_bt_current" : "nav_bt");
        menuGrid.setWidget(0, 3, manageStudyLab);

        final HTML businessAdminLab =
          createHTML("<div><div><div><a href=\""+urlConstants.AdminSystem()+"\">"+
            menuConstants.business_admin()+"</a></div></div></div>",
            "bt_BusinessAdmin", true);
        businessAdminLab.setStyleName(selectedMenu.equalsIgnoreCase("5") ? "nav_bt_current" : "nav_bt");
        menuGrid.setWidget(0, 4, businessAdminLab);

        RootPanel.get("menuContainer").add(menuGrid);

        //second row of the menu
        final String insideHtml = "<div><div><div>\n" +
          "                        <a href=\""+urlConstants.ListStudySubject()+"\">"+menuConstants.subjects()+"</a>\n" +
          "                        <a href=\""+urlConstants.ListSubjectGroupClass()+"\">"+menuConstants.groups()+"</a>\n" +
          "                        <a href=\""+urlConstants.ViewStudyEvents()+"\">"+menuConstants.events()+"</a>\n" +
          "                        <a href=\""+urlConstants.ListDiscNotesSubjectServlet()+"\">"+menuConstants.notes_discrepancies()+"</a>" +
          "<a href=\""+urlConstants.ViewRuleAssignment()+"\">"+menuConstants.rules()+"</a><br>" +
          "<span class=\"gwt_subnav_second_row\">\n" +
          "<a href=\""+urlConstants.ListStudyUser()+"\">"+menuConstants.users()+"</a>\n" +
          "<a href=\""+urlConstants.ListSite()+"\">"+menuConstants.sites()+"</a>\n" +
          "<a href=\""+urlConstants.ListEventDefinition()+"\">"+menuConstants.event_definitions()+"</a>\n" +
          "<a href=\""+urlConstants.ListCRF()+"\">"+menuConstants.crfs()+"</a>\n" +
          "<a href=\""+urlConstants.AuditLogStudy()+"\">"+menuConstants.view_audit_logs()+"</a></span></div></div></div>";

        final HTML manageSubNav =
          createHTML(insideHtml,
            "subnav_ManageStudy", true);
        manageSubNav.setStyleName("gwt_subnav");

        horizontalPanel.addStyleName("panelMargin");
        horizontalPanel.add(manageSubNav);

        //This Map links the top menu to its second row; home doesn't have a second row
        HashMap<String,HorizontalPanel> menuSecondRow = new HashMap<String,HorizontalPanel>();
        menuSecondRow.put("manage_study",horizontalPanel);
        //home does not have a second row
        HorizontalPanel panel = new HorizontalPanel();
        HTML lab = new HTML("",false);
        panel.add(lab);
        menuSecondRow.put("home",panel);

        //second row of the menu for Submit Data
        String submitHtml = "<div><div><div>\n" +
          "                        <a href=\""+urlConstants.ListStudySubjectsSubmit()+"\">"+menuConstants.view_all_subjects()+"</a>\n" +
          "                        <a href=\""+urlConstants.AddNewSubject()+"\">"+menuConstants.add_subjects()+"</a>\n" +
          "                        <a href=\""+urlConstants.CreateNewStudyEvent()+"\">"+menuConstants.add_new_study_event()+"</a>\n" +
          "                        <br>" +
          "<span class=\"gwt_subnav_second_row\">\n" +
          "<a href=\""+urlConstants.ViewStudyEvents()+"\">"+menuConstants.view_events()+"</a>\n" +
          "<a href=\""+urlConstants.ImportCRFData()+"\">"+menuConstants.import_data()+"</a>\n" +
          "<a href=\""+urlConstants.ListDiscNotesSubjectServletSubmit()+"\">"+menuConstants.notes_discrepancies()+"</a>\n" +
          "</span></div></div></div>";

        panel = this.createSecondRow(submitHtml,"subnav_Submit");
        menuSecondRow.put("submit_data",panel);

        //second row of the menu for Extract Data
        submitHtml = "<div><div><div>\n" +
          "                        <a href=\""+urlConstants.ViewDatasets()+"\">"+menuConstants.view_dataset()+"</a>\n" +
          "                        <a href=\""+urlConstants.CreateDataset()+"\">"+menuConstants.create_dataset()+"</a>\n" +
          "                        </div></div></div>";

        panel = this.createSecondRow(submitHtml,"subnav_Extract");
        menuSecondRow.put("extract_data",panel);

        //second row of the menu for Business Admin
        submitHtml = "<div><div><div>\n" +
          "                        <a href=\""+urlConstants.ListUserAccounts()+"\">"+menuConstants.users()+"</a>\n" +
          "                        <a href=\""+urlConstants.ListSubject()+"\">"+menuConstants.subjects()+"</a>\n" +
          "                        <a href=\""+urlConstants.ListStudy()+"\">"+menuConstants.studies()+"</a>\n" +
          "                        <a href=\""+urlConstants.ListCRFAdmin()+"\">"+menuConstants.crfs()+"</a>\n" +
          "                        </div></div></div>";

        panel = this.createSecondRow(submitHtml,"subnav_Admin");
        menuSecondRow.put("business_admin",panel);


        if(! "".equalsIgnoreCase(selectedMenu)){
            panel = menuSecondRow.get( menuPosition.get(selectedMenu));
        } else {
            panel = menuSecondRow.get( menuPosition.get("4"));

        }
        RootPanel.get("menuContainer").add(panel);


    }

    /* A convenience method for creating HTML labels */
    private HTML createHTML(String htmlText, String ID, boolean wrapText) {
        HTML lab = new HTML(htmlText,wrapText);
        //implements a unique ID for each element
        UIObject.ensureDebugId(lab.getElement(),ID);
        return lab;
    }

    private HorizontalPanel createSecondRow(String insideHtml,String id) {
        HorizontalPanel panel = new HorizontalPanel();


        HTML subNav =
          createHTML(insideHtml,
            id, true);
        subNav.setStyleName("gwt_subnav");

        panel.addStyleName("panelMargin");
        panel.add(subNav);
        return panel;
    }
}
