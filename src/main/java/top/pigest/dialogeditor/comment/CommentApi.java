package top.pigest.dialogeditor.comment;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import top.pigest.dialogeditor.util.RequestUtils;

public class CommentApi {
    public static PlayerCommentResponse getPlayerComment(String guid, String next, String sortType) {
        JsonObject cursor = new JsonObject();
        cursor.addProperty("next", next);
        cursor.addProperty("size", 15);
        cursor.addProperty("sort_type", sortType);
        JsonObject jsonObject = RequestUtils.requestToJson(
                RequestUtils.httpPost("https://bbs-api.miyoushe.com/community/ugc_community/web/api/reply/list?lang=zh-cn")
                        .appendJsonParameter("cursor", cursor)
                        .appendJsonParameter("level_id", new JsonPrimitive(guid))
                        .appendJsonParameter("region", new JsonPrimitive("cn_gf01"))
                        .appendJsonParameter("uid", new JsonPrimitive("")).build());
        if ((jsonObject.has("code") && jsonObject.get("code").getAsInt() == -114514)
        || jsonObject.get("retcode").getAsInt() != 0) {
            throw new RuntimeException(jsonObject.get("message").getAsString());
        }
        JsonObject data = jsonObject.getAsJsonObject("data");
        return new PlayerCommentResponse(
                data.getAsJsonObject("cursor").get("has_more").getAsBoolean(),
                data.getAsJsonObject("cursor").get("next").getAsString(),
                data.getAsJsonObject("cursor").get("sort_type").getAsString(),
                PlayerComment.deserialize(data.getAsJsonArray("reply_list"))
        );
    }
}
