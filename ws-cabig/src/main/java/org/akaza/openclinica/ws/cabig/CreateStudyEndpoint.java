/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;

import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.sql.DataSource;

public class CreateStudyEndpoint extends AbstractCabigDomEndpoint {
    
    public CreateStudyEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        
        super(dataSource, messages, coreResources);
    }
    
    protected Element invokeInternal(
            Element requestElement,
            Document document) throws Exception {
        System.out.println("Request text create study ");
        NodeList nlist = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studyProtocol");
        this.logNodeList(nlist);
        return mapRegisterSubjectConfirmation("null");
    }

}
