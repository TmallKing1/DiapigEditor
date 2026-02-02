package top.pigest.dialogeditor.comment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record PlayerComment(String uid, String uName, String clientIp, String comment, int timestamp, boolean isRecommend, List<PlayerComment> subReplies) {
    public static PlayerComment deserialize(JsonObject o) {
        JsonObject userInfo = o.getAsJsonObject("user_info");
        return new PlayerComment(
                userInfo.get("uid").getAsString(),
                userInfo.get("nickname").getAsString(),
                o.get("client_ip").getAsString(),
                o.get("content").getAsString(),
                o.get("created_at").getAsInt(),
                o.get("is_recommend").getAsBoolean(),
                deserialize(o.getAsJsonArray("sub_replies"))
        );
    }

    public static List<PlayerComment> deserialize(JsonArray o) {
        List<PlayerComment> playerComments = new ArrayList<>();
        for (JsonElement e : o) {
            JsonObject obj = e.getAsJsonObject();
            PlayerComment playerComment = deserialize(obj);
            playerComments.add(playerComment);
        }
        return playerComments;
    }
}
