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
import org.akaza.openclinica.dao.core.*;
import org.akaza.openclinica.domain.crfdata.InstantOnChangePairContainer;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author ssachs
 */
public class ItemFormMetadataDAO<K extends String,V extends ArrayList> extends EntityDAO {

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
      //  setCache(new EhCacheWrapper("ItemFormMetadataDAO",R);
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
    public ArrayList<ItemFormMetadataBean> findByMultiplePKs(ArrayList ints) throws OpenClinicaException {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        Iterator it = ints.iterator();
        while (it.hasNext()) {
            Integer newInt = (Integer) it.next();
            ItemFormMetadataBean ifmBean = (ItemFormMetadataBean) this.findByPK(newInt.intValue());
            // check to make sure we have what we need
            logger.debug("options: " + ifmBean.getResponseSetId() + " bean options list: " + ifmBean.getResponseSet().getOptions().toString());
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
        ind++; // item form metadata id 2
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // item id 3
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // crf version id 4
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // header 5
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // subheader 6
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // parent id 7
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // parent label 8
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // column number 9
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // page number label 10
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // question number label 11
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // left item text 12
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // right item text 13
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // section id 14
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // decision condition id 15
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // response set id 16
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // regexp 17
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // regexp error msg 18
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // ordinal 19
        this.setTypeExpected(ind, TypeNames.BOOL);
        ind++; // required 20
        this.setTypeExpected(ind, TypeNames.STRING); // default_value
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING); // response_layout 21
        ind++;
        this.setTypeExpected(ind, TypeNames.STRING); // width_decimal 22
        ind++;
        // will need to set the boolean value here, tbh 23
        this.setTypeExpected(ind, TypeNames.BOOL);
        ind++; // show_item 24
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // response_set.response_type_id 25
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // response_set.label 26
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // response_set.options_text 27
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
    public Collection<ItemFormMetadataBean> findAll() throws OpenClinicaException {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

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

    /*
     * <query>
        <name>findAllCountHiddenByCRFVersionId</name>
        <sql>
           select count(i.*) as number from item i, item_form_metadata ifm
           where i.item_id = ifm.item_id
           and ifm.crf_version_id=?
           and ifm.required=true
           and ifm.show_item=false
        </sql>
    </query>
     */
    public int findCountAllHiddenByCRFVersionId(int crfVersionId) {
        int answer = 0;
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfVersionId));
        String sql = digester.getQuery("findAllCountHiddenByCRFVersionId");

        ArrayList rows = select(sql, variables);

        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = ((Integer) row.get("number")).intValue();
        }

        // what about those shown but in a hidden section?
        /*
         * select count(i.*) as number from item i, item_form_metadata ifm, item_group_metadata igm
           where i.item_id = ifm.item_id
           and ifm.item_id = igm.item_id
           and ifm.crf_version_id=?
           and ifm.required=true
           and ifm.show_item=true
           and igm.show_group=false
         */
        int answer2 = 0;

