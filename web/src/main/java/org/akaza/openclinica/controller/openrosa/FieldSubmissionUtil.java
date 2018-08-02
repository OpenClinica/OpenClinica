package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.hibernate.UserTypeDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.user.UserType;
import org.akaza.openclinica.service.OCUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
public class FieldSubmissionUtil {

    @Autowired
    UserAccountDao userAccountDao;
    @Autowired
    UserTypeDao userTypeDao;
    @Bean
    @Scope(value = "prototype")
    @Lazy(value = true)
    public UserAccount getUserAccount(OCUserDTO userDTO) {
        UserAccount rootUser = userAccountDao.findByUserId(1);
        UserAccount createdUser = new UserAccount();
        createdUser.setUserName(userDTO.getUsername());
        createdUser.setUserUuid(userDTO.getUuid());
        createdUser.setFirstName(userDTO.getFirstName());
        createdUser.setLastName(userDTO.getLastName());
        createdUser.setEmail(userDTO.getEmail());
        createdUser.setRunWebservices(true);
        createdUser.setEnabled(true);
        UserType userType = new UserType(2);
        createdUser.setUserType(userType);
        createdUser.setUserAccount(rootUser);
        createdUser.setStatus(Status.AVAILABLE);
        // need to change schema here before saving, as user_id_seq is not available in the tenant schema
        String currentSchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        createdUser = userAccountDao.saveOrUpdate(createdUser);
        CoreResources.setRequestSchema(currentSchema);
        return createdUser;
    }
}
