package com.meska.consumer.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meska.consumer.entity.WikimediaEvent;
import com.meska.consumer.repo.WikimediaEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class WikimediaConsumer {

    private final ObjectMapper objectMapper;
    private final WikimediaEventRepository wikimediaEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "wikimedia-stream", groupId = "myGroup")
    public void consumeMsg(String msg) {
        log.info("Consuming the message from wikimedia-stream Topic:: {}", msg);
        processMessage(msg);
    }

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void processMessage(String msg) {
        try {
            JsonNode json = objectMapper.readTree(msg);

            // validate required fields
            if (!isValidEvent(json)) {
                log.error("Invalid event, missing required fields: {}", msg);
                kafkaTemplate.send("wikimedia-stream-dlq", msg);
                log.info("Sent to DLQ: {}", msg);
                return;
            }
            WikimediaEvent event = new WikimediaEvent();
            event.setId(json.get("id").asText());
            event.setType(json.get("type").asText());
            event.setTitle(json.get("title").asText());
            event.setUser(json.get("user").asText());
            event.setTimestamp(json.get("timestamp").asLong());
            event.setWiki(json.get("wiki").asText());
            event.setComment(json.get("comment").asText());

            wikimediaEventRepository.save(event);
            log.info("Saved event with ID: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to process message: {}", msg, e);
            kafkaTemplate.send("wikimedia-stream-dlq", msg);
            log.info("Sent to DLQ: {}", msg);
            throw new RuntimeException("Moved to DLQ", e);
        }
    }

    private boolean isValidEvent(JsonNode json) {
        return json.hasNonNull("id") &&
                json.hasNonNull("type") &&
                json.hasNonNull("title") &&
                json.hasNonNull("user") &&
                json.hasNonNull("timestamp") &&
                json.hasNonNull("wiki");
    }
}
