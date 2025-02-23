package com.example.pubsubemulator;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration(proxyBeanMethods = false)
class PubsubEmulatorConfiguration {
    @Bean
    public CredentialsProvider testCredentialsProvider() {
        return new NoCredentialsProvider();
    }
}
