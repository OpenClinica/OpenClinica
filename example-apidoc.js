/**
 * @api {get} /rest/metadata/:html/print/:study/:event/:form Return printable blank case report forms
 * @apiVersion 3.7.0
 * @apiName GetPrintableCRF
 * @apiGroup Metadata
 * @apiPermission user
 *
 * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
 *
 * @apiParam {String} study Study or Site OID. Use '*' for all.
 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
 *
 * @apiExample Example usage - gets all forms in a study:
 * curl -i demo2.eclinicalhosting.com/OpenClinica9/rest/metadata/html/print/S_NCT02438/SE_ABC/F_123
 *
 * @apiSuccess {String}   ODM            The form(s) in HTML.
 *
 * @apiSuccessExample {html} Example success (html):
 *     HTTP/1.1 200 Success
 *     {
 *       "odm": "forms displayed here"
 *     }
 *
 * @apiError NoAccessRight Only authenticated users can access the data.
 * @apiError NotFound   The resource was not found.
 *
 * @apiErrorExample Response (example):
 *     HTTP/1.1 401 Not Authenticated
 *     {
 *       "error": "NoAccessRight"
 *     }
 */
/**
 * @api {get} /rest/metadata/xml/view/:study/:event/:form Return CDISC ODM XML case report form definitions
 * @apiVersion 3.7.0
 * @apiName GetODM
 * @apiGroup Metadata
 * @apiPermission user
 *
 * @apiDescription Return CDISC ODM XML study definition. Use asterisks in place of OIDs as wildcards
 *
 * @apiParam {String} study Study or Site OID. Use '*' for all.
 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
 *
 * @apiExample Example usage - gets all forms in a study:
 * curl -i demo2.eclinicalhosting.com/OpenClinica9/rest/metadata/xml/view/S_NCT02438/SE_ABC/F_123
 *
 * @apiSuccess {String}   ODM            The study metadata in ODM XML.
 *
 * @apiSuccessExample {xml} Example success (xml):
 *     HTTP/1.1 200 Success
 *     {
 *       "odm": "study ODM XML metadata here"
 *     }
 *
 * @apiError NoAccessRight Only authenticated users can access the data.
 * @apiError NotFound   The resource was not found.
 *
 * @apiErrorExample Response (example):
 *     HTTP/1.1 401 Not Authenticated
 *     {
 *       "error": "NoAccessRight"
 *     }
 */
/**
 * @api {get} /rest/metadata/json/view/:study/:event/:form Return CDISC ODM JSON case report form definitions
 * @apiVersion 3.7.0
 * @apiName GetODMJSON
 * @apiGroup Metadata
 * @apiPermission user
 *
 * @apiDescription Return CDISC ODM JSON study definition. Use asterisks in place of OIDs as wildcards
 *
 * @apiParam {String} study Study or Site OID. Use '*' for all.
 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
 *
 * @apiExample Example usage - gets all forms in a study:
 * curl -i demo2.eclinicalhosting.com/OpenClinica9/rest/metadata/json/view/S_NCT02438/SE_ABC/F_123
 *
 * @apiSuccess {String}   ODM            The form(s) in ODM JSON.
 *
 * @apiSuccessExample {json} Example success (json):
 *     HTTP/1.1 200 Success
 *     {
 *       "odm": "study ODM metadata here"
 *     }
 *
 * @apiError NoAccessRight Only authenticated users can access the data.
 * @apiError NotFound   The resource was not found.
 *
 * @apiErrorExample Response (example):
 *     HTTP/1.1 401 Not Authenticated
 *     {
 *       "error": "NoAccessRight"
 *     }
 */
