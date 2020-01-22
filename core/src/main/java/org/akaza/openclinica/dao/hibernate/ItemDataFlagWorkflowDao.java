/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.domain.datamap.EventCrfFlag;
import org.akaza.openclinica.domain.datamap.ItemDataFlag;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemDataFlagWorkflow;

public class ItemDataFlagWorkflowDao extends AbstractDomainDao<ItemDataFlagWorkflow> {

    @Override
    Class<ItemDataFlagWorkflow> domainClass() {
        // TODO Auto-generated method stub
        return ItemDataFlagWorkflow.class;
    }


    
    
}
