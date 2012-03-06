package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.jdom.Element;

import java.util.List;

/**
 * This interface defines the methods for creating HTML input types. The inputs
 * represent the content for table cells or TD tags, which are implemented as
 * JDOM Elements. Created by IntelliJ IDEA. User: bruceperry Date: May 17, 2007
 */
public interface InputGenerator {
    Element createTextInputTag(Element tdCell, Integer itemId, Integer tabNumber, String defaultValue, boolean isDateType, String dbValue, boolean hasSavedData);

    Element createTextareaTag(Element tdCell, Integer itemId, Integer tabNumber, String dbValue, String defaultValue, boolean hasSavedData);

    Element createCheckboxTag(Element tdCell, Integer itemId, List options, Integer tabNumber, boolean includeLabel, String dbValue, String defaultValue,
            boolean isHorizontal, boolean hasSavedData);

    Element createRadioButtonTag(Element tdCell, Integer itemId, List options, Integer tabNumber, boolean includeLabel, String dbValue, String defaultValue,
            boolean isHorizontal, boolean hasSavedData);

    Element createSingleSelectTag(Element tdCell, Integer itemId, List options, Integer tabNumber);

    Element createMultiSelectTag(Element tdCell, Integer itemId, List options, Integer tabNumber, String dbValue, String defaultValue, boolean hasSavedData);

    // YW, 1-10-2007 << response type = calculation
    Element createCaculationTag(Element tdCell, Integer itemId, ResponseSetBean responseSet, boolean isDateType, String dbValue, boolean hasSavedData);
    // YW >>

    Element createInstantTag(Element tdCell, Integer itemId, Integer tabNumber, String dbValue, boolean hasSavedData);
}
