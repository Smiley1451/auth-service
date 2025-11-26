package com.eduhub.auth_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import reactor.kafka.sender.SenderOptions;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import java.util.HashMap;
import java.util.Map;
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topics.user-created}")
    private String userCreatedTopic;

    @Bean
    public NewTopic userCreatedTopic() {
        return new NewTopic(userCreatedTopic, 3, (short) 1);
    }

    @Bean
    public ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 300000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 300000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 300000);

        return new ReactiveKafkaProducerTemplate<>(SenderOptions.create(props));
    }
}