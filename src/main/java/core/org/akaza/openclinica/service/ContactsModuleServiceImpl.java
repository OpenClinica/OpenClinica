package core.org.akaza.openclinica.service;

import org.akaza.openclinica.controller.dto.ContactsModuleDTO;
import core.org.akaza.openclinica.dao.hibernate.*;
import core.org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.service.ValidateService;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This Service class is used with View Study Subject Page
 *
 * @author joekeremian
 */

@Service( "studyParameterService" )
public class ContactsModuleServiceImpl implements ContactsModuleService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String CONTACTS_MODULE="contactsModule";

    @Autowired
    @Qualifier( "dataSource" )
    private BasicDataSource dataSource;


    @Autowired
    StudyDao studyDao;


    @Autowired
    ValidateService validateService;


    @Autowired
    StudyParameterDao studyParameterDao;

    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";

    public void updateContactModuleStatus(String studyOid, ContactsModuleDTO contactsModuleDTO) {
        Study tenantStudy = studyDao.findByOcOID(studyOid);

        List<StudyParameterValue> studyParameterValues = tenantStudy.getStudyParameterValues();
        addParameterValue(studyParameterValues, tenantStudy, contactsModuleDTO);

        tenantStudy.setStudyParameterValues(studyParameterValues);
        studyDao.saveOrUpdate(tenantStudy);
    }


    public ContactsModuleDTO getContactModuleStatus(String studyOid) {
        Study tenantStudy = studyDao.findByOcOID(studyOid);

        List<StudyParameterValue> studyParameterValues = tenantStudy.getStudyParameterValues();
       return getParameterValue(studyParameterValues);

    }


    private ContactsModuleDTO getParameterValue(List<StudyParameterValue> studyParameterValues) {
        ContactsModuleDTO contactsModuleDTO = new ContactsModuleDTO(DISABLED);

        for (StudyParameterValue s : studyParameterValues) {
            if (s.getStudyParameter().getHandle().equals(CONTACTS_MODULE)) {
                contactsModuleDTO.setStatus(s.getValue());
                return contactsModuleDTO;
            }
        }
return contactsModuleDTO;

    }

    private void addParameterValue(List<StudyParameterValue> studyParameterValues, Study schemaStudy, ContactsModuleDTO contactsModuleDTO) {
        boolean parameterValueExist = false;
        for (StudyParameterValue s : studyParameterValues) {
            if (s.getStudyParameter().getHandle().equals(CONTACTS_MODULE)) {
                s.setValue(contactsModuleDTO.getStatus());
                parameterValueExist = true;
                break;
            }
        }


        if (!parameterValueExist) {
            StudyParameterValue studyParameterValue = new StudyParameterValue();
            studyParameterValue.setStudy(schemaStudy);
            StudyParameter sp = studyParameterDao.findByHandle(CONTACTS_MODULE);
            studyParameterValue.setStudyParameter(sp);
            studyParameterValue.setValue(contactsModuleDTO.getStatus());
            studyParameterValues.add(studyParameterValue);
        }
    }
}