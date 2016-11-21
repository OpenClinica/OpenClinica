package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import static org.akaza.openclinica.service.crfdata.EnketoUrlService.FS_QUERY_ATTRIBUTE;

/**
 * Created by yogi on 11/10/16.
 */
public interface QueryService {
    Logger logger = LoggerFactory.getLogger(QueryService.class);

    void process(SubmissionContainer container, CrfVersion crfVersion, EventCrf eventCrf) throws Exception;
    default String getQueryAttribute(Node itemNode) {
        if (!itemNode.hasAttributes())
            return null;
        NamedNodeMap attributes = itemNode.getAttributes();
        for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
            if (attributes.item(attrIndex).getNodeName().equals(FS_QUERY_ATTRIBUTE)) {
                return attributes.item(attrIndex).getNodeValue();
            }
        }
        return null;
    }
}
