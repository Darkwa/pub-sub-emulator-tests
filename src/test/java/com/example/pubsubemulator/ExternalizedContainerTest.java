package com.example.pubsubemulator;

import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.PreDestroy;

@Import({
        PubsubEmulatorConfiguration.class,
        ExternalizedContainerTest.PubSubEmulatorConfiguration.class
})
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

    @TestConfiguration(proxyBeanMethods = false)
    public static class PubSubEmulatorConfiguration {

        private ManagedChannel channel;

        @Bean(name = {"subscriberTransportChannelProvider", "publisherTransportChannelProvider"})
        @ConditionalOnProperty(prefix = "spring.cloud.gcp.pubsub", name = "emulator-host")
        public TransportChannelProvider transportChannelProvider(
                GcpPubSubProperties gcpPubSubProperties) {
            this.channel =
                    ManagedChannelBuilder.forTarget("dns:///" + gcpPubSubProperties.getEmulatorHost())
                            .usePlaintext()
                            .build();
            return FixedTransportChannelProvider.create(GrpcTransportChannel.create(this.channel));
        }

        @PreDestroy
        public void closeManagedChannel() {
            if (this.channel != null) {
                this.channel.shutdown();
            }
        }
    }
}
