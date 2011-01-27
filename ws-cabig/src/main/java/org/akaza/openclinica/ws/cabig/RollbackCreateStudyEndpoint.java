package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;

import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sql.DataSource;

public class RollbackCreateStudyEndpoint extends AbstractCabigDomEndpoint {
    
    public RollbackCreateStudyEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        
        super(dataSource, messages, coreResources);

    }
    
    protected Element invokeInternal(
            Element requestElement,
            Document document) throws Exception {
        System.out.println("Request text rollback create study ");
        return mapRegisterSubjectConfirmation("null");
    }

}
