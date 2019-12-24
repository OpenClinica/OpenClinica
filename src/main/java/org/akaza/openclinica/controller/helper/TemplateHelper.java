package org.akaza.openclinica.controller.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@JsonIgnoreProperties
public class TemplateHelper {

    public Integer random(int digit_number) throws TemplateException {
        if (digit_number < 1 || digit_number > 9) {
            throw new TemplateException("Random number " + digit_number + " is out of range", null);
        }
        int max = Integer.parseInt(StringUtils.repeat("9", digit_number));
        return ThreadLocalRandom.current().nextInt(0, max + 1);
    }
}
