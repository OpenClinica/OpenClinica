package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.core.OCCreatePostgresAppServer;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.OCUserDTO;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

@Configuration
public class FieldSubmissionUtil {

    @Autowired
    UserAccountDao userAccountDao;
    @Bean
    @Scope(value = "prototype")
    @Lazy(value = true)
    public UserAccount getUserAccount(OCUserDTO userDTO) {
        UserAccount rootUser = userAccountDao.findByUserId(1);
        UserAccount createdUser = new UserAccount();

        return createdUser;
    }
}
