package top.pigest.queuemanagerdemo.liveroom.predicate;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.util.IntegerRange;

public class HasMedalPredicate implements UserPredicate {
    public IntegerRange range;
    public HasMedalPredicate(IntegerRange range) {
        this.range = range;
    }
    @Override
    public boolean test(User user) {
        return user.getFansMedal() != null && range.test(user.getFansMedal().getLevel());
    }

    public static UserPredicate fromJson(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            IntegerRange range = IntegerRange.fromJson(jsonElement.getAsJsonObject().get("range"));
            return new HasMedalPredicate(range);
        }
        return new HasMedalPredicate(IntegerRange.any());
    }

    @Override
    public JsonElement toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("range", range.toJson());
        return jsonObject;
    }
}
