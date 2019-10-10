package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.ItemBean;
import core.org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import core.org.akaza.openclinica.bean.submit.ItemGroupBean;
import core.org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import core.org.akaza.openclinica.bean.submit.SectionBean;
import core.org.akaza.openclinica.dao.submit.ItemDAO;
import core.org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupDAO;
import core.org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import core.org.akaza.openclinica.dao.submit.SectionDAO;

/**
 * Utility class with method for retrieving the metadata for a CRFVersion.
 */
public class CRFVersionMetadataUtil {

    private DataSource dataSource = null;

    public CRFVersionMetadataUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Builds and returns an ArrayList of SectionBeans that comprise the metadata of a CRFVersion.
     */
    public ArrayList<SectionBean> retrieveFormMetadata(FormLayoutBean formLayout) throws Exception {

        ItemDAO idao = new ItemDAO(dataSource);
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(dataSource);

        // tbh, 102007
        SectionDAO sdao = new SectionDAO(dataSource);
        ItemGroupDAO igdao = new ItemGroupDAO(dataSource);
        ItemGroupMetadataDAO igmdao = new ItemGroupMetadataDAO(dataSource);
        ArrayList sections = (ArrayList) sdao.findByLayoutId(formLayout.getId());
        HashMap versionMap = new HashMap();
        for (int i = 0; i < sections.size(); i++) {
            SectionBean section = (SectionBean) sections.get(i);
            versionMap.put(new Integer(section.getId()), section.getItems());
            // YW 08-21-2007, add group metadata
            ArrayList<ItemGroupBean> igs = (ArrayList<ItemGroupBean>) igdao.findGroupByLayoutId(formLayout.getId());
            for (int j = 0; j < igs.size(); ++j) {
                ArrayList<ItemGroupMetadataBean> igms = (ArrayList<ItemGroupMetadataBean>) igmdao.findMetaByGroupAndSection(igs.get(j).getId(),
                        section.getCRFVersionId(), section.getId());
                if (!igms.isEmpty()) {
                    // Note, the following logic has been adapted here -
                    // "for a given crf version,
                    // all the items in the same group have the same group
                    // metadata
                    // so we can get one of them and set metadata for the
                    // group"
                    igs.get(j).setMeta(igms.get(0));
                    igs.get(j).setItemGroupMetaBeans(igms);
                }
            }
            ((SectionBean) sections.get(i)).setGroups(igs);
            // YW >>
        }
        ArrayList items = idao.findAllItemsByLayoutId(formLayout.getId());
        // YW 08-22-2007, if this crf_version_id doesn't exist in
        // item_group_metadata table,
        // items in this crf_version will not exist in item_group_metadata,
        // then different query will be used
        if (igmdao.versionIncluded(formLayout.getId())) {
            for (int i = 0; i < items.size(); i++) {
                ItemBean item = (ItemBean) items.get(i);
                ItemFormMetadataBean ifm = ifmdao.findByItemIdAndFormLayoutId(item.getId(), formLayout.getId());

                item.setItemMeta(ifm);
                // logger.info("option******" +
                // ifm.getResponseSet().getOptions().size());
                ArrayList its = (ArrayList) versionMap.get(new Integer(ifm.getSectionId()));
                its.add(item);
            }
        } else {
            for (int i = 0; i < items.size(); i++) {
                ItemBean item = (ItemBean) items.get(i);
                ItemFormMetadataBean ifm = ifmdao.findByItemIdAndFormLayoutIdNotInIGM(item.getId(), formLayout.getId());

                item.setItemMeta(ifm);
                // logger.info("option******" +
                // ifm.getResponseSet().getOptions().size());
                ArrayList its = (ArrayList) versionMap.get(new Integer(ifm.getSectionId()));
                its.add(item);
            }
        }

        for (int i = 0; i < sections.size(); i++) {
            SectionBean section = (SectionBean) sections.get(i);
            section.setItems((ArrayList) versionMap.get(new Integer(section.getId())));
        }
        return sections;
    }

}
