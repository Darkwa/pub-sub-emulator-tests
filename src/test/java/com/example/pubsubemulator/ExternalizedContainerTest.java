package com.example.pubsubemulator;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@Import(PubsubEmulatorConfiguration.class)
@ImportTestcontainers(ExternalizedContainerTest.PubsubContainer.class)
@SpringBootTest
@DirtiesContext
class ExternalizedContainerTest extends PubsubTest {

    public static class PubsubContainer {
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
}
