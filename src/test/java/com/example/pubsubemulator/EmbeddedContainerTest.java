package com.example.pubsubemulator;

import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubAutoConfiguration;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubEmulatorAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringJUnitConfig
@Testcontainers
class EmbeddedContainerTest extends PubsubTest {

    @Container
    static PubSubEmulatorContainer pubSubEmulator = new PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:510.0.0-emulators")
    );

    @DynamicPropertySource
    static void pubSubEmulatorProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.pubsub.emulator-host", pubSubEmulator::getEmulatorEndpoint);
        registry.add("spring.cloud.gcp.project-id", () -> "my-project-id");
    }

    @Configuration(proxyBeanMethods = false)
    @ImportAutoConfiguration({
            GcpPubSubEmulatorAutoConfiguration.class,
            GcpPubSubAutoConfiguration.class,
            GcpContextAutoConfiguration.class
    })
    @Import({PubsubEmulatorConfiguration.class})
    static class TestConfiguration {

    }
}
