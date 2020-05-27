package org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.hibernate.ItemDataDao;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Created to make the capturing of item data saves in Enketo public for
// capture via Spring AoP.
// jmcinerney May 2020
@Service
public class DataSaveServiceImpl {

    @Autowired
    private ItemDataDao itemDataDao;

    public ItemData saveOrUpdate(ItemData itemData){
        return itemDataDao.saveOrUpdate(itemData);
    }
}
