package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.CrfVersionMedia;
import org.hibernate.Query;

public class CrfVersionMediaDao extends AbstractDomainDao<CrfVersionMedia> {

    @Override
    Class<CrfVersionMedia> domainClass() {
        return CrfVersionMedia.class;
    }

    public ArrayList<CrfVersionMedia> findByCrfVersionId(int crf_version_id) {
        String query = "from " + getDomainClassName() + " crf_version_media  where crf_version_media.crfVersion.crfVersionId = :crfversionid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("crfversionid", crf_version_id);
        return (ArrayList<CrfVersionMedia>) q.list();
    }

    public CrfVersionMedia findByCrfVersionIdAndFileName(int crf_version_id, String fileName) {
        String query = "from " + getDomainClassName() + " do  where do.crfVersion.crfVersionId = :crfversionid and do.name = :fileName";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("crfversionid", crf_version_id);
        q.setString("fileName", fileName);
        return (CrfVersionMedia) q.uniqueResult();
    }

}
