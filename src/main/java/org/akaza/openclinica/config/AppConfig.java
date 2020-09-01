package org.akaza.openclinica.config;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.service.randomize.RandomizationService;
import core.org.akaza.openclinica.web.filter.OpenClinicaSpringSecurityRequestAuthenticatorFactory;
import core.org.akaza.openclinica.web.rest.client.auth.impl.KeycloakClientImpl;
import core.org.akaza.openclinica.web.rest.client.impl.CustomerServiceClientImpl;
import org.akaza.openclinica.service.CoreUtilServiceImpl;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@EnableAspectJAutoProxy
public class AppConfig extends KeycloakWebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private String securedRoute = "/**";

    @Autowired
    private KeycloakClientImpl keycloakClient;

    @Autowired
    private CoreUtilServiceImpl coreUtilService;
    @Autowired
    private CustomerServiceClientImpl customerServiceClient;
    @Autowired
    ConfigurableEnvironment configurableEnvironment;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider
                = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        return multipartResolver;
    }

    @Bean
    public SmartInitializingSingleton setInstanceCustomerUuid() {
        return () -> {
            String accessToken = keycloakClient.getSystemToken();
            String customerUuid = customerServiceClient.getCustomerUuid(accessToken);
            coreUtilService.setCustomerUuid(customerUuid);
            logger.info("Customer UUID: " + customerUuid);
        };
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                .antMatchers("/css/**", "/includes/**", "/images/**", "/fonts/**",
                        "/js/**",
                        "/callback",
                        "/sso/login",
                        "/pages/customer-service/**",
                        "/pages/ocLogin",
                        "/pages/resetOCAppTimeout",
                        "/pages/logout",
                        "/pages/invalidateAuth0Token",
                        "/pages/auth/api/**",
                        "/pages/studyversion/**",
                        "/rest2/openrosa/**",
                        "/pages/odmk/**",
                        "/pages/openrosa/**",
                        "/pages/accounts/**",
                        "/pages/itemdata/**",
                        "/pages/odmss/**",
                        "/pages/v2/api-docs",
                        "/pages/swagger-resources/**"
                ).permitAll()
                .antMatchers("/partner/home").permitAll()
                .antMatchers(securedRoute).hasAnyAuthority("ROLE_USER")
                .antMatchers(securedRoute).authenticated()
                .and()
                .cors()
                .configurationSource(getCorsConfigurationSource());
        http.authorizeRequests().antMatchers("/includes/**").permitAll().anyRequest().permitAll();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
        http.csrf().disable();
    }


    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * @return CORS configuration that allows requests from Study Manager application.
     * @throws MalformedURLException if there is an error parsing study manager url.
     */
    private CorsConfigurationSource getCorsConfigurationSource() throws MalformedURLException {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.setAlwaysUseFullPath(true);


        // Set up CORS configuration for REST API endpoints
        CorsConfiguration restApiCorsConfiguration = new CorsConfiguration();
        // Allow requests originated from Study Manager
        String sbsUrl = CoreResources.getField("SBSBaseUrl");
        restApiCorsConfiguration.addAllowedOrigin(sbsUrl + "/user-service/api/users/");
        // This code should be removed once participate calls are proxied through gateway.
        restApiCorsConfiguration.addAllowedOrigin("*");
        restApiCorsConfiguration.setAllowCredentials(true);
        restApiCorsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        restApiCorsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfigurationSource.registerCorsConfiguration("/pages/auth/api/**", restApiCorsConfiguration);
        return corsConfigurationSource;
    }

    @Bean
    public KeycloakConfigResolver keycloakConfigResolver() {
        return new CustomKeycloakConfigResolver();
    }

    @Bean
    protected KeycloakAuthenticationProcessingFilter keycloakAuthenticationProcessingFilter() throws Exception {
        KeycloakAuthenticationProcessingFilter filter = new KeycloakAuthenticationProcessingFilter(authenticationManagerBean());
        filter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
        filter.setRequestAuthenticatorFactory(new OpenClinicaSpringSecurityRequestAuthenticatorFactory());
        return filter;
    }
}
