/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.view.form;

import org.jdom.Element;

/**
 * This interface defines the public interface for dynamically creating the
 * markup for an XHTML or HTML table. User: bruceperry Date: May 15, 2007
 */
public interface FormBuilder {
    String createMarkup();

    Element createTable();

    Element createThead();

    Element createThCell();

    Element createThCell(String content);

    Element createColGroup();

    Element createTbody();

    Element createTfoot();

    Element createRow();

    Element createCell();

    Element createCell(String content);

    Element setClassNames(Element styledElement);

}
