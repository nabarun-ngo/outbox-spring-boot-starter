package ngo.nabarun.outbox.config;

import java.util.List;
import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
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
@AutoConfiguration
@EnableAsync
@EnableRetry
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
}
