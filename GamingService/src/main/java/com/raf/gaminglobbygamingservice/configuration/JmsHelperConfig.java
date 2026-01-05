package com.raf.gaminglobbygamingservice.configuration;

import com.raf.jms.MessageHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ServiceLoader;

@Configuration
public class JmsHelperConfig {

    @Bean
    public MessageHelper messageHelper() {
        return ServiceLoader.load(MessageHelper.class)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No MessageHelper implementation found"));
    }
}
