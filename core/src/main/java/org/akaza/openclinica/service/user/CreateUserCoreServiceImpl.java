package org.akaza.openclinica.service.user;

import liquibase.util.StringUtils;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.*;

@Service("createUserCoreService")
public class CreateUserCoreServiceImpl implements CreateUserCoreService {
    @Autowired AuthoritiesDao authoritiesDao;
    UserAccountDAO udao;
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public UserAccountBean createUser(HttpServletRequest request, HashMap<String, String> map) throws Exception {
        logger.info("In createUserAccount");
        UserAccountBean uBean = null;

        ZonedDateTime java8DateTime = ZonedDateTime.now();
        String username = map.get("username");
        String fName = map.get("fName");
        String lName = map.get("lName");
        String institution = map.get("institution");
        String email = map.get("email");
        String roleName = map.get("role_name");
        String userType = map.get("user_type");
        String apiKey = map.get("api_key");
        apiKey = apiKey == null ? "" : apiKey;
        String authorizeSoap = map.get("authorize_soap"); // true or false
        String userUuid = map.get("user_uuid");
        request.setAttribute("username", username);
        request.setAttribute("fName", fName);
        request.setAttribute("lName", lName);
        request.setAttribute("institution", institution);
        request.setAttribute("email", email);
        request.setAttribute("role_name", roleName);

        UserAccountBean ownerUserAccount = (UserAccountBean) request.getSession().getAttribute("userBean");
        if (!ownerUserAccount.isActive() && (!ownerUserAccount.isTechAdmin() || !ownerUserAccount.isSysAdmin())) {
            logger.info("The Owner User Account is not Valid Account or Does not have Admin user type");
            System.out.println("The Owner User Account is not Valid Account or Does not have Admin user type");
            return uBean;
        }
        String password = "password";
        String passwordHash = "5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8";

        // Validate Entry Fields
        Locale locale = new Locale("en_US");
        request.getSession().setAttribute(LocaleResolver.getLocaleSessionAttributeName(), locale);
        ResourceBundleProvider.updateLocale(locale);

        // Role
        ResourceBundle resterm = org.akaza.openclinica.i18n.util.ResourceBundleProvider.getTermsBundle();
        Map<Integer, String> roleMap = buildRoleMap(false, resterm);
        boolean found = false;
        Role role = null;
        for (Map.Entry<Integer, String> entry : roleMap.entrySet()) {
            if (roleName.equalsIgnoreCase(entry.getValue())) {
                Integer key = entry.getKey();
                role = Role.get(key);
                found = true;
                break;
            }
        }

        if (!found) {
            logger.error("The Role is not a Valid Role for the Study or Site");
            return uBean;
        }

        // User Types
        UserType uType = null;
        ArrayList<UserType> types = UserType.toArrayList();
        types.remove(UserType.INVALID);
        switch (userType) {
        case "Business Admin":
            uType = UserType.SYSADMIN;
            break;
        case "Tech Admin":
            uType = UserType.TECHADMIN;
            break;
        case "User":
            uType = UserType.USER;
            break;
        default:
            uType = UserType.INVALID;
            break;
        }


        if (uType == UserType.INVALID) {
            logger.error("The Type is not a Valid User Type");
            return uBean;
        }
        // build UserName

        uBean = buildUserAccount(username, fName, lName, password, institution, ownerUserAccount, email, passwordHash, Boolean.valueOf(authorizeSoap), role, uType);
        uBean.setApiKey(apiKey);
        uBean.setUserUuid(userUuid);

        UserAccountBean uaBean = getUserAccount(uBean.getName());
        if (!uaBean.isActive()) {
            createUserAccount(uBean);
            uBean.setUpdater(uBean.getOwner());
            updateUserAccount(uBean);
            logger.info("***New User Account is created***");
            uBean.setPasswd(password);
        }
        request.setAttribute("createdUaBean", uBean);
        return uBean;
    }

    private UserAccountBean getUserAccount(String userName) {
        udao = new UserAccountDAO(dataSource);
        UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
        return userAccountBean;
    }

