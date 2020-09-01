package core.org.akaza.openclinica.service.modules;

import core.org.akaza.openclinica.bean.service.StudyParameterValueBean;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.config.StudyParamNames;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("studyCalendarService")
public class StudyCalendarServiceImpl implements StudyCalendarService {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    StudyParameterValueDAO studyParameterValueDAO;

    @Override
    public void processModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study study) {
        Optional<ModuleConfigDTO> studyCalendarModuleConfig = moduleConfigDTOs.stream().findFirst();

        if (studyCalendarModuleConfig.isPresent()){
            updateStudyCalendarStatus(studyCalendarModuleConfig.get(), study);
        }
    }

    private void updateStudyCalendarStatus(ModuleConfigDTO studyCalendarModuleConfig, Study study) {
        StudyParameterValueBean accessLinkParameterValue = studyParameterValueDAO.findByHandleAndStudy(study.getStudyId(), StudyParamNames.STUDY_CALENDAR);
        String moduleStatus = studyCalendarModuleConfig.getStatus().name();

        if (!accessLinkParameterValue.isActive()) {
            accessLinkParameterValue = new StudyParameterValueBean();
            accessLinkParameterValue.setStudyId(study.getStudyId());
            accessLinkParameterValue.setParameter(StudyParamNames.STUDY_CALENDAR);
            accessLinkParameterValue.setValue(moduleStatus);
            studyParameterValueDAO.create(accessLinkParameterValue);
        } else if (accessLinkParameterValue.isActive() && !accessLinkParameterValue.getValue().equals(moduleStatus)) {
            accessLinkParameterValue.setValue(moduleStatus);
            studyParameterValueDAO.update(accessLinkParameterValue);
        }
    }
}
