package core.org.akaza.openclinica.web.rest.client.us.impl;

import core.org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class UserServiceClientImpl {
    private String sbsUrl = CoreResources.getField("SBSBaseUrl");
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

}
