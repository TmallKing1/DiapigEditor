package top.pigest.queuemanagerdemo.misc.ui;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.control.ListPagedContainer;
import top.pigest.queuemanagerdemo.control.MultiMenuProvider;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.User;
import top.pigest.queuemanagerdemo.misc.UserEntryTracker;
import top.pigest.queuemanagerdemo.util.Utils;

import java.util.concurrent.CompletableFuture;

public class TrackedUserContainer extends ListPagedContainer<User> {
    public TrackedUserContainer(String id, ObservableList<User> items) {
        super(id, items, 10, true);
        items.addListener((ListChangeListener<? super User>) c -> Platform.runLater(this::refresh));
    }

    @Override
    public Node getNode(User item) {
        BorderPane borderPane = User.userNode(item, false, false, false);
        borderPane.setBorder(new Border(MultiMenuProvider.DEFAULT_BORDER_STROKE));
        QMButton blacklist = new QMButton("拉黑", null);
        blacklist.setTextFill(Paint.valueOf("RED"));
        blacklist.setGraphic(new FontIcon("fas-ban:8:RED"));
        blacklist.setOnAction(e -> Utils.showChoosingDialog("确认拉黑", "你真的要拉黑 %s 吗？".formatted(item.getUsername()), "确定", "取消",
                event -> CompletableFuture.supplyAsync(() -> LiveRoomApi.modifyRelation(item, 5))
                        .whenComplete((result, throwable) -> {
                            if (result != null) {
                                Platform.runLater(() ->
                                        Utils.showDialogMessage(result, true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                            } else {
                                Platform.runLater(() ->
                                        Utils.showDialogMessage("拉黑成功", false, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                            }
                        }), event -> {}, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
        QMButton delete = new QMButton("删除", null);
        delete.setGraphic(new FontIcon("fas-trash-alt"));
        delete.setOnAction(e -> Utils.onPresent(UserEntryTracker.INSTANCE, t -> t.getUsers().remove(item)));
        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(blacklist, delete);
        borderPane.setRight(hBox);
        borderPane.setPadding(new Insets(5, 0, 5, 0));
        BorderPane.setAlignment(delete, Pos.CENTER);
        return borderPane;
    }
}
