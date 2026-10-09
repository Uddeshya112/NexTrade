package com.nextrade.service.email;

import com.nextrade.persistence.entity.OutboxEventEntity;
import com.nextrade.persistence.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void publish(com.nextrade.service.OutboxEvent event) {
        OutboxEventEntity entity = new OutboxEventEntity();
        entity.setEventId(UUID.randomUUID());
        entity.setEventType(event.getType());
        entity.setAggregateId(event.getAggregateId());
        entity.setPayload(event.getPayload());
        entity.setCreatedAt(Instant.now());
        outboxRepository.save(entity);
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void publishPending() {
        List<OutboxEventEntity> events = outboxRepository.findUnpublished(100);
        for (OutboxEventEntity e : events) {
            try {
                kafkaTemplate.send("nextrade." + e.getEventType().toLowerCase(), e.getAggregateId().toString(), e.getPayload()).get();
                e.setPublished(true);
                e.setPublishedAt(Instant.now());
                outboxRepository.save(e);
            } catch (Exception ex) {
                e.setAttemptCount(e.getAttemptCount() + 1);
                e.setNextAttemptAt(Instant.now().plusSeconds((long) Math.pow(2, e.getAttemptCount()) * 10));
                if (e.getAttemptCount() > 10) {
                    e.setStatus("DEAD_LETTER");
                    e.setFailureReason(ex.getMessage());
                }
                outboxRepository.save(e);
            }
        }
    }
}
