package ngo.nabarun.outbox.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ngo.nabarun.event.dispatcher.AppEventDispatcher;
import ngo.nabarun.outbox.domain.port.EventOutboxRepositoryPort;
import ngo.nabarun.outbox.service.OutboxProcessor;

@Configuration
@ConditionalOnMissingBean(OutboxProcessor.class)
public class OutboxAutoConfiguration {

    @Bean
    public OutboxProcessor outboxProcessor(
    		EventOutboxRepositoryPort repository,
            AppEventDispatcher dispatcher) {
        return new OutboxProcessor(repository, dispatcher);
    }
}
