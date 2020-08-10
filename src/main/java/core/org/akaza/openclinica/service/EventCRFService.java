package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.controller.dto.FormRequestDTO;
import org.akaza.openclinica.controller.dto.FormResponseDTO;

public interface EventCRFService {
    FormResponseDTO createEventCrf(FormRequestDTO formRequestDTO, String studyOid, String siteOid, UserAccountBean userAccountBean);
    FormResponseDTO updateEventCrf(FormRequestDTO formRequestDTO, String studyOid, String siteOid, UserAccountBean userAccountBean);
}