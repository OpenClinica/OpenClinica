/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import java.util.TreeSet;

import org.akaza.openclinica.bean.extract.ODMSASFormatNameValidator;
import org.akaza.openclinica.bean.extract.SasNameValidator;
import org.akaza.openclinica.bean.odmbeans.ODMBean;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create ODM XML document.
 * 
 * @author ywang (May, 2008)
 */

public abstract class OdmXmlReportBean {
    private String ODMVersion;
    private StringBuffer xmlOutput;
    private String xmlHeading;
    private ODMBean odmbean;

    private String indent;
    private SasNameValidator sasNameValidator;
    private ODMSASFormatNameValidator sasFormatValidator;
    private TreeSet<String> uniqueNameTable;

    private static String nls = System.getProperty("line.separator");

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * In this constructor, xmlHeading = "<?xml version=\"1.0\"
     * encoding=\"UTF-8\"?>";
     */
    public OdmXmlReportBean() {
        xmlOutput = new StringBuffer();
        xmlHeading = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        indent = "    ";
        // locale_df_string =
        // ResourceBundleProvider.getFormatBundle().getString("date_format_string");
        uniqueNameTable = new TreeSet<String>();
        sasNameValidator = new SasNameValidator();
        sasNameValidator.setUniqueNameTable(this.uniqueNameTable);
        sasFormatValidator = new ODMSASFormatNameValidator();
        sasFormatValidator.setUniqueNameTable(this.uniqueNameTable);
    }

    public abstract void createOdmXml(boolean isDataset);

    public void addHeading() {
        xmlOutput.append(this.xmlHeading);
        xmlOutput.append(nls);
    }

    /**
     * Append the beginning line of ODM root element
     * 
     * @param odmXml
     * @param odmbean
     */
    public void addRootStartLine() {
        String ov = odmbean.getODMVersion();
        ov = ov.startsWith("oc") ? ov.substring(2) : ov;
        xmlOutput.append("<ODM FileOID=\"" + StringEscapeUtils.escapeXml(odmbean.getFileOID()) + "\" Description=\""
                + StringEscapeUtils.escapeXml(odmbean.getDescription()) + "\" CreationDateTime=\"" + odmbean.getCreationDateTime() + "\" FileType=\""
                + odmbean.getFileType() + "\" ODMVersion=\"" + StringEscapeUtils.escapeXml(ov) + "\" ");
        for (String s : odmbean.getXmlnsList()) {
            xmlOutput.append(s + " ");
        }
        xmlOutput.append("xmlns:xsi=\"" + odmbean.getXsi() + "\" xsi:schemaLocation=\"" + odmbean.getSchemaLocation() + "\" >");
        xmlOutput.append(nls);
    }

    public void addRootEndLine() {
        xmlOutput.append("</ODM>");
    }

    @Override
    public String toString() {
        return xmlOutput.toString();
    }

    public StringBuffer getXmlOutput() {
        return this.xmlOutput;
    }

    public void setXmlOutput(StringBuffer odmXml) {
        this.xmlOutput = odmXml;
    }

    public String getXmlHeading() {
        return this.xmlHeading;
    }

    public void setXmlHeading(String xmlheading) {
        this.xmlHeading = xmlheading;
    }

    public void setOdmBean(ODMBean odmbean) {
        this.odmbean = odmbean;
    }

    public ODMBean getOdmBean() {
        return this.odmbean;
    }

    public void setSasNameValidator(SasNameValidator validator) {
        this.sasNameValidator = validator;
    }

    public SasNameValidator getSasNameValidator() {
        return this.sasNameValidator;
    }

    public void setSasFormatValidator(ODMSASFormatNameValidator validator) {
        this.sasFormatValidator = validator;
    }

    public ODMSASFormatNameValidator getSasFormValidator() {
        return this.sasFormatValidator;
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public String getIndent() {
        return this.indent;
    }

    public void setUniqueNameTable(TreeSet<String> nameTable) {
        this.uniqueNameTable = nameTable;
    }

    public TreeSet<String> getUniqueNameTable() {
        return this.uniqueNameTable;
    }

    public void setODMVersion(String ODMVersion) {
        this.ODMVersion = ODMVersion;
    }

    public String getODMVersion() {
        return this.ODMVersion;
    }
}