package org.akaza.openclinica.config;

import javax.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class StickySessionConfig {

    @Bean
    Filter springSessionRepositoryFilter() {
        return new DoNothingFilter();
    }
}
