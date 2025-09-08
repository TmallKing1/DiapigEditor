package top.pigest.queuemanagerdemo.liveroom;

import java.util.Collection;
import java.util.Objects;

public record LiveArea(int id, String name, Collection<SubLiveArea> subAreas) {
    public SubLiveArea findSubArea(String subId) {
        return this.subAreas.stream().filter(subLiveArea -> Objects.equals(subLiveArea.id(), subId)).findFirst().orElse(null);
    }
}
