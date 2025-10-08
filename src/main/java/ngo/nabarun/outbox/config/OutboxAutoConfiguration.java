package ngo.nabarun.outbox.config;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.event.dispatcher.AppEventDispatcher;
import ngo.nabarun.event.handler.AppEventHandler;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;
import ngo.nabarun.outbox.service.OutboxProcessor;
import ngo.nabarun.outbox.service.OutboxPublisher;

@Configuration
@ConditionalOnMissingBean(OutboxProcessor.class)
@AutoConfiguration
public class OutboxAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AppEventDispatcher appEventDispatcher(List<AppEventHandler<?>> handlers, ObjectMapper objectMapper) {
		return new AppEventDispatcher(handlers, objectMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public OutboxProcessor outboxProcessor(EventOutboxRepositoryPort repository, AppEventDispatcher dispatcher) {
		return new OutboxProcessor(repository, dispatcher);
	}

	@Bean
	public OutboxPublisher outboxPublisher(EventOutboxRepositoryPort repository,
			ApplicationEventPublisher eventPublisher) {
		return new OutboxPublisher(repository, new ObjectMapper(), eventPublisher);
	}
}
