package org.akaza.openclinica.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.Client;
import com.stormpath.spring.config.EnableStormpath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by yogi on 7/8/16.
 */
@Configuration
@EnableStormpath //enables the @Autowired beans below
@ComponentScan("org.akaza.openclinica")
@PropertySource("classpath:application.properties")
public class SSOAppConfig {



}
