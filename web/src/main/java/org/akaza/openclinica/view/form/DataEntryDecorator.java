package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jdom.Element;

import java.util.List;
import java.util.Random;

/**
 * Using the Decorator design pattern, this class adds functionality to
 * DataEntryInputGenerator. The functionality is necessary for printing CRFs.
 */
public class DataEntryDecorator {
    private final DataEntryInputGenerator inputGenerator = new DataEntryInputGenerator();

    public Element createCheckboxTag(Element tdCell, Integer itemId, List options, Integer tabNumber, boolean includeLabel, String dbValue,
            String defaultValue, boolean isHorizontal, boolean hasSavedData) {
        // The input element
        Element element;
        // the span element that contains the input element
        Element spanElement;
        String[] arrayOfValues = new String[] {};
        int count = 0;
        // Handles lone Strings, or Strings separated by commas
        if (dbValue != null && dbValue.length() > 0) {
            synchronized (inputGenerator) {
                arrayOfValues = inputGenerator.handleSplitString(dbValue);
            }
        } else if (!hasSavedData && defaultValue != null && defaultValue.length() > 0) {
            synchronized (inputGenerator) {
                arrayOfValues = inputGenerator.handleSplitString(defaultValue);
            }
        }
        for (Object responseOptBean : options) {

            spanElement = new Element("span");
            spanElement.setAttribute("style", "float:left;clear:both");
            synchronized (inputGenerator) {
                element = inputGenerator.initializeInputElement("checkbox", itemId, tabNumber);
            }
            spanElement.addContent(element);
            String value = ((ResponseOptionBean) responseOptBean).getValue();
            String forDefVal = ((ResponseOptionBean) responseOptBean).getText();
            element.setAttribute("value", value);
            // It's checked if its value equals the DB value
            if (dbValue != null && dbValue.length() > 0) {
                // && value.equalsIgnoreCase(dbValue)
                for (String string : arrayOfValues) {
                    if (value.equalsIgnoreCase(string)) {
                        element.setAttribute("checked", "checked");
                    }
                }
            } else if (!hasSavedData && defaultValue != null && defaultValue.length() > 0) {
                // && value.equalsIgnoreCase(dbValue)
                for (String string : arrayOfValues) {
                    if (forDefVal.equalsIgnoreCase(string) || value.equalsIgnoreCase(string)) {
                        element.setAttribute("checked", "checked");
                    }
                }
            }

            tdCell.addContent(spanElement);
            if (includeLabel) {
                spanElement.addContent(((ResponseOptionBean) responseOptBean).getText());
            }
        }// end for each option

        return tdCell;
    }

    public Element createRadioButtonTag(Element tdCell, Integer itemId, List options, Integer tabNumber, boolean includeLabel, String dbValue,
            String defaultValue, boolean isHorizontal, boolean hasSavedData) {
        // the input element
        Element element;
        // the span element that contains the input element
        Element spanElement;
        // for the preview, where the item id is 0, we have to generate random
        // IDs,
        // so that the input elements will have unique IDs
        if (itemId == 0) {
            Random rand = new Random();
            itemId = rand.nextInt(10000) + 1;
        }

        // Do not use the default value if there is a valid database value
        boolean hasData = dbValue != null && dbValue.length() > 0;
        for (Object responseOptBean : options) {

            spanElement = new Element("span");
            spanElement.setAttribute("style", "float:left;clear:both");
            synchronized (inputGenerator) {
                element = inputGenerator.initializeInputElement("radio", itemId, tabNumber);
            }
            spanElement.addContent(element);

            String value = ((ResponseOptionBean) responseOptBean).getValue();
            String forDefVal = ((ResponseOptionBean) responseOptBean).getText();
            element.setAttribute("value", value);
            // It's checked if its value equals the DB value
            if (dbValue != null && dbValue.length() > 0 && value.equalsIgnoreCase(dbValue)) {
                element.setAttribute("checked", "checked");
            }
            if (!hasSavedData && defaultValue != null && (forDefVal.equalsIgnoreCase(defaultValue) || value.equalsIgnoreCase(defaultValue))) {
                element.setAttribute("checked", "checked");
            }
            // dealing with IE/repetition model library bug
            if (isHorizontal) {
                element.setAttribute("onclick", "if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'horizontal');}");
            } else {
                element.setAttribute("onclick", "if(detectIEWindows(navigator.userAgent)){this.checked=true; unCheckSiblings(this,'vertical');}");
            }
            tdCell.addContent(spanElement);
            spanElement.addContent(" ");
            if (includeLabel) {
                spanElement.addContent(((ResponseOptionBean) responseOptBean).getText());
            }
        }
        return tdCell;
    }

    // Create a span element with a class attribute referring to the "alert"
    // class.
    // This method returns the altered Element object, which contains the span
    // element as content
    public Element createRequiredAlert(Element tdCell) {

        Element alertReq = new Element("span");
        alertReq.setAttribute("style", "float:left;clear:both");
        alertReq.setAttribute("class", "alert");
        alertReq.addContent("*");
        tdCell.addContent(alertReq);
        return tdCell;
    }

    // This method creates an "a href" element that contains an img element.
    // the link element is designed to generate a discrepancy note. The method
    // uses the same markup as the existing discrepancy-note related JSPs.
    public Element createDiscrepancyNoteSymbol(Integer numDiscrepancyNotes, Integer tabNumber, Integer itemDataId, Integer itemId, boolean forPrinting) {
        Element spanElement = new Element("span");
        spanElement.setAttribute("style", "float:left;clear:both");
        Element ahref = new Element("a");

        // add the href to the span
        spanElement.addContent(ahref);

        ahref.setAttribute("tabindex", tabNumber + 1000 + "");
        ahref.setAttribute("href", "#");
        // disable this note for printing
        // In the future, this method might be used for more than printing CRFs
        // so keep note disabling an option, true or false
        StringBuilder clickValue = new StringBuilder("");
        if (forPrinting) {
            clickValue.append("javascript:void 0");
        }
        ahref.setAttribute("onClick", clickValue.toString());

        Element img = new Element("img");
        img.setAttribute("name", "flag_input" + itemId);
        String fileName = numDiscrepancyNotes > 0 ? "icon_Note.gif" : "icon_noNote.gif";
        img.setAttribute("src", "images/" + fileName);
        img.setAttribute("border", "0");
        img.setAttribute("alt", ResourceBundleProvider.getWordsBundle().getString("discrepancy_note"));
        img.setAttribute("title", ResourceBundleProvider.getWordsBundle().getString("discrepancy_note"));
        ahref.addContent(img);
        return spanElement;
    }
}
