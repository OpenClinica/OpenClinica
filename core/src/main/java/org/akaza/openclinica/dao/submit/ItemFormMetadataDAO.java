/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.sql.DataSource;

/**
 * @author ssachs
 */
public class ItemFormMetadataDAO extends EntityDAO {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEMFORMMETADATA;
    }

    private void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        getNextPKName = "getNextPK";
    }

    public ItemFormMetadataDAO(DataSource ds) {
        super(ds);
    }

    public ItemFormMetadataDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public ItemFormMetadataDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    /**
     * Search for a set of ItemFormMetadataBean objects.
     * 
     * @param ints
     *            An array of primary keys.
     * @return An ArrayList of ItemFormMetadataBean, each one corresponding to
     *         the primary key in <code>ints</code>.
     * @throws OpenClinicaException
     */
    public ArrayList findByMultiplePKs(ArrayList ints) throws OpenClinicaException {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        Iterator it = ints.iterator();
        while (it.hasNext()) {
            Integer newInt = (Integer) it.next();
            ItemFormMetadataBean ifmBean = (ItemFormMetadataBean) this.findByPK(newInt.intValue());
            // check to make sure we have what we need
            logger.info("options: " + ifmBean.getResponseSetId() + " bean options list: " + ifmBean.getResponseSet().getOptions().toString());
            /*
             * ArrayList options = ifmBean.getResponseSet().getOptions();
             * Iterator robit = options.iterator(); while (robit.hasNext()) {
             * ResponseOptionBean rob = (ResponseOptionBean)robit.next();
             * logger.info("rob text: "+rob.getText()); logger.info("value:
             * "+rob.getValue()); }
             */
            answer.add(ifmBean);
        }
        return answer;
    }

    private int getIntFromRow(HashMap row, String column) {
        Integer i = (Integer) row.get(column);
        if (i == null) {
            return 0;
        } else {
            return i.intValue();
        }
    }

    private boolean getBooleanFromRow(HashMap row, String column) {
        Boolean i = (Boolean) row.get(column);
        if (i == null) {
            return false;
        } else {
            return i.booleanValue();
        }
    }

    private String getStringFromRow(HashMap row, String column) {
        String s = (String) row.get(column);
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#getEntityFromHashMap(java.util.HashMap)
     */
    public Object getEntityFromHashMap(HashMap hm) {
        ItemFormMetadataBean answer = new ItemFormMetadataBean();

        answer.setId(getIntFromRow(hm, "item_form_metadata_id"));
        answer.setItemId(getIntFromRow(hm, "item_id"));
        answer.setCrfVersionId(getIntFromRow(hm, "crf_version_id"));
        answer.setHeader(getStringFromRow(hm, "header"));
        answer.setSubHeader(getStringFromRow(hm, "subheader"));
        answer.setParentId(getIntFromRow(hm, "parent_id"));
        answer.setParentLabel(getStringFromRow(hm, "parent_label"));
        answer.setColumnNumber(getIntFromRow(hm, "column_number"));
        answer.setPageNumberLabel(getStringFromRow(hm, "page_number_label"));
        answer.setQuestionNumberLabel(getStringFromRow(hm, "question_number_label"));
        answer.setLeftItemText(getStringFromRow(hm, "left_item_text"));
        answer.setRightItemText(getStringFromRow(hm, "right_item_text"));
        answer.setSectionId(getIntFromRow(hm, "section_id"));
        answer.setDescisionConditionId(getIntFromRow(hm, "decision_condition_id"));
        answer.setResponseSetId(getIntFromRow(hm, "response_set_id"));
        answer.setRegexp(getStringFromRow(hm, "regexp"));
        answer.setRegexpErrorMsg(getStringFromRow(hm, "regexp_error_msg"));
        answer.setOrdinal(getIntFromRow(hm, "ordinal"));
        answer.setRequired(getBooleanFromRow(hm, "required"));
        // YW 08-02-2007, default_value column has been added
        answer.setDefaultValue(getStringFromRow(hm, "default_value"));
        answer.setResponseLayout(getStringFromRow(hm, "response_layout"));
        answer.setWidthDecimal(getStringFromRow(hm, "width_decimal"));
        // answer.setShowItem(((Boolean) hm.get("show_item")).booleanValue());
        answer.setShowItem(getBooleanFromRow(hm, "show_item"));
        // System.out.println("found show item: " + getBooleanFromRow(hm, "show_item"));
        // now get the response set
        ResponseSetBean rsb = new ResponseSetBean();

        rsb.setId(getIntFromRow(hm, "response_set_id"));
        rsb.setLabel(getStringFromRow(hm, "label"));
        rsb.setResponseTypeId(getIntFromRow(hm, "response_type_id"));

        String optionsText = getStringFromRow(hm, "options_text");
        String optionsValues = getStringFromRow(hm, "options_values");
        rsb.setOptions(optionsText, optionsValues);
        answer.setResponseSet(rsb);

        return answer;
    }

    public void setTypesExpected() {
        this.unsetTypeExpected();

        int ind = 1;
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // item form metadata id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // item id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // crf version id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // header
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // subheader
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // parent id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // parent label
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // column number
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // page number label
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // question number label
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // left item text
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // right item text
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // section id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // decision condition id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // response set id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // regexp
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // regexp error msg
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // ordinal
        this.setTypeExpected(ind, TypeNames.BOOL);
        ind++; // required
        this.setTypeExpected(ind, TypeNames.STRING); // default_value
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING); // response_layout
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING); // width_decimal
        ind++;
        // will need to set the boolean value here, tbh
        this.setTypeExpected(ind, TypeNames.BOOL);
        ind++; // show_item
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // response_set.response_type_id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // response_set.label
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // response_set.options_text
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // response_set.options_values
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAll(java.lang.String,
     *      boolean, java.lang.String)
     */
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        // Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAll()
     */
    public Collection findAll() throws OpenClinicaException {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        String sql = digester.getQuery("findAll");
        ArrayList alist = this.select(sql);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    public ArrayList findAllByCRFVersionId(int crfVersionId) throws OpenClinicaException {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));

        String sql = digester.getQuery("findAllByCRFVersionId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    public ArrayList findAllByCRFVersionIdAndResponseTypeId(int crfVersionId, int responseTypeId) throws OpenClinicaException {
        ArrayList answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), new Integer(responseTypeId));

        String sql = digester.getQuery("findAllByCRFVersionIdAndResponseTypeId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    public ArrayList findAllByItemId(int itemId) {

        // TODO place holder for returning here, tbh
        ArrayList answer = new ArrayList();

        this.setTypesExpected();
        // BWP: changed from 25 to 26 when added response_layout?
        // YW: now added width_decimal
        this.setTypeExpected(28, TypeNames.STRING);// version name
        // add more here for display, tbh 082007
        this.setTypeExpected(29, TypeNames.STRING);// group_label
        this.setTypeExpected(30, TypeNames.INT);// repeat_max
        this.setTypeExpected(31, TypeNames.STRING);// section_name
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(itemId));

        String sql = digester.getQuery("findAllByItemId");
        // logger.info("<<<found SQL: "+sql);
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap(hm);
            String versionName = (String) hm.get("cvname");
            String groupLabel = (String) hm.get("group_label");
            String sectionName = (String) hm.get("section_name");
            int repeatMax = new Integer((Integer) hm.get("repeat_max")).intValue();
            ifmb.setCrfVersionName(versionName);
            ifmb.setGroupLabel(groupLabel);
            // logger.info(">>>added group name: "+groupLabel);
            ifmb.setSectionName(sectionName);
            // logger.info("<<<added section name: "+sectionName);
            ifmb.setRepeatMax(repeatMax);
            answer.add(ifmb);
        }

        return answer;
    }

    public ArrayList findAllBySectionId(int sectionId) throws OpenClinicaException {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sectionId));

        String sql = digester.getQuery("findAllBySectionId");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    public ArrayList findAllByCRFVersionIdAndSectionId(int crfVersionId, int sectionId) throws OpenClinicaException {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), new Integer(sectionId));

        String sql = digester.getQuery("findAllByCRFVersionIdAndSectionId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#findByPK(int)
     */
    public EntityBean findByPK(int id) throws OpenClinicaException {
        ItemFormMetadataBean ifmb = new ItemFormMetadataBean();
        this.setTypesExpected();

        // TODO place holder to return here, tbh
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(id));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return ifmb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#create(org.akaza.openclinica.bean.core.EntityBean)
     */
    public EntityBean create(EntityBean eb) throws OpenClinicaException {
        ItemFormMetadataBean ifmb = (ItemFormMetadataBean) eb;
        HashMap variables = new HashMap();

        int ind = 0;
        int id = getNextPK();
        variables.put(new Integer(ind), new Integer(id));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getItemId()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getCrfVersionId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getHeader());
        ind++;
        variables.put(new Integer(ind), ifmb.getSubHeader());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getParentId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getParentLabel());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getColumnNumber()));
        ind++;
        variables.put(new Integer(ind), ifmb.getPageNumberLabel());
        ind++;
        variables.put(new Integer(ind), ifmb.getQuestionNumberLabel());
        ind++;
        variables.put(new Integer(ind), ifmb.getLeftItemText());
        ind++;
        variables.put(new Integer(ind), ifmb.getRightItemText());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getSectionId()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getDescisionConditionId()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getResponseSetId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getRegexp());
        ind++;
        variables.put(new Integer(ind), ifmb.getRegexpErrorMsg());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getOrdinal()));
        ind++;
        variables.put(new Integer(ind), new Boolean(ifmb.isRequired()));
        ind++;
        variables.put(new Integer(ind), ifmb.getDefaultValue());
        ind++;
        variables.put(new Integer(ind), ifmb.getResponseLayout());
        ind++;
        variables.put(new Integer(ind), ifmb.getWidthDecimal());
        ind++;
        variables.put(new Integer(ind), new Boolean(ifmb.isShowItem()));

        execute("create", variables);

        if (isQuerySuccessful()) {
            ifmb.setId(id);
        }

        return ifmb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#update(org.akaza.openclinica.bean.core.EntityBean)
     */
    public EntityBean update(EntityBean eb) throws OpenClinicaException {
        ItemFormMetadataBean ifmb = (ItemFormMetadataBean) eb;
        HashMap variables = new HashMap();

        int ind = 0;

        variables.put(new Integer(ind), new Integer(ifmb.getItemId()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getCrfVersionId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getHeader());
        ind++;
        variables.put(new Integer(ind), ifmb.getSubHeader());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getParentId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getParentLabel());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getColumnNumber()));
        ind++;
        variables.put(new Integer(ind), ifmb.getPageNumberLabel());
        ind++;
        variables.put(new Integer(ind), ifmb.getQuestionNumberLabel());
        ind++;
        variables.put(new Integer(ind), ifmb.getLeftItemText());
        ind++;
        variables.put(new Integer(ind), ifmb.getRightItemText());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getSectionId()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getDescisionConditionId()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getResponseSetId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getRegexp());
        ind++;
        variables.put(new Integer(ind), ifmb.getRegexpErrorMsg());
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getOrdinal()));
        ind++;
        variables.put(new Integer(ind), new Boolean(ifmb.isRequired()));
        ind++;
        variables.put(new Integer(ind), new Integer(ifmb.getId()));
        ind++;
        variables.put(new Integer(ind), ifmb.getDefaultValue());
        ind++;
        variables.put(new Integer(ind), ifmb.getResponseLayout());
        ind++;
        variables.put(new Integer(ind), ifmb.getWidthDecimal());
        ind++;
        variables.put(new Integer(ind), new Boolean(ifmb.isShowItem()));

        execute("update", variables);

        if (!isQuerySuccessful()) {
            ifmb.setId(0);
            ifmb.setActive(false);
        }

        return ifmb;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAllByPermission(java.lang.Object,
     *      int, java.lang.String, boolean, java.lang.String)
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        // Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAllByPermission(java.lang.Object,
     *      int)
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        // Auto-generated method stub
        return null;
    }

    public ItemFormMetadataBean findByItemIdAndCRFVersionId(int itemId, int crfVersionId) {
        this.setTypesExpected();
        // TODO note to come back here, tbh
        this.setTypeExpected(28, TypeNames.STRING);// version name
        // add more here for display, tbh 082007
        this.setTypeExpected(29, TypeNames.STRING);// group_label
        this.setTypeExpected(30, TypeNames.INT);// repeat_max
        this.setTypeExpected(31, TypeNames.STRING);// section_name

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(itemId));
        variables.put(new Integer(2), new Integer(crfVersionId));

        String sql = digester.getQuery("findByItemIdAndCRFVersionId");
        ArrayList alist = this.select(sql, variables);

        Iterator it = alist.iterator();

        ItemFormMetadataBean ifmb = new ItemFormMetadataBean();
        HashMap hm = new HashMap();
        if (it.hasNext()) {
            hm = (HashMap) it.next();
            ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap(hm);
        }
        // EntityBean eb =
        // this.executeFindByPKQuery("findByItemIdAndCRFVersionId", variables);

        /*
         * if (!ifmb.isActive()) { return new ItemFormMetadataBean(); } else {
         */
        // ItemFormMetadataBean ifmb = (ItemFormMetadataBean) eb;
        String versionName = (String) hm.get("cvname");
        String groupLabel = (String) hm.get("group_label");
        String sectionName = (String) hm.get("section_name");
        Integer repeatMax = (Integer) hm.get("repeat_max");
        int repeatMaxInt = repeatMax != null ? repeatMax.intValue() : 0;
        // caught an NPE here, tbh 082007?
        // new Integer((Integer)hm.get("repeat_max")).intValue();
        ifmb.setCrfVersionName(versionName);
        ifmb.setGroupLabel(groupLabel);
        // logger.info(">>>added group name: "+groupLabel);
        ifmb.setSectionName(sectionName);
        // logger.info("<<<added section name: "+sectionName);
        ifmb.setRepeatMax(repeatMaxInt);
        // return (ItemFormMetadataBean) eb;
        return ifmb;
        // }
    }

    // YW 8-22-2007
    public ItemFormMetadataBean findByItemIdAndCRFVersionIdNotInIGM(int itemId, int crfVersionId) {
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(itemId));
        variables.put(new Integer(2), new Integer(crfVersionId));

        EntityBean eb = this.executeFindByPKQuery("findByItemIdAndCRFVersionIdNotInIGM", variables);

        if (!eb.isActive()) {
            return new ItemFormMetadataBean();
        } else {
            return (ItemFormMetadataBean) eb;
        }
    }

    public ResponseSetBean findResponseSetByPK(int id) {
        this.unsetTypeExpected();
        int ind = 1;
        this.setTypeExpected(ind, TypeNames.INT);// response_set_id
        ind++;
        this.setTypeExpected(ind, TypeNames.INT);// response_type_id
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING);// label
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING);// option_text
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING);// options_values
        ind++;
        this.setTypeExpected(ind, TypeNames.INT);// version_id
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING);// name
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING);// description
        ind++;

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(id));

        return (ResponseSetBean) this.executeFindByPKQuery("findResponseSetByPK", variables);

    }
}
