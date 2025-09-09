package top.pigest.queuemanagerdemo.liveroom;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.liveroom.data.*;
import top.pigest.queuemanagerdemo.util.RequestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 直播间相关 API 实用工具类
 */
public class LiveRoomApi {
    /**
     * 获取自己的用户名和 UID
     *
     * @return {@link User} 对象，仅包含用户名和 UID 信息
     */
    public static User uid() throws IOException {
        JsonObject element = RequestUtils.requestToJson(RequestUtils.httpGet("https://api.bilibili.com/x/space/myinfo").build());
        if (element.get("code").getAsInt() == 0) {
            long uid = element.getAsJsonObject("data").get("mid").getAsLong();
            String name = element.getAsJsonObject("data").get("name").getAsString();
            return new User(name, uid);
        }
        return null;
    }

    /**
     * 获取用户直播间真实 ID
     *
     * @param uid 用户 UID
     * @return 直播间真实ID
     */
    public static long liveRoomId(long uid) {
        return getLiveRoomInfo(uid).getKey();
    }

    /**
     * 获取用户直播间信息
     * @param uid 用户 UID
     * @return 两个值，分别为直播间真实 ID 直播间信息 {@link JsonObject}
     */
    public static Pair<Long, JsonObject> getLiveRoomInfo(long uid) {
        JsonObject object = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.live.bilibili.com/live_user/v1/Master/info")
                        .appendUrlParameter("uid", uid)
                        .build()
        );

