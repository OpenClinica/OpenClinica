package org.akaza.openclinica.config;

/**
 * Created by krikorkrumlian on 3/8/17.
 */

import com.auth0.spring.security.mvc.Auth0Config;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@Configuration
@ComponentScan(basePackages = {"com.auth0.web", "com.auth0.spring.security.mvc"})
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@PropertySources({
        @PropertySource("classpath:auth0.properties")
})
public class AppConfig extends Auth0Config {
    @Override
    protected void authorizeRequests(final HttpSecurity http) throws Exception {
        System.out.println("KKKKKK");
        http.authorizeRequests()
                .antMatchers("/css/**", "/fonts/**", "/js/**", "/login", "/logout", "/callback").permitAll()
                .antMatchers("/partner/home").permitAll()
                .antMatchers("/partner/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .antMatchers(securedRoute).authenticated();
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
