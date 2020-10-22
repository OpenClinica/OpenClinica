package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventCrfService {
    @Autowired
    EventCrfDao eventCrfDao;

    // This method is listened by AOP
    public EventCrf saveOrUpdate(EventCrf eventCrf){
        return eventCrfDao.saveOrUpdate(eventCrf);
    }

    // This method is not listened by AOP
    public EventCrf saveOrUpdateWithoutAOPListener(EventCrf eventCrf){
        return eventCrfDao.saveOrUpdate(eventCrf);
    }
}
