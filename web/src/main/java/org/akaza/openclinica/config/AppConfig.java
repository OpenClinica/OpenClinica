package org.akaza.openclinica.config;

import com.auth0.AuthenticationController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import java.io.UnsupportedEncodingException;

@SuppressWarnings("unused")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AppConfig extends WebSecurityConfigurerAdapter {
    /**
     * This is your auth0 domain (tenant you have created when registering with auth0 - account name)
     */
    @Value(value = "${auth0.domain}")
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


    @Bean
    public AuthenticationController authenticationController() throws UnsupportedEncodingException {
        return AuthenticationController.newBuilder(domain, clientId, clientSecret)
                .withResponseType("code")
                .build();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
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
                .antMatchers(securedRoute).hasAnyAuthority("ROLE_USER")
                .antMatchers(securedRoute).authenticated();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);
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
}
