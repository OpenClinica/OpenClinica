package org.akaza.openclinica.dao.hibernate.multitenant;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * Created by yogi on 1/23/17.
 */
@Service
public interface SchemaChangingService {
    public String changeSchema(HttpSession session, String tenant);
}
