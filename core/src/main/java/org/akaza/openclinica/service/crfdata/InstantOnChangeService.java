/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplayItemWithGroupBean;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.crfdata.InstantOnChangePairContainer;
import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStr;
import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStrGroup;
import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStrParcel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.*;

/**
 * For instant-calculation func:onchange
 */
//ywang (Aug. 2011)
public class InstantOnChangeService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource dataSource;

    public InstantOnChangeService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean needRunInstantInSection(int sectionId) {
        return getItemFormMetadataDAO().instantTypeExistsInSection(sectionId);
    }

    public Map<Integer,InstantOnChangeFrontStrParcel> instantOnChangeFrontStrParcelInCrfVersion(Integer crfVersionId) {
        Map<Integer,InstantOnChangeFrontStrParcel> parcels = new HashMap<Integer,InstantOnChangeFrontStrParcel>();
        Map<Integer,List<InstantOnChangePairContainer>> insMap
            = getItemFormMetadataDAO().sectionInstantMapInSameSection(crfVersionId);
        if(insMap == null || insMap.size()==0) {
            logger.debug("cannot find instant-calculation item in crf_version =" + crfVersionId);
        } else {
            for (Integer sectionId : insMap.keySet()) {
                Map<String,Map<Integer,InstantOnChangeFrontStrGroup>> repOrigins = new HashMap<String,Map<Integer,InstantOnChangeFrontStrGroup>>();
                Map<Integer,InstantOnChangeFrontStrGroup> nonRepOrigins = new HashMap<Integer,InstantOnChangeFrontStrGroup>();
                List<InstantOnChangePairContainer> instantPairs = insMap.get(sectionId);
                if(instantPairs == null || instantPairs.size() == 0) {
                    logger.debug("get empty instantPair list in section_id = " + sectionId);
                } else {
                    for(InstantOnChangePairContainer meta : instantPairs) {
                        if(meta.getOriginRepeating().equals(Boolean.TRUE)) {
                            addToRepOrigins(repOrigins,meta);
                        } else {
                            addToNonRepOrigins(nonRepOrigins,meta);
                        }

                    }
                }
                InstantOnChangeFrontStrParcel parcel = new InstantOnChangeFrontStrParcel();
                boolean doSet = false;
                if(repOrigins.size()>0) {
                    parcel.setRepOrigins(repOrigins);
                    doSet = true;
                }
                if(nonRepOrigins.size()>0) {
                    parcel.setNonRepOrigins(nonRepOrigins);
                    doSet = true;
                }
                if(doSet) {
                    parcel.setCrfVersionId(crfVersionId);
                    parcel.setSectionId(sectionId);
                    parcels.put(sectionId, parcel);
                }
            }
        }
        return parcels;
    }

    /**
     * Both origin and destination are in the same repeating group.
     * @param allItems
     * @param groupedOrigins
     */
    public void addToRepOrigins(Map<String,Map<Integer,InstantOnChangeFrontStrGroup>> repOrigins, InstantOnChangePairContainer instantPair) {
        String oigOid = instantPair.getOriginItemGroupOid();
        Integer oigItemId = instantPair.getOriginItemId();
        if(oigOid.length()>0 && oigItemId > 0 && instantPair.getDestItemId()>0 && instantPair.getOptionValue().length()>0) {
            if(repOrigins.containsKey(oigOid)) {
                Map<Integer,InstantOnChangeFrontStrGroup> strMap = repOrigins.get(oigOid);
                if(strMap==null) {
                    logger.debug("repeating "+oigOid+" contains Null InstantOnChangeFrontStr.");
                } else {
                    if(strMap.containsKey(oigItemId)) {
                        InstantOnChangeFrontStr repGrpFrontStr = strMap.get(oigItemId).getSameRepGrpFrontStr();
                        repGrpFrontStr.chainUpFrontStr(instantPair);
                    }else {
                        InstantOnChangeFrontStr repGrp = new InstantOnChangeFrontStr();
                        repGrp.chainUpFrontStr(instantPair);
                        if(repGrp.getFrontStr().length()>0) {
                            InstantOnChangeFrontStrGroup iocstr = new InstantOnChangeFrontStrGroup();
                            iocstr.setOriginItemId(oigItemId);
                            iocstr.setSameRepGrpFrontStr(repGrp);
                            strMap.put(oigItemId, iocstr);
                        }
                    }
                }
            } else {
                InstantOnChangeFrontStr repGrp = new InstantOnChangeFrontStr();
                repGrp.chainUpFrontStr(instantPair);
                if(repGrp.getFrontStr().length()>0) {
                    Map<Integer,InstantOnChangeFrontStrGroup> smap = new HashMap<Integer,InstantOnChangeFrontStrGroup>();
                    InstantOnChangeFrontStrGroup instantFrontStr = new InstantOnChangeFrontStrGroup();
                    instantFrontStr.setOriginItemId(oigItemId);
                    instantFrontStr.setSameRepGrpFrontStr(repGrp);
                    smap.put(oigItemId, instantFrontStr);
                    repOrigins.put(oigOid, smap);
                }
            }
        } else {
            logger.debug("Empty found upon origin_group_oid/item_ids/option_value, so no InstantOnChangeFrontStrGroup available.");
        }
    }

    public void addToNonRepOrigins(Map<Integer,InstantOnChangeFrontStrGroup> nonRepOrigins, InstantOnChangePairContainer instantPair) {
        Integer oigItemId = instantPair.getOriginItemId();
        if(oigItemId > 0 && instantPair.getDestItemId()>0 && instantPair.getOptionValue().length()>0) {
            if(nonRepOrigins.containsKey(oigItemId)) {
                InstantOnChangeFrontStrGroup istr = nonRepOrigins.get(oigItemId);
                InstantOnChangeFrontStr nonRep = istr.getNonRepFrontStr();
                nonRep.chainUpFrontStr(instantPair);
                istr.setNonRepFrontStr(nonRep);
            }else {
                InstantOnChangeFrontStr nonRep = new InstantOnChangeFrontStr();
                nonRep.chainUpFrontStr(instantPair);
                if(nonRep.getFrontStr().length()>0) {
                    InstantOnChangeFrontStrGroup istr = new InstantOnChangeFrontStrGroup();
                    istr.setOriginItemId(oigItemId);
                    istr.setNonRepFrontStr(nonRep);
                    nonRepOrigins.put(oigItemId, istr);
                }
            }
        } else {
            logger.debug("Empty found upon item_ids/option_value, so no InstantOnChangeFrontStrGroup available.");
        }
    }

    public void itemGroupsInstantUpdate(List<DisplayItemWithGroupBean> allItems,
            Map<String, Map<Integer,InstantOnChangeFrontStrGroup>> repOrigins) {
        for(int i=0; i<allItems.size(); ++i) {
            if(allItems.get(i).isInGroup()) {
                List<DisplayItemGroupBean> digs = allItems.get(i).getItemGroups();
                String oid = allItems.get(i).getItemGroup().getItemGroupBean().getOid();
                for(DisplayItemGroupBean dig : digs) {
                    if(repOrigins.containsKey(oid)) {
                        HashMap<Integer,InstantOnChangeFrontStrGroup> oMap = (HashMap<Integer,InstantOnChangeFrontStrGroup>) repOrigins.get(oid);
                        List<DisplayItemBean> items = dig.getItems();
                        for(DisplayItemBean dib : items) {
                            if(oMap.containsKey(dib.getItem().getId())) {
                                Integer itId = dib.getItem().getId();
                                dib.setInstantFrontStrGroup(oMap.get(itId));
                            }
                        }
                    }
                }
            }
        }
    }

    public void itemsInstantUpdate(List<DisplayItemWithGroupBean> allItems, HashMap<Integer,InstantOnChangeFrontStrGroup> nonRepOrigins) {
        Set<Integer> idSet = new HashSet<Integer>();
        for(int i=0; i<allItems.size(); ++i) {
            if(!allItems.get(i).isInGroup()) {
                DisplayItemBean dib = allItems.get(i).getSingleItem();
                Integer id = dib.getMetadata().getParentId()>0 ? 0 : dib.getItem().getId();
                if(nonRepOrigins.containsKey(id)) {
                    dib.setInstantFrontStrGroup(nonRepOrigins.get(id));
                    idSet.add(id);
                    if(idSet.size()==nonRepOrigins.size()) break;
                    for(int j=0; j<dib.getChildren().size(); ++j) {
                        DisplayItemBean cib = (DisplayItemBean) dib.getChildren().get(j);
                        Integer cid = cib.getItem().getId();
                        if(nonRepOrigins.containsKey(cid)) {
                            cib.setInstantFrontStrGroup(nonRepOrigins.get(cid));
                            idSet.add(id);
                            if(idSet.size()==nonRepOrigins.size()) break;
                        }
                    }
                }
            }
        }
    }


    public ItemFormMetadataDAO getItemFormMetadataDAO() {
        return new ItemFormMetadataDAO(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}