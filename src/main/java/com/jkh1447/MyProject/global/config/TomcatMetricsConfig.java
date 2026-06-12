package com.jkh1447.MyProject.global.config;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class TomcatMetricsConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatMetricsCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            TomcatMetrics tomcatMetrics = new TomcatMetrics(context.getManager(), Collections.emptyList());
            tomcatMetrics.bindTo(Metrics.globalRegistry);
        });
    }
}