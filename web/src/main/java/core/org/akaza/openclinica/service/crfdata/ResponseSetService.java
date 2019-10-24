package core.org.akaza.openclinica.service.crfdata;

import core.org.akaza.openclinica.dao.hibernate.ResponseSetDao;
import core.org.akaza.openclinica.domain.datamap.CrfVersion;
import core.org.akaza.openclinica.domain.datamap.ResponseSet;
import core.org.akaza.openclinica.domain.datamap.ResponseType;
import core.org.akaza.openclinica.domain.xform.XformItem;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

@Service
public class ResponseSetService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    private ResponseSetDao responseSetDao;

    public ResponseSetService() {
    }

    public ResponseSet getResponseSet(XformItem xformItem, CrfVersion crfVersion, ResponseType responseType, core.org.akaza.openclinica.domain.datamap.Item item,
                                      Errors errors, List<ResponseSet> responseSets) throws Exception {
        ResponseSet responseSet = null;


        for (ResponseSet response : emptyIfNull(responseSets)) {
            if (response.getLabel().equals(xformItem.getItemName())) {
                responseSet = response;
                break;
            }
        }

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
                         if(optValues.length()<=4000 && optText.length()<=4000) {
                             responseSet.setOptionsText(optText);
                             responseSet.setOptionsValues(optValues);
                             responseSet.setResponseType(responseType);
                             responseSet = responseSetDao.saveOrUpdate(responseSet);
                         }else{
                             if(optValues.length()>4000){
                                 errors.rejectValue("name", "xform_validation_error", "Form \'"+crfVersion.getCrf().getName()+ "\' Element \'" +xformItem.getItemName()+ "\' must not have total choice names longer than 4,000 characters across all versions of the form - FAILED");
                                 logger.info("Form \""+crfVersion.getCrf().getName()+ "\" Element \"" +xformItem.getItemName()+ "\" must not have total choice names longer than 4,000 characters across all versions of the form - FAILED");
                             }
                             if(optText.length()>4000){
                                 errors.rejectValue("name", "xform_validation_error", "Form \'"+crfVersion.getCrf().getName()+ "\' Element \'" +xformItem.getItemName()+ "\' must not have total choice labels longer than 4,000 characters across all versions of the form - FAILED");
                                 logger.info("Form \""+crfVersion.getCrf().getName()+ "\" Element \"" +xformItem.getItemName()+ "\" must not have total choice labels longer than 4,000 characters across all versions of the form - FAILED");
                             }
                         }

                    }
                }
            }
        }
        return responseSet;
    }

}
