package org.akaza.openclinica.controller;

import org.akaza.openclinica.core.OCCreatePostgresAppServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * Created by yogi on 5/19/17.
 */
@Configuration
public class WsConfig {
    @Autowired ApplicationContext context;
    @Bean
    public PropertiesFactoryBean auth0Properties() {
        PropertiesFactoryBean factoryBean = new PropertiesFactoryBean();
        factoryBean.setSingleton(true);
        factoryBean.setLocation(context.getResource("classpath:auth0.properties"));
        return factoryBean;
    }
}
