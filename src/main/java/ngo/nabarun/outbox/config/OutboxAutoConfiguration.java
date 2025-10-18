package ngo.nabarun.outbox.config;

import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.event.dispatcher.AppEventDispatcher;
import ngo.nabarun.event.handler.AppEventHandler;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;
import ngo.nabarun.outbox.service.OutboxProcessor;
import ngo.nabarun.outbox.service.OutboxPublisher;

@Configuration
@ConditionalOnMissingBean(OutboxProcessor.class)
@EnableAsync
@EnableRetry
public class OutboxAutoConfiguration {

	@Value("${outbox.retry.maxAttempts:5}")
	private int maxAttempts;

	@Value("${outbox.retry.initialInterval:5000}")
	private long initialInterval;

	@Value("${outbox.retry.multiplier:2.0}")
	private double multiplier;

	@Value("${outbox.retry.maxInterval:900000}")
	private long maxInterval;

	@Bean
	@ConditionalOnMissingBean
	public AppEventDispatcher appEventDispatcher(List<AppEventHandler<?>> handlers, ObjectMapper objectMapper) {
		return new AppEventDispatcher(handlers, objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public OutboxProcessor outboxProcessor(EventOutboxRepositoryPort repository, AppEventDispatcher dispatcher,
			RetryTemplate retryTemplate) {
		return new OutboxProcessor(repository, dispatcher, retryTemplate);
	}

	@Bean
	public OutboxPublisher outboxPublisher(EventOutboxRepositoryPort repository,
			ApplicationEventPublisher eventPublisher) {
		return new OutboxPublisher(repository, new ObjectMapper(), eventPublisher);
	}

	@Bean(name = "outboxExecutor")
	@ConditionalOnMissingBean
	public Executor outboxExecutor() {
		ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
		exec.setCorePoolSize(2);
		exec.setMaxPoolSize(5);
		exec.setQueueCapacity(50);
		exec.setThreadNamePrefix("outbox-");
		exec.initialize();
		return exec;
	}

	@Bean
	@ConditionalOnMissingBean
	RetryTemplate retryTemplate() {
		RetryTemplate retryTemplate = new RetryTemplate();

		// Retry Policy
		SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
		retryPolicy.setMaxAttempts(maxAttempts);
		retryTemplate.setRetryPolicy(retryPolicy);

		// Backoff Policy
		ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
		backOffPolicy.setInitialInterval(initialInterval);
		backOffPolicy.setMultiplier(multiplier);
		backOffPolicy.setMaxInterval(maxInterval);
		retryTemplate.setBackOffPolicy(backOffPolicy);

		return retryTemplate;
	}
}
