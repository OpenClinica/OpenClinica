/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <P>
 * SectionDAO.java, the data access object for creation and access to the
 * sections of a CRF. CRFs will have more than one version, which in turn will
 * have one or more sections, which will have one or more items with metadata
 * for presentation.
 *
 * @author thickerson
 *
 *
 */
public class SectionDAO extends AuditableEntityDAO {

    // private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SECTION;
    }

    public SectionDAO(DataSource ds) {
        super(ds);
    }

    public SectionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public SectionDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);// crf version id
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);// label
        this.setTypeExpected(5, TypeNames.STRING);// title
        this.setTypeExpected(6, TypeNames.STRING);// subtitle
        this.setTypeExpected(7, TypeNames.STRING);// instructions
        this.setTypeExpected(8, TypeNames.STRING);// page num label
        this.setTypeExpected(9, TypeNames.INT);// order by
        this.setTypeExpected(10, TypeNames.INT);// parent id
        this.setTypeExpected(11, TypeNames.DATE);
        this.setTypeExpected(12, TypeNames.DATE);
        this.setTypeExpected(13, TypeNames.INT);// owner id
        this.setTypeExpected(14, TypeNames.INT);// update id
        this.setTypeExpected(15, TypeNames.INT);// borders

    }

    public EntityBean update(EntityBean eb) {
        SectionBean sb = (SectionBean) eb;
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sb.getCRFVersionId()));
        variables.put(new Integer(2), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(3), sb.getLabel());
        variables.put(new Integer(4), sb.getTitle());
        variables.put(new Integer(5), sb.getInstructions());
        variables.put(new Integer(6), sb.getSubtitle());
        variables.put(new Integer(7), sb.getPageNumberLabel());
        variables.put(new Integer(8), new Integer(sb.getOrdinal()));
        variables.put(new Integer(9), new Integer(sb.getUpdaterId()));
        variables.put(new Integer(10), new Integer(sb.getBorders()));
        variables.put(new Integer(11), new Integer(sb.getId()));
        this.execute(digester.getQuery("update"), variables);
        return eb;
    }

    public EntityBean create(EntityBean eb) {
        SectionBean sb = (SectionBean) eb;
        HashMap variables = new HashMap();

        variables.put(new Integer(1), new Integer(sb.getCRFVersionId()));
        variables.put(new Integer(2), new Integer(sb.getStatus().getId()));
        variables.put(new Integer(3), sb.getLabel());
        variables.put(new Integer(4), sb.getTitle());
        variables.put(new Integer(5), sb.getInstructions());
        variables.put(new Integer(6), sb.getSubtitle());
        variables.put(new Integer(7), sb.getPageNumberLabel());
        variables.put(new Integer(8), new Integer(sb.getOrdinal()));
        variables.put(new Integer(9), new Integer(sb.getParentId()));
        variables.put(new Integer(10), new Integer(sb.getOwnerId()));
        variables.put(new Integer(11), new Integer(sb.getBorders()));
        this.execute(digester.getQuery("create"), variables);
        return eb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        SectionBean eb = new SectionBean();
        this.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("section_id")).intValue());
        eb.setCRFVersionId(((Integer) hm.get("crf_version_id")).intValue());
        eb.setLabel((String) hm.get("label"));
        eb.setTitle((String) hm.get("title"));
        eb.setInstructions((String) hm.get("instructions"));
        eb.setSubtitle((String) hm.get("subtitle"));
        eb.setPageNumberLabel((String) hm.get("page_number_label"));
        eb.setOrdinal(((Integer) hm.get("ordinal")).intValue());
        eb.setParentId(((Integer) hm.get("parent_id")).intValue());
        eb.setBorders(((Integer) hm.get("borders")).intValue());
        return eb;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SectionBean eb = (SectionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findByVersionId(int ID) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByVersionId");
        ArrayList alist = this.selectByCache(sql, variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SectionBean eb = (SectionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findByLayoutId(int ID) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByLayoutId");
        ArrayList alist = this.selectByCache(sql, variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SectionBean eb = (SectionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public EntityBean findByPK(int ID) {
        SectionBean eb = new SectionBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.selectByCache(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (SectionBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    public ArrayList findAllByCRFVersionId(int crfVersionId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));

        return this.executeFindAllQuery("findAllByCRFVersion", variables);
    }

    private HashMap getNumItemsBySectionIdFromRows(ArrayList rows) {
        HashMap answer = new HashMap();
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            Integer sectionIdInt = (Integer) hm.get("section_id");
            Integer numItemsInt = (Integer) hm.get("num_items");

            if (numItemsInt != null && sectionIdInt != null) {
                answer.put(sectionIdInt, numItemsInt);
            }
        }

        return answer;
    }

    // JN : bySectionID indicates group by section id
    public HashMap getNumItemsBySectionId() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        String sql = digester.getQuery("getNumItemsBySectionId");
        ArrayList rows = this.select(sql);
        return getNumItemsBySectionIdFromRows(rows);
    }

    /**
     * Groups by sectionId and takes sectionID
     * 
     * @param sb
     * @return
     */
    public HashMap getNumItemsBySection(SectionBean sb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        String sql = digester.getQuery("getNumItemsBySection");
        ArrayList rows = this.select(sql);
        return getNumItemsBySectionIdFromRows(rows);

    }

    public HashMap getNumItemsPlusRepeatBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        String sql = digester.getQuery("getNumItemsPlusRepeatBySectionId");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap getNumItemsCompletedBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        String sql = digester.getQuery("getNumItemsCompletedBySectionId");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap getNumItemsCompletedBySection(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        String sql = digester.getQuery("getNumItemsCompletedBySection");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap getNumItemsPendingBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        String sql = digester.getQuery("getNumItemsPendingBySectionId");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap getNumItemsPendingBySection(EventCRFBean ecb, SectionBean sb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        variables.put(new Integer(1), new Integer(sb.getId()));
        String sql = digester.getQuery("getNumItemsPendingBySection");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap getNumItemsBlankBySectionId(EventCRFBean ecb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        String sql = digester.getQuery("getNumItemsBlankBySectionId");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public HashMap getNumItemsBlankBySection(EventCRFBean ecb, SectionBean sb) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id
        this.setTypeExpected(2, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));
        variables.put(new Integer(1), new Integer(sb.getId()));
        String sql = digester.getQuery("getNumItemsBlankBySectionId");

        ArrayList rows = this.select(sql, variables);
        return getNumItemsBySectionIdFromRows(rows);
    }

    public SectionBean findNext(EventCRFBean ecb, SectionBean current) {
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getCRFVersionId()));
        variables.put(new Integer(2), new Integer(current.getOrdinal()));

        String sql = digester.getQuery("findNext");
        ArrayList rows = this.select(sql, variables);

        SectionBean answer = new SectionBean();
        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = (SectionBean) getEntityFromHashMap(row);
        }

        return answer;
    }

    public SectionBean findPrevious(EventCRFBean ecb, SectionBean current) {
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getCRFVersionId()));
        variables.put(new Integer(2), new Integer(current.getOrdinal()));

        String sql = digester.getQuery("findPrevious");
        ArrayList rows = this.select(sql, variables);
        SectionBean answer = new SectionBean();

        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = (SectionBean) getEntityFromHashMap(row);
        }

        return answer;
    }

    public void deleteTestSection(String label) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        this.execute(digester.getQuery("deleteTestSection"), variables);
    }

    public boolean hasSCDItem(Integer sectionId) {
        return countSCDItemBySectionId(sectionId) > 0;
    }

    public int countSCDItemBySectionId(Integer sectionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // count

        HashMap variables = new HashMap();
        variables.put(new Integer(1), sectionId);
        ArrayList rows = this.select(digester.getQuery("countSCDItemBySectionId"), variables);
        if (rows.size() > 0) {
            return (Integer) ((HashMap) rows.iterator().next()).get("count");
        } else {
            return 0;
        }
    }

    public boolean containNormalItem(Integer crfVersionId, Integer sectionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // item_id

        HashMap variables = new HashMap();
        variables.put(new Integer(1), sectionId);
        variables.put(new Integer(2), crfVersionId);
        variables.put(new Integer(3), crfVersionId);
        variables.put(new Integer(4), sectionId);
        variables.put(new Integer(5), crfVersionId);
        ArrayList rows = this.select(digester.getQuery("containNormalItem"), variables);
        if (rows.size() > 0) {
            return (Integer) ((HashMap) rows.iterator().next()).get("item_id") > 0;
        } else {
            return false;
        }
    }

    public HashMap getSectionIdForTabId(int crfVersionId, int tabId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // section_id

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), new Integer(tabId));

        ArrayList rows = this.select(digester.getQuery("getSectionIdForTabId"), variables);
        return getSectionIdFromRows(rows);
    }

    private HashMap getSectionIdFromRows(ArrayList rows) {
        HashMap answer = new HashMap();
        Iterator it = rows.iterator();
        HashMap hm = new HashMap();
        while (it.hasNext()) {
            hm = (HashMap) it.next();
        }

        return hm;
    }
}