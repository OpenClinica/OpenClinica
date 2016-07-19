package org.akaza.openclinica.core;

import java.util.List;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

public class OpenClinicaRedisSerializer extends JdkSerializationRedisSerializer {

    @Override
    public Object deserialize(byte[] bytes) {
        Object obj = super.deserialize(bytes);
        //if (obj != null && obj.getClass().getName().startsWith("org.akaza.openclinica")) System.out.println("Deserializing " + obj.getClass().getName());

        if (obj instanceof UserAccountBean) {
            List<StudyUserRoleBean> roles = ((UserAccountBean) obj).getRoles();
            for (StudyUserRoleBean studyUserRole:roles) {
                if (studyUserRole.getRole() != null) {
                    Integer roleId = studyUserRole.getRole().getId();
                    switch (roleId) {
                        case 0: studyUserRole.setRole(Role.INVALID); break;
                        case 1: studyUserRole.setRole(Role.ADMIN); break;
                        case 2: studyUserRole.setRole(Role.COORDINATOR); break;
                        case 3: studyUserRole.setRole(Role.STUDYDIRECTOR); break;
                        case 4: studyUserRole.setRole(Role.INVESTIGATOR); break;
                        case 5: studyUserRole.setRole(Role.RESEARCHASSISTANT); break;
                        case 6: studyUserRole.setRole(Role.MONITOR); break;
                        case 7: studyUserRole.setRole(Role.RESEARCHASSISTANT2); break;
                    }
                }
            }
        }
        if (obj instanceof StudyUserRoleBean) {
            StudyUserRoleBean studyUserRole = (StudyUserRoleBean) obj;
            if (studyUserRole.getRole() != null) {
                Integer roleId = studyUserRole.getRole().getId();
                switch (roleId) {
                    case 0: studyUserRole.setRole(Role.INVALID); break;
                    case 1: studyUserRole.setRole(Role.ADMIN); break;
                    case 2: studyUserRole.setRole(Role.COORDINATOR); break;
                    case 3: studyUserRole.setRole(Role.STUDYDIRECTOR); break;
                    case 4: studyUserRole.setRole(Role.INVESTIGATOR); break;
                    case 5: studyUserRole.setRole(Role.RESEARCHASSISTANT); break;
                    case 6: studyUserRole.setRole(Role.MONITOR); break;
                    case 7: studyUserRole.setRole(Role.RESEARCHASSISTANT2); break;
                }
            }
        }
        if (obj instanceof StudyBean) {
            StudyBean study = (StudyBean) obj;
            if (study.getStatus() != null) {
                Integer statusId = study.getStatus().getId();
                switch (statusId) {
                    case 0: study.setStatus(Status.INVALID); break;
                    case 1: study.setStatus(Status.AVAILABLE); break;
                    case 2: study.setStatus(Status.UNAVAILABLE); break;
                    case 3: study.setStatus(Status.PRIVATE); break;
                    case 4: study.setStatus(Status.PENDING); break;
                    case 5: study.setStatus(Status.DELETED); break;
                    case 6: study.setStatus(Status.LOCKED); break;
                    case 7: study.setStatus(Status.AUTO_DELETED); break;
                    case 8: study.setStatus(Status.SIGNED); break;
                    case 9: study.setStatus(Status.FROZEN); break;
                    case 10: study.setStatus(Status.SOURCE_DATA_VERIFICATION); break;
                    case 11: study.setStatus(Status.RESET); break;
                }
            }
        }
        return obj;
    }

    @Override
    public byte[] serialize(Object object) {
        //if (object != null && object.getClass().getName().startsWith("org.akaza.openclinica")) System.out.println("Serializing " + object.getClass().getName());
        return super.serialize(object);
    }

}
