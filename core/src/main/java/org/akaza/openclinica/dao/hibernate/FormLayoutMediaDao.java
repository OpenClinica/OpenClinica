package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.hibernate.Query;

public class FormLayoutMediaDao extends AbstractDomainDao<FormLayoutMedia> {

    @Override
    Class<FormLayoutMedia> domainClass() {
        return FormLayoutMedia.class;
    }

    public ArrayList<FormLayoutMedia> findByFormLayoutIdForNoteTypeMedia(int formLayoutId) {
        String query = "from " + getDomainClassName()
                + " form_layout_media  where form_layout_media.formLayout.formLayoutId = :formlayoutid and form_layout_media.eventCrfId=0 ";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("formlayoutid", formLayoutId);
        return (ArrayList<FormLayoutMedia>) q.list();
    }

    public ArrayList<FormLayoutMedia> findByEventCrfId(int eventCrfId) {
        String query = "from " + getDomainClassName() + " form_layout_media  where form_layout_media.eventCrfId = :eventCrfId ";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        return (ArrayList<FormLayoutMedia>) q.list();
    }

    public FormLayoutMedia findByFormLayoutIdAndFileName(int formLayoutId, String fileName) {
        String query = "from " + getDomainClassName() + " do  where do.formLayout.formLayoutId = :formlayoutid and do.name = :fileName";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("formlayoutid", formLayoutId);
        q.setString("fileName", fileName);
        return (FormLayoutMedia) q.uniqueResult();
    }

    public FormLayoutMedia findByEventCrfIdAndFileName(int eventCrfId, String fileName) {
        String query = "from " + getDomainClassName() + " do  where do.eventCrfId = :eventCrfId and do.name = :fileName";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setString("fileName", fileName);
        return (FormLayoutMedia) q.uniqueResult();
    }

    public FormLayoutMedia findByFormLayoutIdAndFilePath(int formLayoutId, String dir) {
        String query = "from " + getDomainClassName() + " do  where do.formLayout.formLayoutId = :formlayoutid and do.path = :dir";
        Query q = getCurrentSession().createQuery(query);
        q.setInteger("formlayoutid", formLayoutId);
        q.setString("dir", dir);
        return (FormLayoutMedia) q.uniqueResult();
    }
}
