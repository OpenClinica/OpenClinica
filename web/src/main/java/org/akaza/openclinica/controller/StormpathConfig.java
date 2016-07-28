package org.akaza.openclinica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

/**
 * Created by yogi on 7/27/16.
 */
@Configuration
@PropertySource(value = {"classpath:stormpath.properties"})
public class StormpathConfig {

    @Autowired
    private Environment env;


}
