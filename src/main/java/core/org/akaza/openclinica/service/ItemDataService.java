package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.hibernate.ItemDataDao;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ItemDataService {

    @Autowired
    ItemDataDao itemDataDao;

    // This method is listened by AOP
    public ItemData saveOrUpdate(ItemData itemData){
        return itemDataDao.saveOrUpdate(itemData);
    }

    // This method is not listened by AOP
    public ItemData saveOrUpdateWithoutAOPListener(ItemData itemData){
        return itemDataDao.saveOrUpdate(itemData);
    }
}
