package top.pigest.queuemanagerdemo.liveroom.data.event;

import top.pigest.queuemanagerdemo.liveroom.data.User;

public record Danmaku(User sender, String content) {
}
