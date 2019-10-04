package org.akaza.openclinica.control.managestudy;

import core.org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.view.Page;
import core.org.akaza.openclinica.web.InsufficientPermissionException;

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

        if(request.getAttribute("advsearchStatus").equals(DISABLED)){
            addPageMessage(respage.getString("contacts_module_is_disabled") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("contacts_module_is_disabled"), "1");
        }


        Role r = currentRole.getRole();
        if (!r.equals(Role.INVESTIGATOR) && !r.equals(Role.RESEARCHASSISTANT)) {
            addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
            throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_crc_nor_investigator"), "1");
        }

    }


    @Override
    public void processRequest() throws Exception {
        forwardPage(Page.ADVANCED_SEARCH);
    }
}
