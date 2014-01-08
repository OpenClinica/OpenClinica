package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.UserAccount;

public class UserAccountDao extends AbstractDomainDao<UserAccount> {
	
    @Override
    public Class<UserAccount> domainClass() {
        return UserAccount.class;
    }
}
