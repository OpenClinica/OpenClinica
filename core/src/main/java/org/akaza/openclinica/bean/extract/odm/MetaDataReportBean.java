/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.akaza.openclinica.bean.odmbeans.BasicDefinitionsBean;
import org.akaza.openclinica.bean.odmbeans.CodeListBean;
import org.akaza.openclinica.bean.odmbeans.CodeListItemBean;
import org.akaza.openclinica.bean.odmbeans.ElementRefBean;
import org.akaza.openclinica.bean.odmbeans.FormDefBean;
import org.akaza.openclinica.bean.odmbeans.GlobalVariablesBean;
import org.akaza.openclinica.bean.odmbeans.ItemDefBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupDefBean;
import org.akaza.openclinica.bean.odmbeans.MeasurementUnitBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionBean;
import org.akaza.openclinica.bean.odmbeans.MultiSelectListBean;
import org.akaza.openclinica.bean.odmbeans.MultiSelectListItemBean;
import org.akaza.openclinica.bean.odmbeans.OdmStudyBean;
import org.akaza.openclinica.bean.odmbeans.RangeCheckBean;
import org.akaza.openclinica.bean.odmbeans.StudyEventDefBean;
import org.akaza.openclinica.bean.odmbeans.StudyGroupClassListBean;
import org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean;
import org.akaza.openclinica.bean.odmbeans.SymbolBean;
import org.akaza.openclinica.bean.odmbeans.TranslatedTextBean;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Create ODM XML Study Element for a study.
 * 
 * @author ywang (May, 2008)
 */

public class MetaDataReportBean extends OdmXmlReportBean {
    private OdmStudyBean odmstudy;
    private LinkedHashMap<String, OdmStudyBean> odmStudyMap;

    public MetaDataReportBean(OdmStudyBean odmstudy) {
        super();
        this.odmstudy = odmstudy;
    }

    public MetaDataReportBean(LinkedHashMap<String, OdmStudyBean> odmStudyMap) {
        super();
        this.odmStudyMap = odmStudyMap;
    }

    private static String nls = System.getProperty("line.separator");

