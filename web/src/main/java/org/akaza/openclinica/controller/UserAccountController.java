package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.user.CreateUserCoreService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@Controller
@RequestMapping(value = "/auth/api/v1")
@ResponseStatus(value = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
public class UserAccountController {

	@Autowired
	@Qualifier("dataSource")
	private BasicDataSource dataSource;

	@Autowired

	ServletContext context;

	@Autowired
	AuthoritiesDao authoritiesDao;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	StudyDAO sdao;
	@Autowired CreateUserCoreService userCoreService;

	/**
	 * @api {post} /pages/auth/api/v1/createuseraccount Create a user account
	 * @apiName createOrUpdateAccount2
	 * @apiPermission admin
	 * @apiVersion 3.8.0
	 * @apiParam {String} username UserName
	 * @apiParam {String} fName First Name
	 * @apiParam {String} lName Last Name
	 * @apiParam {String} institution Institution
	 * @apiParam {String} email Email Address
	 * @apiParam {String} study_name Study Name
	 * @apiParam {String} role_name Role Name
	 * @apiParam {String} user_type User Type
	 * @apiParam {String} authorize_soap Authorize Soap
	 *
	 * @apiGroup User Account
	 * @apiDescription Creates a user account
	 * @apiParamExample {json} Request-Example:
	 *                  {
	 *                  "username": "testingUser",
	 *                  "fName": "Jimmy",
	 *                  "lName": "Sander",
	 *                  "institution": "OC",
	 *                  "email": "abcde@yahoo.com",
	 *                  "study_name": "Baseline Study 101",
	 *                  "role_name": "Data Manager",
	 *                  "user_type": "user",
	 *                  "authorize_soap":"false"
	 *                  }
	 * @apiErrorExample {json} Error-Response:
	 *                  HTTP/1.1 400 Bad Request
	 *                  {
	 *                  }
	 * @apiSuccessExample {json} Success-Response:
	 *                    HTTP/1.1 200 OK
	 *                    {
	 *                    "lastName": "Sander",
	 *                    "username": "testingUser",
	 *                    "firstName": "Jimmy",
	 *                    "password": "rgluVsO0",
	 *                    "apiKey": "5f462a16b3b04b1b9747262968bd5d2f"
	 *                    }
	 */

	@RequestMapping(value = "/createuseraccount", method = RequestMethod.POST)
	public ResponseEntity<HashMap> createOrUpdateAccount(HttpServletRequest request, @RequestBody HashMap<String, String> map) throws Exception {
		UserAccountBean  uBean = userCoreService.createUser(request, map);
		HashMap<String, Object> userDTO = new HashMap<String, Object>();
		userDTO.put("username", uBean.getName());
		userDTO.put("password", uBean.getPasswd());
		userDTO.put("firstName", uBean.getFirstName());
		userDTO.put("lastName", uBean.getLastName());
		userDTO.put("apiKey", uBean.getApiKey());
		userDTO.put("userUuid", uBean.getUserUuid());
		return new ResponseEntity<HashMap>(userDTO, org.springframework.http.HttpStatus.OK);
	}

	protected UserDetails getUserDetails() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			return (UserDetails) principal;
		} else {
			return null;
		}
	}
}
