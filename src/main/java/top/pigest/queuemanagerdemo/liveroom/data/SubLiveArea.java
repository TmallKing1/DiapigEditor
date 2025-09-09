package top.pigest.queuemanagerdemo.liveroom.data;

import java.util.Objects;

public record SubLiveArea(int liveArea, String id, String name) {
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SubLiveArea that)) return false;
        return Objects.equals(id(), that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id());
    }
}
