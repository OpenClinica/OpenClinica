/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package core.org.akaza.openclinica.service.managestudy;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class DiscrepancyNoteService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    private DiscrepancyNoteDAO discrepancyNoteDao;

    public DiscrepancyNoteService(DataSource ds) {
        this.ds = ds;
    }

    public void saveFieldNotes(String description, int entityId, String entityType, Study sb, UserAccountBean ub) {

        // Create a new thread each time
        DiscrepancyNoteBean parent = createDiscrepancyNoteBean(description, entityId, entityType, sb, ub, null);
        createDiscrepancyNoteBean(description, entityId, entityType, sb, ub, parent.getId());

    }

    private DiscrepancyNoteBean createDiscrepancyNoteBean(String description, int entityId, String entityType, Study sb, UserAccountBean ub,
            Integer parentId) {
        DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
        dnb.setEntityId(entityId);
        dnb.setStudyId(sb.getStudyId());
        dnb.setEntityType(entityType);
        dnb.setDescription(description);
        dnb.setDiscrepancyNoteTypeId(1);
        dnb.setResolutionStatusId(1);
        dnb.setColumn("value");
        dnb.setOwner(ub);
        if (parentId != null) {
            dnb.setParentDnId(parentId);
        }
        dnb = (DiscrepancyNoteBean) getDiscrepancyNoteDao().create(dnb);
        getDiscrepancyNoteDao().createMapping(dnb);
        return dnb;

    }

    private DiscrepancyNoteDAO getDiscrepancyNoteDao() {
        discrepancyNoteDao = this.discrepancyNoteDao != null ? discrepancyNoteDao : new DiscrepancyNoteDAO(ds);
        return discrepancyNoteDao;
    }

}