/**
 * @api {get} /rest/clinicaldata/html/print/:study/:subject/:event/:form Return blank case report forms
 * @apiVersion 3.7.0
 * @apiName GetPrintableCRF
 * @apiGroup ClinicalData
 * @apiPermission user
 *
 * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
 *
 * @apiParam {String} study Study or Site OID.
 * @apiParam {String} subject Subject Key or ID.
 * @apiParam {String} event Study Event Definition OID. Use '*' for all.
 * @apiParam {String} form Case Report Form Version OID. Use '*' for all.
 *
 * @apiExample Example usage - gets all forms in a study:
 * curl -i demo2.eclinicalhosting.com/OpenClinica9/rest/clinicaldata/html/print/S_NCT02438/SS_123/SE_ABC/F_123
 *
 * @apiSuccess {String}   ODM            The form(s) in ODM XML, JSON, or HTML.
 *
 * @apiSuccessExample {json} Example success (json):
 *     HTTP/1.1 200 Success
 *     {
 *       "odm": "ODM JSON metadata and subject clinical data here"
 *     }
 *
 * @apiSuccessExample {xml} Example success (xml):
 *     HTTP/1.1 200 Success
 *     {
 *       "odm": "ODM XML metadata and subject clinical data here"
 *     }
 *
 * @apiSuccessExample {html} Example success (html):
 *     HTTP/1.1 200 Success
 *     {
 *       "odm": "subject casebook here"
 *     }
 *
 * @apiError NoAccessRight Only authenticated users can access the data.
 * @apiError NotFound   The resource was not found.
 *
 * @apiErrorExample Response (example):
 *     HTTP/1.1 401 Not Authenticated
 *     {
 *       "error": "NoAccessRight"
 *     }
 */

/**
	 * @api {post} /auth/api/v1/studies/:uniqueProtocolId/subjects Create New Study Subject in an OpenClinica Study (proposed; not implemented)
	 * @apiName createNewSite
	 * @apiPermission user
	 * @apiVersion 1.0.0
	 * @apiParam {String} uniqueProtocolId Study or site unique protocol ID or OID.
	 * @apiParam {String} ssid Study Subject ID (optional or required, depending on study configuration).
	 * @apiParam {Date} dob Date of Birth (optional or required, send full date or just year depending on study configuration).
	 * @apiParam {String} sex Sex (optional or required, depending on study configuration).
	 * @apiParam {Date} enrollmentDate Enrollment or registration date. Defaults to current date if omitted.
	 * @apiParam {Array} groups Assign the study subject to one or more Groups in this Study.
	 * @apiParam {Array} events Create one or more events for the study subject
	 * @apiParam {Array} data Add data to a CRF in the study subject's record. For path, use EVENT_OID[EVENT_ORDINAL]/CRF_OID/ITEMGROUP_OID[GROUP_ORDINAL]/ITEM_OID
	 * @apiGroup Subject
	 * @apiHeader {String} api_key User's unique access-key.
	 * @apiDescription This API is to create a study subject in an OpenClinica study or site in OC.
	 *                 All the fields are required fields and can't be left blank.
	 *                 You need to provide your Api-key to be connected.
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "uniqueProtocolId": "S_ABC1234",
	 *                  "dob": "1944-08-22",
	 *                  "sex": "m",
	 *                  "enrollmentDate": "2015-10-06",
	 *                  "groups": [
	 *                  { "groupClass" : "Arm", "group" : "Arm B"},
	 *                  { "groupClass" : "Age Group", "group" : ">60"}
	 *                  ],
	 *                  "event": [
	 *                  { "event" : "E_FIRSTVISIT", "startDate" : "2015-10-07"},
	 *                  { "event" : "E_FIRSTDIARY", "startDate" : "2015-10-08"} 
	 *                  ],
	 *                  "data": [
	 *                  { "path" : "E_CONSENT/F_INFORMED_CONSENT/IG_MAIN/I_CONSENTDATE", "value" : "2015-10-05"},
	 *                  { "path" : "E_CONSENT/F_INFORMED_CONSENT/IG_MAIN/I_CONSENTSIGNED", "value" : "Y"}
	 *                  ]
	 *                  }
	 * 
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  "message": "FAILED",
	 *                  "errors": [
	 *                  { "field": "UniqueProtocolId", "resource": "Stite Object","code": "Unique Protocol Id/OID must match a study or site OID for which you have been granted a user role" }
	 *                  ]
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "message": "SUCCESS",
	 *                    "ssid": "101",
	 *                    "subjectKey": "SS_101"
	 *                    }
	 */

