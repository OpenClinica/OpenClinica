/**
 * 
 */
package core.org.akaza.openclinica.service;

import org.akaza.openclinica.controller.dto.ContactsModuleDTO;

/**
 * @author joekeremian
 *
 */
public interface ContactsModuleService {

    void updateContactModuleStatus(String studyOid, ContactsModuleDTO contactsModuleDTO);

    ContactsModuleDTO getContactModuleStatus(String studyOid);


}