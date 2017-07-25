package org.akaza.openclinica.domain.datamap;

/**
 * Created by yogi on 4/27/17.
 */

/**
 * Enum representing the types of environment to which the protocol can be published
 */

public enum StudyEnvEnum {
    PROD("prod"),TEST("test");
    private String env;
    StudyEnvEnum(String env) {
        this.env = env;
    }
    @Override
    public String toString(){
        return env;
    }
}
