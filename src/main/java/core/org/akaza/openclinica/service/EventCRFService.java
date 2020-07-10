package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.RestReponseDTO;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.controller.dto.FormUpdateRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventScheduleRequestDTO;
import org.akaza.openclinica.controller.dto.StudyEventUpdateRequestDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


public interface EventCRFService {
    Object eventCrfProcess(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String methodType);
    void populateOdmContainerForFormUpdate(ODMContainer odmContainer, FormUpdateRequestDTO formUpdateRequestDTO, String siteOid);
}
