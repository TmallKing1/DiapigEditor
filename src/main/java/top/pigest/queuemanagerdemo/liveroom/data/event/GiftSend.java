package top.pigest.queuemanagerdemo.liveroom.data.event;

import top.pigest.queuemanagerdemo.liveroom.data.Gift;
import top.pigest.queuemanagerdemo.liveroom.data.User;

public record GiftSend(User user, Gift gift, int actualPrice) {
}
