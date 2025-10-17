package ngo.nabarun.event.publisher;

import ngo.nabarun.event.AppEvent;

public interface AppEventPublisher {

    public void publishAppEvent(AppEvent event);
    
    public <T> void publishEvent(T event);

}