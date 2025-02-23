package com.example.pubsubemulator;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.spring.autoconfigure.pubsub.GcpPubSubProperties;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;

@TestConfiguration(proxyBeanMethods = false)
class PubsubEmulatorConfiguration {
    private ManagedChannel channel;

    @Bean
    public CredentialsProvider testCredentialsProvider() {
        return new NoCredentialsProvider();
    }

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
