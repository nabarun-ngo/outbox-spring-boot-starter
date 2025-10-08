package ngo.nabarun.event.dispatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

import ngo.nabarun.event.handler.AppEventHandler;

import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppEventDispatcher {

    private final Map<String, AppEventHandler<?>> handlerByEventType = new HashMap<>();
    private final ObjectMapper objectMapper;

    public AppEventDispatcher(List<AppEventHandler<?>> handlers, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        handlers.forEach(this::registerHandler);
    }

    private void registerHandler(AppEventHandler<?> handler) {
        ResolvableType rt = ResolvableType.forClass(handler.getClass()).as(AppEventHandler.class);
        Class<?> eventType = rt.getGeneric(0).resolve();
        if (eventType != null) {
            handlerByEventType.put(eventType.getName(), handler);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void dispatch(String eventTypeName, String payload) throws Exception {
    	AppEventHandler handler = handlerByEventType.get(eventTypeName);
        if (handler == null) {
            throw new IllegalStateException("No handler registered for " + eventTypeName);
        }

        Class<?> clazz = Class.forName(eventTypeName);
        Object eventObj = objectMapper.readValue(payload, clazz);
        handler.handle(eventObj);
    }
}
