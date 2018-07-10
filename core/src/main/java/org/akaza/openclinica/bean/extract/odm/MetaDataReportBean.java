/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.BasicDefinitionsBean;
import org.akaza.openclinica.bean.odmbeans.CodeListBean;
import org.akaza.openclinica.bean.odmbeans.CodeListItemBean;
import org.akaza.openclinica.bean.odmbeans.ConfigurationParameters;
import org.akaza.openclinica.bean.odmbeans.ElementRefBean;
import org.akaza.openclinica.bean.odmbeans.EventDefinitionDetailsBean;
import org.akaza.openclinica.bean.odmbeans.FormDefBean;
import org.akaza.openclinica.bean.odmbeans.FormDetailsBean;
import org.akaza.openclinica.bean.odmbeans.GlobalVariablesBean;
import org.akaza.openclinica.bean.odmbeans.ItemDefBean;
import org.akaza.openclinica.bean.odmbeans.ItemDetailsBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupDefBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupDetailsBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupRepeatBean;
import org.akaza.openclinica.bean.odmbeans.ItemPresentInFormBean;
import org.akaza.openclinica.bean.odmbeans.ItemResponseBean;
import org.akaza.openclinica.bean.odmbeans.MeasurementUnitBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionBean;
import org.akaza.openclinica.bean.odmbeans.MultiSelectListBean;
import org.akaza.openclinica.bean.odmbeans.MultiSelectListItemBean;
import org.akaza.openclinica.bean.odmbeans.OdmStudyBean;
import org.akaza.openclinica.bean.odmbeans.PresentInEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.PresentInFormBean;
import org.akaza.openclinica.bean.odmbeans.RangeCheckBean;
import org.akaza.openclinica.bean.odmbeans.SimpleConditionalDisplayBean;
import org.akaza.openclinica.bean.odmbeans.StudyEventDefBean;
import org.akaza.openclinica.bean.odmbeans.StudyGroupClassListBean;
import org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean;
import org.akaza.openclinica.bean.odmbeans.SymbolBean;
import org.akaza.openclinica.bean.odmbeans.TranslatedTextBean;
import org.akaza.openclinica.bean.service.StudyParameterConfig;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.rule.RulesPostImportContainer;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.odmExport.MetadataUnit;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.castor.xml.XMLConfiguration;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLContext;

/**
 * Create ODM XML Study Element for a study.
 * 
 * @author ywang (May, 2008)
 */

public class MetaDataReportBean extends OdmXmlReportBean {
    private OdmStudyBean odmstudy;
    private LinkedHashMap<String, OdmStudyBean> odmStudyMap;
    private CoreResources coreResources;

    public MetaDataReportBean(OdmStudyBean odmstudy) {
        super();
        this.odmstudy = odmstudy;
    }

    public MetaDataReportBean(OdmStudyBean odmstudy, CoreResources coreResources) {
        super();
        this.odmstudy = odmstudy;
        this.coreResources = coreResources;
    }

    public MetaDataReportBean(LinkedHashMap<String, OdmStudyBean> odmStudyMap, CoreResources coreResources) {
        super();
        this.odmStudyMap = odmStudyMap;
        this.coreResources = coreResources;
    }

    public MetaDataReportBean() {
        // TODO Auto-generated constructor stub
    }

    private static String nls = System.getProperty("line.separator");

    public void createChunkedOdmXml(boolean isDataset) {
        this.addHeading();
        this.addRootStartLine();
        Iterator<OdmStudyBean> itm = this.odmStudyMap.values().iterator();
        while (itm.hasNext()) {
            OdmStudyBean s = itm.next();
            odmstudy = s;
            this.addNodeStudy(isDataset);
        }
    }

