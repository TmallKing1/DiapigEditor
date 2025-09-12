package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonObject;
import top.pigest.queuemanagerdemo.liveroom.data.GuardType;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.liveroom.data.event.GuardBuy;

public class GuardBuyEvent extends LiveMessageEvent<GuardBuy> {
    public static final GuardBuyEvent INSTANCE = new GuardBuyEvent();

    private GuardBuyEvent() {
        super("GUARD_BUY");
    }

    @Override
    protected GuardBuy convert(JsonObject jsonObject) {
        JsonObject data = jsonObject.getAsJsonObject("data");
        User user = new User(data.get("username").getAsString(), data.get("uid").getAsLong());
        GuardType guardType = GuardType.valueOf(data.get("guard_level").getAsString());
        int num = data.get("num").getAsInt();
        return new GuardBuy(user, guardType, num);
    }
}
