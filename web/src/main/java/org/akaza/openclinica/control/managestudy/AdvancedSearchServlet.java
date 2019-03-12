package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

/**
 * @author ronpanduwana
 *
 *         Processes 'advanced search' request
 */
public class AdvancedSearchServlet extends SecureController {

    /**
     * Checks whether the user has the right permission to proceed function
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {
        int roleid = ub.getActiveStudyRole().getId();
        if (roleid == Role.INVESTIGATOR.getId() || roleid == Role.RESEARCHASSISTANT.getId())
            return;
        
        throw new InsufficientPermissionException(Page.ADVANCED_SEARCH, resexception.getString("not_crc_nor_investigator"), "1");
    }


    @Override
    public void processRequest() throws Exception {
        forwardPage(Page.ADVANCED_SEARCH);
    }
}
