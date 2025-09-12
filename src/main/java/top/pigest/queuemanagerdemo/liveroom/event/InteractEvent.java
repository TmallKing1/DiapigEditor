package top.pigest.queuemanagerdemo.liveroom.event;

import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.scene.paint.Color;
import top.pigest.queuemanagerdemo.liveroom.InteractWordOuterClass;
import top.pigest.queuemanagerdemo.liveroom.data.FansMedal;
import top.pigest.queuemanagerdemo.liveroom.data.GuardType;
import top.pigest.queuemanagerdemo.liveroom.data.event.InteractWord;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.util.Utils;

import java.util.Base64;

public class InteractEvent extends LiveMessageEvent<InteractWord> {
    public static final InteractEvent INSTANCE = new InteractEvent();

    private InteractEvent() {
        super("INTERACT_WORD_V2");
    }

    @Override
    protected InteractWord convert(JsonObject jsonObject) {
        byte[] bytes = Base64.getDecoder().decode(jsonObject.getAsJsonObject("data").get("pb").getAsString());
        try {
            InteractWordOuterClass.InteractWord interactWord = InteractWordOuterClass.InteractWord.parseFrom(bytes);
            InteractWord.MessageType messageType = InteractWord.MessageType.values()[Math.toIntExact(interactWord.getMsgType())];
            InteractWordOuterClass.InteractWord.UserInfo userInfo = interactWord.getUinfo();
            long uid = userInfo.getUid();
            String name = userInfo.getBase().getUname();
            String face = userInfo.getBase().getFace();
            int honor = userInfo.getWealth().getLevel();
            FansMedal fansMedal = null;
            if (userInfo.hasMedalInfo()) {
                InteractWordOuterClass.InteractWord.UserInfo.MedalInfo medalInfo = userInfo.getMedalInfo();
                fansMedal = new FansMedal()
                        .setMedalName(medalInfo.getMedalName())
                        .setLevel(medalInfo.getMedalLevel())
                        .setGuardType(GuardType.valueOf(medalInfo.getGuardLevel()))
                        .setExpFromScore(medalInfo.getScore())
                        .setLighted(medalInfo.getIsLighted() == 1)
                        .setGuardIcon(medalInfo.getGuardIcon())
                        .setOldStyle(new FansMedal.FansMedalStyleOld(
                                Utils.toColor(medalInfo.getColor()),
                                Utils.toColor(medalInfo.getColorStart()),
                                Utils.toColor(medalInfo.getColorEnd()),
                                Utils.toColor(medalInfo.getColorBorder())
                        ))
                        .setStyle(new FansMedal.FansMedalStyle(
                                Color.valueOf(medalInfo.getV2MedalColorStart()),
                                Color.valueOf(medalInfo.getV2MedalColorEnd()),
                                Color.valueOf(medalInfo.getV2MedalColorBorder()),
                                Color.valueOf(medalInfo.getV2MedalColorText()),
                                Color.valueOf(medalInfo.getV2MedalColorLevel())
                        ));
            }
            User user = new User(name, uid).setFace(face).setHonor(honor).setFansMedal(fansMedal);
            return new InteractWord(user, messageType);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
