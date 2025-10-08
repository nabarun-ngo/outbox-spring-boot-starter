package ngo.nabarun.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.event.AppEvent;
import ngo.nabarun.event.publisher.AppEventPublisher;
import ngo.nabarun.outbox.domain.EventOutbox;
import ngo.nabarun.outbox.domain.event.OutboxCreatedEvent;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxPublisher implements AppEventPublisher{

    private final EventOutboxRepositoryPort repo;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;


    public OutboxPublisher(EventOutboxRepositoryPort repo, ObjectMapper objectMapper,ApplicationEventPublisher eventPublisher) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.eventPublisher=eventPublisher;
    }

    /**
     * Persist the domain event into outbox collection.
     * Must be called inside the same transactional boundary as domain changes.
     */
    @Transactional
    @Override
    public void publishEvent(AppEvent appEvent) {
        try {
        	String id = UUID.randomUUID().toString();
        	String type = appEvent.getClass().getName();
        	String payload = objectMapper.writeValueAsString(appEvent);
            EventOutbox e = new EventOutbox(id,type, payload, 5);
            repo.save(e);
            eventPublisher.publishEvent(new OutboxCreatedEvent(e.getId()));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize domain event", ex);
        }
    }
}
