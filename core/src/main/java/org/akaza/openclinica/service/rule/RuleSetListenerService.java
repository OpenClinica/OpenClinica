package org.akaza.openclinica.service.rule;

import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class RuleSetListenerService implements ApplicationListener<OnStudyEventUpdated>  {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	

@Override
	public void onApplicationEvent(final OnStudyEventUpdated event) {
		LOGGER.debug("listening");
	
	}

}
