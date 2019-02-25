package org.akaza.openclinica.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
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
import java.net.URL;

@SuppressWarnings("unused")
@KeycloakConfiguration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySources({
        @PropertySource("classpath:datainfo.properties")
})
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)

public class AppConfig extends KeycloakWebSecurityConfigurerAdapter {



    private String securedRoute = "/**";

    @Value(value = "${SBSUrl}")
    private String sbsUrl;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider
                = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }
/*
    @Override
    protected AuthenticationEntryPoint authenticationEntryPoint() throws Exception {
        KeycloakAuthenticationEntryPoint authenticationEntryPoint = new KeycloakAuthenticationEntryPoint(this.adapterDeploymentContext());
        authenticationEntryPoint.setLoginUri("/pages/ocLogin");
        return authenticationEntryPoint;
    }
*/

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
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.authorizeRequests()
                .antMatchers("/css/**","/includes/**","/images/**", "/fonts/**",
                        "/js/**",
                        "/callback", "/sso/login",
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

        URL studyManagerUrl = new URL(sbsUrl);
        String studyManagerHost = studyManagerUrl.getProtocol() + "://" + studyManagerUrl.getAuthority();

        // Set up CORS configuration for REST API endpoints
        CorsConfiguration restApiCorsConfiguration = new CorsConfiguration();
        // Allow requests originated from Study Manager
        restApiCorsConfiguration.addAllowedOrigin(studyManagerHost);
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
}
