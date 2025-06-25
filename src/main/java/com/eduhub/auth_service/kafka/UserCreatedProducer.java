package com.eduhub.auth_service.kafka;

import java.time.Instant;
import com.eduhub.auth_service.entity.User;
import com.eduhub.auth_service.exception.EventPublishingException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCreatedProducer {

    private final ReactiveKafkaProducerTemplate<String, String> reactiveKafkaProducerTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.user-created}")
    private String topic;

    public Mono<SenderResult<Void>> sendUserCreatedEvent(User user, String source) {
        UserCreatedEvent event = new UserCreatedEvent(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                Instant.now(),
                source
        );

        try {
            String message = objectMapper.writeValueAsString(event);
            return reactiveKafkaProducerTemplate.send(topic, user.getId(), message)
                    .doOnSuccess(result -> log.info("Sent user created event: {}", event))
                    .doOnError(e -> log.error("Send failed for event: {}", event, e));
        } catch (JsonProcessingException e) {
            return Mono.error(new EventPublishingException("Failed to serialize event", e));
        }
    }
}