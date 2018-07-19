package org.akaza.openclinica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global freemarker configuration.
 * @author svadla@openclinica.com
 */
@Configuration
public class FreemarkerConfig {
    private static final String COMPUTER_NUMBER_FORMAT = "computer";

    @Bean
    public freemarker.template.Configuration freemarkerConfiguration() {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration();
        configuration.setNumberFormat(COMPUTER_NUMBER_FORMAT);
        return configuration;
    }
}
