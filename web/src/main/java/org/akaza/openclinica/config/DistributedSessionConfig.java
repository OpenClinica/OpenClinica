package org.akaza.openclinica.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession
@Profile(DistributedSessionConfig.DISTRIBUTED_SESSION_SPRING_PROFILE)
public class DistributedSessionConfig {
    
    public static final String DISTRIBUTED_SESSION_SPRING_PROFILE = "distributedSession";
    public static final String REDIS_URL = "REDIS_URL";

    @Autowired
    private Environment environment;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() throws Exception {
        
        String port = null;
        String host = null;
        String password = null;
        
        try {
            // Redis URL should be of the format redis://h:<PASSWORD>@<HOSTNAME>:<PORT>
            String redisUrl = environment.getProperty(REDIS_URL);

            port = redisUrl.substring(redisUrl.lastIndexOf(":") + 1, redisUrl.length());
            host = redisUrl.substring(redisUrl.lastIndexOf("@") + 1, redisUrl.lastIndexOf(":"));
            password = redisUrl.substring(redisUrl.indexOf("h:") + 2, redisUrl.lastIndexOf("@"));
            if (port == null || port.equals("") || host == null || host.equals("") || password == null || password.equals(""))
                throw new Exception();
        } catch (Exception e) {
            throw new Exception("Error:  REDIS_URL environment variable not defined or improperly formatted.");
        }
        
        JedisConnectionFactory jedisConnFactory = new JedisConnectionFactory();
        jedisConnFactory.setHostName(host);
        jedisConnFactory.setPort(Integer.valueOf(port));
        jedisConnFactory.setPassword(password);
        return jedisConnFactory;
    }

    @Bean
    public OpenClinicaRedisSerializer springSessionDefaultRedisSerializer() {
        return new OpenClinicaRedisSerializer();
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