        String sql2 = digester.getQuery("findAllCountHiddenUnderGroupsByCRFVersionId");
        rows = select(sql2, variables);
        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer2 = ((Integer) row.get("number")).intValue();
        }
        return answer + answer2;
    }

    /*
     * <query>
        <name>findAllCountHiddenButShownByCRFVersionId</name>
        <sql>
           select count(dyn.*) as number from item_form_metadata ifm, dyn_item_form_metadata dyn
           where dyn.item_form_metadata_id = ifm.item_form_metadata_id
           and dyn.event_crf_id = ?
           and ifm.required=true
           and dyn.show_item=true
        </sql>
    </query>
     */
    public int findCountAllHiddenButShownByEventCRFId(int eventCrfId) {
        int answer = 0;
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(eventCrfId));
        String sql = digester.getQuery("findAllCountHiddenButShownByEventCrfId");

        ArrayList rows = select(sql, variables);

        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = ((Integer) row.get("number")).intValue();
        }

        return answer;
    }

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionId(int crfVersionId) throws OpenClinicaException {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
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

    public ArrayList<ItemFormMetadataBean> findAllItemsRequiredAndShownByCrfVersionId(int crfVersionId)  {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfVersionId));

        String sql = digester.getQuery("findAllItemsRequiredAndShownByCrfVersionId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    
    public ArrayList<ItemFormMetadataBean> findAllItemsRequiredAndHiddenByCrfVersionId(int crfVersionId)  {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfVersionId));

        String sql = digester.getQuery("findAllItemsRequiredAndHiddenByCrfVersionId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    
    public ArrayList<ItemFormMetadataBean> findAllByCRFIdItemIdAndHasValidations(int crfId, int itemId) {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfId));
        variables.put(new Integer(2), new Integer(itemId));

        String sql = digester.getQuery("findAllByCRFIdItemIdAndHasValidations");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndResponseTypeId(int crfVersionId, int responseTypeId) throws OpenClinicaException {
     //Caching purpose
        V  value;
        K key;


        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), new Integer(responseTypeId));
        ArrayList alist;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables);

        String sql = digester.getQuery("findAllByCRFVersionIdAndResponseTypeId");

        key = (K) (sql+","+crfVersionId+","+responseTypeId);

        if((alist=(V) cache.get(key))==null)
        {
         alist = this.select(sql, variables);
         if(alist!=null)
             cache.put(key, alist);
        }

        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }

        return answer;
    }



    public ArrayList<ItemFormMetadataBean> findAllByItemId(int itemId) {

        // TODO place holder for returning here, tbh
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();
        // BWP: changed from 25 to 26 when added response_layout?
        // YW: now added width_decimal
        this.setTypeExpected(28, TypeNames.STRING);// version name
        // add more here for display, tbh 082007
        this.setTypeExpected(29, TypeNames.STRING);// group_label
        this.setTypeExpected(30, TypeNames.INT);// repeat_max
        this.setTypeExpected(31, TypeNames.STRING);// section_name
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
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

    public ArrayList<ItemFormMetadataBean> findAllByItemIdAndHasValidations(int itemId) {

        // TODO place holder for returning here, tbh
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();
        // BWP: changed from 25 to 26 when added response_layout?
        // YW: now added width_decimal
        this.setTypeExpected(28, TypeNames.STRING);// version name
        // add more here for display, tbh 082007
        this.setTypeExpected(29, TypeNames.STRING);// group_label
        this.setTypeExpected(30, TypeNames.INT);// repeat_max
        this.setTypeExpected(31, TypeNames.STRING);// section_name
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(itemId));

        String sql = digester.getQuery("findAllByItemIdAndHasValidations");
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

    public ArrayList<ItemFormMetadataBean> findAllBySectionId(int sectionId) throws OpenClinicaException {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
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

    public ArrayList<ItemFormMetadataBean> findAllByCRFVersionIdAndSectionId(int crfVersionId, int sectionId) throws OpenClinicaException {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();

        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
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
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
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
        HashMap<Integer, Comparable> variables = new HashMap<Integer, Comparable>();

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
        HashMap<Integer, Comparable> variables = new HashMap<Integer, Comparable>();

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
        ind++;
        variables.put(new Integer(ind), ifmb.getId());

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
    private void logMe(String message){
        logger.debug(message);
      //  System.out.println(message);
    }

    public ItemFormMetadataBean findByItemIdAndCRFVersionId(int itemId, int crfVersionId) {
        this.setTypesExpected();
        // TODO note to come back here, tbh
        this.setTypeExpected(28, TypeNames.STRING);// version name
        // add more here for display, tbh 082007
        this.setTypeExpected(29, TypeNames.STRING);// group_label
        this.setTypeExpected(30, TypeNames.INT);// repeat_max
        this.setTypeExpected(31, TypeNames.STRING);// section_name

        logMe("Current Thread:::"+Thread.currentThread()+"types Expected?");
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(itemId));
        variables.put(new Integer(2), new Integer(crfVersionId));


        String sql = digester.getQuery("findByItemIdAndCRFVersionId");

        logMe("Thread?"+Thread.currentThread()+"SQL?"+sql+"variables?"+variables);

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

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
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

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(new Integer(1), new Integer(id));

        return (ResponseSetBean) this.executeFindByPKQuery("findResponseSetByPK", variables);
    }

    /**
     * Find all ItemFormMetadataBean which is simple_conditional_display
     * @param sectionId
     * @return
     */
    public ArrayList<ItemFormMetadataBean> findSCDItemsBySectionId(Integer sectionId) {
        ArrayList<ItemFormMetadataBean> answer = new ArrayList<ItemFormMetadataBean>();
        this.unsetTypeExpected();
        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), sectionId);

        String sql = digester.getQuery("findSCDItemsBySectionId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            ItemFormMetadataBean ifmb = (ItemFormMetadataBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ifmb);
        }
        return answer;
    }

    public int findMaxId() {
        int answer = 0;
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        String sql = "select max(ifm.item_form_metadata_id) as max_id from item_form_metadata ifm";
        ArrayList rows = this.select(sql);
        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = ((Integer) row.get("max_id")).intValue();
        }

        return answer;
    }

    public boolean instantTypeExistsInSection(int sectionId) {
        Integer id = null;
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), sectionId);
        //String sql = "select ifm.item_form_metadata_id from item_form_metadata ifm, response_set rs"
        //    +" where rs.response_type_id = 10 and ifm.section_id = ? and ifm.response_set_id = rs.response_set_id limit 1";
        ArrayList alist = this.select(digester.getQuery("instantTypeExistsInSection"),variables);
        for(Iterator it = alist.iterator(); it.hasNext();) {
            HashMap row = (HashMap) it.next();
            id = (Integer) row.get("item_form_metadata_id");
        }
        return id != null && id > 0;
    }

    public Map<Integer,List<InstantOnChangePairContainer>> sectionInstantMapInSameSection(int crfVersionId) {
        Map<Integer,List<InstantOnChangePairContainer>> pairs = new HashMap<Integer,List<InstantOnChangePairContainer>>();
        this.setInstantTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), crfVersionId);
        variables.put(new Integer(2), crfVersionId);
        variables.put(new Integer(3), crfVersionId);
        variables.put(new Integer(4), crfVersionId);
        variables.put(new Integer(5), crfVersionId);
        String sql = digester.getQuery("findInstantItemsByCrfVersionId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            InstantOnChangePairContainer instantItemPair = new InstantOnChangePairContainer();
            HashMap row = (HashMap) it.next();
            Integer sectionId = (Integer) row.get("o_sec_id");
            instantItemPair.setOriginSectionId(sectionId);
            instantItemPair.setOriginItemId((Integer) row.get("o_item_id"));
            instantItemPair.setOriginItemGroupOid((String) row.get("o_ig_oid"));
            Boolean isUng = Boolean.FALSE;
            if("Ungrouped".equalsIgnoreCase((String) row.get("o_ig_name"))) {
                isUng = Boolean.TRUE;
            }
            instantItemPair.setOriginUngrouped(isUng);
            Boolean isRep = (Boolean) row.get("o_repeating");
            isRep = isRep == null ? Boolean.FALSE : isRep;
            instantItemPair.setOriginRepeating(isRep);
            instantItemPair.setDestSectionId((Integer) row.get("d_sec_id"));
            instantItemPair.setDestItemId((Integer) row.get("d_item_id"));
            instantItemPair.setDestItemGroupOid((String) row.get("d_ig_oid"));
            isUng = Boolean.FALSE;
            if("Ungrouped".equalsIgnoreCase((String) row.get("d_ig_name"))) {
                isUng = Boolean.TRUE;
            }
            instantItemPair.setDestUngrouped(isUng);
            isRep = (Boolean) row.get("d_repeating");
            isRep = isRep == null ? Boolean.FALSE : isRep;
            instantItemPair.setDestRepeating(isRep);
            instantItemPair.setDestItemFormMetadataId((Integer) row.get("d_ifm_id"));
            instantItemPair.setOptionValue((String) row.get("option_name"));
            if(pairs.containsKey(sectionId)) {
                ((ArrayList<InstantOnChangePairContainer>)pairs.get(sectionId)).add(instantItemPair);
            } else {
                List<InstantOnChangePairContainer> ins = new ArrayList<InstantOnChangePairContainer>();
                ins.add(instantItemPair);
                pairs.put(sectionId, ins);
            }
        }
        return pairs;
    }

    private void setInstantTypesExpected() {
        this.unsetTypeExpected();
        /*oifm.section_id as o_sec_id, oit.item_id as o_item_id, oig.oc_oid as o_ig_oid,
    oig.name as o_ig_name, oigm.repeating_group as o_repeating,
    idfm.section_id as d_sec_id, difm.item_id as d_item_id, dig.oc_oid as d_ig_oid,
    dig.name as d_ig_name, digm.repeating_group as d_repeating,
    difm.item_form_metadata_id as d_ifm_id, ri.option_name */
        int ind = 1; //o_sec_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // o_item_id 2
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; //o_ig_oid 3
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // o_ig_name 4
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // o_repeating 5
        this.setTypeExpected(ind, TypeNames.BOOL);
        ind++; // d_sec_id 6
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // d_item_id 7
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // d_ig_oid 8
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // d_ig_name 9
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // repeating_group 10
        this.setTypeExpected(ind, TypeNames.BOOL);
        ind++; // d_ifm_id 11
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // option_name 12
        this.setTypeExpected(ind, TypeNames.STRING);
    }

/**
 * need to use this method when you want the results to be cached. i.e they do not get updated.
 */
    @Override
    public ArrayList<V> select(String query, HashMap variables) {
        clearSignals();

        ArrayList results = new ArrayList();
        V  value;
        K key;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables);
        PreparedStatement ps = null;

        try {
            con = ds.getConnection();
            CoreResources.setSchema(con);

            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

           ps = con.prepareStatement(query);


            ps = psf.generate(ps);// enter variables here!
            key = (K) ps.toString();
            if((results=(V) cache.get(key))==null)
            {
            rs = ps.executeQuery();
            results = this.processResultRows(rs);
            if(results!=null){
                cache.put(key,results);
            }
            }

         //   if (logger.isInfoEnabled()) {
                logger.debug("Executing dynamic query, EntityDAO.select:query " + query);
         //   }
            signalSuccess();


        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing dynamic query, GenericDAO.select: " + query + ":message: " + sqle.getMessage());
                sqle.printStackTrace();
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return results;

    }
}