        if (object.get("code").getAsInt() == 0) {
            long roomId = object.getAsJsonObject("data").get("room_id").getAsLong();
            if (roomId != 0) {
                JsonObject obj1 = RequestUtils.requestToJson(
                        RequestUtils.httpGet("https://api.live.bilibili.com/xlive/web-room/v1/index/getRoomBaseInfo")
                                .appendUrlParameter("req_biz", "web_room_componet")
                                .appendUrlParameter("room_ids", roomId)
                                .build()
                );

                if (obj1.get("code").getAsInt() == 0) {
                    Map.Entry<String, JsonElement> next1 = obj1.getAsJsonObject("data").getAsJsonObject("by_room_ids").entrySet().iterator().next();
                    roomId = Long.parseLong(next1.getKey());
                    return new Pair<>(roomId, next1.getValue().getAsJsonObject());
                } else {
                    throw new RuntimeException(obj1.get("message").getAsString());
                }
            } else {
                throw new RuntimeException("请先在 B 站开通直播间");
            }
        } else {
            throw new RuntimeException(object.get("message").getAsString());
        }
    }

    /**
     * 获取直播间分区列表
     *
     * @return {@link LiveArea} 对象列表，所有直播大分区以及其子分区的信息
     */
    public static List<LiveArea> getLiveAreas() {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://api.live.bilibili.com/xlive/app-blink/v1/preLive/GetAreaListForLive")
                .appendUrlParameter("show_pinyin", 0)
                .appendUrlParameter("platform", "pc")
                .build());
        if (object.get("code").getAsInt() == 0) {
            JsonArray array = object.getAsJsonObject("data").getAsJsonArray("area_v1_info");
            List<LiveArea> liveAreas = new ArrayList<>();
            for (JsonElement jsonElement : array) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                int id = jsonObject.get("id").getAsInt();
                String name = jsonObject.get("name").getAsString();
                List<SubLiveArea> subLiveAreas = new ArrayList<>();
                JsonArray sub = jsonObject.getAsJsonArray("list");
                for (JsonElement element : sub) {
                    JsonObject subObject = element.getAsJsonObject();
                    subLiveAreas.add(new SubLiveArea(id, subObject.get("id").getAsString(), subObject.get("name").getAsString()));
                }
                liveAreas.add(new LiveArea(id, name, subLiveAreas));
            }
            return liveAreas;
        } else {
            return List.of();
        }
    }

    /**
     * 获取当前登陆账号最近选择的直播分区
     *
     * @return {@link SubLiveArea} 对象列表，自己最近选择的三个直播子分区
     */
    public static List<SubLiveArea> getSelectedAreas() {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpGet("https://api.live.bilibili.com/room/v1/Area/getMyChooseArea")
                .appendUrlParameter("roomid", QueueManager.INSTANCE.ROOM_ID).build());
        if (object.get("code").getAsInt() == 0) {
            JsonArray array = object.getAsJsonArray("data");
            List<SubLiveArea> subLiveAreas = new ArrayList<>();
            for (JsonElement jsonElement : array) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                subLiveAreas.add(new SubLiveArea(Integer.parseInt(jsonObject.get("parent_id").getAsString()), jsonObject.get("id").getAsString(), jsonObject.get("name").getAsString()));
            }
            return subLiveAreas;
        } else {
            return List.of();
        }
    }

    /**
     * 更新直播分区
     * @param subLiveArea 子分区
     * @return 更新是否成功
     */
    public static boolean updateArea(SubLiveArea subLiveArea) {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://api.live.bilibili.com/room/v1/Room/update")
                .appendFormDataParameter("csrf", RequestUtils.getCookie("bili_jct"))
                .appendFormDataParameter("room_id", QueueManager.INSTANCE.ROOM_ID)
                .appendFormDataParameter("area_id", subLiveArea.id())
                .build());
        return object.get("code").getAsInt() == 0;
    }

    /**
     * 更新直播间标题
     * @param title 标题内容
     * @return 若成功，返回审核信息，否则返回错误信息
     */
    public static String updateTitle(String title) {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://api.live.bilibili.com/xlive/app-blink/v1/preLive/UpdatePreLiveInfo")
                .appendFormDataParameter("csrf", RequestUtils.getCookie("bili_jct"))
                .appendFormDataParameter("csrf_token", RequestUtils.getCookie("bili_jct"))
                .appendFormDataParameter("build", 1)
                .appendFormDataParameter("platform", "pc")
                .appendFormDataParameter("mobi_app", "pc")
                .appendFormDataParameter("room_id", QueueManager.INSTANCE.ROOM_ID)
                .appendFormDataParameter("title", title)
                .build());
        if (object.get("code").getAsInt() == 0) {
            return object.getAsJsonObject("data").getAsJsonObject("audit_info").get("audit_title").getAsString();
        }
        return object.get("message").getAsString();
    }

    /**
     * 开始直播
     * @param subLiveArea 直播子分区
     * @return 操作是否成功
     */
    public static boolean startLive(SubLiveArea subLiveArea) {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://api.live.bilibili.com/xlive/app-blink/v1/streaming/WebLiveCenterStartLive")
                .appendUrlParameter("room_id", QueueManager.INSTANCE.ROOM_ID)
                .appendUrlParameter("platform", "pc")
                .appendUrlParameter("area_v2", subLiveArea.id())
                .appendUrlParameter("backup_stream", 0)
                .appendUrlParameter("csrf_token", RequestUtils.getCookie("bili_jct"))
                .appendUrlParameter("csrf", RequestUtils.getCookie("bili_jct"))
                .buildWithWbiSign());
        if (object.get("code").getAsInt() == 0) {
            return object.getAsJsonObject("data").get("change").getAsInt() == 1 && object.getAsJsonObject("data").get("status").getAsString().equals("LIVE");
        }
        return false;
    }

    /**
     * 获取直播间推流链接
     * @return 若成功，长度为 2 的字符串列表，第一个元素为链接，第二个元素为推流码；失败返回空列表
     */
    public static List<String> fetchStreamAddress() {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://api.live.bilibili.com/xlive/app-blink/v1/live/FetchWebUpStreamAddr")
                .appendFormDataParameter("platform", "pc")
                .appendFormDataParameter("backup_stream", 0)
                .appendFormDataParameter("csrf_token", RequestUtils.getCookie("bili_jct"))
                .appendFormDataParameter("csrf", RequestUtils.getCookie("bili_jct"))
                .build());
        if (object.get("code").getAsInt() == 0) {
            JsonObject o1 = object.getAsJsonObject("data").getAsJsonObject("addr");
            return List.of(o1.get("addr").getAsString(), o1.get("code").getAsString());
        }
        return List.of();
    }

    /**
     * 关闭直播
     * @return 操作是否成功
     */
    public static boolean stopLive() {
        JsonObject object = RequestUtils.requestToJson(RequestUtils.httpPost("https://api.live.bilibili.com/room/v1/Room/stopLive")
                .appendFormDataParameter("platform", "pc")
                .appendFormDataParameter("room_id", QueueManager.INSTANCE.ROOM_ID)
                .appendFormDataParameter("csrf_token", RequestUtils.getCookie("bili_jct"))
                .appendFormDataParameter("csrf", RequestUtils.getCookie("bili_jct"))
                .build());
        if (object.get("code").getAsInt() == 0) {
            return object.getAsJsonObject("data").get("change").getAsInt() == 1 && !object.getAsJsonObject("data").get("status").getAsString().equals("LIVE");
        }
        return false;
    }

    /**
     * 获取当前登录账号的所有房管信息
     *
     * @return 一个 {@link Map} 对象，键为房管 UID，值为房管用户名
     */
    public static Map<Long, String> getRoomAdmins() {
        Map<Long, String> map = new HashMap<>();
        for (int page = 1; ; page++) {
            JsonObject obj = RequestUtils.requestToJson(
                    RequestUtils.httpGet("https://api.live.bilibili.com/xlive/app-ucenter/v1/roomAdmin/get_by_anchor")
                            .appendUrlParameter("page", page)
                            .build()
            );

            if (obj.get("code").getAsInt() == 0) {
                JsonElement element = obj.getAsJsonObject("data").get("data");
                if (element.isJsonNull()) {
                    break;
                } else {
                    element.getAsJsonArray().forEach(elem -> {
                        JsonObject obj2 = elem.getAsJsonObject();
                        long uid = obj2.get("uid").getAsLong();
                        String uname = obj2.get("uname").getAsString();
                        map.put(uid, uname);
                    });
                    if (page == obj.getAsJsonObject("data").getAsJsonObject("page").get("total_page").getAsInt()) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        return map;
    }

    /**
     * 查询用户在当前登录主播账号的粉丝牌信息（{@code medal_info} 模板）
     *
     * @param uid 需要查询的用户 UID
     * @return 一个 {@link FansMedal} 对象，表示粉丝牌信息，若用户没有已登录主播账号的粉丝牌则为 {@code null}
     */
    public static FansMedal getFansMedalInfo(long uid) {
        return getFansMedalInfo(uid, QueueManager.getSelfUid());
    }

    /**
     * 查询用户在特定 UID 主播的粉丝牌信息（{@code medal_info} 模板）
     *
     * @param user   需要查询的用户 UID
     * @param anchor 主播 UID
     * @return 一个 {@link FansMedal} 对象，表示粉丝牌信息，若用户没有该主播的粉丝牌则为 {@code null}
     */
    public static FansMedal getFansMedalInfo(long user, long anchor) {
        JsonObject obj = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.live.bilibili.com/xlive/app-ucenter/v1/fansMedal/user_medal_info")
                        .appendUrlParameter("uid", user)
                        .appendUrlParameter("up_uid", anchor)
                        .build()
        );

        if (obj.get("code").getAsInt() == 0) {
            JsonObject element = obj.getAsJsonObject("data");
            if (!element.get("lookup_medal").isJsonNull()) {
                JsonObject lookupMedal = element.getAsJsonObject("lookup_medal");
                return FansMedal.deserializeMedalInfo(lookupMedal);
            }
        }
        return null;
    }

    /**
     * 查询用户在当前登录主播账号的粉丝牌信息（{@code uinfo_medal} 模板）
     *
     * @param uid 需要查询的用户 UID
     * @return 一个 {@link FansMedal} 对象，表示粉丝牌信息，若用户没有已登录主播账号的粉丝牌则为 {@code null}
     */
    public static FansMedal getFansUInfoMedal(long uid) {
        return getFansUInfoMedal(uid, QueueManager.getSelfUid());
    }

    /**
     * 查询用户在特定 UID 主播的粉丝牌信息（{@code uinfo_medal} 模板）
     *
     * @param user   需要查询的用户 UID
     * @param anchor 主播 UID
     * @return 一个 {@link FansMedal} 对象，表示粉丝牌信息，若用户没有该主播的粉丝牌则为 {@code null}
     */
    public static FansMedal getFansUInfoMedal(long user, long anchor) {
        JsonObject obj = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.live.bilibili.com/xlive/app-ucenter/v1/fansMedal/user_medal_info")
                        .appendUrlParameter("uid", user)
                        .appendUrlParameter("up_uid", anchor)
                        .build()
        );

        if (obj.get("code").getAsInt() == 0) {
            JsonObject element = obj.getAsJsonObject("data");
            if (!element.get("lookup_v2").isJsonNull()) {
                JsonObject lookupMedal = element.getAsJsonObject("lookup_v2");
                return FansMedal.deserializeUInfoMedal(lookupMedal);
            }
        }
        return null;
    }

    /**
     * 查询当前登录主播账号的舰长列表，返回为 {@link User} 对象列表，包含舰长的粉丝牌信息
     *
     * @param page 页码，每页 20 个。特殊地，为 1 时返回前 23 个舰长（若存在），为 2 时返回第 24 - 43 个舰长（若存在）……
     * @return {@link User} 列表，表示获取到的舰长用户信息
     */
    public static List<User> getGuards(int page) {
        List<User> users = new ArrayList<>();
        JsonObject obj = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.live.bilibili.com/xlive/app-room/v2/guardTab/topListNew")
                        .appendUrlParameter("roomid", QueueManager.INSTANCE.ROOM_ID)
                        .appendUrlParameter("page", page)
                        .appendUrlParameter("page_size", 20)
                        .appendUrlParameter("ruid", QueueManager.getSelfUid())
                        .appendUrlParameter("typ", 5)
                        .build()
        );

        if (obj.get("code").getAsInt() == 0) {
            JsonObject element = obj.getAsJsonObject("data");
            if (page == 1) {
                element.getAsJsonArray("top3").forEach(elem -> {
                    JsonObject obj2 = elem.getAsJsonObject().getAsJsonObject("uinfo");
                    long uid = obj2.get("uid").getAsLong();
                    String uname = obj2.getAsJsonObject("base").get("name").getAsString();
                    users.add(new User(uname, uid)
                            .setFansMedal(obj2.getAsJsonObject("medal"))
                            .setFace(obj2.getAsJsonObject("base").get("face").toString()));
                });
            }
            element.getAsJsonArray("list").forEach(elem -> {
                JsonObject obj2 = elem.getAsJsonObject().getAsJsonObject("uinfo");
                long uid = obj2.get("uid").getAsLong();
                String uname = obj2.getAsJsonObject("base").get("name").getAsString();
                users.add(new User(uname, uid)
                        .setFansMedal(obj2.getAsJsonObject("medal"))
                        .setFace(obj2.getAsJsonObject("base").get("face").toString()));
            });
        }
        return users;
    }

    /**
     * 查询当前登录主播账号的粉丝团列表，返回为 {@link User} 对象列表，包含粉丝牌信息
     *
     * @param page     页码，每页 20 个。
     * @param rankType 获取方式，为 2 时获取所有未上舰的粉丝团成员，为其他数字时获取粉丝牌当前点亮的粉丝团成员
     * @return {@link User} 列表，表示获取到的舰长用户信息
     */
    public static List<User> getFans(int page, int rankType) {
        List<User> users = new ArrayList<>();
        JsonObject obj = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.live.bilibili.com/xlive/general-interface/v1/rank/getFansMembersRank")
                        .appendUrlParameter("page", page)
                        .appendUrlParameter("page_size", 20)
                        .appendUrlParameter("ruid", QueueManager.getSelfUid())
                        .appendUrlParameter("rank_type", rankType)
                        .appendUrlParameter("ts", System.currentTimeMillis())
                        .build()
        );

        if (obj.get("code").getAsInt() == 0) {
            JsonObject element = obj.getAsJsonObject("data");
            element.getAsJsonArray("item").forEach(elem -> {
                JsonObject obj2 = elem.getAsJsonObject();
                long uid = obj2.get("uid").getAsLong();
                String uname = obj2.get("name").getAsString();
                users.add(new User(uname, uid)
                        .setFansMedal(obj2.getAsJsonObject("uinfo_medal"))
                        .setFace(obj2.get("face").getAsString()));
            });
        }
        return users;
    }

    /**
     * 查询当前登录主播账号的舰长列表，包含舰长的到期时间信息
     *
     * @param page 查询的页码，若为 {@code 0} 则返回所有舰长
     * @return {@link GuardInfo} 列表，表示获取到的舰长信息
     */
    public static List<GuardInfo> getGuardsWithExpireDate(int page) {
        List<GuardInfo> guards = new ArrayList<>();
        if (page == 0) {
            for (int page1 = 1; ; page1++) {
                JsonObject obj = RequestUtils.requestToJson(
                        RequestUtils.httpGet("https://api.live.bilibili.com/xlive/web-ucenter/user/sailors")
                                .appendUrlParameter("page", page1)
                                .appendUrlParameter("page_size", 20)
                                .build()
                );

                if (obj.get("code").getAsInt() == 0) {
                    JsonObject element = obj.getAsJsonObject("data");
                    JsonArray array = element.getAsJsonArray("list");
                    array.forEach(elem -> {
                        JsonObject object = elem.getAsJsonObject();
                        long uid = object.get("uid").getAsLong();
                        int guardLevel = object.get("guard_level").getAsInt();
                        String uname = object.get("username").getAsString();
                        String expiredTime = object.get("expired_time").getAsString();
                        LocalDate localDate = LocalDate.parse(expiredTime, DateTimeFormatter.ISO_LOCAL_DATE);
                        guards.add(new GuardInfo(uid, GuardType.valueOf(guardLevel), uname, localDate));
                    });
                    int totalPages = element.getAsJsonObject("pageInfo").get("totalPages").getAsInt();
                    if (page1 >= totalPages) {
                        return guards;
                    }
                }
            }
        } else {
            JsonObject obj = RequestUtils.requestToJson(
                    RequestUtils.httpGet("https://api.live.bilibili.com/xlive/web-ucenter/user/sailors")
                            .appendUrlParameter("page", page)
                            .build()
            );

            if (obj.get("code").getAsInt() == 0) {
                JsonObject element = obj.getAsJsonObject("data");
                JsonArray array = element.getAsJsonArray("list");
                array.forEach(elem -> {
                    JsonObject object = elem.getAsJsonObject();
                    long uid = object.get("uid").getAsLong();
                    int guardLevel = object.get("guard_level").getAsInt();
                    String uname = object.get("username").getAsString();
                    String expiredTime = object.get("expired_time").getAsString();
                    LocalDate localDate = LocalDate.parse(expiredTime, DateTimeFormatter.ISO_LOCAL_DATE);
                    guards.add(new GuardInfo(uid, GuardType.valueOf(guardLevel), uname, localDate));
                });
                return guards;
            }
        }
        return List.of();
    }

    /**
     * 查询当前登录主播账号直播间的所有礼物信息
     * @return {@link Gift} 列表
     */
    public static List<Gift> getGiftList() {
        List<Gift> gifts = new ArrayList<>();
        JsonObject jsonObject = RequestUtils.requestToJson(RequestUtils.httpGet("https://api.live.bilibili.com/xlive/web-room/v1/giftPanel/roomGiftList")
                .appendUrlParameter("platform", "pc")
                .appendUrlParameter("room_id", QueueManager.INSTANCE.ROOM_ID)
                .build());
        if (jsonObject.get("code").getAsInt() == 0) {
            JsonArray element = jsonObject.getAsJsonObject("data").getAsJsonObject("gift_config").getAsJsonObject("base_config").getAsJsonArray("list");
            element.forEach(elem -> {
                JsonObject obj = elem.getAsJsonObject();
                int id = obj.get("id").getAsInt();
                int price = obj.get("price").getAsInt();
                String name = obj.get("name").getAsString();
                int type = obj.get("gift_type").getAsInt();
                gifts.add(new Gift(name, id).setPrice(price).setType(type));
            });
        }
        return gifts;
    }

    /**
     * 查询盲盒概率
     * @return 一个以礼物内容为键、概率为值、按照价格降序排列的 {@link LinkedHashMap}
     */
    public static LinkedHashMap<Gift, String> getBlindInfo(Gift blind) {
        LinkedHashMap<Gift, String> blinds = new LinkedHashMap<>();
        JsonObject jsonObject = RequestUtils.requestToJson(RequestUtils.httpGet("https://api.live.bilibili.com/xlive/general-interface/v1/blindFirstWin/getInfo")
                .appendUrlParameter("gift_id", blind.getId())
                .build());
        if (jsonObject.get("code").getAsInt() == 0) {
            JsonArray element = jsonObject.getAsJsonObject("data").getAsJsonArray("gifts");
            element.forEach(elem -> {
                JsonObject obj = elem.getAsJsonObject();
                int id = obj.get("gift_id").getAsInt();
                String name = obj.get("gift_name").getAsString();
                int price = obj.get("price").getAsInt();
                String img = obj.get("gift_img").getAsString();
                String win = obj.get("is_win_gift").getAsString();
                String chance = obj.get("chance").getAsString();
                blinds.put(new Gift(name, id).setPrice(price).setImgBasic(img).setProperty("win", win), chance);
            });
        }
        return blinds;
    }

    /**
     * 根据多个用户 UID 获取用户名和头像链接
     *
     * @param uids UID 集合
     * @return {@link User} 对象列表，每个对象仅包含用户名、UID和头像链接
     */
    public static List<User> getUserBriefInfo(Collection<Long> uids) {
        List<User> users = new ArrayList<>();
        JsonObject obj = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.vc.bilibili.com/x/im/user_infos")
                        .appendUrlParameter("uids", String.join(",", uids.stream().map(String::valueOf).toList()))
                        .build()
        );

        if (obj.get("code").getAsInt() == 0) {
            JsonArray element = obj.getAsJsonArray("data");
            element.forEach(elem -> {
                JsonObject object = elem.getAsJsonObject();
                long uid = object.get("mid").getAsLong();
                String uname = object.get("name").getAsString();
                String face = object.get("face").getAsString();
                users.add(new User(uname, uid).setFace(face));
            });
        }
        return users;
    }

    /**
     * 搜索多个用户名对应的 UID
     *
     * @param usernames 用户名集合
     * @return {@link User} 对象列表，每个对象仅包含用户名和 UID
     */
    public static List<User> userNameToUid(Collection<String> usernames) {
        List<User> users = new ArrayList<>();
        JsonObject obj = RequestUtils.requestToJson(
                RequestUtils.httpGet("https://api.bilibili.com/x/polymer/web-dynamic/v1/name-to-uid")
                        .appendUrlParameter("names", String.join(",", usernames))
                        .build()
        );

        if (obj.get("code").getAsInt() == 0) {
            JsonArray element = obj.getAsJsonObject("data").getAsJsonArray("uid_list");
            element.forEach(elem -> {
                JsonObject object = elem.getAsJsonObject();
                long uid = object.get("uid").getAsLong();
                String uname = object.get("name").getAsString();
                users.add(new User(uname, uid));
            });
        }
        return users;
    }
}
