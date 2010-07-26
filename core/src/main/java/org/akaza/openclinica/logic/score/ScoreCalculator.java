/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.logic.score;

/**
 * The ScoreCalculator acts as the Controller for scoring. Tasks performed as
 * following:
 * 
 * 1) Find out all items with "calculation" control type. 2) Parse the actual
 * formula using the <code>Parser</code>. 3) According to the parser, picks
 * the right function and instantiate this function. 4) Performs the actual
 * calculation using the function.
 * 
 * 
 * @author Hailong Wang, Ph.D
 * @version 1.0 08/25/2006
 * 
 * @author ywang <br>
 *         <p>
 *         Modified for OpenClinica version 2.2 to enable scoring for group
 *         items. For this purpose, class structure has been modified. (ywang,
 *         Jan. 2008)
 * 
 */

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.exception.ScoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public class ScoreCalculator {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final SessionManager sm;

    private final EventCRFBean ecb;

    private final UserAccountBean ub;

    private final ArrayList<String> errors = new ArrayList<String>();

    private static int DEFAULT_DECIMAL = 4;

    public ScoreCalculator(SessionManager sm, EventCRFBean ecb, UserAccountBean ub) {
        this.sm = sm;
        this.ecb = ecb;
        this.ub = ub;
    }

    /*
     * Perform all calculations in a CRFVersion. The parameter 'itemdata' might
     * be overwritten.
     * 
     * @param calcItemGroupSizes @param items @param itemdata @param
     * itemGroupSizes @param databaseErrors @return ArrayList<String> which
     * records left_item_text of items who failed to be updated into database.
     * 
     * 
     * //this method has not been fully tested yet
     * 
     * 
     * public ArrayList<String> doCalculations( HashMap<String,ItemBean>
     * items, HashMap<String,String> itemdata, HashMap<Integer,TreeSet<Integer>>
     * itemOrdinals) { ArrayList<String> updateFailedItems = new ArrayList<String>();
     * if(itemdata==null) { //here, actually only record errors but doesn't
     * return errors. logger.error("In scoreCalculator doCalculations(), items
     * are empty!"); errors.add("Calculation cannot be started because needed
     * items are empty!"); return updateFailedItems; } ItemFormMetadataDAO
     * ifmdao = new ItemFormMetadataDAO(sm.getDataSource()); ItemDAO idao = new
     * ItemDAO(sm.getDataSource()); ItemDataDAO iddao = new
     * ItemDataDAO(sm.getDataSource());
     * 
     * NumberFormat nf = NumberFormat.getInstance(); Parser parser = new
     * Parser(items,itemdata); try { //for calculation type List<ItemFormMetadataBean>
     * derivedItemList = ifmdao.findAllByCRFVersionIdAndResponseTypeId(
     * ecb.getCRFVersionId(), ResponseType.CALCULATION.getId());
     * if(derivedItemList.size()>0) { Collections.sort(derivedItemList); for
     * (ItemFormMetadataBean ifmb: derivedItemList) { ItemBean ib =
     * (ItemBean)idao.findByPK(ifmb.getItemId()); ResponseOptionBean rob =
     * (ResponseOptionBean)ifmb.getResponseSet().getOptions().get(0); //YW,
     * 1-16-2008, << enable: 1. evaluate combination of expression and
     * functions 2. scoring for group items. //int groupsize =
     * iddao.getMaxOrdinalForGroup(ecb,
     * (SectionBean)sdao.findByPK(ifmb.getSectionId()), //
     * (ItemGroupBean)igdao.findByName(ifmb.getGroupLabel())); int groupsize =
     * 1; if(itemOrdinals.containsKey(ib.getId())) { groupsize =
     * (itemOrdinals.get(ib.getId())).size(); } String value = ""; String
     * parsedExp = ""; for(int i=0; i<groupsize; ++i) { ItemDataBean idb =
     * (ItemDataBean)iddao.findByItemIdAndEventCRFIdAndOrdinal(ifmb
     * .getItemId(), ecb.getId(), i+1); StringBuffer err = new StringBuffer();
     * parsedExp = parser.parse(rob.getValue(), i+1);
     * if(parser.getErrors().length()>0) { err.append(parser.getErrors());
     * parser.setErrors(new StringBuffer()); } else { value =
     * ScoreUtil.eval(parsedExp, err); //YW >> String exp = rob.getValue(); exp =
     * exp.replace("##", ","); if(writeToDB(ib,idb,exp,value,err)) {
     * itemdata.put(ib.getId()+"_"+(i+1), idb.getValue()); }else { String key =
     * (i+1)>1 ? ifmb.getLeftItemText()+"_"+(i+1) : ifmb.getLeftItemText();
     * updateFailedItems.add(key); } } if(err.length()>0) { String key = (i+1)>1 ?
     * ifmb.getLeftItemText()+"_"+(i+1) : ifmb.getLeftItemText();
     * errors.add("Item " + key + " contains calculation errors: " +
     * err.toString()); } } } }
     * 
     * //YW, 1-16-2008, for group-calculation type. Current restrictions: //1.
     * an item with group-calculation type is not repeatable. //2. only
     * calculate sum(), avg(), min(), max(), median(), stdev() //3. formula
     * arguments only contain item beans //4. only one item bean per argument
     * List<ItemFormMetadataBean> itemList =
     * ifmdao.findAllByCRFVersionIdAndResponseTypeId( ecb.getCRFVersionId(),
     * ResponseType.GROUP_CALCULATION.getId()); if(itemList.size()>0) {
     * Collections.sort(itemList); for (ItemFormMetadataBean ifmb: itemList) {
     * ItemBean ib = (ItemBean)idao.findByPK(ifmb.getItemId());
     * ResponseOptionBean rob =
     * (ResponseOptionBean)ifmb.getResponseSet().getOptions().get(0);
     * StringBuffer err = new StringBuffer(); parser.setErrors(err); String
     * parsedExp = parser.parse(rob.getValue(),itemOrdinals); String value = "";
     * if(parser.getErrors().length()>0) { err.append(parser.getErrors()); }else {
     * value = ScoreUtil.eval(parsedExp,err);
     * 
     * ItemDataBean idb =
     * (ItemDataBean)iddao.findByItemIdAndEventCRFIdAndOrdinal(ifmb
     * .getItemId(), ecb.getId(), 1); String exp = rob.getValue(); exp =
     * exp.replace("##", ","); if(writeToDB(ib,idb,exp,value,err)) {
     * itemdata.put(ib.getId()+"_"+idb.getOrdinal(), idb.getValue()); } else {
     * updateFailedItems.add(ifmb.getLeftItemText()); } }
     * 
     * if(err.length()>0) { errors.add("Item " + ifmb.getLeftItemText() + "
     * contains calculation errors: " + err.toString()); } } } } catch
     * (OpenClinicaException e) { logger.error(e.getMessage()); }
     * //ecb.setNeedsRecalc(false); //EventCRFDAO ecdao = new
     * EventCRFDAO(sm.getDataSource()); //ecb = ecdao.update(ecb);
     * 
     * return updateFailedItems; }
     */

    /**
     * Performs calculation. <br>
     * Notice: both parameter 'itemdata' and parameter 'errs' might be updated
     * in this method.
     * 
     * @param displayItems
     * @param items
     * @param itemdata
     * @param errs
     * @return
     * 
     * @author ywang (Jan. 2008)
     */
    public String doCalculation(DisplayItemBean displayItem, HashMap<String, ItemBean> items, HashMap<String, String> itemdata,
            HashMap<Integer, TreeSet<Integer>> itemOrdinals, StringBuffer errs, int ordinal) {
        if (itemdata == null) {
            logger.error("In DataEntryServlet doCalculation(), itemdata map is empty!");
            errs.append("Calculation cannot be started because needed items are empty" + "; ");
            return "";
        }
        String value = "";
        NumberFormat nf = NumberFormat.getInstance();
        Parser parser = new Parser(items, itemdata);

        ItemBean ib = displayItem.getItem();
        ItemFormMetadataBean ifm = displayItem.getMetadata();
        ResponseOptionBean rob = (ResponseOptionBean) ifm.getResponseSet().getOptions().get(0);
        ArrayList<ScoreToken> parsedExp = new ArrayList<ScoreToken>();
        int type = ifm.getResponseSet().getResponseTypeId();
        if (type == 8) {
            parsedExp = parser.assignVariables(parser.parseScoreTokens(rob.getValue()), ordinal);
        } else if (type == 9) {
            // YW, 1-16-2008, for group-calculation type. Current restrictions:
            // 1. only calculate sum(), avg(), min(), max(), median(), stdev()
            // 2. formula arguments only contain item beans
            // 3. only one item bean per argument
            parsedExp = parser.assignVariables(parser.parseScoreTokens(rob.getValue()), itemOrdinals);
        }
        if (parser.getErrors().length() > 0) {
            errs.append(parser.getErrors());
        } else {
            try {
                value = ScoreUtil.eval(parsedExp);
            } catch (ScoreException se) {
                logger.error(se.getMessage());
            }
            ItemDataType idt = ib.getDataType();
            if (value == null || value.length() == 0) {
                value = "";
                String exp = rob.getValue();
                exp = exp.replace("##", ",");
                errs.append("Result is empty in" + " " + exp + "; ");
                // errors.append(resexception.getString("result_is_empty_in") +
                // " " + exp + "; ");
            } else {
                value = this.getMathContextValue(value, ifm, idt, errs);
            }
            // idb.setStatus(Status.UNAVAILABLE);
            itemdata.put(ib.getId() + "_" + ordinal, value);
        }

        return value;
    }

    /**
     * Re-do calculations if funcs include changed item(s) and funcs are not
     * included in the current section. If calculation can not sucessfully redo,
     * old value will be erased and "<erased>" will be saved in database. <br>
     * The parameter 'itemdata' might be overwritten.
     * 
     * @param itemGroupSizes
     * @param items
     * @param itemdata
     * @param oldItemdata
     * @param updatedData
     * @param sectionId
     * @return ArrayList<String> which records left_item_text of items who
     *         failed to be updated into database.
     */
    public ArrayList<String> redoCalculations(HashMap<String, ItemBean> items, HashMap<String, String> itemdata, TreeSet<String> changedItems,
            HashMap<Integer, TreeSet<Integer>> itemOrdinals, int sectionId) {
        ArrayList<String> updateFailedItems = new ArrayList<String>();
        if (itemdata == null) {
            logger.error("In ScoreCalculator redoCalculations(), itemdata is empty!");
            errors.add("In ScoreCalculator redoCalculations(), 'itemdata' map is empty!");
            return updateFailedItems;
        }
        if (changedItems == null) {
            logger.error("In ScoreCalculator redoCalculations(), 'changeItems' set is empty!");
            errors.add("In ScoreCalculator redoCalculations(), 'changeItems' set is empty!");
            return updateFailedItems;
        }
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(sm.getDataSource());
        ItemDAO idao = new ItemDAO(sm.getDataSource());
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());

        NumberFormat nf = NumberFormat.getInstance();
        Parser parser = new Parser(items, itemdata);
        try {
            // for calculation type
            List<ItemFormMetadataBean> derivedItemList = ifmdao.findAllByCRFVersionIdAndResponseTypeId(ecb.getCRFVersionId(), ResponseType.CALCULATION.getId());
            if (derivedItemList.size() > 0) {
                Collections.sort(derivedItemList);
                for (ItemFormMetadataBean ifmb : derivedItemList) {
                    if (ifmb.getSectionId() != sectionId) {
                        ItemBean ib = (ItemBean) idao.findByPK(ifmb.getItemId());
                        ResponseOptionBean rob = (ResponseOptionBean) ifmb.getResponseSet().getOptions().get(0);
                        int groupsize = 1;
                        if (itemOrdinals.containsKey(ib.getId())) {
                            groupsize = itemOrdinals.get(ib.getId()).size();
                        }
                        String value = "";
                        ArrayList<ScoreToken> parsedExp = new ArrayList<ScoreToken>();
                        for (int i = 0; i < groupsize; ++i) {
                            ItemDataBean idb = iddao.findByItemIdAndEventCRFIdAndOrdinal(ifmb.getItemId(), ecb.getId(), i + 1);
                            // is there any changed item
                            Parser p = new Parser(items, itemdata);
                            parsedExp = parser.parseScoreTokens(rob.getValue());
                            if (p.isChanged(changedItems, parsedExp)) {
                                StringBuffer err = new StringBuffer();
                                parsedExp = parser.assignVariables(parsedExp, i + 1);
                                // if parser has error and has been calculated
                                // before, set "<erased>"
                                if (parser.getErrors().length() > 0) {
                                    err.append(parser.getErrors());
                                    if (idb.isActive()) {
                                        idb.setValue("<erased>");
                                        idb.setStatus(Status.UNAVAILABLE);
                                        idb = (ItemDataBean) iddao.update(idb);
                                        if (!idb.isActive()) {
                                            String key = i + 1 > 1 ? ifmb.getLeftItemText() + "_" + (i + 1) : ifmb.getLeftItemText();
                                            updateFailedItems.add(key);
                                        }
                                    }
                                    parser.setErrors(new StringBuffer());
                                }
                                // otherwise do calculation
                                else {
                                    try {
                                        value = ScoreUtil.eval(parsedExp);
                                    } catch (ScoreException se) {
                                        logger.error(se.getMessage());
                                    }
                                    String exp = rob.getValue();
                                    exp = exp.replace("##", ",");
                                    if (writeToDB(ib, ifmb, idb, exp, value, err)) {
                                        changedItems.add(ib.getName());
                                        itemdata.put(ib.getId() + "_" + (i + 1), idb.getValue());
                                    } else {
                                        String key = i + 1 > 1 ? ifmb.getLeftItemText() + "_" + (i + 1) : ifmb.getLeftItemText();
                                        updateFailedItems.add(key);
                                    }
                                }
                                if (err.length() > 0) {
                                    String key = i + 1 > 1 ? ifmb.getLeftItemText() + "_" + (i + 1) : ifmb.getLeftItemText();
                                    errors.add("Item " + key + " contains calculation errors: " + err.toString());
                                }
                            }
                        }
                    }
                }
            }

            List<ItemFormMetadataBean> itemList = ifmdao.findAllByCRFVersionIdAndResponseTypeId(ecb.getCRFVersionId(), ResponseType.GROUP_CALCULATION.getId());
            if (itemList.size() > 0) {
                Collections.sort(itemList);
                for (ItemFormMetadataBean ifmb : itemList) {
                    if (ifmb.getSectionId() != sectionId) {
                        ItemBean ib = (ItemBean) idao.findByPK(ifmb.getItemId());
                        ResponseOptionBean rob = (ResponseOptionBean) ifmb.getResponseSet().getOptions().get(0);
                        String value = "";
                        Parser p = new Parser(items, itemdata);
                        ArrayList<ScoreToken> parsedExp = parser.parseScoreTokens(rob.getValue());
                        if (p.isChanged(changedItems, parsedExp)) {
                            StringBuffer err = new StringBuffer();
                            parser.setErrors(err);
                            parsedExp = parser.assignVariables(parsedExp, itemOrdinals);
                            ItemDataBean idb = iddao.findByItemIdAndEventCRFIdAndOrdinal(ifmb.getItemId(), ecb.getId(), 1);
                            if (parser.getErrors().length() > 0) {
                                err.append(parser.getErrors());
                                if (idb.isActive()) {
                                    idb.setValue("<erased>");
                                    idb.setStatus(Status.UNAVAILABLE);
                                    idb = (ItemDataBean) iddao.update(idb);
                                    if (!idb.isActive()) {
                                        updateFailedItems.add(ifmb.getLeftItemText());
                                    }
                                }
                            } else {
                                try {
                                    value = ScoreUtil.eval(parsedExp);
                                } catch (ScoreException se) {
                                    logger.error(se.getMessage());
                                }
                                String exp = rob.getValue();
                                exp = exp.replace("##", ",");
                                if (writeToDB(ib, ifmb, idb, exp, value, err)) {
                                    changedItems.add(ib.getName());
                                    itemdata.put(ib.getId() + "_" + idb.getOrdinal(), idb.getValue());
                                } else {
                                    updateFailedItems.add(ifmb.getLeftItemText());
                                }

                            }
                            if (err.length() > 0) {
                                errors.add("Item " + ifmb.getLeftItemText() + " contains calculation errors: " + err.toString());
                            }
                        }
                    }
                }
            }
        } catch (OpenClinicaException e) {
            logger.error(e.getMessage());
        }

        return updateFailedItems;
    }

    protected boolean writeToDB(ItemBean ib, ItemFormMetadataBean ifm, ItemDataBean idb, String exp, String value, StringBuffer err) {
        ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
        NumberFormat nf = NumberFormat.getInstance();

        if (idb == null) {
            idb = new ItemDataBean();
        }

        ItemDataType idt = ib.getDataType();
        if (value == null || value.length() == 0) {
            if (idb.isActive() && !"".equals(idb.getValue())) {
                idb.setValue("<erased>");
            } else {
                idb.setValue("");
            }
            err.append("Result is empty in" + " " + exp + "; ");
        } else {
            idb.setValue(this.getMathContextValue(value, ifm, idt, err));
        }

        idb.setStatus(Status.UNAVAILABLE);
        // idb.setNeedsRecalc(false);
        if (!idb.isActive()) {
            // will this need to change for double data entry?
            idb.setCreatedDate(new Date());
            idb.setOwner(ub);
            idb.setItemId(ib.getId());
            idb.setEventCRFId(ecb.getId());

            idb = (ItemDataBean) iddao.create(idb);
        } else {
            idb = (ItemDataBean) iddao.update(idb);
        }

        return idb.isActive();

    }

    public int parseWidth(String widthDecimal) {
        String w = "";
        widthDecimal = widthDecimal.trim();
        if (widthDecimal.startsWith("(")) {
        } else if (widthDecimal.contains("(")) {
            w = widthDecimal.split("\\(")[0];
        } else {
            w = widthDecimal;
        }
        if (w.length() > 0) {
            return "w".equalsIgnoreCase(w) ? 0 : Integer.parseInt(w);
        }
        return 0;
    }

    public int parseDecimal(String widthDecimal) {
        String d = "";
        widthDecimal = widthDecimal.trim();
        if (widthDecimal.startsWith("(")) {
            d = widthDecimal.substring(1, widthDecimal.length() - 1);
        } else if (widthDecimal.contains("(")) {
            d = widthDecimal.split("\\(")[1].trim();
            d = d.substring(0, d.length() - 1);

        }
        if (d.length() > 0) {
            return "d".equalsIgnoreCase(d) ? 0 : Integer.parseInt(d);
        }
        return 0;
    }

    protected String getMathContextValue(String value, ItemFormMetadataBean ifm, ItemDataType idt, StringBuffer errorMessage) {
        ResponseOptionBean rob = (ResponseOptionBean) ifm.getResponseSet().getOptions().get(0);
        String widthDecimal = ifm.getWidthDecimal();
        int width = parseWidth(widthDecimal);
        int decimal = parseDecimal(widthDecimal);
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        if (idt.equals(ItemDataType.INTEGER)) {
            try {
                Double d = nf.parse(value).doubleValue();
                int p = 0;
                if (width > 0) {
                    p = d >= 0 ? width : width - 1;
                } else {
                    p = BigDecimal.valueOf(d).precision();
                }
                MathContext mc = new MathContext(p, RoundingMode.valueOf(BigDecimal.ROUND_HALF_UP));
                value = (new BigDecimal(d, mc)).setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
            } catch (ParseException e) {
                logger.error("Number was expected in " + rob.getValue() + " : " + value);
                String exp = rob.getValue();
                exp = exp.replace("##", ",");
                errorMessage.append("Number was expected in" + " " + exp + " : " + value + "; ");
                // errors.append(resexception.getString("number_expected_in")
                // + " " + exp + ": " + value + "; ");
                value = "";
            }
        } else if (idt.equals(ItemDataType.REAL)) {
            try {
                Double d = nf.parse(value).doubleValue();
                // there should be no width specify for calculation item
                int p = BigDecimal.valueOf(d).precision();
                // set default scale as 4
                int scale = decimal > 0 ? decimal : DEFAULT_DECIMAL;
                MathContext mc = new MathContext(p, RoundingMode.valueOf(BigDecimal.ROUND_HALF_UP));
                value = (new BigDecimal(d, mc)).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();
            } catch (Exception ee) {
                String exp = rob.getValue();
                exp = exp.replace("##", ",");
                logger.error("Number was expected in " + exp + " : " + value);
                errorMessage.append("Number was expected in" + " " + exp + " : " + value + "; ");
                // errors.append(resexception.getString("number_expected_in")
                // + " " + exp + ": " + value + "; ");
                value = "";
            }
        }
        return value;
    }
}
