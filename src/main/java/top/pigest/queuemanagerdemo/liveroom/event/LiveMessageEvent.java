package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public abstract class LiveMessageEvent<T> {
    private final List<EventHandler<T>> handlers;
    private final String command;

    LiveMessageEvent(String command) {
        this.handlers = new ArrayList<>();
        this.command = command;
    }

    protected abstract T convert(JsonObject jsonObject);

    public void onReceive(JsonObject jsonObject) {
        if (jsonObject.get("cmd").getAsString().equals(command)) {
            List<EventHandler<T>> list = handlers.stream().filter(h -> h.test(jsonObject)).toList();
            if (!list.isEmpty()) {
                T obj = convert(jsonObject);
                list.forEach(h -> h.consume(obj));
            }
        }
    }

    public void addHandler(EventHandler<T> handler) {
        removeHandler(handler.getId());
        this.handlers.add(handler);
    }

    public void removeHandler(String id) {
        this.handlers.removeIf(h -> h.getId().equals(id));
    }
}
