package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonObject;
import top.pigest.queuemanagerdemo.liveroom.data.Gift;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.liveroom.data.event.GiftSend;

public class GiftSendEvent extends LiveMessageEvent<GiftSend> {
    public static final GiftSendEvent INSTANCE = new GiftSendEvent();

    private GiftSendEvent() {
        super("SEND_GIFT");
    }

    @Override
    protected GiftSend convert(JsonObject jsonObject) {
        JsonObject data = jsonObject.getAsJsonObject("data");
        User user = new User(data.get("uname").getAsString(), data.get("uid").getAsLong())
                .setFace(data.get("face").getAsString());
        Gift gift = new Gift(data.get("giftName").getAsString(), data.get("giftId").getAsInt())
                .setCount(data.get("num").getAsInt())
                .setPrice(data.get("price").getAsInt());
        return new GiftSend(user, gift, data.get("total_coin").getAsInt());
    }
}
