package org.akaza.openclinica.view.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The CssRules clss encapsulates the mapping of element names or element
 * descriptions to certain CSS class names. This helps allow the application of
 * CSS classes to dynamically generated elements, in the manner of td
 * class="aka_cellBorders" .
 */
public class CssRules {

    protected static final Logger logger = LoggerFactory.getLogger(CssRules.class.getName());
    // A Map that maps element names (e.g., td) or short descriptions (e.g.,
    // span left_item)
    // to one or more CSS class names. This implementation could evolve to read
    // the CSS rules directly from the style sheet CSS_FILE_NAME.
    public static Map<String, List<CssClass>> cssMap;
    // Where are the class names located in the application?
    public final static String CSS_FILE_NAME = "styles.css";
    private static String cssRulePrefix = "aka_";
    private static boolean applyPrefix = true;
    // Initialize the Map
    static {
        cssMap = new HashMap<String, List<CssClass>>();
        loadMap("table", CssClass.form_table);
        loadMap("table section", CssClass.form_table_section_header);
        loadMap("td", CssClass.padding_norm, CssClass.cellBorders);
        loadMap("td borders_on", CssClass.padding_norm, CssClass.cellBorders_dark);
        loadMap("span left_item", CssClass.text_block);
        loadMap("th", CssClass.headerBackground, CssClass.padding_large, CssClass.cellBorders);
        loadMap("th borders_on", CssClass.headerBackground, CssClass.padding_large, CssClass.cellBorders_dark);
        loadMap("input date", CssClass.date_input);
        loadMap("input text", CssClass.sm_text_input);
        loadMap("tr header", CssClass.headerBackground);
        loadMap("td header", CssClass.header_border);
        loadMap("td subheader", CssClass.header_border);
        loadMap("td title", CssClass.header_border);
        loadMap("td subtitle", CssClass.header_border);
        loadMap("td instructions", CssClass.header_border);
    }

    // The class names that styles.css uses to style tables
    enum CssClass {
        revised_content, bodywidth, revised, text_block, header_border, headerBackground, stripes, cellBorders, cellBorders_dark, sm_text, padding_norm, padding_large, light_stripes, date_input, sm_text_input, table_cell, table_cell_left, form_table, form_table_section_header
    }

    // This method is used during the initialization of a HashMap that maps
    // element names
    // to one or more CSS classes, as in <input type="text"
    // class="sm_text_input">.
    // loadMap is synchronized for thread safety and is only called during the
    // class'
    // initialization, when it is first loaded.
    private static synchronized void loadMap(String elementDescription, CssClass... cssClassNames) {
        List<CssClass> cssClasses = new ArrayList<CssClass>();
        for (CssClass clas : cssClassNames) {
            cssClasses.add(clas);
        }
        cssMap.put(elementDescription, cssClasses);

    }

    public static String getCssRulePrefix() {
        return cssRulePrefix;
    }

    public static boolean isApplyPrefix() {
        return applyPrefix;
    }

    /**
     * This method returns a string of class names for the tagName parameter.
     * 
     * @param tagName
     *            A tag such as td, TD, or tr, or a tag name plus a description
     *            as in td left_item.
     * @return A String of CSS class names, as in "headerBackground
     *         padding_large"
     */
    public static synchronized String getClassNamesForTag(String tagName) {
        if (tagName == null || tagName.length() == 0)
            return "";
        List<CssClass> classNames = cssMap.get(tagName.toLowerCase());
        if (classNames == null || classNames.size() == 0)
            return "";
        int listSize = classNames.size();

        int counter = 0;
        String allNames = "";
        for (CssClass name : classNames) {
            ++counter;
            allNames += isApplyPrefix() ? getCssRulePrefix() + name.toString() : name.toString();
            if (!(counter == listSize))
                allNames += " ";
        }
        return allNames;
    }

    public static void main(String[] args) {
        // CssRules cssRules = new CssRules();
        String element = "<th";
        String _class = CssRules.getClassNamesForTag("th");
        if (_class != null && _class.length() > 0) {
            element += " class=\"";
            element += _class;
            element += "\">";
        }
        logger.info(element);
        element = "<td";
        _class = CssRules.getClassNamesForTag("td");
        if (_class != null && _class.length() > 0) {
            element += " class=\"";
            element += _class;
            element += "\">";
        }
        logger.info(element);
    }
}
