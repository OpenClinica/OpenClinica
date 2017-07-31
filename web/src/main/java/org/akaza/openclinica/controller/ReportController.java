package org.akaza.openclinica.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

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
	RuleSetService ruleSetService;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * @api {post} /pages/healthcheck/runonschedule Evaluate runOnSchedule behavior
	 * @apiName ruleTrigger
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} serverZoneId Server TimeZone.
	 * @apiParam {String} ssZoneId Study Subject TimeZone .
	 * @apiParam {String} runTime Scheduled Run Time .
	 * @apiParam {String} serverTime Server Time .
	 * @apiGroup Rule
	 * @apiDescription Evaluate runOnSchedule behavior taking into consideration different time zones the subject and the server could be on.
	 * @apiParamExample {json} Request-Example:
	 *
	 *                  {
	 *                  "serverZoneId" :"America/New_York",
	 *                  "ssZoneId" :"America/New_York",
	 *                  "runTime" :"11",
	 *                  "serverTime" :"12"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  "result": false
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "result": true
	 *                    }
	 */

	@RequestMapping(value = "/runonschedule", method = RequestMethod.POST)
	public ResponseEntity<HashMap> ruleTrigger(@RequestBody HashMap<String, String> hashMap) throws Exception {

		String serverZoneId = hashMap.get("serverZoneId");
		String ssZoneId = hashMap.get("ssZoneId");
		String runTime = hashMap.get("runTime");
		String serverTime = hashMap.get("serverTime");

		HashMap<String, Boolean> map = new HashMap<>();
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		Boolean result = ruleSetService.calculateTimezoneDiff(TimeZone.getTimeZone(serverZoneId), TimeZone.getTimeZone(ssZoneId), Integer.valueOf(runTime), Integer.valueOf(serverTime));
		map.put("result", result);

		if (result) {
			return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);
		} else {
			return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * @api {post} /pages/healthcheck/rulecurrentdate Evaluate currentDate behavior
	 * @apiName getSSZone
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} serverZoneId Server TimeZone.
	 * @apiParam {String} ssZoneId Study Subject TimeZone .
	 * @apiGroup Rule
	 * @apiDescription Evaluate current date taking into consideration different time zones the subject and the server could be on.
	 * @apiParamExample {json} Request-Example:
	 *
	 *                  {
	 *                  "serverZoneId" :"America/New_York",
	 *                  "ssZoneId" :"America/New_York"
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "ssDate": "2015-10-07",
	 *                    "serverZoneId": "America/New_York",
	 *                    "serverDate": "2015-10-07"
	 *                    }
	 */

	@RequestMapping(value = "/rulecurrentdate", method = RequestMethod.POST)
	public ResponseEntity<HashMap> getSSZone(@RequestBody HashMap<String, String> hashMap) throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		String ssZoneId = hashMap.get("ssZoneId");
		String serverZoneId = hashMap.get("serverZoneId");
		HashMap<String, String> map = expressionService.getSSDate(ssZoneId, serverZoneId);
		return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

	}

	/**
	 * @api {post} /pages/healthcheck/runtime Retrieve runOnSchedule default runTime
	 * @apiName getRunTime
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiGroup Rule
	 * @apiDescription Retrieves the default runOnSchedule runtime for rules. The runOnSchedule when configured, allows you to run rules on a schedule.
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "result": 20
	 *                    }
	 */

	@RequestMapping(value = "/runtime", method = RequestMethod.POST)
	public ResponseEntity<HashMap> getRunTime() throws Exception {
		ResourceBundleProvider.updateLocale(new Locale("en_US"));
		HashMap<String, Integer> map = new HashMap<>();
		int result = ruleSetService.getRunTimeWhenTimeIsNotSet();
		map.put("result", result);
		return new ResponseEntity<HashMap>(map, org.springframework.http.HttpStatus.OK);

	}

}
