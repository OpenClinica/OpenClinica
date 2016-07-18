package org.akaza.openclinica.validator.xform;

import org.akaza.openclinica.dao.hibernate.ResponseSetDao;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ResponseSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Arrays;
import java.util.List;

public class ResponseSetValidator implements Validator {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private ResponseSetDao responseSetDao = null;
    private Item item;

    public ResponseSetValidator(ResponseSetDao responseSetDao, Item item) {
        this.responseSetDao = responseSetDao;
        this.item = item;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ResponseSet.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ResponseSet responseSet = (ResponseSet) target;
        if (responseSet.getResponseSetId() == 0 ){
            errors.rejectValue("label", "found_unsupported_usercontrol", item.getName());    
            return;
        }
        
        List<String> newTexts = Arrays.asList(responseSet.getOptionsText().split("(?<!\\\\),"));
        List<String> newValues = Arrays.asList(responseSet.getOptionsValues().split("(?<!\\\\),"));

        // Look up existing response sets derived from the same item in existing crf versions
        List<ResponseSet> existingSets = responseSetDao.findAllByItemId(item.getItemId());

        for (int i = 0; i < existingSets.size(); i++) {
            ResponseSet existingSet = existingSets.get(i);
            List<String> existingTexts = Arrays.asList(existingSet.getOptionsText().split("(?<!\\\\),"));
            List<String> existingValues = Arrays.asList(existingSet.getOptionsValues().split("(?<!\\\\),"));

            // Verify response set label has not changed
            if (!existingSet.getLabel().equals(responseSet.getLabel())) {
                errors.rejectValue("label", "crf_val_responseset_difflabel", item.getName());
                return;
            }

            for (int j = 0; j < existingTexts.size(); j++) {
                for (int k = 0; k < newTexts.size(); k++) {
                    // Verify a text is not changed on an exist value or vice versa
                    if (existingTexts.get(j).equalsIgnoreCase(newTexts.get(k)) && !existingValues.get(j).equals(newValues.get(k))) {
                        logger.debug("Found modified response value (not allowed).  Old value:  " + existingValues.get(j) + " - New value: " + newValues.get(k));
                        errors.rejectValue("optionsValues", "crf_val_responseset_diffoptionvalues", item.getName());
                        return;
                    } else if (!existingTexts.get(j).equalsIgnoreCase(newTexts.get(k)) && existingValues.get(j).equals(newValues.get(k))) {
                        logger.debug("Found modified response text (not allowed).  Old text:  " + existingValues.get(j) + " - New text: " + newValues.get(k));
                        errors.rejectValue("optionsText", "crf_val_responseset_diffoptiontext", item.getName());
                        return;
                    }

                }
            }
        }
    }

}
