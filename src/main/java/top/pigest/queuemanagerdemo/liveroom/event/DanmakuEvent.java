package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import top.pigest.queuemanagerdemo.liveroom.data.event.Danmaku;
import top.pigest.queuemanagerdemo.liveroom.data.FansMedal;
import top.pigest.queuemanagerdemo.liveroom.data.User;

public class DanmakuEvent extends LiveMessageEvent<Danmaku> {
    public static final DanmakuEvent INSTANCE = new DanmakuEvent();

    private DanmakuEvent() {
        super("DANMU_MSG");
    }

    @Override
    protected Danmaku convert(JsonObject jsonObject) {
        JsonArray info = jsonObject.getAsJsonArray("info");
        String content = info.get(1).getAsString();
        JsonObject userObj = info.get(0).getAsJsonArray().get(15).getAsJsonObject().getAsJsonObject("user");
        long uid = userObj.get("uid").getAsLong();
        String name = userObj.getAsJsonObject("base").get("name").getAsString();
        String face = userObj.getAsJsonObject("base").get("face").getAsString();
        FansMedal medal = userObj.get("medal").isJsonNull() ? null : FansMedal.deserializeUInfoMedal(userObj.getAsJsonObject("medal"));
        int honor = info.get(16).getAsJsonArray().get(0).getAsInt();
        User user = new User(name, uid).setFace(face).setFansMedal(medal).setHonor(honor);
        return new Danmaku(user, content);
    }
}
