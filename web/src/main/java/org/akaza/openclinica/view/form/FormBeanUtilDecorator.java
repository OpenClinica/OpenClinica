package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.jdom.Element;

import java.util.List;

/**
 * This class is designed to intercept calls to FormBeanUtil methods, and for
 * the purposes of creating a Print CRF view, change radio and select response
 * types to checkboxes.
 */
public class FormBeanUtilDecorator {
    // Delegate the method call to this object, as in the Decorator design
    // pattern
    private final FormBeanUtil formBeanUtil = new FormBeanUtil();

    /**
     * Create a printable form in XHTML for a non-group type table. This method
     * converts radio and select response types to checkbox, prior to delegating
     * the actual method call to the FormBeanUtil object.
     *
     * @param items
     *            The DisplayItemBeans that provide metadata for the forms.
     * @param tabindex
     *            A number that is not used in printable forms.
     * @param hasDiscrepancyMgt
     *            A boolean value indicating whether to display discrepancy
     *            icons.
     * @param hasDBValues
     *            A boolean value indicating whether there are prefilled
     *            database values.
     * @param forPrinting
     *            A boolean value indicating whether CRF printing is involved
     *            (if true, then discrepancy note icons are not clickable).
     * @return A JDom Element containing the markup for an XHTML table.
     */
    public Element createXHTMLTableFromNonGroup(List<DisplayItemBean> items, Integer tabindex, boolean hasDiscrepancyMgt, boolean hasDBValues,
            boolean forPrinting) {

        convertResponseTypes(items);
        synchronized (formBeanUtil) {
            return formBeanUtil.createXHTMLTableFromNonGroup(items, tabindex, hasDiscrepancyMgt, hasDBValues, forPrinting);
        }

    }

    /**
     * Convert various response types to checkboxes for the purpose of CRF print
     * views.
     *
     * @param items
     *            A List of DisplayItemBeans.
     */
    private void convertResponseTypes(List<DisplayItemBean> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        String tempStr;
        ResponseSetBean responseSetBean;

        for (DisplayItemBean displayBean : items) {

            // Get the response type name, like "checkbox"
            tempStr = displayBean.getMetadata().getResponseSet().getResponseType().getName();
            if (checkForTypes(tempStr)) {
                responseSetBean = displayBean.getMetadata().getResponseSet();
                responseSetBean.setResponseType(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX);

            }
        }

    }

    /**
     * Check whether a response name is radio, single-select, or multi-select.
     *
     * @param responseName
     *            The name of the type of input element.
     * @return boolean true if the name is radio, single-select, or
     *         multi-select.
     */
    private boolean checkForTypes(String responseName) {
        return "radio".equalsIgnoreCase(responseName) || "single-select".equalsIgnoreCase(responseName) || "multi-select".equalsIgnoreCase(responseName);
    }

}
