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
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
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

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class OpenRosaXmlGenerator {

	private XMLContext xmlContext = null;
	private DataSource dataSource = null;
	protected final Logger log = LoggerFactory.getLogger(OpenRosaXmlGenerator.class);

	public OpenRosaXmlGenerator(CoreResources core, DataSource dataSource) throws Exception {
		this.dataSource = dataSource;

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

	private ArrayList<ItemGroupBean> getItemGroupBeans(SectionBean section) throws Exception {
		ArrayList<ItemGroupBean> itemGroupBeans = null;

		ItemGroupDAO itemGroupDAO = new ItemGroupDAO(dataSource);
		itemGroupBeans = (ArrayList<ItemGroupBean>) itemGroupDAO.findGroupBySectionId(section.getId());

		return itemGroupBeans;
	}

	private ItemFormMetadataBean getItemFormMetadata(ItemBean item, CRFVersionBean crfVersion) throws Exception {
		ItemFormMetadataBean itemFormMetadataBean = null;

		ItemFormMetadataDAO itemFormMetadataDAO = new ItemFormMetadataDAO(dataSource);
		itemFormMetadataBean = itemFormMetadataDAO.findByItemIdAndCRFVersionId(item.getId(), crfVersion.getId());

		return itemFormMetadataBean;
	}

	private ItemGroupMetadataBean getItemGroupMetadata(ItemGroupBean itemGroupBean, CRFVersionBean crfVersion, SectionBean section)
			throws Exception {
		ArrayList<ItemGroupMetadataBean> itemGroupMetadataBean = null;

		ItemGroupMetadataDAO itemGroupMetadataDAO = new ItemGroupMetadataDAO(dataSource);
		itemGroupMetadataBean = (ArrayList<ItemGroupMetadataBean>) itemGroupMetadataDAO.findMetaByGroupAndSection(itemGroupBean.getId(),
				crfVersion.getId(), section.getId());

		return itemGroupMetadataBean.get(0);
	}

	private void mapBeansToDTO(Html html, CRFBean crf, CRFVersionBean crfVersion, ArrayList<SectionBean> crfSections) throws Exception {
		ItemFormMetadataBean itemFormMetadataBean = null;
		Body body = html.getBody();
		body.setCssClass("pages");
		ArrayList<Group> groups = new ArrayList<Group>();
		ArrayList<Bind> bindList = new ArrayList<Bind>();
		WidgetFactory factory = new WidgetFactory(crfVersion);
		html.getHead().setTitle(crf.getName());

		for (SectionBean section : crfSections) {
			ArrayList<ItemGroupBean> itemGroupBeans = getItemGroupBeans(section);

			/*
			 * Label sectionLabel = new Label();
			 * sectionLabel.setLabel(section.getLabel() + " ------ ");
			 * group.setLabel(sectionLabel);
			 */
			for (ItemGroupBean itemGroupBean : itemGroupBeans) {
				Group group = new Group();
				Repeat repeat = new Repeat();
        		group.setUsercontrol(new ArrayList<UserControl>());
				repeat.setUsercontrol(new ArrayList<UserControl>());
				Label repeatLabel = new Label();
          
				group.setAppearance("field-list");
				Label groupLabel = new Label();
				groupLabel.setLabel(section.getLabel());
				group.setLabel(groupLabel);
				boolean isGroupRepeating = getItemGroupMetadata(itemGroupBean, crfVersion, section).isRepeatingGroup();

				int groupRepeatNum = getItemGroupMetadata(itemGroupBean, crfVersion, section).getRepeatNum();
				int groupMaxRepeatNum = getItemGroupMetadata(itemGroupBean, crfVersion, section).getRepeatMax();
				
				String nodeset = "/" + crfVersion.getOid() + "/" + itemGroupBean.getOid();
				// String count =String.valueOf(groupRepeatNum);
				repeat.setNodeset(nodeset);
                repeat.setLabel(repeatLabel);
				repeatLabel.setLabel( itemGroupBean.getName());
				ItemDAO itemdao = new ItemDAO(dataSource);

				ArrayList<ItemBean> items = (ArrayList<ItemBean>) itemdao.findAllItemsByGroupIdOrdered(itemGroupBean.getId(),
						crfVersion.getId());
				for (ItemBean item : items) {

					itemFormMetadataBean = getItemFormMetadata(item, crfVersion);
					int responseTypeId = itemFormMetadataBean.getResponseSet().getResponseTypeId();
					boolean isItemRequred = itemFormMetadataBean.isRequired();
					int itemGroupRepeatNumber = 1;

					Widget widget = factory.getWidget(item, responseTypeId, itemGroupBean, itemFormMetadataBean, itemGroupRepeatNumber,
							isItemRequred, isGroupRepeating);
					if (widget != null) {
						bindList.add(widget.getBinding());

						if (isGroupRepeating) {
							// repeat.setCount(count);
							repeat.getUsercontrol().add(widget.getUserControl());
						} else {
							group.getUsercontrol().add(widget.getUserControl());
						}

					} else {
						log.debug("Unsupported datatype encountered while loading PForm (" + item.getDataType().getName() + "). Skipping.");
					}
				} // item
				if (isGroupRepeating) 
					group.setRepeat(repeat);

				groups.add(group);
				body.setGroup(groups);
				html.getHead().getModel().setBind(bindList);

			} // multi group
		} // section

	} // method

	private String buildInstance(Model model, CRFVersionBean crfVersion, ArrayList<SectionBean> crfSections) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder build = docFactory.newDocumentBuilder();
		Document doc = build.newDocument();
		Element root = doc.createElement(crfVersion.getOid());
		root.setAttribute("id", crfVersion.getOid());
		doc.appendChild(root);
		for (SectionBean section : crfSections) {
			ArrayList<ItemGroupBean> itemGroupBeans = getItemGroupBeans(section);
			for (ItemGroupBean itemGroupBean : itemGroupBeans) {

				int groupRepeatNum = getItemGroupMetadata(itemGroupBean, crfVersion, section).getRepeatNum();
				// for (int x = 0; x < groupRepeatNum; x = x + 1) {
				Element groupOid = doc.createElement(itemGroupBean.getOid());
				// groupOid.setAttribute("ordinal", String.valueOf(1));
				root.appendChild(groupOid);
				ItemDAO itemdao = new ItemDAO(dataSource);
				ArrayList<ItemBean> items = (ArrayList<ItemBean>) itemdao.findAllItemsByGroupIdOrdered(itemGroupBean.getId(),
						crfVersion.getId());

				for (ItemBean item : items) {
					Element question = doc.createElement(item.getOid());
					groupOid.appendChild(question);
				} // end of item

				// } // end of repeating group number
			} // end of group

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
}