    /**
     * has not been implemented yet
     */
    @Override
    public void createOdmXml(boolean isDataset) {
        // this.addHeading();
        // this.addRootStartLine();
        // addNodeStudy();
        // this.addRootEndLine();
    }

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
            // xml.append(currentIndent + indent + "<TranslatedText xml:lang=\""
            // + text.getXmlLang() + "\">" + text.getText() +
            // "</TranslatedText>");
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
        if (isDataset) {
            xml.append(currentIndent + "<MetaDataVersion OID=\"" + StringEscapeUtils.escapeXml(meta.getOid()) + "\" Name=\""
                + StringEscapeUtils.escapeXml(meta.getName()) + "\">");
            xml.append(nls);
            // for <Include>,
            // 1. In order to have <Include>, previous metadataversionOID must
            // be
            // given.
            // 2. If there is no previous study, then previous study OID is as
            // the
            // same as the current study OID
            // 3. there is no Include if both previous study and previous
            // metadataversionOID are empty
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
        } else {
            xml.append(currentIndent + "<MetaDataVersion>");
            xml.append(nls);
        }
        //
        addProtocol(currentIndent + indent);
        if (odmstudy.getMetaDataVersion().getStudyEventDefs().size() > 0) {
            addStudyEventDef(currentIndent + indent);
            if (odmstudy.getMetaDataVersion().getItemGroupDefs().size() > 0) {
                addFormDef(currentIndent + indent);
                addItemGroupDef(currentIndent + indent);
                addItemDef(currentIndent + indent);
                addCodeList(currentIndent + indent);
                if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                    addMultiSelectList(currentIndent + indent);
                    addStudyGroupClassList(currentIndent + indent);
                }
            }
        }
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

    public void addStudyEventDef(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<StudyEventDefBean> seds = (ArrayList<StudyEventDefBean>) odmstudy.getMetaDataVersion().getStudyEventDefs();
        for (StudyEventDefBean sed : seds) {
            xml.append(currentIndent + "<StudyEventDef OID=\"" + StringEscapeUtils.escapeXml(sed.getOid()) + "\"  Name=\""
                + StringEscapeUtils.escapeXml(sed.getName()) + "\" Repeating=\"" + sed.getRepeating() + "\" Type=\"" + sed.getType() + "\">");
            xml.append(nls);
            ArrayList<ElementRefBean> forms = (ArrayList<ElementRefBean>) sed.getFormRefs();
            for (ElementRefBean form : forms) {
                xml.append(currentIndent + indent + "<FormRef FormOID=\"" + StringEscapeUtils.escapeXml(form.getElementDefOID()) + "\" Mandatory=\""
                    + form.getMandatory() + "\"/>");
                xml.append(nls);
            }
            xml.append(currentIndent + "</StudyEventDef>");
            xml.append(nls);
        }
    }

    public void addFormDef(String currentIndent) {
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
            xml.append(currentIndent + "</FormDef>");
            xml.append(nls);
        }
    }

    public void addItemGroupDef(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<ItemGroupDefBean> igs = (ArrayList<ItemGroupDefBean>) odmstudy.getMetaDataVersion().getItemGroupDefs();
        for (ItemGroupDefBean ig : igs) {
            if (ig.getComment().length() > 0) {
                xml.append(currentIndent + "<ItemGroupDef OID=\"" + StringEscapeUtils.escapeXml(ig.getOid()) + "\" Name=\""
                    + StringEscapeUtils.escapeXml(ig.getName()) + "\" Repeating=\"" + ig.getRepeating() + "\" SASDatasetName=\""
                    + this.getSasNameValidatory().getValidSasName(ig.getPreSASDatasetName()) + "\" Comment=\"" + StringEscapeUtils.escapeXml(ig.getComment())
                    + "\">");
            } else {
                xml.append(currentIndent + "<ItemGroupDef OID=\"" + StringEscapeUtils.escapeXml(ig.getOid()) + "\" Name=\""
                    + StringEscapeUtils.escapeXml(ig.getName()) + "\" Repeating=\"" + ig.getRepeating() + "\" SASDatasetName=\""
                    + this.getSasNameValidatory().getValidSasName(ig.getPreSASDatasetName()) + "\">");
            }
            xml.append(nls);
            ArrayList<ElementRefBean> items = (ArrayList<ElementRefBean>) ig.getItemRefs();
            for (ElementRefBean item : items) {
                xml.append(currentIndent + indent + "<ItemRef ItemOID=\"" + StringEscapeUtils.escapeXml(item.getElementDefOID()) + "\" OrderNumber=\""
                    + item.getOrderNumber() + "\" Mandatory=\"" + item.getMandatory() + "\"/>");
                xml.append(nls);
            }
            xml.append(currentIndent + "</ItemGroupDef>");
            xml.append(nls);
        }
    }

    public void addItemDef(String currentIndent) {
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
            xml.append(" SASFieldName=\"" + this.getSasNameValidatory().getValidSasName(item.getPreSASFieldName()) + "\"");
            if (item.getComment().length() > 0) {
                xml.append(" Comment=\"" + StringEscapeUtils.escapeXml(item.getComment()) + "\"");
            }
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                xml.append(" OpenClinica:FormOIDs=\""+item.getFormOIDs()+"\"");
            }
            boolean hasNode = false;
            // add question
            TranslatedTextBean t = item.getQuestion();
            if (t != null && t.getText() != null && t.getText().length() > 0) {
                if (!hasNode) {
                    hasNode = true;
                    xml.append(">");
                    xml.append(nls);
                }
                xml.append(currentIndent + indent + "<Question>");
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
                        xml.append(currentIndent + indent + "<OpenClinica:MultiSelectListRef OpenClinica:MultiSelectListID=\""
                            + StringEscapeUtils.escapeXml(mslOid) + "\"/>");
                        xml.append(nls);
                    }
                }
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
                    xml.append(currentIndent + "<OpenClinica:MultiSelectList OpenClinica:ID=\"" + StringEscapeUtils.escapeXml(l.getOid()) + "\" ");
                    if (l.getName() != null) {
                        xml.append("OpenClinica:Name=\"" + StringEscapeUtils.escapeXml(l.getName()) + "\" ");
                    }
                    if (l.getDataType() != null) {
                        xml.append("OpenClinica:DataType=\"" + l.getDataType() + "\" ");
                    }
                    if (l.getActualDataType() != null) {
                        xml.append("OpenClinica:ActualDataType=\"" + StringEscapeUtils.escapeXml(l.getActualDataType()) + "\" ");
                    }
                    xml.append(">");
                    xml.append(nls);

                    ArrayList<MultiSelectListItemBean> mslis = (ArrayList<MultiSelectListItemBean>) l.getMultiSelectListItems();
                    if (mslis != null && mslis.size() > 0) {
                        for (MultiSelectListItemBean msli : mslis) {
                            xml.append(currentIndent + indent + "<OpenClinica:MultiSelectListItem OpenClinica:CodedOptionValue=\""
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

    public void addStudyGroupClassList(String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        ArrayList<StudyGroupClassListBean> lists = (ArrayList<StudyGroupClassListBean>) odmstudy.getMetaDataVersion().getStudyGroupClassLists();
        if (lists != null) {
            if (lists.size() > 0) {
                for (StudyGroupClassListBean l : lists) {
                    xml.append(currentIndent + "<OpenClinica:StudyGroupClassList OpenClinica:ID=\"" + StringEscapeUtils.escapeXml(l.getId()) + "\" ");
                    if (l.getName() != null) {
                        xml.append("OpenClinica:Name=\"" + StringEscapeUtils.escapeXml(l.getName()) + "\" ");
                    }
                    if (l.getStatus() != null) {
                        xml.append("OpenClinica:Status=\"" + l.getStatus() + "\" ");
                    }
                    if (l.getType() != null) {
                        xml.append("OpenClinica:Type=\"" + StringEscapeUtils.escapeXml(l.getType()) + "\" ");
                    }
                    if (l.getSubjectAssignment() != null) {
                        xml.append("OpenClinica:SubjectAssignment=\"" + StringEscapeUtils.escapeXml(l.getSubjectAssignment()) + "\" ");
                    }
                    xml.append(">");
                    xml.append(nls);
                    for (StudyGroupItemBean i : (ArrayList<StudyGroupItemBean>) l.getStudyGroupItems()) {
                        xml.append(currentIndent + indent + "<OpenClinica:StudyGroupItem ");
                        if (i.getName() != null) {
                            xml.append("OpenClinica:Name=\"" + StringEscapeUtils.escapeXml(i.getName()) + "\" ");
                        }
                        if (i.getDescription() != null) {
                            xml.append("OpenClinica:Description=\"" + StringEscapeUtils.escapeXml(i.getDescription()) + "\" ");
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

    protected String measurementUnitRefString(String muRefOid) {
        String temp = "";
        temp = "<MeasurementUnitRef MeasurementUnitOID=\"" + StringEscapeUtils.escapeXml(muRefOid) + "\"/>";
        return temp;
    }
}