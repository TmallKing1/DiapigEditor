package top.pigest.queuemanagerdemo.misc.ui;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.liveroom.data.FansMedal;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.control.ChildPage;
import top.pigest.queuemanagerdemo.control.NamedPage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MedalQueryPage extends VBox implements NamedPage, ChildPage {
    private Node display;
    private final JFXTextField user;
    private final JFXTextField up;
    private final QMButton execute;
    private Pane parentPage;

    public MedalQueryPage() {
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(30);
        Label label = Utils.createLabel("此处可查询任意用户在任意主播的粉丝牌信息\n填写用户与主播的名称/UID即可查询");
        label.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(label, new Insets(50, 0, 0, 0));
        user = new JFXTextField();
        user.setFont(Settings.DEFAULT_FONT);
        user.setPromptText("用户名称/UID");
        user.setLabelFloat(true);
        user.setMaxWidth(400);
        up = new JFXTextField();
        up.setFont(Settings.DEFAULT_FONT);
        up.setPromptText("主播名称/UID");
        up.setLabelFloat(true);
        up.setMaxWidth(400);

        execute = getExecute(new WhiteFontIcon("fas-search"));
        this.getChildren().addAll(label, user, up, execute);
    }

    private QMButton getExecute(WhiteFontIcon whiteFontIcon) {
        QMButton execute = new QMButton("查询", "#1a8bcc");
        execute.setGraphic(whiteFontIcon);
        execute.setDefaultButton(true);
        execute.setPrefWidth(150);
        execute.setOnAction(event -> {
            startExecute();
            this.getChildren().remove(display);
            CompletableFuture.runAsync(() -> {
                String userText = user.getText();
                String upText = up.getText();
                User user;
                User up;
                try {
                    long userId, upId;
                    try {
                        userId = Long.parseLong(userText);
                    } catch (NumberFormatException e) {
                        List<User> u = LiveRoomApi.userNameToUid(List.of(userText));
                        if (u.isEmpty()) {
                            Platform.runLater(() -> Utils.showDialogMessage("未搜索到用户（用户栏）", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                            return;
                        } else {
                            userId = u.getFirst().getUid();
                        }
                    }
                    try {
                        upId = Long.parseLong(upText);
                    } catch (NumberFormatException e) {
                        List<User> u = LiveRoomApi.userNameToUid(List.of(upText));
                        if (u.isEmpty()) {
                            Platform.runLater(() -> Utils.showDialogMessage("未搜索到用户（主播栏）", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                            return;
                        } else {
                            upId = u.getFirst().getUid();
                        }
                    }
                    List<User> u1 = LiveRoomApi.getUserBriefInfo(List.of(userId));
                    if (u1.isEmpty()) {
                        Platform.runLater(() -> Utils.showDialogMessage("未找到 UID 对应的用户（用户栏）", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                        return;
                    } else {
                        user = u1.getFirst();
                    }
                    List<User> u2 = LiveRoomApi.getUserBriefInfo(List.of(upId));
                    if (u2.isEmpty()) {
                        Platform.runLater(() -> Utils.showDialogMessage("未找到 UID 对应的用户（主播栏）", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                        return;
                    } else {
                        up = u2.getFirst();
                    }
                    FansMedal fansMedal = LiveRoomApi.getFansUInfoMedal(user.getUid(), up.getUid());
                    FansMedal fansMedal1 = LiveRoomApi.getFansMedalInfo(user.getUid(), up.getUid());
                    if (fansMedal == null) {
                        Platform.runLater(() -> Utils.showDialogMessage("用户没有主播的粉丝勋章", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                        return;
                    }
                    if (fansMedal1 != null) {
                        fansMedal.setExp(fansMedal1.getExp());
                        fansMedal.setNextExp(fansMedal1.getNextExp());
                    }
                    user.setFansMedal(fansMedal);
                    User finalUser = user;
                    Platform.runLater(() -> {
                        display = getNode(finalUser);
                        this.getChildren().add(display);
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> Utils.showDialogMessage("请求错误", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                }
            }).thenAccept(node -> Platform.runLater(this::endExecute));
        });
        return execute;
    }

    private void startExecute() {
        this.execute.disable(true);
        this.execute.setGraphic(new WhiteFontIcon("fas-bullseye"));
        this.execute.setText("查询中");
    }

    private void endExecute() {
        this.execute.disable(false);
        this.execute.setGraphic(new WhiteFontIcon("fas-search"));
        this.execute.setText("查询");
    }

    public Node getNode(User item) {
        BorderPane borderPane = User.userNode(item);

        borderPane.setPadding(new Insets(30, 30, 30, 30));
        return borderPane;
    }

    @Override
    public String getName() {
        return "粉丝牌";
    }

    @Override
    public Pane getParentPage() {
        return parentPage;
    }

    @Override
    public void setParentPage(Pane parentPage) {
        this.parentPage = parentPage;
    }
}
