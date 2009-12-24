/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.view.Table;

/**
 * @author thickerson
 *
 *
 */
public class DatasetTable extends Table {
    public DatasetTable() {
        columns.add("Dataset Name");
        columns.add("Description");
        columns.add("Owner");
        columns.add("Creation Date");
        columns.add("Status");
        columns.add("Actions");
    }

    @Override
    public String getEntitiesNamePlural() {
        return "datasets";
    }

    @Override
    public String showRow(EntityBean e) {
        DatasetBean db = (DatasetBean) e;
        Status s = db.getStatus();

        String row = "<tr>\n";

        // dataset name
        String colorOn = s.equals(Status.AVAILABLE) ? "" : "<font color='gray'>";
        String colorOff = s.equals(Status.AVAILABLE) ? "" : "</font>";
        row += "<td>" + colorOn + db.getName() + colorOff + "</td>\n";

        row += "<td>" + db.getDescription() + "</td>\n";
        row += "<td>" + db.getOwner().getName() + "</td>\n";
        // created date
        row += "<td>" + db.getCreatedDate().toString() + "</td>\n";
        // status
        row += "<td>" + s.getName() + "</td>\n";

        // actions
        row += "<td>";
        if (!s.equals(Status.DELETED)) {
            String confirmQuestion = "Are you sure you want to delete " + db.getName() + "?";
            String onClick = "onClick=\"return confirm('" + confirmQuestion + "');\"";
            row += "<a href='" + ViewDatasetsServlet.getLink(db.getId()) + "'>view</a>";
            row += " <a href='" + EditDatasetServlet.getLink(db.getId()) + "'>edit</a>";
            row += " <a href='" + RemoveDatasetServlet.getLink(db.getId()) + "'" + onClick + ">delete</a>";
            row += " <a href='" + ExportDatasetServlet.getLink(db.getId()) + "'" + onClick + ">export dataset</a>";
        } else {
            // write the servlet to restore filters later, tbh 01-23-2005

            /*
             * String confirmQuestion = "Are you sure you want to restore " +
             * u.getName() + "?"; String onClick = "onClick=\"return confirm('" +
             * confirmQuestion + "');\""; row += " <a href='" +
             * DeleteUserServlet.getLink(u, EntityAction.RESTORE) + "'" +
             * onClick + ">restore</a>";
             */
        }
        row += "</td>\n";

        row += "</tr>\n";

        row += "<tr>\n";
        row += "</tr>\n";

        return row;
    }
}
