package ngo.nabarun.event.handler;

public interface AppEventHandler<T> {
    void handle(T event) throws Exception;
}
