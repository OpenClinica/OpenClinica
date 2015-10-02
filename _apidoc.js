// ------------------------------------------------------------------------------------------
// General apiDoc documentation blocks and old history blocks.
// ------------------------------------------------------------------------------------------

// ------------------------------------------------------------------------------------------
// Current Success.
// ------------------------------------------------------------------------------------------


// ------------------------------------------------------------------------------------------
// Current Errors.
// ------------------------------------------------------------------------------------------



// ------------------------------------------------------------------------------------------
// Current Permissions.
// ------------------------------------------------------------------------------------------
/**
 * @apiDefinePermission admin Admin access rights needed.
 *
 * @apiVersion 3.7.0
 */
/**
 * @apiDefinePermission user Study or Site user role needed.
 * See https://docs.openclinica.com for more information on user roles
 *
 * @apiVersion 3.7.0
 */

// ------------------------------------------------------------------------------------------
// History.
// ------------------------------------------------------------------------------------------
/**
 * @apiDefinePermission admin This title is visible in version 3.6.0
 * @apiVersion 3.6.0
 */

/**
 * @api {get} /rest/metadata/:html/print/:study/:event/:form Return printable blank case report forms
 * @apiVersion 3.6.0
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
 * @apiVersion 3.6.0
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
 * @apiVersion 3.6.0
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
 * @apiVersion 3.6.0
 * @apiName GetPrintableCRF
 * @apiGroup ClinicalData
 * @apiPermission user
 *
 * @apiDescription Annotated case report forms in printable HTML format. Use asterisks in place of OIDs as wildcards
 *
 * @apiParam {String} study Study or Site OID.
 * @apiParam {String} subject Subject OID.
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

