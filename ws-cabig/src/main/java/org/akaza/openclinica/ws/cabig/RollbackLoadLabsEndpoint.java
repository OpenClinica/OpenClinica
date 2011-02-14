package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;

public class RollbackLoadLabsEndpoint extends AbstractCabigDomEndpoint {

    public RollbackLoadLabsEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {

        super(dataSource, messages, coreResources);
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        System.out.println("Request text rollback load labs");
        NodeList nlist = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedClinicalResult");
        // result or results?
        this.logNodeList(nlist);
        return this.mapLoadLabsConfirmation();
        // return this.mapLoadLabsErrorConfirmation(message, exception)

    }

}
