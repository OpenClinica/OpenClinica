/* 
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.service.managestudy;

import java.util.Date;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfTagDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrfTag;
import org.akaza.openclinica.domain.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;

public class EventDefinitionCrfTagService {

    @Autowired
    EventDefinitionCrfTagDao eventDefinitionCrfTagDao = null;

    @Autowired
    UserAccountDao userDaoDomain = null;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EventDefinitionCrfTagService() {
    }

    public boolean getEventDefnCrfOfflineStatus(int tagId, String crfPath, boolean active) {
        EventDefinitionCrfTag eventDefinitionCrfTag = getEventDefinitionCrfTagDao().findByCrfPath(tagId, crfPath, active);
        if (eventDefinitionCrfTag == null)
            return false;
        else
            return true;
    }

    public void saveEventDefnCrfOfflineTag(int tagId, String crfPath, EventDefinitionCRFBean edc, StudyEventDefinitionBean sed) {
        boolean active = edc.isOffline();
        EventDefinitionCrfTag eventDefinitionCrfTagActive = getEventDefinitionCrfTagDao().findByCrfPath(tagId, crfPath, true);
        EventDefinitionCrfTag eventDefinitionCrfTagNonActive = getEventDefinitionCrfTagDao().findByCrfPath(tagId, crfPath, false);

        if (active) {
            if (eventDefinitionCrfTagActive != null && sed.isRepeating()) {

            } else if (eventDefinitionCrfTagActive != null && !sed.isRepeating()) {
                updateEventDefnCrfTagObject(eventDefinitionCrfTagActive, false, edc);

            } else if (eventDefinitionCrfTagNonActive != null && sed.isRepeating()) {
                updateEventDefnCrfTagObject(eventDefinitionCrfTagNonActive, true, edc);

            } else if (eventDefinitionCrfTagNonActive != null && !sed.isRepeating()) {
            } else {
                buildAndSaveEventDefnCrfTagObject(tagId, crfPath, active, edc);
            }

        } else {
            if (eventDefinitionCrfTagNonActive != null) {
            } else if (eventDefinitionCrfTagActive != null) {
                updateEventDefnCrfTagObject(eventDefinitionCrfTagActive, false, edc);
            }

        }
    }

    public void updateEventDefnCrfTagObject(EventDefinitionCrfTag eventDefinitionCrfTag, boolean active, EventDefinitionCRFBean edc) {
        int userId = (edc.getUpdaterId() != 0) ? edc.getUpdaterId() : edc.getOwnerId();

        eventDefinitionCrfTag.setActive(active);
        eventDefinitionCrfTag.setDateUpdated(new Date());
        eventDefinitionCrfTag.setUpdateId(userId);
        getEventDefinitionCrfTagDao().saveOrUpdate(eventDefinitionCrfTag);

    }

    public void buildAndSaveEventDefnCrfTagObject(int tagId, String crfPath, boolean active, EventDefinitionCRFBean edc) {
        int userId = (edc.getUpdaterId() != 0) ? edc.getUpdaterId() : edc.getOwnerId();

        UserAccount userAccount = getUserDaoDomain().findById(userId);
        EventDefinitionCrfTag eventDefinitionCrfTag = new EventDefinitionCrfTag();
        eventDefinitionCrfTag.setTagId(tagId);
        eventDefinitionCrfTag.setPath(crfPath);
        eventDefinitionCrfTag.setActive(active);
        eventDefinitionCrfTag.setDateCreated(new Date());
        eventDefinitionCrfTag.setUserAccount(userAccount);
        getEventDefinitionCrfTagDao().saveOrUpdate(eventDefinitionCrfTag);

    }

    public UserAccountDao getUserDaoDomain() {
        return userDaoDomain;
    }

    public void setUserDaoDomain(UserAccountDao userDaoDomain) {
        this.userDaoDomain = userDaoDomain;
    }

    public void setEventDefinitionCrfTagDao(EventDefinitionCrfTagDao eventDefinitionCrfTagDao) {
        this.eventDefinitionCrfTagDao = eventDefinitionCrfTagDao;
    }

    public EventDefinitionCrfTagDao getEventDefinitionCrfTagDao() {
        return eventDefinitionCrfTagDao;
    }

}
