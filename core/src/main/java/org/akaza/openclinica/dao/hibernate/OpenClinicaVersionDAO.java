/**
 * 
 */
package org.akaza.openclinica.dao.hibernate;

import java.sql.Timestamp;

import org.akaza.openclinica.domain.OpenClinicaVersionBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author pgawade
 *
 */
public class OpenClinicaVersionDAO extends AbstractDomainDao<OpenClinicaVersionBean> {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass().getName());
    @Override
    public Class<OpenClinicaVersionBean> domainClass() {
        return OpenClinicaVersionBean.class;
    }

    @Transactional
    public OpenClinicaVersionBean findDefault() {
        String query = "from " + getDomainClassName() + " ocVersion";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (OpenClinicaVersionBean) q.uniqueResult();
    }

    @Transactional
    public void saveOCVersionToDB(String OpenClinicaVersion) {
        logger.debug("OpenClinicaVersionDAO -> saveOCVersionToDB");
        logger.debug("OpenClinicaVersion: " + OpenClinicaVersion);
        // Delete the previous entry if exists in the database
        deleteDefault();
        // Insert new entry
        Timestamp currentTimestamp = new Timestamp(new java.util.Date().getTime());
        OpenClinicaVersionBean openClinicaVersionBean = new OpenClinicaVersionBean();
        openClinicaVersionBean.setName(OpenClinicaVersion);
        openClinicaVersionBean.setUpdate_timestamp(currentTimestamp);
        saveOrUpdate(openClinicaVersionBean);

    }

    @Transactional
    public int deleteDefault() {
        String query = "delete from " + getDomainClassName() + " ocVersion";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return q.executeUpdate();
    }

}
