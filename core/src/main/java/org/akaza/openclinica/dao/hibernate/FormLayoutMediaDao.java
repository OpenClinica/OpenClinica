package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.hibernate.Query;

public class FormLayoutMediaDao extends AbstractDomainDao<FormLayoutMedia> {

    @Override
    Class<FormLayoutMedia> domainClass() {
        return FormLayoutMedia.class;
    }

    public ArrayList<FormLayoutMedia> findByFormLayoutId(int formLayoutId) {
        String query = "from " + getDomainClassName() + " form_layout_media  where form_layout_media.formLayout.formLayoutId = :formlayoutid ";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("formlayoutid", formLayoutId);
        return (ArrayList<FormLayoutMedia>) q.list();
    }

    public FormLayoutMedia findByFormLayoutIdAndFileName(int formLayoutId, String fileName) {
        String query = "from " + getDomainClassName() + " do  where do.formLayout.formLayoutId = :formlayoutid and do.name = :fileName";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("formlayoutid", formLayoutId);
        q.setString("fileName", fileName);
        return (FormLayoutMedia) q.uniqueResult();
    }

}
