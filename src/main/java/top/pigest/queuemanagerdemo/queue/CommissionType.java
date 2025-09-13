package top.pigest.queuemanagerdemo.queue;

import com.google.gson.annotations.SerializedName;
import javafx.scene.paint.Color;
import top.pigest.queuemanagerdemo.liveroom.predicate.UserPredicate;

/**
 * 委托类型
 */
public class CommissionType {
    @SerializedName("id")
    private String id;
    @SerializedName("command")
    private String command;
    @SerializedName("display_name")
    private String display;
    @SerializedName("display_color")
    private Color color;
    @SerializedName("predicate")
    private UserPredicate predicate;
}
