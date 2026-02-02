package top.pigest.dialogeditor.util.gi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Function;

public class GIVariableType<T> {
    private final String name;
    private final Function<JsonElement, T> converter;

    public GIVariableType(String name, Function<JsonElement, T> converter) {
        this.name = name;
        this.converter = converter;
    }

    public String getName() {
        return name;
    }

    public T convertVariable(JsonObject obj) {
        if (GIVariableUtils.getParamType(obj).equals(name)) {
            return convertValue(obj.get("value"));
        }
        throw new RuntimeException("Illegal variable type.");
    }

    public T convertValue(JsonElement value) {
        return converter.apply(value);
    }
}
