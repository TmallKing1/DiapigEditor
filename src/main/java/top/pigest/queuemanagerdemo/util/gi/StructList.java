package top.pigest.queuemanagerdemo.util.gi;

import java.util.ArrayList;

public class StructList<T> extends ArrayList<Struct<T>> {
    private String structId;

    public StructList(String structId) {
        this.structId = structId;
    }

    public void addValue(T value) {
        this.add(new Struct<>(structId, value));
    }

    public String getStructId() {
        return structId;
    }

    public void setStructId(String structId) {
        this.structId = structId;
    }
}
