/**
 * 
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.controller.dto.ContactsModuleDTO;
import org.akaza.openclinica.domain.datamap.StudyParameterValue;

import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface ContactsModuleService {

    void updateContactModuleStatus(String studyOid, ContactsModuleDTO contactsModuleDTO);

    ContactsModuleDTO getContactModuleStatus(String studyOid);


}