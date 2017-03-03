package org.akaza.openclinica.service;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.processor.QueryServiceHelperBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.akaza.openclinica.service.crfdata.EnketoUrlService.FS_QUERY_ATTRIBUTE;

/**
 * Created by yogi on 11/10/16.
 */
public interface ProtocolBuildService {
    Logger logger = LoggerFactory.getLogger(ProtocolBuildService.class);
    public String process(String name, String uniqueId, HttpServletRequest request);
}
