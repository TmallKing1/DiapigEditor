package top.pigest.queuemanagerdemo.util.gi;

public class Struct<T> {
    private String structId;
    private T value;

    public Struct(String structId, T value) {
        this.value = value;
        this.structId = structId;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getStructId() {
        return structId;
    }

    public void setStructId(String structId) {
        this.structId = structId;
    }
}
