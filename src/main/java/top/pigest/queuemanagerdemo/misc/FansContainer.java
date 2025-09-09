package top.pigest.queuemanagerdemo.misc;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.control.DynamicListPagedContainer;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FansContainer extends DynamicListPagedContainer<User> {
    private final int rankType;
    public FansContainer(String id, int maxPerPage, int rankType) {
        super(id, maxPerPage);
        this.rankType = rankType;
    }

    @Override
    public List<User> getNextItems(int page) {
        try {
            return LiveRoomApi.getFans(page, this.rankType);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Node getNode(User item) {
        BorderPane borderPane = new BorderPane();

        ImageView face = new ImageView();
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

        borderPane.setBorder(new Border(MultiMenuProvider.DEFAULT_BORDER_STROKE));
        borderPane.setPadding(new Insets(5, 10, 5, 10));
        return borderPane;
    }
}
