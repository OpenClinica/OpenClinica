package org.akaza.openclinica.view.form;

import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;
import java.util.Random;

/**
 * This class adds the required repetition-model related attributes to an
 * Element. These attributes involve repeating form elements, and use a
 * JavaScript library. See the repetition-model specification at
 * http://www.whatwg.org/specs/web-forms/current-work/#repeatingFormControls.
 */
public class RepeatManager {

    /*
     * Add the attributes to an HTML tag that provide the template for other
     * repeating form elements. This method adds the required attributes to the
     * repeater parameter than returns the changed element.
     */
    public Element addParentRepeatAttributes(Element repeater, String parentRepeaterId, Integer repeatStart, Integer repeatMax) {
        repeater.setAttribute("id", parentRepeaterId);
    //    repeater.setAttribute("repeat", "template");
      repeater.setAttribute("repeat-start", repeatStart.toString());
        repeater.setAttribute("repeat-max", repeatMax.toString());
        repeater.setAttribute("style","");
        return repeater;
    }

    /*
     * Add the attributes to an HTML tag that provide the template for other
     * repeating form elements. This method adds the required attributes to the
     * repeater parameter than returns the changed element.
     */
    public Element addChildRepeatAttributes(Element repeater, String parentId, Integer itemId, String forcedInputNameIndex) {
        // The code needs a input name prefix that is unique for every table
        // to make sure that the input name is unique for every form field
        StringBuilder nameVal = new StringBuilder(parentId);
        if (forcedInputNameIndex == null || forcedInputNameIndex.length() < 1) {
            nameVal.append("_[").append(parentId).append("]input");
        } else {
            nameVal.append("_[").append(forcedInputNameIndex).append("]input");
        }
        // for the preview, where the item id is 0, we have to generate random
        // IDs,
        // so that the input elements will have unique IDs
        if (itemId == 0) {
            Random rand = new Random();
            itemId = rand.nextInt(100000) + 1;
        }
        nameVal.append(itemId);
        // if the element does not have an input "name" attribute, then create a
        // new one; otherwise
        // remove and edit the existing one to add this required repeat
        // information.
        // The child elements have names of input, select, or textarea
        List<Element> inputs = repeater.getChildren("input");
        if (inputs.isEmpty()) {
            inputs = repeater.getChildren("select");
        }
        if (inputs.isEmpty()) {
            inputs = repeater.getChildren("textarea");
        }
        for (Element input : inputs) {
            // do not include input type="hidden"
            boolean isHidden;
            Attribute attribute = input.getAttribute("type");
            if (attribute == null || attribute.getValue() == null || !attribute.getValue().equalsIgnoreCase("hidden")) {
                if (input.getAttribute("name") != null) {
                    input.removeAttribute("name");
                }
            } else {
                continue;
            }
            // the input Element has to use the id of its "repeat parent"
            // element
            input.setAttribute("name", nameVal.toString());
        }
        return repeater;
    }

    public static Element createrepeatButtonControl(String type, String templateName) {
        /* <button type="add" template="order" >Add Row</button> */

        Element element = new Element("button");
        element.setAttribute("type", type);
        element.setAttribute("template", templateName);
        if (type != null && type.equalsIgnoreCase("remove")) {
            element.setAttribute("class", "button_remove");
        } else {
            element.setAttribute("class", "button_search");
            // +" row"
            element.addContent(type);
        }

        return element;
    }

}