    private UserAccountBean buildUserAccount(String username, String fName, String lName, String password, String institution,
                                             UserAccountBean ownerUserAccount, String email,
            String passwordHash, Boolean authorizeSoap, Role roleName, UserType userType) throws Exception {

        UserAccountBean createdUserAccountBean = new UserAccountBean();

        createdUserAccountBean.setName(username);
        createdUserAccountBean.setFirstName(fName);
        createdUserAccountBean.setLastName(lName);
        createdUserAccountBean.setEmail(email);
        createdUserAccountBean.setInstitutionalAffiliation(institution);
        createdUserAccountBean.setLastVisitDate(null);
        createdUserAccountBean.setPasswdTimestamp(null);
        createdUserAccountBean.setPasswdChallengeQuestion("");
        createdUserAccountBean.setPasswdChallengeAnswer("");
        createdUserAccountBean.setOwner(ownerUserAccount);
        createdUserAccountBean.setRunWebservices(true);
        createdUserAccountBean.setPhone("");
        createdUserAccountBean.setAccessCode("");
        createdUserAccountBean.setPasswd(password);
        createdUserAccountBean.setEmail(email);
        createdUserAccountBean.setEnableApiKey(true);
        createdUserAccountBean.setPasswd(passwordHash);
        createdUserAccountBean.setRunWebservices(authorizeSoap);


        createdUserAccountBean.setApiKey(username);

        createdUserAccountBean.addUserType(userType);

        String requestSchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        authoritiesDao.saveOrUpdate(new AuthoritiesBean(createdUserAccountBean.getName()));
        if (StringUtils.isNotEmpty(requestSchema))
            CoreResources.setRequestSchema(requestSchema);
        return createdUserAccountBean;
    }

    private void updateUserAccount(UserAccountBean userAccountBean) {
        udao.update(userAccountBean);
    }

    private ArrayList getRoles() {

        ArrayList roles = Role.toArrayList();
        roles.remove(Role.ADMIN);

        return roles;
    }

    private void createUserAccount(UserAccountBean userAccountBean) {
        udao = new UserAccountDAO(dataSource);
        udao.create(userAccountBean);
    }

    public Map buildRoleMap(boolean siteFlag, ResourceBundle resterm) {
        Map roleMap = new LinkedHashMap();

        if (siteFlag) {
            for (Iterator it = getRoles().iterator(); it.hasNext();) {
                Role role = (Role) it.next();
                switch (role.getId()) {
                // case 2: roleMap.put(role.getId(), resterm.getString("site_Study_Coordinator").trim());
                // break;
                // case 3: roleMap.put(role.getId(), resterm.getString("site_Study_Director").trim());
                // break;
                case 4:
                    roleMap.put(role.getId(), resterm.getString("site_investigator").trim());
                    break;
                case 5:
                    roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Person").trim());
                    break;
                case 6:
                    roleMap.put(role.getId(), resterm.getString("site_monitor").trim());
                    break;
                case 7:
                    roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Person2").trim());
                    break;
                case 8:
                    roleMap.put(role.getId(), resterm.getString("site_Data_Entry_Participant").trim());
                    break;
                default:
                    // logger.info("No role matched when setting role description");
                }
            }
        } else {
            for (Iterator it = getRoles().iterator(); it.hasNext();) {
                Role role = (Role) it.next();
                switch (role.getId()) {
                case 2:
                    roleMap.put(role.getId(), resterm.getString("Study_Coordinator").trim());
                    break;
                case 3:
                    roleMap.put(role.getId(), resterm.getString("Study_Director").trim());
                    break;
                case 4:
                    roleMap.put(role.getId(), resterm.getString("Investigator").trim());
                    break;
                case 5:
                    roleMap.put(role.getId(), resterm.getString("Data_Entry_Person").trim());
                    break;
                case 6:
                    roleMap.put(role.getId(), resterm.getString("Monitor").trim());
                    break;
                default:
                    // logger.info("No role matched when setting role description");
                }
            }
        }
        return roleMap;
    }

}
