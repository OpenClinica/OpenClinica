package org.akaza.openclinica.web.pform;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.managestudy.CRFVersionMetadataUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.RuleActionPropertyDao;
import org.akaza.openclinica.dao.hibernate.RuleDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.SCDItemMetadataDao;
import org.akaza.openclinica.dao.rule.action.RuleActionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.domain.crfdata.SCDItemMetadataBean;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.ConditionalOpNode;
import org.akaza.openclinica.logic.expressionTree.EqualityOpNode;
import org.akaza.openclinica.logic.expressionTree.ExpressionNode;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.akaza.openclinica.logic.rulerunner.RuleActionContainer;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.akaza.openclinica.web.pform.dto.*;
import org.akaza.openclinica.web.pform.widget.Widget;
import org.akaza.openclinica.web.pform.widget.WidgetFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author joekeremian
 * 
 */
public class OpenRosaXmlGenerator {

    private XMLContext xmlContext = null;
    private DataSource dataSource = null;
    protected final Logger log = LoggerFactory.getLogger(OpenRosaXmlGenerator.class);
    CoreResources coreResources;

    private RuleActionPropertyDao ruleActionPropertyDao;
    private ItemDAO idao;
    private ItemGroupDAO igdao;
    private ItemGroupMetadataDAO igmdao;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private SectionDAO sdao;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public OpenRosaXmlGenerator(CoreResources core, DataSource dataSource, RuleActionPropertyDao ruleActionPropertyDao) throws Exception {
        this.dataSource = dataSource;
        this.coreResources = core;
        this.ruleActionPropertyDao = ruleActionPropertyDao;

        try {
            xmlContext = new XMLContext();
            Mapping mapping = xmlContext.createMapping();
            mapping.loadMapping(core.getURL("openRosaXFormMapping.xml"));
            xmlContext.addMapping(mapping);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
    }

    /**
     * @param formId
     * @return
     * @throws Exception
     */
    public String buildForm(String formId) throws Exception {
        try {
            CRFVersionDAO versionDAO = new CRFVersionDAO(dataSource);
            CRFVersionBean crfVersion = versionDAO.findByOid(formId);
            CRFDAO crfDAO = new CRFDAO(dataSource);
            CRFBean crf = (CRFBean) crfDAO.findByPK(crfVersion.getCrfId());
            CRFVersionMetadataUtil metadataUtil = new CRFVersionMetadataUtil(dataSource);
            ArrayList<SectionBean> crfSections = metadataUtil.retrieveFormMetadata(crfVersion);

            StringWriter writer = new StringWriter();
            IOUtils.copy(getClass().getResourceAsStream("/properties/xform_template.xml"), writer, "UTF-8");
            String xform = writer.toString();
            Html html = buildJavaXForm(xform);

            int sectionCount = mapBeansToDTO(html, crf, crfVersion, crfSections);
            if (sectionCount > 1)
                setFormPaging(html);
            String xformMinusInstance = buildStringXForm(html);
            String preInstance = xformMinusInstance.substring(0, xformMinusInstance.indexOf("<instance>"));
            String instance = buildInstance(html.getHead().getModel(), crfVersion, crfSections);
            String postInstance = xformMinusInstance.substring(xformMinusInstance.indexOf("</instance>") + "</instance>".length());
            logger.debug(preInstance + "<instance>\n" + instance + "\n</instance>" + postInstance);
            System.out.println(preInstance + "<instance>\n" + instance + "\n</instance>" + postInstance);
            return preInstance + "<instance>\n" + instance + "\n</instance>" + postInstance;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ArrayList<ItemGroupBean> getItemGroupBeans(SectionBean section) throws Exception {
        ArrayList<ItemGroupBean> itemGroupBeans = null;

        igdao = new ItemGroupDAO(dataSource);
        itemGroupBeans = (ArrayList<ItemGroupBean>) igdao.findGroupBySectionId(section.getId());
        return itemGroupBeans;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ArrayList<ItemGroupBean> getItemGroupBeansByCrfVersion(CRFVersionBean crfVersion) throws Exception {
        ArrayList<ItemGroupBean> itemGroupBeans = null;

        igdao = new ItemGroupDAO(dataSource);
        itemGroupBeans = (ArrayList<ItemGroupBean>) igdao.findGroupByCRFVersionID(crfVersion.getId());
        return itemGroupBeans;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ItemGroupBean getItemGroupBeanByItemId(Integer itemId) {
        ArrayList<ItemGroupBean> itemGroupBean = null;
        igdao = new ItemGroupDAO(dataSource);

        itemGroupBean = (ArrayList<ItemGroupBean>) igdao.findGroupsByItemID(itemId);
        return itemGroupBean.get(0);
    }

    private SectionBean getSectionBean(Integer ID) {
        sdao = new SectionDAO(dataSource);
        SectionBean sBean = (SectionBean) sdao.findByPK(ID);
        return sBean;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ItemBean getItemBean(String itemOid) {
        ArrayList<ItemBean> itemBean = null;
        idao = new ItemDAO(dataSource);
        itemBean = (ArrayList<ItemBean>) idao.findByOid(itemOid);
        return itemBean.get(0);
    }

    private ItemBean getItemBean(int itemId) {
        ItemBean itemBean = null;
        idao = new ItemDAO(dataSource);
        itemBean = (ItemBean) idao.findByPK(itemId);
        return itemBean;
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    private ItemFormMetadataBean getItemFormMetadataBeanById(Integer id) throws OpenClinicaException {
        itemFormMetadataDAO = new ItemFormMetadataDAO(dataSource);
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) itemFormMetadataDAO.findByPK(id);
        return itemFormMetadataBean;
    }

    @SuppressWarnings("rawtypes")
    private ItemFormMetadataBean getItemFormMetadata(ItemBean item, CRFVersionBean crfVersion) throws Exception {
        ItemFormMetadataBean itemFormMetadataBean = null;

        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(dataSource);
        itemFormMetadataBean = ifmdao.findByItemIdAndCRFVersionId(item.getId(), crfVersion.getId());

        return itemFormMetadataBean;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ItemGroupMetadataBean getItemGroupMetadata(ItemGroupBean itemGroupBean, CRFVersionBean crfVersion, SectionBean section) throws Exception {
        ArrayList<ItemGroupMetadataBean> itemGroupMetadataBean = null;

        ItemGroupMetadataDAO itemGroupMetadataDAO = new ItemGroupMetadataDAO(dataSource);
        itemGroupMetadataBean = (ArrayList<ItemGroupMetadataBean>) itemGroupMetadataDAO.findMetaByGroupAndSection(itemGroupBean.getId(), crfVersion.getId(),
                section.getId());

        return itemGroupMetadataBean.get(0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ItemGroupMetadataBean getItemGroupMetadataByGroup(ItemGroupBean itemGroupBean, CRFVersionBean crfVersion) throws Exception {
        ArrayList<ItemGroupMetadataBean> itemGroupMetadataBean = null;

        ItemGroupMetadataDAO itemGroupMetadataDAO = new ItemGroupMetadataDAO(dataSource);
        itemGroupMetadataBean = (ArrayList<ItemGroupMetadataBean>) itemGroupMetadataDAO.findMetaByGroupAndCrfVersion(itemGroupBean.getId(), crfVersion.getId());

        return itemGroupMetadataBean.get(0);
    }

    /**
     * For Skip Pattern;
     * 
     * @param itemOid
     * @param groupOid
     * @return
     */
    private ArrayList<PropertyBean> getItemGroupPropertyBean(String itemOid, String groupOid) {
        ArrayList<PropertyBean> propertyBeans = null;
        propertyBeans = getRuleActionPropertyDao().findByOid(itemOid, groupOid);
        return propertyBeans;
    }

    private ArrayList<PropertyBean> getGroupPropertyBean(String groupOid) {
        ArrayList<PropertyBean> propertyBeans = null;
        propertyBeans = getRuleActionPropertyDao().findByOid(groupOid);
        return propertyBeans;
    }

    /**
     * For Skip Pattern
     * 
     * @param itemBean
     * @param itemGroupBean
     * @return
     */
    private ExpressionExpressionEvaluate getSkipPattern(ItemBean itemBean, ItemGroupBean itemGroupBean) {
        ExpressionExpressionEvaluate eev = new ExpressionExpressionEvaluate();
        boolean expressionEvaluate = true;
        String expression = null;
        ExpressionBean expressionBean = null;
        ArrayList<PropertyBean> propertyBeans = null;
        if (itemBean == null) {
            propertyBeans = getGroupPropertyBean(itemGroupBean.getOid());
        } else {
            propertyBeans = getItemGroupPropertyBean(itemBean.getOid(), itemGroupBean.getOid());
        }

        if (propertyBeans.size() != 0) {
            for (PropertyBean propertyBean : propertyBeans) {
                logger.info("property bean oid:   " + propertyBean.getOid());
                RuleActionBean ruleActionBean = propertyBean.getRuleActionBean();
                if (ruleActionBean.getActionType().getCode() == 3 && ruleActionBean.getRuleSetRule().getStatus().getCode() == 1) {
                    expressionBean = ruleActionBean.getRuleSetRule().getRuleBean().getExpression();
                    expressionEvaluate = ruleActionBean.getExpressionEvaluatesTo();
                    logger.info("    ExpressionBean:   " + expressionBean.getValue());
                    if (expression != null) {
                        expression = expression + " and " + expressionBean.getValue();

                    } else {
                        expression = expressionBean.getValue();
                    }
                }
            }
        }
        eev.setExpressionEvaluate(expressionEvaluate);
        eev.setExpression(expression);
        return eev;
    }

    /**
     * @param itemGroupBean
     * @param crfVersion
     * @param section
     * @param factory
     * @param bindList
     * @return
     * @throws Exception
     */
    private HashMap<String, Object> getGroupInfo(ItemGroupBean itemGroupBean, CRFVersionBean crfVersion, SectionBean section, WidgetFactory factory,
            ArrayList<Bind> bindList) throws Exception {
        boolean expressionEvaluate = true;
        igmdao = new ItemGroupMetadataDAO(dataSource);
        List<ItemGroupMetadataBean> itemGroupMetadata = igmdao.findMetaByGroupAndSection(itemGroupBean.getId(), crfVersion.getId(), section.getId());

        HashMap<String, Object> map = new HashMap<String, Object>();
        Group group = new Group();
        Repeat repeat = new Repeat();
        group.setUsercontrol(new ArrayList<UserControl>());
        repeat.setUsercontrol(new ArrayList<UserControl>());

        Label groupHeader = new Label();
        groupHeader.setLabel(itemGroupMetadata.get(0).getHeader());
        boolean isGroupRepeating = getItemGroupMetadata(itemGroupBean, crfVersion, section).isRepeatingGroup();

        String nodeset = "/" + crfVersion.getOid() + "/" + itemGroupBean.getOid();
        // repeat.setJrNoAddRemove("true()");
        repeat.setJrCount(nodeset);
        group.setRef(nodeset);
        repeat.setNodeset(nodeset);
        String groupExpression = null;

        ExpressionExpressionEvaluate eev = getSkipPattern(null, itemGroupBean);
        groupExpression = eev.getExpression();
        expressionEvaluate = eev.isExpressionEvaluate();
        if (groupExpression != null)
            groupExpression = getFullExpressionToParse(groupExpression, crfVersion, expressionEvaluate);

        setGroupWidget(section, crfVersion, groupExpression, bindList, factory, itemGroupBean, group, repeat, isGroupRepeating, groupHeader);

        map.put("group", group);
        map.put("repeat", repeat);
        map.put("isGroupRepeating", isGroupRepeating);

        return map;

    }

    private void setFormPaging(Html html) {
        html.getBody().setCssClass("pages");
        List<Group> groups = html.getBody().getGroup();
        for (Group group : groups) {
            group.setAppearance("field-list");
        }
    }

    /**
     * @param html
     * @param crf
     * @param crfVersion
     * @param crfSections
     * @return
     * @throws Exception
     */
    private Integer mapBeansToDTO(Html html, CRFBean crf, CRFVersionBean crfVersion, ArrayList<SectionBean> crfSections) throws Exception {
        boolean expressionEvaluate = true;
        int sectionCount = 0;
        ItemFormMetadataBean itemFormMetadataBean = null;
        Body body = html.getBody();
        ArrayList<Group> allSections = new ArrayList<Group>();
        ArrayList<Bind> bindList = new ArrayList<Bind>();
        WidgetFactory factory = new WidgetFactory(crfVersion);
        html.getHead().setTitle(crf.getName());

        for (SectionBean section : crfSections) {
            ArrayList<Group> groups = new ArrayList<Group>();
            Group singleSection = new Group();
            singleSection.setUsercontrol(new ArrayList<UserControl>());
            String ref = "/" + crfVersion.getOid() + "/" + "SECTION_" + section.getLabel().replaceAll("\\W", "_");
            singleSection.setRef(ref);
            String sectionExpression = null;

            igdao = new ItemGroupDAO(dataSource);
            ArrayList<ItemGroupBean> groupBeans = (ArrayList<ItemGroupBean>) igdao.findGroupBySectionId(section.getId());
            int count = 0;
            if (groupBeans.size() > 0) {
                for (ItemGroupBean groupBean : groupBeans) {

                    ExpressionExpressionEvaluate eev = getSkipPattern(null, groupBean);
                    String expr = eev.getExpression();
                    expressionEvaluate = eev.isExpressionEvaluate();

                    if (expr != null) {
                        if (sectionExpression != null) {
                            sectionExpression = sectionExpression + " and " + expr;
                            count++;
                        } else {
                            sectionExpression = expr;
                            count++;
                        }
                    }
                }

                if (sectionExpression != null && groupBeans.size() == count) {
                    sectionExpression = getFullExpressionToParse(sectionExpression, crfVersion, expressionEvaluate);
                } else {
                    sectionExpression = null;
                }

                setSectionWidget(section, crfVersion, sectionExpression, bindList, factory, singleSection);

                HashMap<String, Object> groupMap = null;
                boolean isGroupRepeating = false;
                Group group = null;
                Repeat repeat = null;

                idao = new ItemDAO(dataSource);
                ArrayList<ItemBean> items = (ArrayList<ItemBean>) idao.findAllBySectionIdOrderedByItemFormMetadataOrdinal(section.getId());

                Integer itemGroupId = 0;
                for (ItemBean item : items) {
                    ItemGroupBean itemGroupBean = getItemGroupBeanByItemId(item.getId());
                    if (itemGroupId != itemGroupBean.getId()) {
                        groupMap = getGroupInfo(itemGroupBean, crfVersion, section, factory, bindList);
                        isGroupRepeating = (Boolean) groupMap.get("isGroupRepeating");
                        group = (Group) groupMap.get("group");
                        repeat = (Repeat) groupMap.get("repeat");
                    }

                    itemFormMetadataBean = getItemFormMetadata(item, crfVersion);
                    int responseTypeId = itemFormMetadataBean.getResponseSet().getResponseTypeId();
                    boolean isItemRequired = itemFormMetadataBean.isRequired();
                    String responseLayout = itemFormMetadataBean.getResponseLayout();
                    String itemExpression = null;
                    ExpressionExpressionEvaluate eev = getSkipPattern(item, itemGroupBean);
                    itemExpression = eev.getExpression();
                    expressionEvaluate = eev.isExpressionEvaluate();

                    if (itemExpression != null)
                        itemExpression = getFullExpressionToParse(itemExpression, crfVersion, expressionEvaluate);

                    // Add the Item Header
                    setHeaderWidget(item, itemFormMetadataBean, itemGroupBean, bindList, factory, isGroupRepeating, repeat, group, itemExpression);
                    // Add the Item SubHeader
                    setSubHeaderWidget(item, itemFormMetadataBean, itemGroupBean, bindList, factory, isGroupRepeating, repeat, group, itemExpression);
                    // Add the Item itself
                    setItemWidget(item, responseTypeId, itemFormMetadataBean, itemGroupBean, bindList, factory, isGroupRepeating, repeat, group,
                            isItemRequired, responseLayout, itemExpression);

                    if (itemGroupId != itemGroupBean.getId()) {
                        groups.add(group);
                        itemGroupId = itemGroupBean.getId();
                    }

                } // end of item loop

                singleSection.setGroup(groups);
                allSections.add(singleSection);
            } // end of groups per section if exist
            sectionCount = allSections.size();
        } // end of section loop

        body.setGroup(allSections);
        html.getHead().getModel().setBind(bindList);
        return sectionCount;
    } // method

    /**
     * @param model
     * @param crfVersion
     * @param crfSections
     * @return
     * @throws Exception
     */
    private String buildInstance(Model model, CRFVersionBean crfVersion, ArrayList<SectionBean> crfSections) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = docFactory.newDocumentBuilder();
        Document doc = build.newDocument();
        Element crfElement = doc.createElement(crfVersion.getOid());
        crfElement.setAttribute("id", crfVersion.getOid());
        doc.appendChild(crfElement);
        crfElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:jr", "http://openrosa.org/javarosa");

        for (SectionBean section : crfSections) {
            Element sectionSubTitle = doc.createElement("SECTION_" + section.getId() + ".SUBTITLE");
            Element sectionInstructions = doc.createElement("SECTION_" + section.getId() + ".INSTRUCTIONS");
            Element sectionElm = doc.createElement("SECTION_" + section.getLabel().replaceAll("\\W", "_"));
            crfElement.appendChild(sectionSubTitle);
            crfElement.appendChild(sectionInstructions);
            crfElement.appendChild(sectionElm);
        }

        ArrayList<ItemGroupBean> itemGroupBeans = getItemGroupBeansByCrfVersion(crfVersion);
        for (ItemGroupBean itemGroupBean : itemGroupBeans) {
            ItemGroupMetadataBean itemGroupMetadataBean = getItemGroupMetadataByGroup(itemGroupBean, crfVersion);

            String repeatGroupMin = itemGroupMetadataBean.getRepeatNum().toString();
            Boolean isrepeating = itemGroupMetadataBean.isRepeatingGroup();

            Element groupElement = doc.createElement(itemGroupBean.getOid());
            if (isrepeating) {
                groupElement.setTextContent(repeatGroupMin);
                groupElement.setAttribute("jr:template", "");
                Element hiddenOrdinalItem = doc.createElement("REPEAT_ORDINAL");
                groupElement.appendChild(hiddenOrdinalItem);
            }
            crfElement.appendChild(groupElement);

            idao = new ItemDAO(dataSource);
            ArrayList<ItemBean> items = (ArrayList<ItemBean>) idao.findAllItemsByGroupIdOrdered(itemGroupBean.getId(), crfVersion.getId());
            for (ItemBean item : items) {
                ItemFormMetadataBean itemMetaData = getItemFormMetadata(item, crfVersion);
                if (itemMetaData.getHeader() != null && !itemMetaData.getHeader().equals("")) {
                    Element header = doc.createElement(item.getOid() + ".HEADER");
                    groupElement.appendChild(header);
                }
                if (itemMetaData.getHeader() != null && !itemMetaData.getSubHeader().equals("")) {
                    Element subHeader = doc.createElement(item.getOid() + ".SUBHEADER");
                    groupElement.appendChild(subHeader);
                }
                Element question = doc.createElement(item.getOid());
                groupElement.appendChild(question);
            } // end of item

        } // end of group

        TransformerFactory transformFactory = TransformerFactory.newInstance();
        Transformer transformer = transformFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        return writer.toString();

    }

    /**
     * To Set Default Values for Item Fields
     * 
     * @param item
     * @param crfVersion
     * @param question
     * @throws Exception
     */
    private void setDefaultElement(ItemBean item, CRFVersionBean crfVersion, Element question) throws Exception {
        Integer responseTypeId = getItemFormMetadata(item, crfVersion).getResponseSet().getResponseTypeId();

        if (responseTypeId == 3 || responseTypeId == 7) {
            String defaultValue = getItemFormMetadata(item, crfVersion).getDefaultValue();
            defaultValue = defaultValue.replace(" ", "");
            defaultValue = defaultValue.replace(",", " ");
            question.setTextContent(defaultValue);
        } else {
            question.setTextContent(getItemFormMetadata(item, crfVersion).getDefaultValue());
        }

    }

    private Html buildJavaXForm(String content) throws Exception {
        // XML to Object
        Reader reader = new StringReader(content);
        Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
        unmarshaller.setClass(Html.class);
        unmarshaller.setWhitespacePreserve(false);
        Html html = (Html) unmarshaller.unmarshal(reader);
        reader.close();
        return html;
    }

    private String buildStringXForm(Html html) throws Exception {
        StringWriter writer = new StringWriter();

        Marshaller marshaller = xmlContext.createMarshaller();
        marshaller.setNamespaceMapping("h", "http://www.w3.org/1999/xhtml");
        marshaller.setNamespaceMapping("jr", "http://openrosa.org/javarosa");
        marshaller.setNamespaceMapping("xsd", "http://www.w3.org/2001/XMLSchema");
        marshaller.setNamespaceMapping("ev", "http://www.w3.org/2001/xml-events");
        marshaller.setNamespaceMapping("", "http://www.w3.org/2002/xforms");
        marshaller.setProperty("org.exolab.castor.indent", "false");
        marshaller.setWriter(writer);
        marshaller.marshal(html);
        String xform = writer.toString();
        return xform;
    }

    /**
     * This method is for Skip pattern to build multiple expressions (not complete yet)
     * 
     * @param expression
     * @param version
     * @return
     */
    private String getFullExpressionToParse(String expression, CRFVersionBean version, boolean expressionEvaluates) throws Exception {
        String result = "";
        expression = " " + expression;
        expression = expression.replaceAll("\\(", "\\( ");
        expression = expression.replaceAll("_CURRENT_DATE", "today()");
        /*
         * today() function returns date and time and will not work with 'eq' operator. But it will work with 'gt' or
         * 'lt' operators
         */
        expression = expression.replaceAll(" I_", " ../I_");
        expression = expression.replaceAll("\\S*/I_", " ../I_");

        expression = expression.replaceAll(" eq ", " = ");
        expression = expression.replaceAll(" ct ", " = "); // convert contains
        expression = expression.replaceAll(" ne ", " != ");
        expression = expression.replaceAll(" gt ", " > ");
        expression = expression.replaceAll(" gte ", " >= ");
        expression = expression.replaceAll(" lt ", " < ");
        expression = expression.replaceAll(" lte ", " <= ");

        if (expressionEvaluates == false) {
            expression = "not(" + expression + ")";
        }

        /*
         * This statement will remove all (SE_ , F_, IG_) entities and will leave only Item_Oid entities. And will
         * neglect the whole path
         */
        String[] exprs = expression.split(" ");
        boolean byPass = false;
        String tempExpr = "";
        for (String expr : exprs) {
            if (expr.contains("../I_")) {
                String itemOid = expr.substring(3);
                logger.info("itemOid:  " + itemOid);

                ItemBean itemBean = getItemBean(itemOid);

                ItemGroupBean itemGroupBean = getItemGroupBeanByItemId(itemBean.getId());
                itemFormMetadataDAO = new ItemFormMetadataDAO(dataSource);
                ItemFormMetadataBean ifmBean = (ItemFormMetadataBean) itemFormMetadataDAO.findByItemIdAndCRFVersionId(itemBean.getId(), version.getId());

                if (ifmBean.getResponseSet().getResponseTypeId() == 3 || ifmBean.getResponseSet().getResponseTypeId() == 7) {
                    byPass = true;
                    tempExpr = expr;
                }
                expr = "/" + version.getOid() + "/" + itemGroupBean.getOid() + "/" + itemOid;
            }

            if (byPass && tempExpr.contains("../I_")) {
                tempExpr = expr;
            } else if (byPass && expr.equals("=")) {
                tempExpr = "selected (" + tempExpr + " , ";
            } else if (byPass && expr.equals("!=")) {
                tempExpr = " not selected (" + tempExpr + " , ";
            } else if (byPass && (expr.equals(">") || expr.equals(">=") || expr.equals("<") || expr.equals("<="))) {
                tempExpr = tempExpr + " " + expr;
                result = result + " " + tempExpr;
                byPass = false;
            } else if (byPass && !expr.contains("../I_") && !expr.equals("!=") && !expr.equals("=") && !expr.equals(">") && !expr.equals(">=")
                    && !expr.equals("<") && !expr.equals("<=")) {
                tempExpr = tempExpr + " " + expr + " ) ";
                result = result + " " + tempExpr;
                byPass = false;
            } else {
                result = result + " " + expr;

            }
        }
        logger.info("Full Expression is:  " + result);
        return result;
    }

    /**
     * @param item
     * @param itemFormMetadataBean
     * @param itemGroupBean
     * @param bindList
     * @param factory
     * @param isGroupRepeating
     * @param repeat
     * @param group
     */
    private void setHeaderWidget(ItemBean item, ItemFormMetadataBean itemFormMetadataBean, ItemGroupBean itemGroupBean, ArrayList<Bind> bindList,
            WidgetFactory factory, boolean isGroupRepeating, Repeat repeat, Group group, String itemExpression) {
        Widget headerWidget = factory.getHeaderWidget(item, itemFormMetadataBean, itemGroupBean, itemExpression);
        if (headerWidget != null) {
            bindList.add(headerWidget.getBinding());
            if (isGroupRepeating)
                repeat.getUsercontrol().add(headerWidget.getUserControl());
            else
                group.getUsercontrol().add(headerWidget.getUserControl());
        } else {
            log.debug("Invalid/Missing instructive header text encountered while loading PForm (" + item.getDataType().getName() + "). Skipping.");
        }
    }

    /**
     * @param item
     * @param itemFormMetadataBean
     * @param itemGroupBean
     * @param bindList
     * @param factory
     * @param isGroupRepeating
     * @param repeat
     * @param group
     */
    private void setSubHeaderWidget(ItemBean item, ItemFormMetadataBean itemFormMetadataBean, ItemGroupBean itemGroupBean, ArrayList<Bind> bindList,
            WidgetFactory factory, boolean isGroupRepeating, Repeat repeat, Group group, String itemExpression) {
        Widget subHeaderWidget = factory.getSubHeaderWidget(item, itemFormMetadataBean, itemGroupBean, itemExpression);
        if (subHeaderWidget != null) {
            bindList.add(subHeaderWidget.getBinding());
            if (isGroupRepeating)
                repeat.getUsercontrol().add(subHeaderWidget.getUserControl());
            else
                group.getUsercontrol().add(subHeaderWidget.getUserControl());
        } else {
            log.debug("Invalid/Missing instructive subheader text encountered while loading PForm (" + item.getDataType().getName() + "). Skipping.");
        }

    }

    /**
     * @param item
     * @param responseTypeId
     * @param itemFormMetadataBean
     * @param itemGroupBean
     * @param bindList
     * @param factory
     * @param isGroupRepeating
     * @param repeat
     * @param group
     * @param isItemRequired
     * @param responseLayout
     * @param itemExpression
     */
    private void setItemWidget(ItemBean item, int responseTypeId, ItemFormMetadataBean itemFormMetadataBean, ItemGroupBean itemGroupBean,
            ArrayList<Bind> bindList, WidgetFactory factory, boolean isGroupRepeating, Repeat repeat, Group group, boolean isItemRequired,
            String responseLayout, String itemExpression) {

        Widget widget = factory.getItemWidget(item, responseTypeId, itemGroupBean, itemFormMetadataBean, isItemRequired, responseLayout, itemExpression);
        if (widget != null) {

            bindList.add(widget.getBinding());

            if (isGroupRepeating) {
                repeat.getUsercontrol().add(widget.getUserControl());
                group.setRepeat(repeat);
            } else {
                group.getUsercontrol().add(widget.getUserControl());
            }

        } else {
            log.debug("Unsupported datatype encountered while loading PForm (" + item.getDataType().getName() + "). Skipping.");
        }

    }

    /**
     * @param section
     * @param crfVersion
     * @param sectionExpression
     * @param bindList
     * @param factory
     * @param singleSection
     */
    private void setSectionWidget(SectionBean section, CRFVersionBean crfVersion, String sectionExpression, ArrayList<Bind> bindList, WidgetFactory factory,
            Group singleSection) {
        Widget sectionWidget = factory.getSectionWidget(section, crfVersion, sectionExpression);
        bindList.add(sectionWidget.getBinding());

        if (section.getTitle() != null && !section.getTitle().equals("")) {
            Label sectionLabel = new Label();
            sectionLabel.setLabel(section.getTitle());
            singleSection.setLabel(sectionLabel);
        }

        singleSection.setGroup(new ArrayList<Group>());
        Widget subtitle = factory.getSectionTextWidget(crfVersion.getOid(), WidgetFactory.SECTION_TEXT_TYPE_SUBTITLE, section);
        Widget instructions = factory.getSectionTextWidget(crfVersion.getOid(), WidgetFactory.SECTION_TEXT_TYPE_INSTRUCTIONS, section);

        if (subtitle != null) {
            singleSection.getUsercontrol().add(subtitle.getUserControl());
            bindList.add(subtitle.getBinding());
        }
        if (instructions != null) {
            singleSection.getUsercontrol().add(instructions.getUserControl());
            bindList.add(instructions.getBinding());
        }

    }

    /**
     * @param section
     * @param crfVersion
     * @param groupExpression
     * @param bindList
     * @param factory
     * @param itemGroupBean
     * @param group
     * @param repeat
     * @param isGroupRepeating
     * @param groupHeader
     */
    private void setGroupWidget(SectionBean section, CRFVersionBean crfVersion, String groupExpression, ArrayList<Bind> bindList, WidgetFactory factory,
            ItemGroupBean itemGroupBean, Group group, Repeat repeat, boolean isGroupRepeating, Label groupHeader) {
        Widget groupWidget = factory.getGroupWidget(itemGroupBean, crfVersion, groupExpression);
        bindList.add(groupWidget.getBinding());
        if (groupWidget != null) {
            if (isGroupRepeating) {
                repeat.setLabel(groupHeader);
            } else {
                group.setLabel(groupHeader);
            }
        }

    }

    public RuleActionPropertyDao getRuleActionPropertyDao() {
        return ruleActionPropertyDao;
    }

    public void setRuleActionPropertyDao(RuleActionPropertyDao ruleActionPropertyDao) {
        this.ruleActionPropertyDao = ruleActionPropertyDao;
    }

}