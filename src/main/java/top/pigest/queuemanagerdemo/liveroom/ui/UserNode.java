package top.pigest.queuemanagerdemo.liveroom.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.resource.RequireCleaning;
import top.pigest.queuemanagerdemo.util.Utils;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;

public class UserNode extends BorderPane implements RequireCleaning {

    private final ImageView face;

    public UserNode(User item) {
        this(item, true, true, true);
    }

    public UserNode(User item, boolean hasFace, boolean medal, boolean intimacy) {
        if (hasFace) {
            face = new ImageView();
            Circle clip = new Circle();
            clip.setCenterX(30);
            clip.setCenterY(30);
            clip.setRadius(30);
            face.setClip(clip);
            CompletableFuture.supplyAsync(() -> new Image(item.getFace())).whenComplete((image, throwable) -> {
                if (throwable == null) {
                    Platform.runLater(() -> face.setImage(image));
                }
            });
            face.setFitWidth(60);
            face.setFitHeight(60);
            this.setLeft(face);
            BorderPane.setAlignment(face, Pos.CENTER);
            BorderPane.setMargin(face, new Insets(0, 15, 0, 0));
        } else {
            face = null;
        }

        VBox center = new VBox(5);
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER_LEFT);
        Text name = new Text(item.getUsername());
        name.setFont(Settings.DEFAULT_FONT);
        hBox.getChildren().add(name);
        Text desc = new Text(String.format("UID: %s", item.getUid()));
        if (item.getGuardInfo() != null) {
            desc = new Text(String.format("%s到期 剩余%s天", item.getGuardInfo().getExpiredTimeString(), item.getGuardInfo().getDaysUntilExpire()));
        }
        desc.setFont(Settings.DEFAULT_FONT);
        if (medal) {
            Node fansMedal = item.getFansMedal().getDisplayNew();
            hBox.getChildren().add(fansMedal);
            center.getChildren().addAll(hBox, desc);
        } else {
            hBox.getChildren().add(desc);
            center.getChildren().add(hBox);
        }
        center.setAlignment(Pos.CENTER_LEFT);
        this.setCenter(center);
        BorderPane.setAlignment(center, Pos.CENTER);

        if (intimacy) {
            VBox right = new VBox(5);
            right.setAlignment(Pos.CENTER_RIGHT);
            BorderPane rightUp = new BorderPane();
            Text exp = new Text("%s".formatted(item.getFansMedal().getExp()));
            exp.setFont(Settings.DEFAULT_FONT);
            Text current = new Text("%s".formatted(item.getFansMedal().getNextExp()));
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
            Color start = item.getFansMedal().getStyle().start();
            bar.setFill(new Color(start.getRed(), start.getGreen(), start.getBlue(), 1));
            stackPane.getChildren().addAll(track, bar);
            rightCenter.getChildren().addAll(stackPane);
            DecimalFormat df = new DecimalFormat("##.##%");
            Text percent = new Text(String.format("总EXP:%s %s", item.getFansMedal().getScore(), df.format(prog)));
            percent.setFont(Settings.DEFAULT_FONT);
            right.getChildren().addAll(rightUp, rightCenter, percent);
            this.setRight(right);
            BorderPane.setAlignment(right, Pos.BOTTOM_CENTER);
        }
    }

    public void clean() {
        Utils.onPresent(this.face.getImage(), Image::cancel);
        this.face.setImage(null);
    }
}
