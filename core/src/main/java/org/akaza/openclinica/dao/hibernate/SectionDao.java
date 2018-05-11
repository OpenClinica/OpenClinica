package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Section;

public class SectionDao extends AbstractDomainDao<Section> {

    @Override
    Class<Section> domainClass() {
        // TODO Auto-generated method stub
        return Section.class;
    }

    public Section findByCrfVersionOrdinal(int crfVersionId, int ordinal) {
        // String query = "from " + getDomainClassName() + " section  where section.crfVersionId = :crfversionid ";
        // org.hibernate.Query q = getCurrentSession().createQuery(query);
        // q.set.setInteger("crfversionid", crf_version_id);
        // return (Section) q.uniqueResult();

        String query = " select s.* from section s where s.crf_version_id = :crfVersionId and ordinal = :ordinal ";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(domainClass());
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("ordinal", ordinal);
        q.setCacheable(true);
        return (Section) q.uniqueResult();
    }

}
