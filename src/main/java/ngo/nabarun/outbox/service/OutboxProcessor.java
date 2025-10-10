package ngo.nabarun.outbox.service;

import java.util.List;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import ngo.nabarun.event.dispatcher.AppEventDispatcher;
import ngo.nabarun.outbox.Constant;
import ngo.nabarun.outbox.domain.EventOutbox;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;
import ngo.nabarun.outbox.domain.event.OutboxCreatedEvent;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;

@Component
public class OutboxProcessor {

	private final EventOutboxRepositoryPort outboxRepositoryPort;
	private final AppEventDispatcher dispatcher;

	public OutboxProcessor(EventOutboxRepositoryPort outboxRepositoryPort, AppEventDispatcher dispatcher) {
		this.outboxRepositoryPort = outboxRepositoryPort;
		this.dispatcher = dispatcher;
	}

	/** Immediate async processing for a newly saved event */
	@Async
    @Retryable(maxAttempts = Constant.MAX_RETRY, retryFor = {Exception.class},backoff = @Backoff(delay = 5000,multiplier = 2))
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleOutboxSaved(OutboxCreatedEvent event) {
		processById(event.outboxId());
	}

	/** Opportunistic retry: process pending events on incoming request */
	@Async
	public void retryPendingEvents(int maxProcess) {
		List<EventOutbox> pending = outboxRepositoryPort.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
		int processed = 0;
		for (EventOutbox event : pending) {
			if (processed >= maxProcess)
				break;
			processById(event.getId());
			processed++;
		}
	}

	/** Internal method to process safely */
	public void processById(String outboxId) {
		outboxRepositoryPort.findById(outboxId).ifPresent(this::processEventSafely);
	}

	/** Actual processing with retry logic and status update 
	 * @throws Exception */
	private void processEventSafely(EventOutbox outboxEvent) {
		try {
			outboxEvent.markProcessing();
			outboxRepositoryPort.save(outboxEvent);
			dispatcher.dispatch(outboxEvent.getEventType(), outboxEvent.getPayload());
			outboxEvent.markSuccess();
			outboxRepositoryPort.save(outboxEvent);
		} catch (Exception e) {
			outboxEvent.markFailed(e);
			outboxRepositoryPort.save(outboxEvent);
			throw new RuntimeException(e);
		}
	}
}
