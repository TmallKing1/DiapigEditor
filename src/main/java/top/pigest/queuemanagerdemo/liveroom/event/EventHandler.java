package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonObject;

import java.util.function.Consumer;
import java.util.function.Function;

public class EventHandler<T> {
    private String id;
    private final Consumer<T> consumer;
    private final Function<JsonObject, Boolean> condition;

    public EventHandler(String id, Consumer<T> consumer) {
        this.id = id;
        this.consumer = consumer;
        this.condition = jsonObject -> true;
    }

    public EventHandler(String id, Consumer<T> consumer, Function<JsonObject, Boolean> condition) {
        this.id = id;
        this.consumer = consumer;
        this.condition = condition;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void consume(T t) {
        consumer.accept(t);
    }

    public boolean test(JsonObject object) {
        return condition.apply(object);
    }
}
