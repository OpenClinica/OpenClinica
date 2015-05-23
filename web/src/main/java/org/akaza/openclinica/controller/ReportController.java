package org.akaza.openclinica.controller;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.JobTriggerService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

@Controller
@RequestMapping(value = "/healthcheck")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class ReportController {

	// Add in Spring Cor files /healthcheck path to avoid firewall
	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired
	ExpressionService expressionService;

	@Autowired
	JobTriggerService jobTriggerService;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@RequestMapping(value = "/runonschedule", method = RequestMethod.POST)
	public ResponseEntity<HashMap> ruleTrigger(@RequestBody HashMap<String, String> hashMap) throws Exception {

		String serverZoneId = hashMap.get("serverZoneId");
		String ssZoneId = hashMap.get("ssZoneId");
		String runTime = hashMap.get("runTime");
		String serverTime = hashMap.get("serverTime");

		HashMap<String, Boolean> map = new HashMap<>();
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		System.out.println("I'm in rest call");
		Boolean result = jobTriggerService.calculateTimezoneDiff(TimeZone.getTimeZone(serverZoneId), TimeZone.getTimeZone(ssZoneId), Integer.valueOf(runTime), Integer.valueOf(serverTime));
		map.put("result", result);

		if (result) {
			return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
		} else {
			return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

		}

	}

	@RequestMapping(value = "/rulecurrentdate", method = RequestMethod.POST)
	public ResponseEntity<HashMap> getSSZone(@RequestBody HashMap<String, String> hashMap) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		String ssZoneId = hashMap.get("ssZoneId");
		String serverZoneId = hashMap.get("serverZoneId");
		System.out.println("I'm in rest call");
		HashMap<String, String> map = expressionService.getSSDate(ssZoneId,serverZoneId );
		return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

	}

}
