package ngo.nabarun.event.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import ngo.nabarun.event.handler.AppEventHandler;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AppEventDispatcher {

    private final Map<String, AppEventHandler<?>> handlerByEventType = new HashMap<>();
    private final ObjectMapper objectMapper;

    public AppEventDispatcher(List<AppEventHandler<?>> handlers, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        handlers.forEach(this::registerHandler);
        log.info("Registered {} event handlers", handlerByEventType.size());
    }

    private void registerHandler(AppEventHandler<?> handler) {
        ResolvableType rt = ResolvableType.forClass(handler.getClass()).as(AppEventHandler.class);
        Class<?> eventType = rt.getGeneric(0).resolve();
        if (eventType != null) {
            handlerByEventType.put(eventType.getName(), handler);
            log.debug("Registered handler [{}] for event type [{}] with classloader [{}]",
                    handler.getClass().getSimpleName(),
                    eventType.getName(),
                    handler.getClass().getClassLoader());
        } else {
            log.warn("Could not resolve generic type for handler [{}]", handler.getClass().getName());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void dispatch(String eventTypeName, String payload) throws Exception {
        AppEventHandler handler = handlerByEventType.get(eventTypeName);
        if (handler == null) {
            log.error("No handler registered for event type {}", eventTypeName);
            throw new IllegalStateException("No handler registered for " + eventTypeName);
        }

        // Use application classloader to avoid class mismatch
        ClassLoader appClassLoader = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = Class.forName(eventTypeName, true, appClassLoader);
        Object eventObj = objectMapper.readValue(payload, clazz);

        log.debug("Dispatching event [{}] to handler [{}]", clazz.getName(), handler.getClass().getSimpleName());
        log.debug("Handler classloader: {}, Event classloader: {}",
                handler.getClass().getClassLoader(),
                eventObj.getClass().getClassLoader());

        try {
            handler.handle(eventObj);
            log.info("Successfully dispatched event [{}] to handler [{}]",
                    clazz.getName(), handler.getClass().getSimpleName());
        } catch (Exception ex) {
            log.error("Exception in handler [{}] for event [{}]", handler.getClass().getSimpleName(), clazz.getName(), ex);
            throw ex;
        }
    }
}
