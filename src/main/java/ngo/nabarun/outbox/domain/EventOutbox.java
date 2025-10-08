package ngo.nabarun.outbox.domain;

import java.util.Date;

import lombok.Getter;
import ngo.nabarun.outbox.domain.enums.OutboxStatus;

@Getter
public class EventOutbox {
	private final String id;
	private final String eventType; // fully-qualified class name
	private final String payload;
	private OutboxStatus status;
	private final Date createdAt;
	private final int maxAttempts;
	private int retryCount;
	private Date processFailedAt;
	private Date processStartAt;
	private Date processEndAt;

	private String errorMessage;

	public EventOutbox(String id, String type, String payload, int maxAttempts) {
		this.eventType = type;
		this.payload = payload;
		this.id = id;
		this.createdAt = new Date();
		this.status = OutboxStatus.PENDING;
		this.maxAttempts = maxAttempts;
		this.errorMessage = "";
	}

	public void markProcessing() {
		this.status = OutboxStatus.PROCESSING;
		this.processStartAt = new Date();
	}

	public void markFailed(Exception e) {
		this.processFailedAt = new Date();
		this.retryCount++;
		this.errorMessage = this.errorMessage + " \n[Retry " + this.retryCount + "] -> " + e.getMessage();
		if (this.retryCount >= this.maxAttempts) {
			this.status = OutboxStatus.FAILED;
		} else {
			this.status = OutboxStatus.PENDING;
		}
	}

	public void markSuccess() {
		this.status = OutboxStatus.SUCCESS;
		this.processEndAt = new Date();
	}

}
