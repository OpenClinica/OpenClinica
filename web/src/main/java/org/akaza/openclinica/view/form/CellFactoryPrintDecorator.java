package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.jdom.Element;

/**
 * This class decorates CellFactory, for the purposes of CRF print views.
 * Specifically, all input types other than input text and textarea have to be
 * converted to checkbox, before the table cells are generated.
 */
public class CellFactoryPrintDecorator {
    // Delegate to this object the actual generation of the input and TD
    // elements
    private final CellFactory cellFactory = new CellFactory();
    // This object handles the special case of checkboxes or radio buttons in
    // "ungrouped" tables
    private final DataEntryDecorator dataEntryDecorator = new DataEntryDecorator();

    /**
     * "Decorate" the CellFactory.createCellContents() method in order to
     * convert input types from selects or radios to checkboxes.
     *
     * @param td
     *            The TD Element that contains the input tags.
     * @param responseName
     *            The String response name like "radio."
     * @param displayBean
     *            The DisplayItemBean that is being displayed on the CRF.
     * @param tabIndex
     *            The tab index number (not used on printable forms).
     * @param hasDiscrepancyMgt
     *            A boolean value indicating whether discrepancy icons are
     *            displayed.
     * @param hasSavedData
     *            A boolean value indicating whether the input elements should
     *            show database values. The acess to the cellFactory object is
     *            synchronized for the sake of servlet thread safety.
     * @param forPrinting
     *            A boolean value indicating whether CRF printing is involved
     *            (if true, then discrepancy note icons are not clickable).
     * @return The altered td Element contain the form input element or tag.
     */
    public Element createCellContents(Element td, String responseName, DisplayItemBean displayBean, Integer tabIndex, boolean hasDiscrepancyMgt,
            boolean hasSavedData, boolean forPrinting) {
        if (responseName.equalsIgnoreCase("radio") || responseName.equalsIgnoreCase("multi-select") || responseName.equalsIgnoreCase("single-select")
            || responseName.equalsIgnoreCase("checkbox")) {

            // Make sure that various elements are converted to
            // checkboxes for the print view.
            synchronized (cellFactory) {
                return cellFactory.createCellContents(td, "checkbox", displayBean, tabIndex, hasDiscrepancyMgt, hasSavedData, forPrinting);
            }

        } else {
            synchronized (cellFactory) {
                return cellFactory.createCellContents(td, responseName, displayBean, tabIndex, hasDiscrepancyMgt, hasSavedData, forPrinting);
            }
        }
    }

    /**
     * This method creates the Table TD cell contents for checkboxes that have a
     * horizontal layout. For the sake of printing a CRF, the method calls the
     * CellFactory.createCellContentsForChecks() method with the response name
     * of "checkbox"; thus converting any radio buttons to checkboxes prior to
     * printing.
     *
     * @param responseName
     *            The type of input such as "radio."
     * @param displayBean
     *            The DisplayItemBean that provides the content for the table
     *            cell.
     * @param optionsLength
     *            The Integer length of checkboxes or radio buttons
     * @param tabIndex
     *            The tab index number, not relevant for printing.
     * @param hasDBValues
     *            A boolean value indicating whether the table is prefilled with
     *            database values.
     * @param forPrinting
     *            A boolean value indicating whether CRF printing is involved
     *            (if true, then discrepancy note icons are not clickable).
     * @return An array of JDom Element objects representing each TD cell
     *         containing its own checkbox (based on what this HTML horizontal
     *         design calls for).
     */
    public Element[] createCellContentsForChecks(String responseName, DisplayItemBean displayBean, Integer optionsLength, Integer tabIndex,
            boolean hasDBValues, boolean forPrinting) {

        // At the moment, the design requirements for horizontal layouts do not
        // include D Note icons

        // Change any radio buttons to checkboxes
        synchronized (cellFactory) {
            return cellFactory.createCellContentsForChecks("checkbox", displayBean, optionsLength, tabIndex, hasDBValues, forPrinting);
        }

    }

    /**
     * Create checkbox or radio buttons in a vertical layout, in ungrouped
     * tables.
     *
     * @param td
     * @param responseName
     *            The type of input such as "radio."
     * @param displayBean
     *            The DisplayItemBean that provides the content for the table
     *            cell. *
     * @param tabIndex
     *            The tab index number, not relevant for printing.
     * @param hasDiscrepancyMgt
     *            True if discrepancy note icons should be displayed.
     * @param hasSavedData
     *            A boolean value indicating whether the input element is
     *            prefilled with database data.
     * @param forPrinting
     *            A boolean value indicating whether CRF printing is involved
     *            (if true, then discrepancy note icons are not clickable).
     * @return The HTML TD cell with the input cell and other elements as its
     *         contents.
     */
    public Element createCellContentsForVerticalLayout(Element td, String responseName, DisplayItemBean displayBean, Integer tabIndex,
            boolean hasDiscrepancyMgt, boolean hasSavedData, boolean forPrinting) {

        if (responseName.equalsIgnoreCase("checkbox")) {
            td =
                dataEntryDecorator.createCheckboxTag(td, displayBean.getItem().getId(), displayBean.getMetadata().getResponseSet().getOptions(), tabIndex,
                        true, displayBean.getData().getValue(), displayBean.getMetadata().getDefaultValue(), false, hasSavedData);

        } else if (responseName.equalsIgnoreCase("radio")) {
            td =
                dataEntryDecorator.createRadioButtonTag(td, displayBean.getItem().getId(), displayBean.getMetadata().getResponseSet().getOptions(), tabIndex,
                        true, displayBean.getData().getValue(), displayBean.getMetadata().getDefaultValue(), false, hasSavedData);

        }

        if (displayBean.getMetadata().isRequired()) {
            td = dataEntryDecorator.createRequiredAlert(td);
        }

        if (hasDiscrepancyMgt) {
            Element href;

            href =
                dataEntryDecorator.createDiscrepancyNoteSymbol(displayBean.getNumDiscrepancyNotes(), tabIndex, displayBean.getData().getId(), displayBean
                        .getItem().getId(), forPrinting);
            if (href != null)
                td.addContent(href);
        }
        // Add any units or right item text
        td = this.addUnits(td, displayBean);

        return td;
    }

    public Element addUnits(Element tdCell, DisplayItemBean displayBean) {
        if (displayBean == null) {
            return tdCell;
        }
        ItemBean itemBean = displayBean.getItem();
        if (itemBean == null) {
            return tdCell;
        }

        StringBuilder units = new StringBuilder(displayBean.getItem().getUnits());
        if (units.length() < 1) {
            return tdCell;
        }

        Element spanElement = new Element("span");
        spanElement.setAttribute("style", "float:left;clear:both");
        // surround units with parentheses
        units = units.insert(0, "(");
        units = units.append(")");
        spanElement.addContent(" " + units.toString());
        tdCell.addContent(spanElement);
        return tdCell;
    }
}
