/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.control.extract.ApplyFilterServlet;
import org.akaza.openclinica.control.extract.EditFilterServlet;
import org.akaza.openclinica.control.extract.RemoveFilterServlet;

/**
 * The extension of Shai Sachs' Table class, Essentially builds the rows for
 * creating a filter table.
 *
 * @author thickerson
 *
 */
public class FilterTable extends Table {
    public FilterTable() {
        columns.add("Filter Name");
        columns.add("Description");
        columns.add("Owner");
        columns.add("Creation Date");
        columns.add("Status");
        columns.add("Actions");
    }

    @Override
    public String getEntitiesNamePlural() {
        return "filters";
    }

    @Override
    public String showRow(EntityBean e) {
        FilterBean fb = (FilterBean) e;
        Status s = fb.getStatus();

        // do the first row, just the "flat" properties
        String row = "<tr>\n";

        // filter name
        String colorOn = s.equals(Status.AVAILABLE) ? "" : "<font color='gray'>";
        String colorOff = s.equals(Status.AVAILABLE) ? "" : "</font>";
        row += "<td>" + colorOn + fb.getName() + colorOff + "</td>\n";

        row += "<td>" + fb.getDescription() + "</td>\n";
        row += "<td>" + fb.getOwner().getName() + "</td>\n";
        // created date
        row += "<td>" + fb.getCreatedDate().toString() + "</td>\n";
        // status
        row += "<td>" + s.getName() + "</td>\n";

        // actions
        row += "<td>";
        if (!s.equals(Status.DELETED) && !s.equals(Status.AUTO_DELETED)) {
            String confirmQuestion = "Are you sure you want to delete " + fb.getName() + "?";
            String onClick = "onClick=\"return confirm('" + confirmQuestion + "');\"";
            row += "<a href='" + ApplyFilterServlet.getLink(fb.getId()) + "'>view</a>";
            row += " <a href='" + EditFilterServlet.getLink(fb.getId()) + "'>edit</a>";
            row += " <a href='" + RemoveFilterServlet.getLink(fb.getId()) + "'" + onClick + ">delete</a>";
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
