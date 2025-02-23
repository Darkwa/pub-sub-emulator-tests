package com.example.pubsubemulator;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class PubsubContainer {
    @Container
    static PubSubEmulatorContainer pubSubEmulator = new PubSubEmulatorContainer(
            DockerImageName.parse("gcr.io/google.com/cloudsdktool/google-cloud-cli:510.0.0-emulators")
    );

    @DynamicPropertySource
    static void pubSubEmulatorProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.gcp.pubsub.emulator-host", pubSubEmulator::getEmulatorEndpoint);
        registry.add("spring.cloud.gcp.project-id", () -> "my-project-id");
    }
}
