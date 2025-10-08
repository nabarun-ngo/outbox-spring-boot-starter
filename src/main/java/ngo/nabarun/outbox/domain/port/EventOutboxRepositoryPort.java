package ngo.nabarun.outbox.domain.port;

import java.util.List;
import java.util.Optional;

import ngo.nabarun.outbox.domain.EventOutbox;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;

public interface EventOutboxRepositoryPort {
    Optional<EventOutbox> findById(String id);
    EventOutbox save(EventOutbox event);
    void updateStatus(String id, OutboxStatus status);
	List<EventOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus pending);
}
