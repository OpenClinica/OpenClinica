package org.akaza.openclinica.service.crfdata;

import java.util.HashMap;
import java.util.Map;

import org.akaza.openclinica.dao.hibernate.ResponseSetDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.ResponseSet;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.xform.XformItem;
import org.apache.commons.lang.StringUtils;
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
        String optionValues = xformItem.getOptionsValues();
        if (optionText != null && optionValues != null) {

            String[] newOptionValues = optionValues.split("(?<!\\\\),", -1);
            String[] newOptionText = optionText.split("(?<!\\\\),", -1);
            if (newOptionValues.length != newOptionText.length) {
                errors.rejectValue("name", "xform_validation_error", "Element \"" + xformItem.getItemName() + "\" on Form \"" + crfVersion.getCrf().getName()
                        + "\" does not have a valid list of choice options - FAILED");
                logger.info("Form <" + crfVersion.getCrf().getName() + "> does not have a valid list of options in choice list for Element<"
                        + xformItem.getItemName() + "> - FAILED");
                if (responseSet == null) {
                    // Create the response set
                    responseSet = new ResponseSet();
                    responseSet.setLabel(xformItem.getItemName());
                    responseSet.setOptionsText("");
                    responseSet.setOptionsValues("");
                    responseSet.setResponseType(responseType);
                    responseSet.setVersionId(crfVersion.getCrfVersionId());
                    responseSet = responseSetDao.saveOrUpdate(responseSet);
                }
            } else {
                if (responseSet == null) {
                    // Create the response set
                    responseSet = new ResponseSet();
                    responseSet.setLabel(xformItem.getItemName());

                    responseSet.setOptionsText(optionText);
                    responseSet.setOptionsValues(optionValues);
                    responseSet.setResponseType(responseType);
                    responseSet.setVersionId(crfVersion.getCrfVersionId());
                    responseSet = responseSetDao.saveOrUpdate(responseSet);

                } else {

                    String[] existingOptionValues = responseSet.getOptionsValues().split("(?<!\\\\),", -1);
                    String[] existingOptionText = responseSet.getOptionsText().split("(?<!\\\\),", -1);

                    Map<String, String> newOptionMap = new HashMap<>();
                    for (int i = 0; i < newOptionValues.length; i++) {
                        newOptionMap.put(newOptionValues[i], newOptionText[i]);
                    }

                    Map<String, String> existingOptionMap = new HashMap<>();
                    for (int i = 0; i < existingOptionValues.length; i++) {
                        existingOptionMap.put(existingOptionValues[i], existingOptionText[i]);
                    }

                    for (Map.Entry<String, String> entry : newOptionMap.entrySet()) {
                        existingOptionMap.put(entry.getKey(), entry.getValue());
                    }

                    String optValues = "";
                    String optText = "";
                    for (Map.Entry<String, String> entry : existingOptionMap.entrySet()) {
                        optValues = optValues + entry.getKey() + ",";
                        optText = optText + entry.getValue() + ",";
                    }
                    if (!StringUtils.isEmpty(optText))
                        optText = optText.substring(0, optText.length() - 1);

                    if (!StringUtils.isEmpty(optValues))
                        optValues = optValues.substring(0, optValues.length() - 1);

                    if (!optText.equals(responseSet.getOptionsText()) || !optValues.equals(responseSet.getOptionsValues())) {
                        responseSet.setOptionsText(optText);
                        responseSet.setOptionsValues(optValues);
                        responseSet.setResponseType(responseType);
                        responseSet = responseSetDao.saveOrUpdate(responseSet);
                    }
                }
            }
        }
        return responseSet;
    }

}