    public void addNodeStudy(boolean isDataset) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        xml.append(indent + "<Study OID=\"" + StringEscapeUtils.escapeXml(odmstudy.getOid()) + "\">");
        xml.append(nls);
        addStudyGlobalVariables();
        addBasicDefinitions();
        addStudyMetaDataVersion(isDataset);
        xml.append(indent + "</Study>");
        xml.append(nls);
    }

    private String handleLoadCastor(RulesPostImportContainer rpic) {

        try {
            // Create Mapping
            Mapping mapping = new Mapping();
            mapping.loadMapping(getCoreResources().getURL("mappingMarshallerMetadata.xml"));
            // Create XMLContext
            XMLContext xmlContext = new XMLContext();
            xmlContext.setProperty(XMLConfiguration.NAMESPACES, "true");
            xmlContext.addMapping(mapping);

            StringWriter writer = new StringWriter();
            Marshaller marshaller = xmlContext.createMarshaller();
            // marshaller.setNamespaceMapping("castor", "http://castor.org/sample/mapping/");
            marshaller.setWriter(writer);
            marshaller.marshal(rpic);
            String result = writer.toString();
            String newResult = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            return newResult;

        } catch (FileNotFoundException ex) {
            throw new OpenClinicaSystemException(ex.getMessage(), ex.getCause());
        } catch (IOException ex) {
            throw new OpenClinicaSystemException(ex.getMessage(), ex.getCause());
        } catch (MarshalException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (ValidationException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (MappingException e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        } catch (Exception e) {
            throw new OpenClinicaSystemException(e.getMessage(), e.getCause());
        }
    }

    public void addNodeRulesData(MetaDataVersionBean a) {

        RulesPostImportContainer rpic = new RulesPostImportContainer();
        rpic.populate(a.getRuleSetRules());

        if (rpic.getRuleSets() != null && rpic.getRuleSets().size() > 0) {
            StringBuffer xml = this.getXmlOutput();
            xml.append(handleLoadCastor(rpic));
        }
    }

    public void addStudyGlobalVariables() {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String currentIndent = indent + indent;
        GlobalVariablesBean gv = odmstudy.getGlobalVariables();
        xml.append(currentIndent + "<GlobalVariables>");
        xml.append(nls);
        xml.append(currentIndent + indent + "<StudyName>" + StringEscapeUtils.escapeXml(gv.getStudyName()) + "</StudyName>");
        xml.append(nls);
        xml.append(currentIndent + indent + "<StudyDescription>");
        xml.append(nls);
        xml.append(currentIndent + indent + indent + StringEscapeUtils.escapeXml(gv.getStudyDescription()));
        xml.append(nls);
        xml.append(currentIndent + indent + "</StudyDescription>");
        xml.append(nls);
        xml.append(currentIndent + indent + "<ProtocolName>" + StringEscapeUtils.escapeXml(gv.getProtocolName()) + "</ProtocolName>");
        xml.append(nls);
        xml.append(currentIndent + "</GlobalVariables>");
        xml.append(nls);
    }

    public void addBasicDefinitions() {
        BasicDefinitionsBean bd = odmstudy.getBasicDefinitions();
        addMeasurementUnits(bd);
    }

    public void addMeasurementUnits(BasicDefinitionsBean bd) {
        ArrayList<MeasurementUnitBean> units = bd.getMeasurementUnits();
        if (units.size() > 0) {
            StringBuffer xml = this.getXmlOutput();
            String indent = this.getIndent();
            String currentIndent = indent + indent;
            xml.append(currentIndent + "<BasicDefinitions>");
            xml.append(nls);
            for (MeasurementUnitBean unit : units) {
                xml.append(currentIndent + indent + "<MeasurementUnit OID=\"" + StringEscapeUtils.escapeXml(unit.getOid()) + "\" Name=\""
                        + StringEscapeUtils.escapeXml(unit.getName()) + "\">");
                xml.append(nls);
                addSymbol(unit, currentIndent + indent + indent);
                xml.append(currentIndent + indent + "</MeasurementUnit>");
                xml.append(nls);
            }
            xml.append(currentIndent + "</BasicDefinitions>");
            xml.append(nls);
        }
    }

    public void addSymbol(MeasurementUnitBean unit, String currentIndent) {
        SymbolBean symbol = unit.getSymbol();
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<TranslatedTextBean> texts = symbol.getTranslatedText();
        xml.append(currentIndent + "<Symbol>");
        xml.append(nls);
        for (TranslatedTextBean text : symbol.getTranslatedText()) {

            xml.append(currentIndent + indent + "<TranslatedText>" + StringEscapeUtils.escapeXml(text.getText()) + "</TranslatedText>");
            xml.append(nls);
        }
        xml.append(currentIndent + "</Symbol>");
        xml.append(nls);
    }

    public void addStudyMetaDataVersion(boolean isDataset) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String currentIndent = indent + indent;
        String ODMVersion = this.getODMVersion();
        MetaDataVersionBean meta = odmstudy.getMetaDataVersion();

        xml.append(currentIndent + "<MetaDataVersion OID=\"" + StringEscapeUtils.escapeXml(meta.getOid()) + "\" Name=\""
                + StringEscapeUtils.escapeXml(meta.getName()) + "\">");
        xml.append(nls);
        if (meta.getInclude() != null) {
            String pmOid = meta.getInclude().getMetaDataVersionOID();
            if (pmOid != null && pmOid.length() > 0) {
                xml.append(currentIndent + indent);
                String psOid = meta.getInclude().getStudyOID();
                if (psOid != null && psOid.length() > 0) {
                    xml.append("<Include StudyOID =\"" + StringEscapeUtils.escapeXml(psOid) + "\"");
                } else {
                    xml.append("<Include StudyOID =\"" + StringEscapeUtils.escapeXml(odmstudy.getOid()) + "\"");
                }
                xml.append(" MetaDataVersionOID=\"" + StringEscapeUtils.escapeXml(pmOid) + "\"/>");
                xml.append(nls);
            }
        }

        //
        addProtocol(currentIndent + indent);
        boolean isStudy = meta.getStudy().getParentStudyId() > 0 ? false : true;
        if (meta.getStudyEventDefs().size() > 0) {
            addStudyEventDef(isStudy, currentIndent + indent);
            if (meta.getItemGroupDefs().size() > 0) {
                addFormDef(isStudy, currentIndent + indent);
                addItemGroupDef(isStudy, currentIndent + indent);
                addItemDef(isStudy, currentIndent + indent);
                addCodeList(currentIndent + indent);
                if ("oc1.2".equalsIgnoreCase(ODMVersion)) {
                    addMultiSelectList(currentIndent + indent);
                    addStudyGroupClassList(currentIndent + indent);
                } else if ("oc1.3".equalsIgnoreCase(ODMVersion)) {
                    addMultiSelectList(currentIndent + indent);
                    addStudyGroupClassList(currentIndent + indent);
                    if (meta.getStudy().getParentStudyId() > 0) {
                        this.addStudyDetails(currentIndent + indent);
                    } else {
                        this.addStudyDetails(currentIndent + indent);
                    }
                }
            }
        } else if (odmstudy.getOid().equals(MetadataUnit.FAKE_STUDY_OID)) {
            addFormDef(isStudy, currentIndent + indent);
            addItemGroupDef(isStudy, currentIndent + indent);
            addItemDef(isStudy, currentIndent + indent);
        }

        addNodeRulesData(meta);

        xml.append(currentIndent + "</MetaDataVersion>");
        xml.append(nls);
    }

    public void addProtocol(String currentIndent) {
        // The protocol lists the kinds of study events that can occur within a
        // specific version of a Study.
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        xml.append(currentIndent + "<Protocol>");
        xml.append(nls);
        for (ElementRefBean seref : odmstudy.getMetaDataVersion().getProtocol().getStudyEventRefs()) {
            // At this point, Mandatory has been set yes
            xml.append(currentIndent + indent + "<StudyEventRef StudyEventOID=\"" + StringEscapeUtils.escapeXml(seref.getElementDefOID()) + "\" OrderNumber=\""
                    + seref.getOrderNumber() + "\" Mandatory=\"Yes\"/>");
            xml.append(nls);
        }
        xml.append(currentIndent + "</Protocol>");
        xml.append(nls);
    }

    public void addStudyEventDef(boolean isStudy, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<StudyEventDefBean> seds = (ArrayList<StudyEventDefBean>) odmstudy.getMetaDataVersion().getStudyEventDefs();
        for (StudyEventDefBean sed : seds) {
            xml.append(currentIndent + "<StudyEventDef OID=\"" + StringEscapeUtils.escapeXml(sed.getOid()) + "\"  Name=\""
                    + StringEscapeUtils.escapeXml(sed.getName()) + "\" Repeating=\"" + sed.getRepeating() + "\" Type=\"" + sed.getType()
                    + "\" OpenClinica:EventType=\"" + sed.getType() + "\" OpenClinica:Status=\"" + sed.getStatus() + "\">");
            xml.append(nls);
            ArrayList<ElementRefBean> formRefs = (ArrayList<ElementRefBean>) sed.getFormRefs();
            for (ElementRefBean formRef : formRefs) {
                xml.append(currentIndent + indent + "<FormRef FormOID=\"" + StringEscapeUtils.escapeXml(formRef.getElementDefOID()) + "\" Mandatory=\""
                        + formRef.getMandatory() + "\" OpenClinica:Status=\"" + formRef.getStatus() + "\">");
                xml.append(nls);
                ConfigurationParameters conf = formRef.getConfigurationParameters();
                if (conf != null) {
                    xml.append(currentIndent + indent + indent + "<OpenClinica:ConfigurationParameters HideCRF=\"" + (conf.isHiddenCrf() ? "Yes" : "No")
                            + "\" ParticipantForm=\"" + (conf.isParticipantForm() ? "Yes" : "No") + "\" AllowAnonymousSubmission=\""
                            + (conf.isAllowAnynymousSubmission() ? "Yes" : "No") + "\" SubmissionUrl=\"" + conf.getSubmissionUrl() + "\" Offline=\""
                            + (conf.isOffline() ? "Yes" : "No") + "\"/>");
                    xml.append(nls);
                    ArrayList<ElementRefBean> formLayoutRefs = (ArrayList<ElementRefBean>) formRef.getFormLayoutRefs();
                    for (ElementRefBean formLayoutRef : formLayoutRefs) {
                        xml.append(currentIndent + indent + indent + "<OpenClinica:FormLayoutRef OID=\"" + StringEscapeUtils.escapeXml(formLayoutRef.getName())
                                + "\" IsDefaultVersion=\"" + (formLayoutRef.isDefaultVersion() ? "Yes" : "No") + "\"/>");
                        xml.append(nls);
                    }
                }
                xml.append(currentIndent + indent + "</FormRef>");
                xml.append(nls);

            }
            // add EventDefinitionDetails for oc1.3
            // MR=416
            if ("oc1.3".equals(this.getODMVersion())) {
                this.addEventDefinitionDetails(sed.getEventDefinitionDetais(), currentIndent + indent);
            }
            xml.append(currentIndent + "</StudyEventDef>");
            xml.append(nls);
        }
    }

    public void addFormDef(boolean isStudy, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<FormDefBean> forms = (ArrayList<FormDefBean>) odmstudy.getMetaDataVersion().getFormDefs();
        for (FormDefBean form : forms) {
            xml.append(currentIndent + "<FormDef OID=\"" + StringEscapeUtils.escapeXml(form.getOid()) + "\" Name=\""
                    + StringEscapeUtils.escapeXml(form.getName()) + "\" Repeating=\"" + form.getRepeating() + "\">");
            xml.append(nls);
            ArrayList<ElementRefBean> igs = (ArrayList<ElementRefBean>) form.getItemGroupRefs();
            for (ElementRefBean ig : igs) {
                xml.append(currentIndent + indent + "<ItemGroupRef ItemGroupOID=\"" + StringEscapeUtils.escapeXml(ig.getElementDefOID()) + "\" Mandatory=\""
                        + ig.getMandatory() + "\"/>");
                xml.append(nls);
            }
            ArrayList<ElementRefBean> formLayoutRefs = (ArrayList<ElementRefBean>) form.getFormLayoutRefs();
            for (ElementRefBean formLayoutRef : formLayoutRefs) {

                xml.append(currentIndent + indent + "<OpenClinica:FormLayoutDef OID=\"" + StringEscapeUtils.escapeXml(formLayoutRef.getName()) + "\">");
                xml.append(nls);
                xml.append(currentIndent + indent + "<OpenClinica:URL>" + StringEscapeUtils.escapeXml(formLayoutRef.getUrl()) + "</OpenClinica:URL>");
                xml.append(nls);
                xml.append(currentIndent + indent + "</OpenClinica:FormLayoutDef>");
                xml.append(nls);
            }

            // add FormDetails for oc1.3
            if ("oc1.3".equals(this.getODMVersion())) {
                this.addFormDetails(form.getFormDetails(), currentIndent + indent);
            }
            xml.append(currentIndent + "</FormDef>");
            xml.append(nls);
        }
    }

    /**
     * The form specific formdef tag
     * 
     * @param isStudy
     * @param currentIndent
     * @param formOID
     */
    public void addFormDef(boolean isStudy, String currentIndent, String formOID) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<FormDefBean> forms = (ArrayList<FormDefBean>) odmstudy.getMetaDataVersion().getFormDefs();
        for (FormDefBean form : forms) {
            xml.append(currentIndent + "<FormDef OID=\"" + StringEscapeUtils.escapeXml(form.getOid()) + "\" Name=\""
                    + StringEscapeUtils.escapeXml(form.getName()) + "\" Repeating=\"" + form.getRepeating() + "\">");
            xml.append(nls);
            ArrayList<ElementRefBean> igs = (ArrayList<ElementRefBean>) form.getItemGroupRefs();
            for (ElementRefBean ig : igs) {
                xml.append(currentIndent + indent + "<ItemGroupRef ItemGroupOID=\"" + StringEscapeUtils.escapeXml(ig.getElementDefOID()) + "\" Mandatory=\""
                        + ig.getMandatory() + "\"/>");
                xml.append(nls);
            }
            // add FormDetails for oc1.3
            if ("oc1.3".equals(this.getODMVersion()) && isStudy) {
                // this.addFormDetails(form.getFormDetails(), currentIndent + indent);
            }
            xml.append(currentIndent + "</FormDef>");
            xml.append(nls);
        }
    }

    public void addItemGroupDef(boolean isStudy, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<ItemGroupDefBean> igs = (ArrayList<ItemGroupDefBean>) odmstudy.getMetaDataVersion().getItemGroupDefs();
        for (ItemGroupDefBean ig : igs) {
            if (ig.getComment().length() > 0) {
                xml.append(currentIndent + "<ItemGroupDef OID=\"" + StringEscapeUtils.escapeXml(ig.getOid()) + "\" Name=\""
                        + StringEscapeUtils.escapeXml(ig.getName()) + "\" Repeating=\"" + ig.getRepeating() + "\" SASDatasetName=\""
                        + this.getSasNameValidator().getValidName(ig.getPreSASDatasetName()) + "\" Comment=\"" + StringEscapeUtils.escapeXml(ig.getComment())
                        + "\">");
            } else {
                xml.append(currentIndent + "<ItemGroupDef OID=\"" + StringEscapeUtils.escapeXml(ig.getOid()) + "\" Name=\""
                        + StringEscapeUtils.escapeXml(ig.getName()) + "\" Repeating=\"" + ig.getRepeating() + "\" SASDatasetName=\""
                        + this.getSasNameValidator().getValidName(ig.getPreSASDatasetName()) + "\">");
            }
            xml.append(nls);
            ArrayList<ElementRefBean> items = (ArrayList<ElementRefBean>) ig.getItemRefs();
            for (ElementRefBean item : items) {
                xml.append(currentIndent + indent + "<ItemRef ItemOID=\"" + StringEscapeUtils.escapeXml(item.getElementDefOID()) + "\" OrderNumber=\""
                        + item.getOrderNumber() + "\" Mandatory=\"" + item.getMandatory() + "\"/>");
                xml.append(nls);
            }
            // add ItemGroupDetails for oc1.3
            if ("oc1.3".equals(this.getODMVersion())) {
                // this.addItemGroupDetails(ig.getItemGroupDetails(), currentIndent + indent);
            }
            xml.append(currentIndent + "</ItemGroupDef>");
            xml.append(nls);
        }
    }

    public void addItemDef(boolean isStudy, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<ItemDefBean> items = (ArrayList<ItemDefBean>) odmstudy.getMetaDataVersion().getItemDefs();
        String ODMVersion = this.getODMVersion();
        for (ItemDefBean item : items) {
            xml.append(currentIndent + "<ItemDef OID=\"" + StringEscapeUtils.escapeXml(item.getOid()) + "\" Name=\""
                    + StringEscapeUtils.escapeXml(item.getName()) + "\" DataType=\"" + item.getDateType() + "\"");
            int len = item.getLength();
            if (len > 0) {
                xml.append(" Length=\"" + len + "\"");
            }
            len = item.getSignificantDigits();
            if (len > 0) {
                xml.append(" SignificantDigits=\"" + len + "\"");
            }
            xml.append(" SASFieldName=\"" + this.getSasNameValidator().getValidName(item.getPreSASFieldName()) + "\"");
            if (item.getComment().length() > 0) {
                xml.append(" Comment=\"" + StringEscapeUtils.escapeXml(item.getComment()) + "\"");
            }
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                xml.append(" OpenClinica:FormOID=\"" + item.getFormOIDs() + "\"");
            }
            boolean hasNode = false;
            // add question
            TranslatedTextBean t = item.getQuestion().getQuestion();
            if (t != null && t.getText() != null && t.getText().length() > 0) {
                if (!hasNode) {
                    hasNode = true;
                    xml.append(">");
                    xml.append(nls);
                }
                if ("oc1.3".equalsIgnoreCase(ODMVersion)) {
                    String qn = item.getQuestion().getQuestionNumber();
                    if (qn != null && qn.length() > 0) {
                        xml.append(currentIndent + indent + "<Question OpenClinica:QuestionNumber=\"" + item.getQuestion().getQuestionNumber() + "\">");
                    } else {
                        xml.append(currentIndent + indent + "<Question>");
                    }
                } else {
                    xml.append(currentIndent + indent + "<Question>");
                }
                xml.append(nls);
                xml.append(currentIndent + indent + indent + "<TranslatedText>");
                xml.append(nls);
                xml.append(currentIndent + indent + indent + StringEscapeUtils.escapeXml(t.getText()));
                xml.append(nls);
                xml.append(currentIndent + indent + indent + "</TranslatedText>");
                xml.append(nls);
                xml.append(currentIndent + indent + "</Question>");
                xml.append(nls);
            }
            // add MeasurementUnitRef
            String muRefOid = item.getMeasurementUnitRef().getElementDefOID();
            if (muRefOid != null && muRefOid.length() > 0) {
                if (!hasNode) {
                    hasNode = true;
                    xml.append(">");
                    xml.append(nls);
                }
                xml.append(currentIndent + indent + this.measurementUnitRefString(muRefOid));
                xml.append(nls);
            }
            // add RangeCheck
            if (item.getRangeCheck() != null) {
                ArrayList<RangeCheckBean> rcs = (ArrayList<RangeCheckBean>) item.getRangeCheck();
                for (RangeCheckBean rc : rcs) {
                    if (rc.getComparator().length() > 0) {
                        if (!hasNode) {
                            hasNode = true;
                            xml.append(">");
                            xml.append(nls);
                        }
                        xml.append(currentIndent + indent + "<RangeCheck Comparator=\"" + StringEscapeUtils.escapeXml(rc.getComparator()) + "\" SoftHard=\""
                                + rc.getSoftHard() + "\">");
                        xml.append(nls);
                        xml.append(currentIndent + indent + indent + "<CheckValue>" + StringEscapeUtils.escapeXml(rc.getCheckValue()) + "</CheckValue>");
                        xml.append(nls);
                        // at this time, only one measurementUnit has been
                        // supported
                        if (muRefOid != null && muRefOid.length() > 0) {
                            xml.append(currentIndent + indent + indent + this.measurementUnitRefString(muRefOid));
                            xml.append(nls);
                        }
                        xml.append(currentIndent + indent + indent + "<ErrorMessage><TranslatedText>"
                                + StringEscapeUtils.escapeXml(rc.getErrorMessage().getText()) + "</TranslatedText></ErrorMessage>");
                        xml.append(nls);
                        xml.append(currentIndent + indent + "</RangeCheck>");
                        xml.append(nls);
                    }
                }
            }
            // add CodeListRef
            String clOid = item.getCodeListOID();
            if (clOid != null && clOid.length() > 0) {
                if (!hasNode) {
                    hasNode = true;
                    xml.append(">");
                    xml.append(nls);
                }
                xml.append(currentIndent + indent + "<CodeListRef CodeListOID=\"" + StringEscapeUtils.escapeXml(clOid) + "\"/>");
                xml.append(nls);
            }
            // add MultiSelectListRef
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                if (item.getMultiSelectListRef() != null) {
                    String mslOid = item.getMultiSelectListRef().getElementDefOID();
                    if (mslOid != null && mslOid.length() > 0) {
                        if (!hasNode) {
                            hasNode = true;
                            xml.append(">");
                            xml.append(nls);
                        }
                        // xml.append(currentIndent + indent +
                        // "<OpenClinica:MultiSelectListRef OpenClinica:MultiSelectListID=\""
                        xml.append(
                                currentIndent + indent + "<OpenClinica:MultiSelectListRef MultiSelectListID=\"" + StringEscapeUtils.escapeXml(mslOid) + "\"/>");
                        xml.append(nls);
                    }
                }
            }
            // add ItemDetails for oc1.3
            if ("oc1.3".equals(ODMVersion)) {
                if (!hasNode) {
                    hasNode = true;
                    xml.append(">");
                    xml.append(nls);
                }
                // this.addItemDetails(item.getItemDetails(), currentIndent + indent);
            }
            if (hasNode) {
                xml.append(currentIndent + "</ItemDef>");
                xml.append(nls);
                hasNode = false;
            } else {
                xml.append("/>");
                xml.append(nls);
            }
        }
    }

    public void addCodeList(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        if (odmstudy.getMetaDataVersion().getCodeLists() != null) {
            ArrayList<CodeListBean> cls = (ArrayList<CodeListBean>) odmstudy.getMetaDataVersion().getCodeLists();
            if (cls.size() > 0) {
                for (CodeListBean cl : cls) {
                    boolean isString = cl.getDataType().equalsIgnoreCase("text") ? true : false;
                    xml.append(currentIndent + "<CodeList OID=\"" + StringEscapeUtils.escapeXml(cl.getOid()) + "\" Name=\""
                            + StringEscapeUtils.escapeXml(cl.getName()) + "\" DataType=\"" + cl.getDataType() + "\" SASFormatName=\""
                            + this.getSasFormValidator().getValidSASFormatName(cl.getName(), isString) + "\">");
                    xml.append(nls);
                    ArrayList<CodeListItemBean> clis = (ArrayList<CodeListItemBean>) cl.getCodeListItems();
                    if (clis != null && clis.size() > 0) {
                        for (CodeListItemBean cli : clis) {
                            xml.append(currentIndent + indent + "<CodeListItem CodedValue=\"" + StringEscapeUtils.escapeXml(cli.getCodedVale()) + "\">");
                            xml.append(nls);
                            xml.append(currentIndent + indent + indent + "<Decode>");
                            xml.append(nls);
                            TranslatedTextBean tt = cli.getDecode();
                            if (tt.getXmlLang().length() > 0) {
                                xml.append(currentIndent + indent + indent + indent + "<TranslatedText xml:lang=\"" + tt.getXmlLang() + "\">"
                                        + StringEscapeUtils.escapeXml(cli.getDecode().getText()) + "</TranslatedText>");
                            } else {
                                xml.append(currentIndent + indent + indent + indent + "<TranslatedText>"
                                        + StringEscapeUtils.escapeXml(cli.getDecode().getText()) + "</TranslatedText>");
                            }
                            xml.append(nls);
                            xml.append(currentIndent + indent + indent + "</Decode>");
                            xml.append(nls);
                            xml.append(currentIndent + indent + "</CodeListItem>");
                            xml.append(nls);
                        }
                    }
                    xml.append(currentIndent + "</CodeList>");
                    xml.append(nls);
                }
            }
        }
    }

    public void addMultiSelectList(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<MultiSelectListBean> lists = (ArrayList<MultiSelectListBean>) odmstudy.getMetaDataVersion().getMultiSelectLists();
        if (lists != null) {
            if (lists.size() > 0) {
                for (MultiSelectListBean l : lists) {
                    xml.append(currentIndent + "<OpenClinica:MultiSelectList ID=\"" + StringEscapeUtils.escapeXml(l.getOid()) + "\" ");
                    if (l.getName() != null) {
                        xml.append("Name=\"" + StringEscapeUtils.escapeXml(l.getName()) + "\" ");
                    }
                    if (l.getDataType() != null) {
                        xml.append("DataType=\"" + l.getDataType() + "\" ");
                    }
                    if (l.getActualDataType() != null) {
                        xml.append("ActualDataType=\"" + StringEscapeUtils.escapeXml(l.getActualDataType()) + "\" ");
                    }
                    xml.append(">");
                    xml.append(nls);

                    ArrayList<MultiSelectListItemBean> mslis = (ArrayList<MultiSelectListItemBean>) l.getMultiSelectListItems();
                    if (mslis != null && mslis.size() > 0) {
                        for (MultiSelectListItemBean msli : mslis) {
                            xml.append(currentIndent + indent + "<OpenClinica:MultiSelectListItem CodedOptionValue=\""
                                    + StringEscapeUtils.escapeXml(msli.getCodedOptionValue()) + "\">");
                            xml.append(nls);
                            xml.append(currentIndent + indent + indent + "<Decode>");
                            xml.append(nls);
                            TranslatedTextBean tt = msli.getDecode();
                            if (tt.getXmlLang().length() > 0) {
                                xml.append(currentIndent + indent + indent + indent + "<TranslatedText xml:lang=\"" + tt.getXmlLang() + "\">"
                                        + StringEscapeUtils.escapeXml(msli.getDecode().getText()) + "</TranslatedText>");
                            } else {
                                xml.append(currentIndent + indent + indent + indent + "<TranslatedText>"
                                        + StringEscapeUtils.escapeXml(msli.getDecode().getText()) + "</TranslatedText>");
                            }
                            xml.append(nls);
                            xml.append(currentIndent + indent + indent + "</Decode>");
                            xml.append(nls);
                            xml.append(currentIndent + indent + "</OpenClinica:MultiSelectListItem>");
                            xml.append(nls);
                        }
                    }

                    xml.append(currentIndent + "</OpenClinica:MultiSelectList>");
                    xml.append(nls);
                }
            }
        }
    }

    public void addStudyDetails(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        StudyBean study = odmstudy.getMetaDataVersion().getStudy();
        String temp = "";
        if (study.getId() > 0) {
            xml.append(currentIndent + "<OpenClinica:StudyDetails StudyOID=\"" + StringEscapeUtils.escapeXml(study.getOid()) + "\"");

            if (study.getParentStudyId() > 0) {
                temp = study.getName();
                if (temp != null && temp.length() > 0) {
                    xml.append(" SiteName=\"" + StringEscapeUtils.escapeXml(temp) + "\"");

                }
                temp = study.getParentStudyName();
                if (temp != null && temp.length() > 0) {
                    xml.append(" ParentStudyName=\"" + StringEscapeUtils.escapeXml(temp) + "\"");
                }
                //
            }
            xml.append(">");
            xml.append(nls);
            xml.append(currentIndent + indent + "<OpenClinica:StudyDescriptionAndStatus");
            temp = study.getOfficialTitle();
            if (temp != null && temp.length() > 0) {
                xml.append(" OfficialTitle=\"" + StringEscapeUtils.escapeXml(temp) + "\"");
            }
            temp = study.getSecondaryIdentifier();
            if (temp != null && temp.length() > 0) {
                xml.append(" SecondaryIDs=\"" + StringEscapeUtils.escapeXml(temp) + "\"");
            }
            xml.append(" DateCreated=\"" + new SimpleDateFormat("yyyy-MM-dd").format(study.getCreatedDate()) + "\"");
            if (study.getDatePlannedStart() instanceof java.util.Date) {
                xml.append(" StartDate=\"" + new SimpleDateFormat("yyyy-MM-dd").format(study.getDatePlannedStart()) + "\"");
            }
            if (study.getDatePlannedEnd() != null) {
                xml.append(" StudyCompletionDate=\"" + new SimpleDateFormat("yyyy-MM-dd").format(study.getDatePlannedEnd()) + "\"");
            }
            xml.append(">");
            xml.append(nls);
            temp = study.getStatus() == null ? "" : study.getStatus().getName();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:StudySytemStatus>" + StringEscapeUtils.escapeXml(temp)
                        + "</OpenClinica:StudySytemStatus>");
                xml.append(nls);
            }
            xml.append(currentIndent + indent + indent + "<OpenClinica:PrincipalInvestigator>" + StringEscapeUtils.escapeXml(study.getPrincipalInvestigator())
                    + "</OpenClinica:PrincipalInvestigator>");
            xml.append(nls);
            temp = study.getProtocolDescription();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:DetailedDescription>" + StringEscapeUtils.escapeXml(temp)
                        + "</OpenClinica:DetailedDescription>");
                xml.append(nls);
            }
            xml.append(currentIndent + indent + indent + "<OpenClinica:Sponsor>" + StringEscapeUtils.escapeXml(study.getSponsor()) + "</OpenClinica:Sponsor>");
            xml.append(nls);
            temp = study.getCollaborators();
            if (temp != null && temp.length() > 0) {
                xml.append(
                        currentIndent + indent + indent + "<OpenClinica:Collaborators>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Collaborators>");
                xml.append(nls);
            }
            xml.append(
                    currentIndent + indent + indent + "<OpenClinica:StudyPhase>" + StringEscapeUtils.escapeXml(study.getPhase()) + "</OpenClinica:StudyPhase>");
            xml.append(nls);
            temp = study.getProtocolType();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:ProtocolType>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:ProtocolType>");
                xml.append(nls);
            }
            if (study.getProtocolDateVerification() != null) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:ProtocolVerificationDate>"
                        + new SimpleDateFormat("yyyy-MM-dd").format(study.getProtocolDateVerification()) + "</OpenClinica:ProtocolVerificationDate>");
                xml.append(nls);
            }
            xml.append(currentIndent + indent + indent + "<OpenClinica:Purpose>" + StringEscapeUtils.escapeXml(study.getPurpose()) + "</OpenClinica:Purpose>");
            xml.append(nls);
            temp = study.getDuration();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Duration>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Duration>");
                xml.append(nls);
            }
            temp = study.getSelection();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Selection>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Selection>");
                xml.append(nls);
            }
            temp = study.getTiming();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Timing>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Timing>");
                xml.append(nls);
            }
            temp = study.getAllocation();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Allocation>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Allocation>");
                xml.append(nls);
            }
            temp = study.getMasking();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Masking>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Masking>");
                xml.append(nls);
            }
            temp = study.getControl();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Control>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Control>");
                xml.append(nls);
            }
            temp = study.getAssignment();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:InterventionModel>" + StringEscapeUtils.escapeXml(temp)
                        + "</OpenClinica:InterventionModel>");
                xml.append(nls);
            }
            temp = study.getEndpoint();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:StudyClassification>" + StringEscapeUtils.escapeXml(temp)
                        + "</OpenClinica:StudyClassification>");
                xml.append(nls);
            }
            xml.append(currentIndent + indent + "</OpenClinica:StudyDescriptionAndStatus>");
            xml.append(nls);

            xml.append(currentIndent + indent + "<OpenClinica:ConditionsAndEligibility>");
            xml.append(nls);
            temp = study.getConditions();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Conditions>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Conditions>");
                xml.append(nls);
            }
            temp = study.getKeywords();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Keywords>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Keywords>");
                xml.append(nls);
            }
            temp = study.getEligibility();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:EligibilityCriteria>" + StringEscapeUtils.escapeXml(temp)
                        + "</OpenClinica:EligibilityCriteria>");
                xml.append(nls);
            }
            temp = study.getGender();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Sex>" + temp + "</OpenClinica:Sex>");
                xml.append(nls);
            }
            temp = study.getAgeMin();
            String temp2 = study.getAgeMax();
            if (temp != null && temp.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Age MinimumAge=\"" + temp + "\"");
                if (temp2 != null && temp2.length() > 0) {
                    xml.append(" MaximumAge=\"" + temp2 + "\"");
                }
                xml.append("/>");
                xml.append(nls);
            } else if (temp2 != null && temp2.length() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:Age  MaximumAge=\"" + temp2 + "\"");
                xml.append("/>");
                xml.append(nls);
            }
            xml.append(currentIndent + indent + indent + "<OpenClinica:HealthyVolunteersAccepted>");
            xml.append(study.getHealthyVolunteerAccepted() ? "Yes" : "No");
            xml.append("</OpenClinica:HealthyVolunteersAccepted>");
            xml.append(nls);
            if (study.getExpectedTotalEnrollment() > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:ExpectedTotalEnrollment>" + study.getExpectedTotalEnrollment()
                        + "</OpenClinica:ExpectedTotalEnrollment>");
                xml.append(nls);
            }
            xml.append(currentIndent + indent + "</OpenClinica:ConditionsAndEligibility>");
            xml.append(nls);

            StringBuffer facility = new StringBuffer();
            temp = study.getFacilityName();
            facility.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:FacilityName>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityName>" + nls
                    : "");
            temp = study.getFacilityCity();
            facility.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:FacilityCity>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityCity>" + nls
                    : "");
            temp = study.getFacilityState();
            facility.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:FacilityState>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityState>" + nls
                    : "");
            temp = study.getFacilityZip();
            facility.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:PostalCode>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:PostalCode>" + nls
                    : "");
            temp = study.getFacilityCountry();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:FacilityCountry>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityCountry>" + nls : "");
            temp = study.getFacilityContactName();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:FacilityContactName>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityContactName>" + nls : "");
            temp = study.getFacilityContactDegree();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:FacilityContactDegree>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityContactDegree>" + nls : "");
            temp = study.getFacilityContactPhone();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:FacilityContactPhone>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityContactPhone>" + nls : "");
            temp = study.getFacilityContactEmail();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:FacilityContactEmail>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:FacilityContactEmail>" + nls : "");
            if (facility.length() > 0) {
                xml.append(currentIndent + indent + "<OpenClinica:FacilityInformation>");
                xml.append(nls);
                xml.append(facility);
                xml.append(currentIndent + indent + "</OpenClinica:FacilityInformation>");
                xml.append(nls);
            }

            facility = new StringBuffer();
            temp = study.getMedlineIdentifier();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:MEDLINEIdentifier>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:MEDLINEIdentifier>" + nls : "");
            facility.append(currentIndent + indent + indent + "<OpenClinica:ResultsReference>" + (study.isResultsReference() ? "Yes" : "No")
                    + "</OpenClinica:ResultsReference>" + nls);
            temp = study.getUrl();
            facility.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:URLReference>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:URLReference>" + nls
                    : "");
            temp = study.getUrlDescription();
            facility.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:URLDescription>"
                    + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:URLDescription>" + nls : "");
            xml.append(currentIndent + indent + "<OpenClinica:RelatedInformation>");
            xml.append(nls);
            xml.append(facility);
            xml.append(currentIndent + indent + "</OpenClinica:RelatedInformation>");
            xml.append(nls);

            StudyParameterConfig spc = study.getStudyParameterConfig();
            xml.append(currentIndent + indent + "<OpenClinica:StudyParameterConfiguration>");
            xml.append(nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_collectDob\"" + " Value=\""
                    + spc.getCollectDob() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_discrepancyManagement\"" + " Value=\""
                    + spc.getDiscrepancyManagement() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_subjectPersonIdRequired\"" + " Value=\""
                    + spc.getSubjectPersonIdRequired() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_genderRequired\"" + " Value=\""
                    + spc.getGenderRequired() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_subjectIdGeneration\"" + " Value=\""
                    + spc.getSubjectIdGeneration() + "\"/>" + nls);
          if(!StringUtils.isEmpty(spc.getParticipantIdTemplate())) {
              xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_participantIdTemplate\"" + " Value=\""
                      + spc.getParticipantIdTemplate() + "\"/>" + nls);
          }
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_interviewerNameRequired\"" + " Value=\""
                    + spc.getInterviewerNameRequired() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_interviewerNameDefault\"" + " Value=\""
                    + spc.getInterviewerNameDefault() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_interviewerNameEditable\"" + " Value=\""
                    + spc.getInterviewerNameEditable() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_interviewDateRequired\"" + " Value=\""
                    + spc.getInterviewDateRequired() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_interviewDateDefault\"" + " Value=\""
                    + spc.getInterviewDateDefault() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_interviewDateEditable\"" + " Value=\""
                    + spc.getInterviewDateEditable() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_personIdShownOnCRF\"" + " Value=\""
                    + spc.getPersonIdShownOnCRF() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_secondaryLabelViewable\"" + " Value=\""
                    + spc.getSecondaryLabelViewable() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_adminForcedReasonForChange\""
                    + " Value=\"" + spc.getAdminForcedReasonForChange() + "\"/>" + nls);
            xml.append(currentIndent + indent + indent + "<OpenClinica:StudyParameterListRef StudyParameterListID=\"SPL_eventLocationRequired\"" + " Value=\""
                    + spc.getEventLocationRequired() + "\"/>" + nls);

            addStudyParameterLists(currentIndent + indent + indent);

            xml.append(currentIndent + indent + "</OpenClinica:StudyParameterConfiguration>");
            xml.append(nls);

            xml.append(currentIndent + "</OpenClinica:StudyDetails>");
            xml.append(nls);
        }
    }

    public void addEventDefinitionDetails(EventDefinitionDetailsBean detail, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String temp = detail.getDescription();
        String des = temp != null && temp.length() > 0
                ? currentIndent + indent + "<OpenClinica:Description>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Description>" + nls : "";
        temp = detail.getCategory();
        String cat = temp != null && temp.length() > 0
                ? currentIndent + indent + "<OpenClinica:Category>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:Category>" + nls : "";
        if (des.length() > 1 || cat.length() > 1) {
            xml.append(currentIndent + "<OpenClinica:EventDefinitionDetails StudyEventOID=\"" + StringEscapeUtils.escapeXml(detail.getOid()) + "\">");
            xml.append(nls);
            xml.append(des);
            temp = detail.getCategory();
            xml.append(cat);
            xml.append(currentIndent + "</OpenClinica:EventDefinitionDetails>");
            xml.append(nls);
        }
    }

    public void addFormDetails(FormDetailsBean detail, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String description = "";
        xml.append(currentIndent + "<OpenClinica:FormDetails>");
        xml.append(nls);

        description = detail.getDescription();
        xml.append(description != null && description.length() > 0
                ? currentIndent + indent + "<OpenClinica:Description>" + StringEscapeUtils.escapeXml(description) + "</OpenClinica:Description>" + nls : "");
        xml.append(currentIndent + "</OpenClinica:FormDetails>");
        xml.append(nls);
    }

    public void addPresentInEventDefinitions(FormDetailsBean detail, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String temp = "";
        ArrayList<PresentInEventDefinitionBean> plist = detail.getPresentInEventDefinitions();
        if (plist == null || plist.size() < 1) {
            logger.debug("No presentInEventDefinitions in FormDetails with formOID=" + detail.getOid());
        } else {
            for (PresentInEventDefinitionBean p : plist) {
                xml.append(currentIndent + "<OpenClinica:PresentInEventDefinition StudyEventOID=\"" + p.getStudyEventOid() + "\"" + " IsDefaultVersion=\""
                        + p.getIsDefaultVersion() + "\"");
                temp = p.getNullValues();
                xml.append(temp != null && temp.length() > 0 ? " NullValues=\"" + StringEscapeUtils.escapeXml(temp) + "\"" : "");
                xml.append(" PasswordRequired=\"" + p.getPasswordRequired() + "\"");
                temp = p.getDoubleDataEntry();
                xml.append((temp != null && temp.length() > 0 ? " DoubleDataEntry=\"" + temp + "\"" : "") + " HideCRF=\"" + p.getHideCrf() + "\""
                        + " ParticipantForm=\"" + p.getParticipantForm() + "\"");
                xml.append(" AllowAnonymousSubmission=\"" + p.getAllowAnonymousSubmission() + "\"");

                temp = p.getSubmissionUrl();
                xml.append(temp != null && temp.length() > 0 ? " SubmissionUrl=\"" + StringEscapeUtils.escapeXml(temp) + "\"" : "");
                xml.append(" Offline=\"" + p.getOffline() + "\"");
                temp = p.getSourceDataVerification();
                xml.append(temp != null && temp.length() > 0 ? " SourceDataVerification=\"" + StringEscapeUtils.escapeXml(temp) + "\"" : "");
                xml.append("/>");
                xml.append(nls);
            }
        }
    }

    public void addItemGroupDetails(ItemGroupDetailsBean detail, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String temp = "";
        Integer itemp = -1;
        Integer itemp2 = -1;
        ArrayList<PresentInFormBean> informs = (ArrayList<PresentInFormBean>) detail.getPresentInForms();
        xml.append(currentIndent + "<OpenClinica:ItemGroupDetails ItemGroupOID=\"" + StringEscapeUtils.escapeXml(detail.getOid()) + "\">");
        xml.append(nls);
        for (PresentInFormBean inform : informs) {
            xml.append(
                    currentIndent + indent + "<OpenClinica:PresentInForm FormOID=\"" + inform.getFormOid() + "\" ShowGroup=\"" + inform.getShowGroup() + "\">");
            xml.append(nls);
            ItemGroupRepeatBean repeat = inform.getItemGroupRepeatBean();
            itemp = repeat.getRepeatNumber();
            itemp2 = repeat.getRepeatMax();
            if (itemp != null && itemp > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:ItemGroupRepeat RepeatNumber=\"" + itemp);
                if (itemp2 != null && itemp2 > 0) {
                    xml.append("\" RepeatMax=\"" + itemp2 + "\"/>");
                } else {
                    xml.append("\"/>");
                }
                xml.append(nls);
            } else if (itemp2 != null && itemp2 > 0) {
                xml.append(currentIndent + indent + indent + "<OpenClinica:ItemGroupRepeat RepeatMax=\"" + itemp2 + "\"/>");
                xml.append(nls);
            }
            temp = inform.getItemGroupHeader();
            xml.append(temp != null && temp.length() > 0 ? currentIndent + indent + indent + "<OpenClinica:ItemGroupHeader>" + StringEscapeUtils.escapeXml(temp)
                    + "</OpenClinica:ItemGroupHeader>" + nls : "");
            xml.append(currentIndent + indent + "</OpenClinica:PresentInForm>");
            xml.append(nls);
        }
        xml.append(currentIndent + "</OpenClinica:ItemGroupDetails>");
        xml.append(nls);
    }

    public void addItemDetails(ItemDetailsBean detail, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String temp = "";
        Integer itemp = -1;
        ArrayList<ItemPresentInFormBean> informs = (ArrayList<ItemPresentInFormBean>) detail.getItemPresentInForm();
        xml.append(currentIndent + "<OpenClinica:ItemDetails ItemOID=\"" + StringEscapeUtils.escapeXml(detail.getOid()) + "\">");
        xml.append(nls);
        for (ItemPresentInFormBean inform : informs) {
            xml.append(currentIndent + indent + "<OpenClinica:ItemPresentInForm FormOID=\"" + inform.getFormOid() + "\"");
            temp = inform.getParentItemOid();
            xml.append(temp != null && temp.length() > 0 ? " ParentItemOID=\"" + temp + "\"" : "");
            itemp = inform.getColumnNumber();
            xml.append(itemp != null && itemp > 0 ? " ColumnNumber=\"" + itemp + "\"" : "");
            temp = inform.getPageNumber();
            xml.append(temp != null && temp.length() > 0 ? " PageNumber=\"" + StringEscapeUtils.escapeXml(temp) + "\"" : "");
            temp = inform.getDefaultValue();
            xml.append(temp != null && temp.length() > 0 ? " DefaultValue=\"" + StringEscapeUtils.escapeXml(temp) + "\"" : "");
            xml.append(" PHI=\"" + inform.getPhi() + "\" ShowItem=\"" + inform.getShowItem() + "\" OrderInForm= \"" + inform.getOrderInForm() + "\">");

            xml.append(nls);
            temp = inform.getLeftItemText();
            xml.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:LeftItemText>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:LeftItemText>" + nls
                    : "");
            temp = inform.getRightItemText();
            xml.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:RightItemText>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:RightItemText>" + nls
                    : "");
            temp = inform.getItemHeader();
            xml.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:ItemHeader>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:ItemHeader>" + nls
                    : "");
            temp = inform.getItemSubHeader();
            xml.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:ItemSubHeader>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:ItemSubHeader>" + nls
                    : "");
            temp = inform.getSectionLabel();
            xml.append(temp != null && temp.length() > 0
                    ? currentIndent + indent + indent + "<OpenClinica:SectionLabel>" + StringEscapeUtils.escapeXml(temp) + "</OpenClinica:SectionLabel>" + nls
                    : "");
            ItemResponseBean response = inform.getItemResponse();
            xml.append(currentIndent + indent + indent + "<OpenClinica:ItemResponse ResponseType=\"" + StringEscapeUtils.escapeXml(response.getResponseType()));
            temp = response.getResponseLayout();
            if (temp != null && temp.length() > 0) {
                xml.append("\" ResponseLayout=\"" + StringEscapeUtils.escapeXml(temp) + "\"/>");
            } else {
                xml.append("\"/>");
            }
            xml.append(nls);
            if ("no".equalsIgnoreCase(inform.getShowItem())) {
                addSimpleConditionalDisplay(inform, currentIndent + indent + indent);
            }
            xml.append(currentIndent + indent + "</OpenClinica:ItemPresentInForm>");
            xml.append(nls);
        }
        xml.append(currentIndent + "</OpenClinica:ItemDetails>");
        xml.append(nls);
    }

    public void addSimpleConditionalDisplay(ItemPresentInFormBean inform, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        SimpleConditionalDisplayBean scd = inform.getSimpleConditionalDisplay();
        String name = scd.getControlItemName(), option = scd.getOptionValue(), message = scd.getMessage();
        // only SimpleConditionalDisplayBean with controlItemName, option and message has been set for ItemDetails
        if (name != null && name.length() > 0) {
            xml.append(currentIndent + "<OpenClinica:SimpleConditionalDisplay>");
            xml.append(nls);
            xml.append(currentIndent + indent + "<OpenClinica:ControlItemName>" + StringEscapeUtils.escapeXml(name) + "</OpenClinica:ControlItemName>");
            xml.append(nls);
            xml.append(currentIndent + indent + "<OpenClinica:OptionValue>" + StringEscapeUtils.escapeXml(option) + "</OpenClinica:OptionValue>");
            xml.append(nls);
            xml.append(currentIndent + indent + "<OpenClinica:Message>" + StringEscapeUtils.escapeXml(message) + "</OpenClinica:Message>");
            xml.append(nls);
            xml.append(currentIndent + "</OpenClinica:SimpleConditionalDisplay>");
            xml.append(nls);
        }
    }

    public void addStudyGroupClassList(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<StudyGroupClassListBean> lists = (ArrayList<StudyGroupClassListBean>) odmstudy.getMetaDataVersion().getStudyGroupClassLists();
        if (lists != null) {
            if (lists.size() > 0) {
                for (StudyGroupClassListBean l : lists) {
                    xml.append(currentIndent + "<OpenClinica:StudyGroupClassList ID=\"" + StringEscapeUtils.escapeXml(l.getId()) + "\" ");
                    if (l.getName() != null) {
                        xml.append("Name=\"" + StringEscapeUtils.escapeXml(l.getName()) + "\" ");
                    }
                    if (l.getStatus() != null) {
                        xml.append("Status=\"" + l.getStatus() + "\" ");
                    }
                    if (l.getType() != null) {
                        xml.append("Type=\"" + StringEscapeUtils.escapeXml(l.getType()) + "\" ");
                    }
                    if (l.getSubjectAssignment() != null) {
                        xml.append("SubjectAssignment=\"" + StringEscapeUtils.escapeXml(l.getSubjectAssignment()) + "\" ");
                    }
                    xml.append(">");
                    xml.append(nls);
                    for (StudyGroupItemBean i : (ArrayList<StudyGroupItemBean>) l.getStudyGroupItems()) {
                        xml.append(currentIndent + indent + "<OpenClinica:StudyGroupItem ");
                        if (i.getName() != null) {
                            xml.append("Name=\"" + StringEscapeUtils.escapeXml(i.getName()) + "\" ");
                        }
                        if (i.getDescription() != null) {
                            xml.append("Description=\"" + StringEscapeUtils.escapeXml(i.getDescription()) + "\" ");
                        }
                        xml.append("/>");
                        xml.append(nls);
                    }

                    xml.append(currentIndent + "</OpenClinica:StudyGroupClassList>");
                    xml.append(nls);
                }
            }
        }
    }

    /*
     * public void addStudyGroupClassList(String currentIndent) { StringBuffer xml = this.getXmlOutput(); String indent
     * =
     * this.getIndent(); ArrayList<StudyGroupClassListBean> lists = (ArrayList<StudyGroupClassListBean>)
     * odmstudy.getMetaDataVersion().getStudyGroupClassLists(); if (lists != null) { if (lists.size() > 0) { for
     * (StudyGroupClassListBean l : lists) { xml.append(currentIndent +
     * "<OpenClinica:StudyGroupClassList OpenClinica:ID=\"" +
     * StringEscapeUtils.escapeXml(l.getId()) + "\" "); if (l.getName() != null) { xml.append("OpenClinica:Name=\"" +
     * StringEscapeUtils.escapeXml(l.getName()) + "\" "); } if (l.getStatus() != null) {
     * xml.append("OpenClinica:Status=\"" +
     * l.getStatus() + "\" "); } if (l.getType() != null) { xml.append("OpenClinica:Type=\"" +
     * StringEscapeUtils.escapeXml(l.getType()) + "\" "); } if (l.getSubjectAssignment() != null) {
     * xml.append("OpenClinica:SubjectAssignment=\"" + StringEscapeUtils.escapeXml(l.getSubjectAssignment()) + "\" "); }
     * xml.append(">"); xml.append(nls); for (StudyGroupItemBean i : (ArrayList<StudyGroupItemBean>)
     * l.getStudyGroupItems()) {
     * xml.append(currentIndent + indent + "<OpenClinica:StudyGroupItem "); if (i.getName() != null) {
     * xml.append("OpenClinica:Name=\"" + StringEscapeUtils.escapeXml(i.getName()) + "\" "); } if (i.getDescription() !=
     * null)
     * { xml.append("OpenClinica:Description=\"" + StringEscapeUtils.escapeXml(i.getDescription()) + "\" "); }
     * xml.append("/>"); xml.append(nls); } xml.append(currentIndent + "</OpenClinica:StudyGroupClassList>");
     * xml.append(nls);
     * } } } }
     */

    public void addStudyParameterLists(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();

        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_collectDob" + "\" Name=\"" + "Collect Subject Date Of Birth" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "1" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "2" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Only Year of Birth" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "3" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Not Used" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_discrepancyManagement" + "\" Name=\"" + "Allow Discrepancy Management" + "\">"
                + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_subjectPersonIdRequired" + "\" Name=\"" + "Person ID Required" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "required" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Required" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "optional" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Optional" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "not used" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Not Used" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_genderRequired" + "\" Name=\"" + "Sex Required" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_subjectIdGeneration" + "\" Name=\"" + "How To Generate ParticipantID"
                + "\">" + nls);

        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "manual" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Manual Entry" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);

        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "auto non-editable" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Auto-generated and Non-editable" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_interviewerNameRequired" + "\" Name=\""
                + "When Performing Data Entry, Interviewer Name Required For Data Entry" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "yes" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "no" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "not_used" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Not Used" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_interviewerNameDefault" + "\" Name=\""
                + "When Performing Data Entry, Interviewer Name Default as Blank" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "blank" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Blank" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "pre-populated" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Pre-Populated from active user" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_interviewerNameEditable" + "\" Name=\""
                + "When Performing Data Entry, Interviewer Name Editable" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_interviewDateRequired" + "\" Name=\""
                + "When Performing Data Entry, Interview Date Required" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "not_used" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Not Used" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_interviewDateDefault" + "\" Name=\""
                + "When Performing Data Entry, Interview Date Default as Blank" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "blank" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Blank" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "pre-populated" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Pre-Populated from Study Event" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_interviewDateEditable" + "\" Name=\""
                + "When Performing Data Entry, Interview Date Editable" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_personIdShownOnCRF" + "\" Name=\"" + "Show Person ID on CRF Header" + "\">"
                + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_secondaryLabelViewable" + "\" Name=\"" + "Secondary Label Viewable" + "\">"
                + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_adminForcedReasonForChange" + "\" Name=\""
                + "Forced Reason For Change in Administrative Editing" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "true" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Yes" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "false" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "No" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
        //
        xml.append(
                currentIndent + "<OpenClinica:StudyParameterList ID=\"" + "SPL_eventLocationRequired" + "\" Name=\"" + "Event Location Required" + "\">" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "required" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Required" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "optional" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Optional" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + indent + "<OpenClinica:StudyParameterListItem CodedParameterValue=\"" + "not_used" + "\">" + nls);
        xml.append(currentIndent + indent + indent + "<Decode>" + nls);
        xml.append(currentIndent + indent + indent + indent + "<TranslatedText>" + "Not Used" + "</TranslatedText>" + nls);
        xml.append(currentIndent + indent + indent + "</Decode>" + nls);
        xml.append(currentIndent + indent + "</OpenClinica:StudyParameterListItem>" + nls);
        xml.append(currentIndent + "</OpenClinica:StudyParameterList>" + nls);
    }

    public void setOdmStudy(OdmStudyBean odmstudy) {
        this.odmstudy = odmstudy;
    }

    public OdmStudyBean getOdmStudyBean() {
        return this.odmstudy;
    }

    public LinkedHashMap<String, OdmStudyBean> getOdmStudyMap() {
        return odmStudyMap;
    }

    public void setOdmStudyMap(LinkedHashMap<String, OdmStudyBean> odmStudyMap) {
        this.odmStudyMap = odmStudyMap;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    protected String measurementUnitRefString(String muRefOid) {
        String temp = "";
        temp = "<MeasurementUnitRef MeasurementUnitOID=\"" + StringEscapeUtils.escapeXml(muRefOid) + "\"/>";
        return temp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.bean.extract.odm.OdmXmlReportBean#createOdmXml(boolean)
     */
    @Override
    public void createOdmXml(boolean isDataset) {
        // TODO Auto-generated method stub

    }

}