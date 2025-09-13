package top.pigest.queuemanagerdemo.liveroom.event;

import java.util.ArrayList;
import java.util.List;

public final class EventRegistry {
    private static final List<LiveMessageEvent<?>> REGISTRIES = new ArrayList<>();

    static {
        REGISTRIES.add(DanmakuEvent.INSTANCE);
        REGISTRIES.add(InteractEvent.INSTANCE);
        REGISTRIES.add(GiftSendEvent.INSTANCE);
        REGISTRIES.add(GuardBuyEvent.INSTANCE);
        REGISTRIES.add(SuperChatEvent.INSTANCE);
    }

    public static List<LiveMessageEvent<?>> getRegistries() {
        return REGISTRIES;
    }
}
