/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2009 Akaza Research
 */
package org.akaza.openclinica.control.admin;

import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.domain.technicaladmin.DatabaseChangeLogBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.jmesa.facade.TableFacade;
import org.jmesa.view.editor.DateCellEditor;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;

import java.util.List;
import java.util.Locale;

import static org.jmesa.facade.TableFacadeFactory.createTableFacade;

/**
 * Servlet for creating a user account.
 *
 * @author Krikor Krumlian
 */
public class AuditDatabaseServlet extends SecureController {

    private static final long serialVersionUID = 1L;

    // < ResourceBundle restext;
    Locale locale;
    private DatabaseChangeLogDao databaseChangeLogDao;

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // < restext =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.notes",locale);

        if (!ub.isSysAdmin()) {
            throw new InsufficientPermissionException(Page.MENU, resexception.getString("you_may_not_perform_administrative_functions"), "1");
        }

        return;
    }

    @Override
    protected void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        String auditDatabaseHtml = renderAuditDatabaseTable(getDatabaseChangeLogDao().findAll());
        request.setAttribute("auditDatabaseHtml", auditDatabaseHtml);
        forwardPage(Page.AUDIT_DATABASE);

    }

    private String renderAuditDatabaseTable(List<DatabaseChangeLogBean> databaseChangeLogs) {

        // Collection<StudyRowContainer> items = getStudyRows(studyBeans);
        TableFacade tableFacade = createTableFacade("databaseChangeLogs", request);
        tableFacade.setColumnProperties("id", "author", "fileName", "dataExecuted", "md5Sum", "description", "comments", "tag", "liquibase");

        tableFacade.setItems(databaseChangeLogs);
        // Fix column titles
        HtmlTable table = (HtmlTable) tableFacade.getTable();

        table.setCaption("");
        HtmlRow row = table.getRow();

        HtmlColumn id = row.getColumn("id");
        id.setTitle("Id");

        HtmlColumn author = row.getColumn("author");
        author.setTitle("Author");

        HtmlColumn fileName = row.getColumn("fileName");
        fileName.setTitle("File Name");

        HtmlColumn dataExecuted = row.getColumn("dataExecuted");
        dataExecuted.setTitle("Date Executed");
        dataExecuted.getCellRenderer().setCellEditor(new DateCellEditor("yyyy-MM-dd hh:mm:ss"));

        HtmlColumn md5Sum = row.getColumn("md5Sum");
        md5Sum.setTitle("md5 sum");

        HtmlColumn description = row.getColumn("description");
        description.setTitle("Description");

        HtmlColumn comments = row.getColumn("comments");
        comments.setTitle("Comments");

        HtmlColumn tag = row.getColumn("tag");
        tag.setTitle("Tag");

        HtmlColumn liquibase = row.getColumn("liquibase");
        liquibase.setTitle("Liquibase");

        return tableFacade.render();
    }

    @Override
    protected String getAdminServlet() {
        return SecureController.ADMIN_SERVLET_CODE;
    }

    public DatabaseChangeLogDao getDatabaseChangeLogDao() {
        databaseChangeLogDao =
            this.databaseChangeLogDao != null ? databaseChangeLogDao : (DatabaseChangeLogDao) SpringServletAccess.getApplicationContext(context).getBean(
                    "databaseChangeLogDao");
        return databaseChangeLogDao;
    }

    public void setDatabaseChangeLogDao(DatabaseChangeLogDao databaseChangeLogDao) {
        this.databaseChangeLogDao = databaseChangeLogDao;
    }

}
