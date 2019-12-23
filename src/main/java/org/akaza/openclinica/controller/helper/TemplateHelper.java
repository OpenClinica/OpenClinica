package org.akaza.openclinica.controller.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Random;

@JsonIgnoreProperties
public class TemplateHelper {


    public Integer random(int digit_number) {
        switch (digit_number) {
            case 1:
                return new Random().nextInt(9);
            case 2:
                return new Random().nextInt(99);
            case 3:
                return new Random().nextInt(999);
            case 4:
                return new Random().nextInt(9999);
            case 5:
                return new Random().nextInt(99999);
            case 6:
                return new Random().nextInt(999999);
            case 7:
                return new Random().nextInt(9999999);
            case 8:
                return new Random().nextInt(99999999);
            case 9:
                return new Random().nextInt(999999999);
            default:
                return null;
        }
    }

}
