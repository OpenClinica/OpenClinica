package org.akaza.openclinica.web.pform;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
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
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.logic.rulerunner.RuleActionContainer;
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

			mapBeansToDTO(html, crf, crfVersion, crfSections);
			if (crfSections.size() > 1)
				setFormPaging(html);
			String xformMinusInstance = buildStringXForm(html);
			String preInstance = xformMinusInstance.substring(0, xformMinusInstance.indexOf("<instance>"));
			String instance = buildInstance(html.getHead().getModel(), crfVersion, crfSections);
			String postInstance = xformMinusInstance.substring(xformMinusInstance.indexOf("</instance>") + "</instance>".length());
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
	private ItemGroupMetadataBean getItemGroupMetadata(ItemGroupBean itemGroupBean, CRFVersionBean crfVersion, SectionBean section)
			throws Exception {
		ArrayList<ItemGroupMetadataBean> itemGroupMetadataBean = null;

		ItemGroupMetadataDAO itemGroupMetadataDAO = new ItemGroupMetadataDAO(dataSource);
		itemGroupMetadataBean = (ArrayList<ItemGroupMetadataBean>) itemGroupMetadataDAO.findMetaByGroupAndSection(itemGroupBean.getId(),
				crfVersion.getId(), section.getId());

		return itemGroupMetadataBean.get(0);
	}

	/**
	 * For Skip Pattern;
	 * 
	 * @param itemOid
	 * @param groupOid
	 * @return
	 */
	private ArrayList<PropertyBean> getPropertyBean(String itemOid, String groupOid) {
		ArrayList<PropertyBean> propertyBeans = null;
		propertyBeans = getRuleActionPropertyDao().findByOid(itemOid, groupOid);
		return propertyBeans;
	}

	/**
	 * For Skip Pattern
	 * 
	 * @param itemBean
	 * @param itemGroupBean
	 * @return
	 */
	private HashMap<String, Object> getSkipPattern(ItemBean itemBean, ItemGroupBean itemGroupBean) {
		ItemBean itemTargetBean = null;
		HashMap<String, Object> map = new HashMap<String,Object>();
		ExpressionBean expressionBean = null;
		ArrayList<PropertyBean> propertyBeans = getPropertyBean(itemBean.getOid(), itemGroupBean.getOid());

		if (propertyBeans.size() != 0) {
			for (PropertyBean propertyBean : propertyBeans) {
				logger.info("property bean oid:   " + propertyBean.getOid());
				RuleActionBean ruleActionBean = propertyBean.getRuleActionBean();
				if (ruleActionBean.getActionType().getCode() == 3 && ruleActionBean.getRuleSetRule().getStatus().getCode() == 1) {
					int itemTargetId = ruleActionBean.getRuleSetRule().getRuleSetBean().getItemId();
					itemTargetBean = getItemBean(itemTargetId);
					expressionBean = ruleActionBean.getRuleSetRule().getRuleBean().getExpression();
					logger.info("itemTargetBean :   " + itemTargetBean.getOid() + "    ExpressionBean:   " + expressionBean.getValue());
					map.put("itemTargetBean", itemTargetBean);
					map.put("expressionBean", expressionBean);
					return map;
				}
			}
		}
		map.put("itemTargetBean", null);
		map.put("expressionBean", null);
		return map;
	}

	private HashMap<String, Object> getGroupInfo(ItemGroupBean itemGroupBean, CRFVersionBean crfVersion, SectionBean section)
			throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Group group = new Group();
		Repeat repeat = new Repeat();
		group.setUsercontrol(new ArrayList<UserControl>());
		repeat.setUsercontrol(new ArrayList<UserControl>());
		Label repeatLabel = new Label();

		// group.setAppearance("field-list");
		Label groupHeader = new Label();
		String grpHeader = !itemGroupBean.getName().equals("Ungrouped") ? itemGroupBean.getName() : "";
		groupHeader.setLabel(grpHeader);
		// group.setLabel(groupHeader);
		boolean isGroupRepeating = getItemGroupMetadata(itemGroupBean, crfVersion, section).isRepeatingGroup();

		String groupRepeatNum = getItemGroupMetadata(itemGroupBean, crfVersion, section).getRepeatNum().toString();
		String groupMaxRepeatNum = getItemGroupMetadata(itemGroupBean, crfVersion, section).getRepeatMax().toString();

		String nodeset = "/" + crfVersion.getOid() + "/" + section.getLabel().replace(" ", "_") + "/" + itemGroupBean.getOid();
		// repeat.setJrNoAddRemove("true()");
		 repeat.setJrCount(groupRepeatNum);
		group.setRef(nodeset);
		// repeat.setAppearance("field-list");
		repeat.setNodeset(nodeset);

		if (isGroupRepeating) {
			repeat.setLabel(groupHeader);
		} else {
			group.setLabel(groupHeader);
		}
		map.put("group", group);
		map.put("repeat", repeat);
		map.put("isGroupRepeating", isGroupRepeating);

		return map;

	}

	private void setFormPaging(Html html) {
		html.getBody().setCssClass("pages");
		List<Group> groups = html.getBody().getGroup();
		for (Group group : groups) {

			List<Group> grps = group.getGroup();
			for (Group grp : grps) {
			}
			// group.setAppearance("field-list");
		}
	}

	private void mapBeansToDTO(Html html, CRFBean crf, CRFVersionBean crfVersion, ArrayList<SectionBean> crfSections) throws Exception {
		ItemFormMetadataBean itemFormMetadataBean = null;
		Body body = html.getBody();
		ArrayList<Group> allSections = new ArrayList<Group>();
		ArrayList<Bind> bindList = new ArrayList<Bind>();
		WidgetFactory factory = new WidgetFactory(crfVersion);
		html.getHead().setTitle(crf.getName());

		for (SectionBean section : crfSections) {
			ArrayList<Group> multiGroups = new ArrayList<Group>();
			ArrayList<ItemGroupBean> itemGroupBeans = getItemGroupBeans(section);
			ArrayList<Group> groups = new ArrayList<Group>();
			Group singleSection = new Group();
			singleSection.setUsercontrol(new ArrayList<UserControl>());

			Label sectionLabel = new Label();
			sectionLabel.setLabel(section.getTitle());
			singleSection.setLabel(sectionLabel);
			singleSection.setAppearance("field-list");
			// singleSection.setGroup(new ArrayList<Group>());

			// for (ItemGroupBean itemGroupBean : itemGroupBeans) {

			HashMap<String, Object> groupMap = null;
			boolean isGroupRepeating = false;
			Group group = null;
			Repeat repeat = null;

			ItemDAO itemdao = new ItemDAO(dataSource);

			ArrayList<ItemBean> items = (ArrayList<ItemBean>) itemdao.findAllBySectionIdOrderedByItemFormMetadataOrdinal(section.getId());

			Integer itemGroupId = 0;
			for (ItemBean item : items) {
				ItemGroupBean itemGroupBean = getItemGroupBeanByItemId(item.getId());
				if (itemGroupId != itemGroupBean.getId()) {
					groupMap = getGroupInfo(itemGroupBean, crfVersion, section);
					isGroupRepeating = (Boolean) groupMap.get("isGroupRepeating");
					group = (Group) groupMap.get("group");
					repeat = (Repeat) groupMap.get("repeat");
					// itemGroupId = itemGroupBean.getId();
				}

				itemFormMetadataBean = getItemFormMetadata(item, crfVersion);
				int responseTypeId = itemFormMetadataBean.getResponseSet().getResponseTypeId();
				boolean isItemRequred = itemFormMetadataBean.isRequired();
				int itemGroupRepeatNumber = 1;
				String responseLayout = itemFormMetadataBean.getResponseLayout();

				HashMap<String, Object> map = getSkipPattern(item, itemGroupBean);
				ItemBean itemTargetBean = (ItemBean) map.get("itemTargetBean");
				String expression = null;
				ExpressionBean expressionBean = (ExpressionBean) map.get("expressionBean");
				if (expressionBean != null) {
					expression = expressionBean.getValue();
					expression = getExpressionParsedAndSortedForSkipPattern(expression, crfVersion, section);
				}
				Widget widget = factory.getWidget(item, responseTypeId, itemGroupBean, itemFormMetadataBean, itemGroupRepeatNumber,
						isItemRequred, isGroupRepeating, responseLayout, itemTargetBean, expression, section);
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
				if (itemGroupId != itemGroupBean.getId()) {
					groups.add(group);
					itemGroupId = itemGroupBean.getId();
				}

			} // item

			// } // multi group
			singleSection.setGroup(groups);
			allSections.add(singleSection);
		} // section
		body.setGroup(allSections);
		html.getHead().getModel().setBind(bindList);

	} // method

	private String buildInstance(Model model, CRFVersionBean crfVersion, ArrayList<SectionBean> crfSections) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder build = docFactory.newDocumentBuilder();
		Document doc = build.newDocument();
		Element crfElement = doc.createElement(crfVersion.getOid());
		crfElement.setAttribute("id", crfVersion.getOid());
		doc.appendChild(crfElement);
		for (SectionBean section : crfSections) {
			Element sectionElement = doc.createElement(section.getLabel().replace(" ", "_"));
			crfElement.appendChild(sectionElement);

			Element groupElement = null;

			ItemDAO itemdao = new ItemDAO(dataSource);
			ArrayList<ItemBean> items = (ArrayList<ItemBean>) itemdao.findAllBySectionIdOrderedByItemFormMetadataOrdinal(section.getId());

			Integer itemGroupId = 0;
			for (ItemBean item : items) {
				ItemGroupBean itemGroupBean = getItemGroupBeanByItemId(item.getId());
				if (itemGroupId != itemGroupBean.getId()) {
					groupElement = doc.createElement(itemGroupBean.getOid());
					sectionElement.appendChild(groupElement);
					itemGroupId = itemGroupBean.getId();
				}

				Element itemElement = doc.createElement(item.getOid());
				groupElement.appendChild(itemElement);

				// To activate Default Values showing in Pfrom , Uncomment
				// below line of code
				// setDefaultElement(item,crfVersion,question);
			} // end of item

			// } // end of group

		} // end of section
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
		marshaller.setWriter(writer);
		marshaller.marshal(html);
		String xform = writer.toString();
		return xform;
	}

	/**
	 * This method is for Skip pattern to build multiple expressions (not
	 * complete yet)
	 * 
	 * @param expression
	 * @param version
	 * @return
	 */
	private String getFullExpressionToParse(String expression, CRFVersionBean version, SectionBean section) throws Exception {
		ArrayList<String> exprList = (ArrayList<String>) Arrays.asList(expression.split("( and )|( or ) "));
		for (String expr : exprList) {
			expression = getExpressionParsedAndSortedForSkipPattern(expr, version, section);
		}

		return expression;
	}

	/**
	 * This method is for skip pattern to build single expression
	 * 
	 * @param expression
	 * @param version
	 * @return
	 */
	private String getExpressionParsedAndSortedForSkipPattern(String expression, CRFVersionBean version, SectionBean section)
			throws Exception {
		String itemOid, operator, value;
		expression = expression.replaceAll("\\s+", " ").trim();
		String[] expr = expression.split(" ");
		if (expr[0].startsWith("I_")) {
			itemOid = expr[0].trim();
			value = expr[2].trim();
		} else {
			itemOid = expr[2].trim();
			value = expr[0].trim();
		}
		operator = expr[1];
		if (operator.equalsIgnoreCase("eq"))
			operator = "=";
		if (operator.equalsIgnoreCase("lt"))
			operator = "<";
		if (operator.equalsIgnoreCase("gt"))
			operator = ">";

		ItemBean itemBean = getItemBean(itemOid);
		ItemGroupBean itemGroupBean = getItemGroupBeanByItemId(itemBean.getId());
		ItemFormMetadataBean itemFormMetadataBean = getItemFormMetadata(itemBean, version);
		Integer sectionId = itemFormMetadataBean.getSectionId();

		SectionBean sectionBean = getSectionBean(sectionId);

		expression = "/" + version.getOid() + "/" + sectionBean.getLabel().replace(" ", "_") + "/" + itemGroupBean.getOid() + "/" + itemOid
				+ " " + operator + " " + value;
		logger.info(expression);

		return expression;
	}

	public RuleActionPropertyDao getRuleActionPropertyDao() {
		return ruleActionPropertyDao;
	}

	public void setRuleActionPropertyDao(RuleActionPropertyDao ruleActionPropertyDao) {
		this.ruleActionPropertyDao = ruleActionPropertyDao;
	}

}