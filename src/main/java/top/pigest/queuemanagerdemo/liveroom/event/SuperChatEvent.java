package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.scene.paint.Color;
import top.pigest.queuemanagerdemo.liveroom.data.FansMedal;
import top.pigest.queuemanagerdemo.liveroom.data.GuardType;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.liveroom.data.event.SuperChat;
import top.pigest.queuemanagerdemo.util.Utils;

public class SuperChatEvent extends LiveMessageEvent<SuperChat> {
    public static final SuperChatEvent INSTANCE = new SuperChatEvent();

    private SuperChatEvent() {
        super("SUPER_CHAT_MESSAGE");
    }

    @Override
    protected SuperChat convert(JsonObject jsonObject) {
        String content = jsonObject.getAsJsonObject("data").get("message").getAsString();
        int price =  jsonObject.getAsJsonObject("data").get("price").getAsInt();
        long uid = jsonObject.getAsJsonObject("data").get("uid").getAsLong();
        String name = jsonObject.getAsJsonObject("data").getAsJsonObject("user_info").get("name").getAsString();
        JsonElement fm1 = jsonObject.getAsJsonObject("data").get("medal_info");
        FansMedal fansMedal;
        if (fm1.isJsonNull()) {
            fansMedal = null;
        } else {
            JsonObject fm = fm1.getAsJsonObject();
            fansMedal = new FansMedal()
                    .setMedalName(fm.get("medal_name").getAsString())
                    .setLighted(fm.get("is_lighted").getAsInt() == 1)
                    .setGuardType(GuardType.valueOf(fm.get("guard_level").getAsInt()))
                    .setOldStyle(new FansMedal.FansMedalStyleOld(
                            Color.valueOf(fm.get("medal_color").getAsString()),
                            Utils.toColor(fm.get("medal_color_start").getAsInt()),
                            Utils.toColor(fm.get("medal_color_end").getAsInt()),
                            Utils.toColor(fm.get("medal_color_border").getAsInt())
                    ))
                    .setLevel(fm.get("medal_level").getAsInt());
        }
        User user = new User(name, uid)
                .setFace(jsonObject.getAsJsonObject("data").getAsJsonObject("user_info").get("face").getAsString())
                .setFansMedal(fansMedal);
        return new SuperChat(user, content, price);
    }
}
