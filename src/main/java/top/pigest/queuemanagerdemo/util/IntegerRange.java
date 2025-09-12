package top.pigest.queuemanagerdemo.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class IntegerRange {
    private int min;
    private int max;

    private IntegerRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean test(int value) {
        return value >= min && value <= max;
    }

    public JsonElement toJson() {
        if (min == max) {
            return new JsonPrimitive(min);
        }
        if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
            return new JsonPrimitive("any");
        }
        JsonObject obj = new JsonObject();
        if (min != Integer.MIN_VALUE) {
            obj.addProperty("min", min);
        }
        if (max != Integer.MAX_VALUE) {
            obj.addProperty("max", max);
        }
        return obj;
    }

    public static IntegerRange fromJson(JsonElement json) {
        if (json.isJsonNull()) {
            return IntegerRange.any();
        }
        if (json.isJsonPrimitive()) {
            String s = json.getAsString();
            if (s.equals("any")) {
                return IntegerRange.any();
            } else {
                int val = json.getAsInt();
                return IntegerRange.equal(val);
            }
        }
        if (json.isJsonObject()) {
            int min = Integer.MIN_VALUE;
            int max = Integer.MAX_VALUE;
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("min")) {
                min = obj.get("min").getAsInt();
            }
            if (obj.has("max")) {
                max = obj.get("max").getAsInt();
            }
            return IntegerRange.between(min, max);
        }
        return IntegerRange.any();
    }

    public static IntegerRange greaterThan(int val) {
        return new IntegerRange(val, Integer.MAX_VALUE);
    }

    public static IntegerRange lessThan(int val) {
        return new IntegerRange(Integer.MIN_VALUE, val);
    }

    public static IntegerRange equal(int val) {
        return new IntegerRange(val, val);
    }

    public static IntegerRange between(int min, int max) {
        return new IntegerRange(min, max);
    }

    public static IntegerRange any() {
        return new IntegerRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
