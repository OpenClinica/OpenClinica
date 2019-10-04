package core.org.akaza.openclinica.bean.core;


import org.keycloak.authorization.client.Configuration;

public class KeyCloakConfiguration extends Configuration {
    private final String sslRequired="external";
    private final String resource="bridge";
    private final Integer confidentialPort=new Integer(0);

    public KeyCloakConfiguration() {
        this.setConfidentialPort(confidentialPort);
        this.setResource(resource);
        this.setSslRequired(sslRequired);
    }
}
