/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 *//* OpenClinica is distributed under the GNU Lesser General Public License (GNU
   * LGPL).
   *
   * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
   * Research
   *
   */
package org.akaza.openclinica.bean.extract.odm;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.akaza.openclinica.bean.odmbeans.LocationBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionRefBean;
import org.akaza.openclinica.bean.odmbeans.OdmAdminDataBean;
import org.akaza.openclinica.bean.odmbeans.UserBean;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Create ODM XML AdminData Element for a study.
 * 
 * @author ywang (March, 2010)
 */

public class AdminDataReportBean extends OdmXmlReportBean {
    private OdmAdminDataBean adminData;
    private LinkedHashMap<String, OdmAdminDataBean> odmAdminDataMap;

    public AdminDataReportBean(OdmAdminDataBean adminData) {
        super();
        this.adminData = adminData;
    }

    public AdminDataReportBean(LinkedHashMap<String, OdmAdminDataBean> odmAdminDataMap) {
        super();
        this.odmAdminDataMap = odmAdminDataMap;
    }

    private static String nls = System.getProperty("line.separator");

    public void createChunkedOdmXml(boolean isDataset) {
        String ODMVersion = this.getODMVersion();
        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            Iterator<OdmAdminDataBean> ita = this.odmAdminDataMap.values().iterator();
            while (ita.hasNext()) {
                OdmAdminDataBean a = ita.next();
                addNodeAdminData(a);
            }
        }
    }

    public void addNodeAdminData(OdmAdminDataBean a) {
        if (a.getUsers().size() > 0) {
            StringBuffer xml = this.getXmlOutput();
            String indent = this.getIndent();
            xml.append(indent + "<AdminData StudyOID=\"" + StringEscapeUtils.escapeXml(a.getStudyOID()) + "\">");
            xml.append(nls);
            for (UserBean u : a.getUsers()) {
                addOneUser(u, indent + indent);
            }
            // for(LocationBean l : this.adminData.getLocations()) {
            // addOneLocation(l, indent+indent);
            // }
            xml.append(indent + "</AdminData>");
            xml.append(nls);
        }
    }

    public void addNodeAdminData() {
        if (this.adminData.getUsers().size() > 0) {
            StringBuffer xml = this.getXmlOutput();
            String indent = this.getIndent();
            xml.append(indent + "<AdminData StudyOID=\"" + StringEscapeUtils.escapeXml(adminData.getStudyOID()) + "\">");
            xml.append(nls);
            for (UserBean u : this.adminData.getUsers()) {
                addOneUser(u, indent + indent);
            }
            // for(LocationBean l : this.adminData.getLocations()) {
            // addOneLocation(l, indent+indent);
            // }
            xml.append(indent + "</AdminData>");
            xml.append(nls);
        }
    }

    public void addOneUser(UserBean user, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        xml.append(currentIndent + "<User OID=\"" + StringEscapeUtils.escapeXml(user.getOid()) + "\">");
        xml.append(nls);
        String fn = user.getFirstName() != null ? user.getFirstName() : "";
        String ln = user.getLastName() != null ? user.getLastName() : "";
        String fullName = fn.length() > 0 && ln.length() > 0 ? fn + " " + ln : "";
        if (fullName.length() > 0) {
            xml.append(currentIndent + indent + "<FullName>" + StringEscapeUtils.escapeXml(fullName) + "</FullName>");
            xml.append(nls);
        }
        if (fn.length() > 0) {
            xml.append(currentIndent + indent + "<FirstName>" + StringEscapeUtils.escapeXml(fn) + "</FirstName>");
            xml.append(nls);
        }
        if (ln.length() > 0) {
            xml.append(currentIndent + indent + "<LastName>" + StringEscapeUtils.escapeXml(ln) + "</LastName>");
            xml.append(nls);
        }
        String og = user.getOrganization() != null ? user.getOrganization() : "";
        if (og.length() > 0) {
            xml.append(currentIndent + indent + "<Organization>" + StringEscapeUtils.escapeXml(og) + "</Organization>");
            xml.append(nls);
        }
        xml.append(currentIndent + "</User>");
        xml.append(nls);
    }

    public void addOneLocation(LocationBean loc, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        xml.append(currentIndent + "<Location OID=\"" + StringEscapeUtils.escapeXml(loc.getOid()) + "\" Name=\"" + StringEscapeUtils.escapeXml(loc.getName())
                + "\">");
        xml.append(nls);
        MetaDataVersionRefBean m = loc.getMetaDataVersionRef();
        xml.append(currentIndent + indent + "<MetaDataVersionRef StudyOID=\"" + StringEscapeUtils.escapeXml(m.getStudyOID()) + "\" MetaDataVersionOID=\""
                + StringEscapeUtils.escapeXml(m.getElementDefOID()) + "\" EffectiveDate=\"" + new SimpleDateFormat("yyyy-MM-dd").format(m.getEffectiveDate())
                + "\"/>");
        xml.append(nls);
        xml.append(currentIndent + "</Location>");
        xml.append(nls);
    }

    public OdmAdminDataBean getAdminData() {
        return adminData;
    }

    public void setAdminData(OdmAdminDataBean adminData) {
        this.adminData = adminData;
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