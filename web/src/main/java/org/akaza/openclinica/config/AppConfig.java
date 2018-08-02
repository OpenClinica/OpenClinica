package org.akaza.openclinica.config;

import com.auth0.AuthenticationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy ;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressWarnings("unused")
@Configuration
@EnableWebSecurity (debug = false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@PropertySources({
        @PropertySource("classpath:auth0.properties"),
        @PropertySource("classpath:datainfo.properties")
})
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)

public class AppConfig extends WebSecurityConfigurerAdapter {
    /**
     * This is your auth0 domain (tenant you have created when registering with auth0 - account name)
     */
    @Value("${auth0.domain}")
    private String domain;

    /**
     * This is the client id of your auth0 application (see Settings page on auth0 dashboard)
     */
    @Value(value = "${auth0.clientId}")
    private String clientId;

    /**
     * This is the client secret of your auth0 application (see Settings page on auth0 dashboard)
     */
    @Value(value = "${auth0.clientSecret}")
    private String clientSecret;

    @Value(value = "${auth0.securedRoute}")
    private String securedRoute;

    @Value(value = "${SBSUrl}")
    private String sbsUrl;

    @Bean
    public AuthenticationController authenticationController() throws UnsupportedEncodingException {
        return AuthenticationController.newBuilder(domain, clientId, clientSecret)
                .withResponseType("code")
                .build();
    }


    @Bean
    public MultipartResolver multipartResolver() {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver();
        return multipartResolver;
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint = new OCLoginUrlAuthenticationEntryPoint("/pages/login");
        loginUrlAuthenticationEntryPoint.setUseForward(false);
        http.exceptionHandling().authenticationEntryPoint(loginUrlAuthenticationEntryPoint);
        http.authorizeRequests()
                .antMatchers("/css/**","/includes/**","/images/**", "/fonts/**",
                        "/js/**",
                        "/callback", "/login",
                        "/pages/customer-service/**",
                        "/pages/callback",
                        "/pages/login",
                        "/pages/resetOCAppTimeout"
,                       "/pages/logout",
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

    public String getDomain() {
        return domain;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
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
        restApiCorsConfiguration.setAllowCredentials(true);
        restApiCorsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        restApiCorsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        corsConfigurationSource.registerCorsConfiguration("/pages/auth/api/**", restApiCorsConfiguration);
        return corsConfigurationSource;
    }
}
