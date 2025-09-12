package top.pigest.queuemanagerdemo.liveroom.data.event;

import top.pigest.queuemanagerdemo.liveroom.data.User;

public record SuperChat(User user, String content, int price) {
}
