package org.akaza.openclinica.dao.hibernate.multitenant;

import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

/**
 * Created by yogi on 1/23/17.
 */
@ComponentScan("org.akaza.openclinica.dao.hibernate.multitenant")
public class SchemaChangingServiceImpl  implements SchemaChangingService {
    @Autowired
    private StudySubjectDao studySubjectDao;

    public String changeSchema(HttpSession session, String tenant) {
        if (StringUtils.isNotEmpty(tenant)) {
            session.setAttribute("current_tenant_id", tenant);
        }
        return studySubjectDao.findById(1).getLabel();

    }
}
