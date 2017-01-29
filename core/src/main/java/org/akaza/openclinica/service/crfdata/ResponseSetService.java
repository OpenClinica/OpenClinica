package org.akaza.openclinica.service.crfdata;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.akaza.openclinica.dao.hibernate.ResponseSetDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.ResponseSet;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.xform.XformItem;
import org.akaza.openclinica.domain.xform.XformUtils;
import org.akaza.openclinica.domain.xform.dto.Group;
import org.akaza.openclinica.domain.xform.dto.Html;
import org.akaza.openclinica.domain.xform.dto.Input;
import org.akaza.openclinica.domain.xform.dto.Item;
import org.akaza.openclinica.domain.xform.dto.ItemSet;
import org.akaza.openclinica.domain.xform.dto.Label;
import org.akaza.openclinica.domain.xform.dto.Repeat;
import org.akaza.openclinica.domain.xform.dto.Select;
import org.akaza.openclinica.domain.xform.dto.Select1;
import org.akaza.openclinica.domain.xform.dto.Upload;
import org.akaza.openclinica.domain.xform.dto.UserControl;
import org.akaza.openclinica.validator.xform.ResponseSetValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
public class ResponseSetService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private ResponseSetDao responseSetDao;

    public ResponseSetService() {
    }

    public ResponseSet getResponseSet(Html html, String submittedXformText, XformItem xformItem, CrfVersion crfVersion, ResponseType responseType,
            org.akaza.openclinica.domain.datamap.Item item, Errors errors) throws Exception {

        ResponseSet existingSet = responseSetDao.findByLabelVersion(xformItem.getItemName(), crfVersion.getCrfVersionId());
        if (existingSet == null) {
            // Create the response set
            ResponseSet responseSet = new ResponseSet();
            responseSet.setLabel(xformItem.getItemName());
            String optionText = getOptionsText(html, submittedXformText, xformItem, responseType);

            if (optionText != null) {
                responseSet.setOptionsText(optionText);
                responseSet.setOptionsValues(getOptionsValues(html, submittedXformText, xformItem, responseType));
                responseSet.setResponseType(responseType);
                responseSet.setVersionId(crfVersion.getCrfVersionId());
                responseSet = responseSetDao.saveOrUpdate(responseSet);
            }
            // Run validation against it
            ResponseSetValidator validator = new ResponseSetValidator(responseSetDao, item);
            DataBinder dataBinder = new DataBinder(responseSet);
            Errors responseSetErrors = dataBinder.getBindingResult();
            validator.validate(responseSet, responseSetErrors);
            errors.addAllErrors(responseSetErrors);

            return responseSet;
        } else
            return existingSet;
    }

    public List<UserControl> groupItemsInBody(List<Group> groups, List<UserControl> controls) {
        for (Group group : groups) {
            if (group.getUsercontrol() != null) {
                controls.addAll(group.getUsercontrol());
            }
            if (group.getGroup() != null) {
                controls = groupItemsInBody(group.getGroup(), controls);
            }
            if (group.getRepeat() != null) {
                List<Repeat> list = new ArrayList();
                list.add(group.getRepeat());
                controls = repeatItemsInBody(list, controls);
            }
        }
        return controls;
    }

    public List<UserControl> repeatItemsInBody(List<Repeat> repeats, List<UserControl> controls) {
        for (Repeat repeat : repeats) {
            if (repeat.getUsercontrol() != null) {
                controls.addAll(repeat.getUsercontrol());
            }
            if (repeat.getGroup() != null) {
                controls = groupItemsInBody(repeat.getGroup(), controls);
            }
            if (repeat.getRepeat() != null) {
                controls = repeatItemsInBody(repeat.getRepeat(), controls);
            }
        }
        return controls;
    }

    public List<UserControl> getUserControl(Html html) {
        List<Group> groups = html.getBody().getGroup();
        List<Repeat> repeats = html.getBody().getRepeat();
        List<UserControl> usercontrol = html.getBody().getUsercontrol();

        List<UserControl> controls = new ArrayList<>();
        if (usercontrol != null) {
            controls.addAll(usercontrol);
        }
        if (groups != null) {
            controls = groupItemsInBody(groups, controls);
        }
        if (repeats != null) {
            controls = repeatItemsInBody(repeats, controls);
        }
        return controls;
    }

    private String getOptionsText(Html html, String submittedXformText, XformItem xformItem, ResponseType responseType) throws Exception {

        String optionsText = "";
        List<UserControl> controls = getUserControl(html);
        for (UserControl control : controls) {
            if (control.getRef().equals(xformItem.getItemPath())) {

                List<Item> items = null;
                ItemSet itemSet = null;

                if (control instanceof Input) {
                    return responseType.getName();
                } else if (control instanceof Select) {
                    items = ((Select) control).getItem();
                    itemSet = ((Select) control).getItemSet();
                } else if (control instanceof Select1) {
                    items = ((Select1) control).getItem();
                    itemSet = ((Select1) control).getItemSet();
                } else if (control instanceof Upload && control.getMediatype().equals("image/*")) {
                    return responseType.getName();
                } else {
                    logger.debug("Found Unsupported UserControl (" + control.getClass().getName() + ".  Returning null text.");
                    return null;
                }

                if (itemSet != null)
                    optionsText = getOptionsTextFromItemSet(submittedXformText, itemSet, html);
                else {
                    for (Item option : items) {
                        String label = lookupLabel(html, option.getLabel());
                        label = label.replaceAll(",", "\\\\,");
                        if (optionsText.isEmpty())
                            optionsText = label;
                        else
                            optionsText += "," + label;
                    }
                }

                // }
            }
        }
        return optionsText;
    }

    private String getOptionsTextFromItemSet(String submittedXformText, ItemSet itemSet, Html html) throws Exception {
        String optionsText = "";

        // Based of ItemSet definition, look up name of element containing each item label.
        // Determine if the value of this element is a reference to an itext lookup.
        String itemSetLabelRef = itemSet.getLabel().getRef();
        boolean hasItextLookup = false;
        String itemSetLabelRefName;
        if (itemSetLabelRef.startsWith("jr:itext(")) {
            hasItextLookup = true;
            itemSetLabelRefName = itemSetLabelRef.substring(itemSetLabelRef.indexOf("(") + 1, itemSetLabelRef.lastIndexOf(")"));
        } else
            itemSetLabelRefName = itemSetLabelRef;

        // Use the XPath built into the ItemSet definition to mine the XML Xform for the list of items
        // contained in this ItemSet.
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document xml = builder.parse(new ByteArrayInputStream(submittedXformText.getBytes(StandardCharsets.UTF_8)));
        String expression = formatItemSetXPath(itemSet.getNodeSet());
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xml, XPathConstants.NODESET);

        // Iterate thru the list of items, build the list of Options Text, Performing Itext lookups if required.
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            Element itemLabelName = (Element) item.getElementsByTagName(itemSetLabelRefName).item(0);
            String label;
            if (hasItextLookup)
                label = XformUtils.getDefaultTranslation(html, itemLabelName.getTextContent());
            else
                label = itemLabelName.getTextContent();
            label = label.replaceAll(",", "\\\\,");
            if (optionsText.equals(""))
                optionsText = label;
            else
                optionsText += "," + label;
        }
        // Return the completed Options Text
        return optionsText;
    }

    private String getOptionsValues(Html html, String submittedXformText, XformItem xformItem, ResponseType responseType) throws Exception {
        String optionsValues = "";
        List<UserControl> controls = getUserControl(html);
        for (UserControl control : controls) {
            if (control.getRef().equals(xformItem.getItemPath())) {

                List<Item> items = null;
                ItemSet itemSet = null;

                if (control instanceof Input) {
                    return responseType.getName();
                } else if (control instanceof Select) {
                    items = ((Select) control).getItem();
                    itemSet = ((Select) control).getItemSet();
                } else if (control instanceof Select1) {
                    items = ((Select1) control).getItem();
                    itemSet = ((Select1) control).getItemSet();
                } else if (control instanceof Upload && control.getMediatype().equals("image/*")) {
                    return responseType.getName();
                } else {
                    logger.debug("Found Unsupported UserControl (" + control.getClass().getName() + ".  Returning null text.");
                    return null;
                }

                if (itemSet != null)
                    optionsValues = getOptionsValuesFromItemSet(submittedXformText, itemSet, html);
                else {
                    for (Item option : items) {
                        String value = option.getValue();
                        if (optionsValues.isEmpty())
                            optionsValues = value;
                        else
                            optionsValues += "," + value;
                    }
                }

            }
        }
        return optionsValues;
    }

    private String getOptionsValuesFromItemSet(String submittedXformText, ItemSet itemSet, Html html) throws Exception {
        String optionsValues = "";

        // Based of ItemSet definition, look up name of element containing each item value.
        String itemSetValue = itemSet.getValue().getRef();

        // Use the XPath built into the ItemSet definition to mine the XML Xform for the list of items
        // contained in this ItemSet.
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        XPath xPath = XPathFactory.newInstance().newXPath();
        Document xml = builder.parse(new ByteArrayInputStream(submittedXformText.getBytes(StandardCharsets.UTF_8)));
        String expression = formatItemSetXPath(itemSet.getNodeSet());
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xml, XPathConstants.NODESET);

        // Iterate thru the list of items, build the list of Options Values.
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            Element itemValue = (Element) item.getElementsByTagName(itemSetValue).item(0);
            String value = itemValue.getTextContent();
            if (optionsValues.equals(""))
                optionsValues = value;
            else
                optionsValues += "," + value;
        }
        // Return the completed Options Values
        return optionsValues;
    }

    private String formatItemSetXPath(String nodeSet) {
        String expression = null;

        // Replace 'instance' function call with standard XPath syntax
        if (nodeSet.trim().startsWith("instance")) {
            String instanceID = nodeSet.substring(nodeSet.indexOf("'") + 1, StringUtils.ordinalIndexOf(nodeSet, "'", 2));
            expression = "//head/model/instance[@id='" + instanceID + "']" + nodeSet.substring(nodeSet.indexOf(")") + 1);
        }
        // Remove filter on end
        if (expression.trim().endsWith("]")) {
            expression = expression.substring(0, expression.lastIndexOf("["));
        }
        return expression;
    }

    private String lookupLabel(Html html, Label label) {
        if (label != null && label.getLabel() != null && !label.getLabel().equals(""))
            return label.getLabel();
        else if (label != null && label.getRef() != null && !label.getRef().equals("")) {
            String ref = label.getRef();
            String itextKey = ref.substring(ref.indexOf("'") + 1, ref.lastIndexOf("'"));
            return XformUtils.getDefaultTranslation(html, itextKey);
        } else
            return "";
    }

}
