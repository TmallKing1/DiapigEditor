package top.pigest.queuemanagerdemo.window.misc;

import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.liveroom.FansMedal;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.User;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.window.main.ChildPage;
import top.pigest.queuemanagerdemo.window.main.NamedPage;

import java.text.DecimalFormat;
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
                    if (fansMedal == null) {
                        Platform.runLater(() -> Utils.showDialogMessage("用户没有主播的粉丝勋章", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                        return;
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
        this.execute.setText("检索中");
    }

    private void endExecute() {
        this.execute.disable(false);
        this.execute.setGraphic(new WhiteFontIcon("fas-search"));
        this.execute.setText("检索");
    }

    public Node getNode(User item) {
        BorderPane borderPane = new BorderPane();

        ImageView face = new ImageView();
        Circle clip = new Circle();
        clip.setCenterX(30);
        clip.setCenterY(30);
        clip.setRadius(30);
        face.setClip(clip);
        CompletableFuture.supplyAsync(() -> new Image(item.getFace())).whenComplete((image, throwable) -> {
            if (throwable != null) {
                Platform.runLater(() -> face.setImage(image));
            }
        });
        face.setFitWidth(60);
        face.setFitHeight(60);
        borderPane.setLeft(face);
        BorderPane.setAlignment(face, Pos.CENTER);
        BorderPane.setMargin(face, new Insets(0, 15, 0, 0));

        VBox center = new VBox(5);
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Text name = new Text(item.getUsername());
        name.setFont(Settings.DEFAULT_FONT);
        Node fansMedal = item.getFansMedal().getDisplayOld();
        hBox.getChildren().addAll(name, fansMedal);
        Text desc = new Text(String.format("UID: %s", item.getUid()));
        desc.setFont(Settings.DEFAULT_FONT);
        center.getChildren().addAll(hBox, desc);
        borderPane.setCenter(center);
        BorderPane.setAlignment(center, Pos.CENTER);

        VBox right = new VBox(5);
        right.setAlignment(Pos.CENTER_RIGHT);

        BorderPane rightUp = new BorderPane();
        Text exp = new Text("亲密度");
        exp.setFont(Settings.DEFAULT_FONT);
        Text current = new Text("%s/%s".formatted(item.getFansMedal().getExp(), item.getFansMedal().getNextExp()));
        current.setFont(Settings.DEFAULT_FONT);
        rightUp.setLeft(exp);
        rightUp.setRight(current);

        HBox rightCenter = new HBox(5);
        rightCenter.setAlignment(Pos.CENTER);
        double prog = (double) item.getFansMedal().getExp() / item.getFansMedal().getNextExp();
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER_LEFT);
        Rectangle track = new Rectangle();
        track.setHeight(3.0);
        track.setWidth(250);
        track.setFill(Color.valueOf("#E0E0E0"));
        Rectangle bar = new Rectangle();
        bar.setHeight(3.0);
        bar.setWidth(250 * prog);
        bar.setFill(item.getFansMedal().getOldStyle().medalColorStart());
        stackPane.getChildren().addAll(track, bar);
        rightCenter.getChildren().addAll(stackPane);

        DecimalFormat df = new DecimalFormat("##.##%");
        Text percent = new Text(String.format("%s", df.format(prog)));
        percent.setFont(Settings.DEFAULT_FONT);
        right.getChildren().addAll(rightUp, rightCenter, percent);
        borderPane.setRight(right);
        BorderPane.setAlignment(right, Pos.BOTTOM_CENTER);

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
