package top.pigest.queuemanagerdemo.liveroom.data.event;

import top.pigest.queuemanagerdemo.liveroom.data.User;

public record InteractWord(User user, MessageType messageType) {
    public enum MessageType {
        UNKNOWN,
        ENTER,
        SUBSCRIBE,
        SHARE
    }
}
