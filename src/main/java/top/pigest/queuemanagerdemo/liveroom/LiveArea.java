package top.pigest.queuemanagerdemo.liveroom;

import java.util.Collection;

public record LiveArea(int id, String name, Collection<SubLiveArea> subAreas) {
}
