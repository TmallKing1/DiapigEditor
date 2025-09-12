package top.pigest.queuemanagerdemo.liveroom.data.event;

import top.pigest.queuemanagerdemo.liveroom.data.GuardType;
import top.pigest.queuemanagerdemo.liveroom.data.User;

public record GuardBuy(User user, GuardType guardType, int length) {
}
