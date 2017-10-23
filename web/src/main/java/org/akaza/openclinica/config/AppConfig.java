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
import org.springframework.security.config.http.SessionCreationPolicy;

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
        http.exceptionHandling().authenticationEntryPoint(new OCLoginUrlAuthenticationEntryPoint("/pages/home"));
        http.authorizeRequests()
                .antMatchers("/css/**","/includes/**","/images/**", "/fonts/**",
                        "/js/**", "/login", "/logout",
                        "/pages/callback",
                        "/pages/home",
                        "/pages/logout",
                        "/pages/invalidateSession",
                        "/pages/auth/api/**",
                        "/pages/studyversion/**",
                        "/rest2/openrosa/**",
                        "/pages/odmk/**",
                        "/pages/openrosa/**",
                        "/pages/accounts/**",
                        "/pages/itemdata/**",
                        "/pages/auth/api/v1/studies/**",
                        "/pages/odmss/**"
                ).permitAll()
                .antMatchers("/partner/home").permitAll()
                .antMatchers("/**").hasAnyAuthority("ROLE_USER")
                .antMatchers(securedRoute).authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    // authenticationProcessingFilterEntryPoint
}
