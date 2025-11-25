package top.pigest.dialogeditor.util.gi;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GIVariableUtils {
    public static int readInt(JsonObject jsonObject) {
        if (getParamType(jsonObject).equals("Int32")) {
            return Integer.parseInt(getValue(jsonObject));
        }
        return 0;
    }

    public static int readInt(JsonArray jsonArray, int index) {
        return readInt(jsonArray.get(index).getAsJsonObject());
    }

    public static String readString(JsonObject jsonObject) {
        if (getParamType(jsonObject).equals("String")) {
            return getValue(jsonObject);
        }
        return "";
    }

    public static String readString(JsonArray jsonArray, int index) {
        return readString(jsonArray.get(index).getAsJsonObject());
    }

    public static List<String> readStringList(JsonObject jsonObject) {
        if (getParamType(jsonObject).equals("StringList")) {
            JsonArray jsonArray = jsonObject.getAsJsonArray("value");
            List<String> list = new ArrayList<>();
            jsonArray.forEach(jsonElement -> {list.add(jsonElement.getAsString());});
            return list;
        }
        return new ArrayList<>();
    }

    public static List<String> readStringList(JsonArray jsonArray, int index) {
        return readStringList(jsonArray.get(index).getAsJsonObject());
    }

    public static boolean readBoolean(JsonObject jsonObject) {
        if (getParamType(jsonObject).equals("Bool")) {
            return getValue(jsonObject).equals("True");
        }
        return false;
    }

    public static boolean readBoolean(JsonArray jsonArray, int index) {
        return readBoolean(jsonArray.get(index).getAsJsonObject());
    }

    /**
     * 读取结构体，带 {@code param_type} 嵌套结构。
     * @param jsonObject 源 JSON 对象
     * @param valueReader 值转换器
     * @return 结构体对象
     * @param <T> 结构体类型形参
     */
    public static <T> Struct<T> readStruct(JsonObject jsonObject, Function<JsonArray, T> valueReader) {
        if (getParamType(jsonObject).equals("Struct")) {
            JsonObject valueObj = jsonObject.getAsJsonObject("value");
            Struct<T> structId = readStructValue(valueObj, valueReader);
            if (structId != null) return structId;
        }
        throw new RuntimeException("Invalid Struct params");
    }

    /**
     * 从 {@link JsonArray} 对应索引的对象中读取结构体，带 {@code param_type} 嵌套结构。
     * @param jsonArray 源 JSON 数组
     * @param index 索引
     * @param valueReader 值转换器
     * @return 结构体对象
     * @param <T> 结构体类型形参
     */
    public static <T> Struct<T> readStruct(JsonArray jsonArray, int index, Function<JsonArray, T> valueReader) {
        return readStruct(jsonArray.get(index).getAsJsonObject(), valueReader);
    }

    /**
     * 读取结构体，不带 {@code param_type} 嵌套结构。
     * @param valueObj 值 JSON 对象
     * @param valueReader 值转换器
     * @return 结构体对象
     * @param <T> 结构体类型形参
     */
    public static <T> Struct<T> readStructValue(JsonObject valueObj, Function<JsonArray, T> valueReader) {
        if (valueObj.get("type").getAsString().equals("Struct")) {
            String structId = valueObj.get("structId").getAsString();
            T value = valueReader.apply(valueObj.getAsJsonArray("value"));
            return new Struct<>(structId, value);
        }
        return null;
    }

    /**
     * 读取结构体列表，带 {@code param_type} 嵌套结构。
     * @param jsonObject 源 JSON 对象
     * @param valueReader 值转换器
     * @return 结构体列表对象
     * @param <T> 结构体类型形参
     */
    public static <T> StructList<T> readStructList(JsonObject jsonObject, Function<JsonArray, T> valueReader) {
        if (getParamType(jsonObject).equals("StructList")) {
            JsonObject valueObj = jsonObject.getAsJsonObject("value");
            return readStructListValue(valueObj, valueReader);
        }
        throw new RuntimeException("Invalid StructList params");
    }

    /**
     * 从 {@link JsonArray} 对应索引的对象中读取结构体列表，带 {@code param_type} 嵌套结构。
     * @param jsonArray 源 JSON 数组
     * @param index 索引
     * @param valueReader 值转换器
     * @return 结构体列表对象
     * @param <T> 结构体类型形参
     */
    public static <T> StructList<T> readStructList(JsonArray jsonArray, int index, Function<JsonArray, T> valueReader) {
        return readStructList(jsonArray.get(index).getAsJsonObject(), valueReader);
    }

    /**
     * 读取结构体列表，不带 {@code param_type} 嵌套结构。
     * @param valueObj 值 JSON 对象
     * @param valueReader 值转换器
     * @return 结构体列表对象
     * @param <T> 结构体类型形参
     */
    public static <T> StructList<T> readStructListValue(JsonObject valueObj, Function<JsonArray, T> valueReader) {
        String structId = valueObj.get("structId").getAsString();
        StructList<T> structList = new StructList<>(structId);
        JsonArray valueArray = valueObj.getAsJsonArray("value");
        valueArray.forEach(value -> structList.add(readStruct(value.getAsJsonObject(), valueReader)));
        return structList;
    }

    public static JsonObject writeInt(int value) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("param_type", "Int32");
        jsonObject.addProperty("value", String.valueOf(value));
        return jsonObject;
    }

    public static JsonObject writeString(String value) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("param_type", "String");
        jsonObject.addProperty("value", value);
        return jsonObject;
    }

    public static JsonObject writeStringList(List<String> value) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        jsonObject.addProperty("param_type", "StringList");
        value.forEach(jsonArray::add);
        jsonObject.add("value", jsonArray);
        return jsonObject;
    }

    public static JsonObject writeBoolean(boolean value) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("param_type", "Bool");
        jsonObject.addProperty("value", value ? "True" : "False");
        return jsonObject;
    }

    /**
     * 写入结构体，带 {@code param_type} 嵌套结构
     * @param struct 源结构体对象
     * @param valueWriter 值转换器
     * @return 结构体对应的 JSON 对象，带 {@code param_type} 嵌套结构
     * @param <T> 结构体类型形参
     */
    public static <T> JsonObject writeStruct(Struct<T> struct, Function<T, JsonArray> valueWriter) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("param_type", "Struct");
        JsonObject valueObject = writeStructValue(struct, valueWriter);
        jsonObject.add("value", valueObject);
        return jsonObject;
    }

    /**
     * 写入结构体，不带 {@code param_type} 嵌套结构
     * @param struct 源结构体对象
     * @param valueWriter 值转换器
     * @return 结构体对应的 JSON 对象，不带 {@code param_type} 嵌套结构
     * @param <T> 结构体类型形参
     */
    public static <T> JsonObject writeStructValue(Struct<T> struct, Function<T, JsonArray> valueWriter) {
        JsonObject valueObject = new JsonObject();
        valueObject.addProperty("structId", struct.getStructId());
        valueObject.addProperty("type", "Struct");
        valueObject.add("value", valueWriter.apply(struct.getValue()));
        return valueObject;
    }

    /**
     * 写入结构体列表，带 {@code param_type} 嵌套结构
     * @param structList 源结构体列表对象
     * @param valueWriter 值转换器
     * @return 结构体列表对应的 JSON 对象，带 {@code param_type} 嵌套结构
     * @param <T> 结构体类型形参
     */
    public static <T> JsonObject writeStructList(StructList<T> structList, Function<T, JsonArray> valueWriter) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("param_type", "StructList");
        JsonObject valueObject = writeStructListValue(structList, valueWriter);
        jsonObject.add("value", valueObject);
        return jsonObject;
    }

    /**
     * 写入结构体列表，不带 {@code param_type} 嵌套结构
     * @param structList 源结构体列表对象
     * @param valueWriter 值转换器
     * @return 结构体列表对应的 JSON 对象，不带 {@code param_type} 嵌套结构
     * @param <T> 结构体类型形参
     */
    public static <T> JsonObject writeStructListValue(StructList<T> structList, Function<T, JsonArray> valueWriter) {
        JsonObject valueObject = new JsonObject();
        valueObject.addProperty("structId", structList.getStructId());
        JsonArray valueArray = new JsonArray();
        structList.forEach(struct -> valueArray.add(writeStruct(struct, valueWriter)));
        valueObject.add("value", valueArray);
        return valueObject;
    }

    public static String getParamType(JsonObject jsonObject) {
        return jsonObject.get("param_type").getAsString();
    }

    public static String getValue(JsonObject jsonObject) {
        return jsonObject.get("value").getAsString();
    }
}
