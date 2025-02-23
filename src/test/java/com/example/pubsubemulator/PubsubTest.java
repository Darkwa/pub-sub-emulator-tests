package com.example.pubsubemulator;

import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class PubsubTest {
    @Autowired
    TopicAdminClient topicAdminClient;

    @Autowired
    SubscriptionAdminClient subscriptionAdminClient;

    @Autowired
    PubSubSubscriberTemplate subscriberTemplate;

    @Autowired
    GcpProjectIdProvider gcpProjectIdProvider;

    private final Supplier<String> projectId = () -> gcpProjectIdProvider.getProjectId();
    private final Supplier<TopicName> topic = () -> TopicName.of(projectId.get(), "my-topic");
    private final Supplier<SubscriptionName> subscription = () -> SubscriptionName.of(projectId.get(), "my-topic");

    @BeforeEach
    void initPubSubResources() {
        topicAdminClient.createTopic(topic.get());
        subscriptionAdminClient.createSubscription(subscription.get(), topic.get(), PushConfig.getDefaultInstance(), 10);
    }

    @AfterEach
    void cleanupPubSubResources() {
        topicAdminClient.deleteTopic(topic.get());
        subscriptionAdminClient.deleteSubscription(subscription.get());
    }

    @Test
    void contextLoads() {
    }

    @Test
    void canPublishAndPull() {
        publish(List.of("1", "2"));

        List<PubsubMessage> messages = pull();

        assertEquals(2, messages.size());
    }

    private List<PubsubMessage> pull() {
        return Stream.generate(() -> subscriberTemplate.pullAndAck(subscription.get().getSubscription(), 100, true))
                .takeWhile(els -> !els.isEmpty())
                .flatMap(Collection::stream)
                .toList();
    }

    private void publish(List<String> messages) {
        var pubsubMessages = messages.stream()
                .map(it -> PubsubMessage.newBuilder().setData(ByteString.copyFromUtf8(it)).build())
                .toList();
        topicAdminClient.publish(topic.get(), pubsubMessages);
    }
}
