package org.akaza.openclinica.config;

/**
 * Created by krikorkrumlian on 3/9/17.
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

/**
 * Holds the configuration specific to this SSO app
 * Taken from properties files
 *
 * Illustrates how easy it is to add custom properties configuration
 * to an application in addition to the auth0.properties the library provides
 *
 * See sso.properties file
 *
 */
@Component
@Configuration
@ConfigurationProperties("sso")
@PropertySources({@PropertySource("classpath:sso.properties")})
public class SsoConfig {
    @Value(value = "${sso.logoutEndpoint}")
    protected String logoutEndpoint;
    @Value(value = "${sso.partnerLoginUrl}")
    protected String partnerLoginUrl;

    public String getLogoutEndpoint() {
        return logoutEndpoint;
    }
    public String getPartnerLoginUrl() {
        return partnerLoginUrl;
    }
}
