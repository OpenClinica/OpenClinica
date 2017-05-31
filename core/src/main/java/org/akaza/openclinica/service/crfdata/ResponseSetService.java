package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.dao.hibernate.ResponseSetDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.ResponseSet;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.xform.XformItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
public class ResponseSetService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private ResponseSetDao responseSetDao;

    public ResponseSetService() {
    }

    public ResponseSet getResponseSet(XformItem xformItem, CrfVersion crfVersion, ResponseType responseType, org.akaza.openclinica.domain.datamap.Item item,
            Errors errors) throws Exception {

        ResponseSet responseSet = responseSetDao.findByLabelVersion(xformItem.getItemName(), crfVersion.getCrfVersionId());
        String optionText = xformItem.getOptionsText();
        if (responseSet == null) {
            // Create the response set
            responseSet = new ResponseSet();
            responseSet.setLabel(xformItem.getItemName());

            if (optionText != null) {
                responseSet.setOptionsText(optionText);
                responseSet.setOptionsValues(xformItem.getOptionsValues());
                responseSet.setResponseType(responseType);
                responseSet.setVersionId(crfVersion.getCrfVersionId());
                responseSet = responseSetDao.saveOrUpdate(responseSet);
            }

        } else {
            if (optionText != null) {
                responseSet.setOptionsText(optionText);
                responseSet.setOptionsValues(xformItem.getOptionsValues());
                responseSet.setResponseType(responseType);
                responseSet = responseSetDao.saveOrUpdate(responseSet);
            }
        }
        return responseSet;
    }

}
