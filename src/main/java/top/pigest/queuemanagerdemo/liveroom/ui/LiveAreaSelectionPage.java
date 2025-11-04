package top.pigest.queuemanagerdemo.liveroom.ui;

import com.jfoenix.controls.JFXScrollPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.*;
import top.pigest.queuemanagerdemo.liveroom.data.LiveArea;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.data.SubLiveArea;
import top.pigest.queuemanagerdemo.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LiveAreaSelectionPage extends VBox implements NamedPage, DataEditor {
    private QMButton selected;
    private SubLiveArea selectedArea;
    private Pane parentPage;

    public LiveAreaSelectionPage(List<LiveArea> liveAreas) {
        this.setAlignment(Pos.TOP_CENTER);
        this.setFillWidth(true);
        Label title = Utils.createLabel("选择直播分区");
        VBox.setMargin(title, new Insets(10, 40, 10, 40));
        GridPane main = new GridPane(10, 10);
        main.setAlignment(Pos.TOP_CENTER);
        int i = 0, j = 0;
        for (LiveArea live : liveAreas) {
            QMButton button = new QMButton(live.name());
            button.setPrefWidth(120);
            button.setOnAction(e -> {
                main.getChildren().forEach(child -> ((QMButton) child).setBackgroundColor("#1a8bcc"));
                button.setBackgroundColor("#1f1e33");
                if (this.getChildren().size() > 2) {
                    this.getChildren().removeLast();
                }
                this.getChildren().add(createScrollPane(live));
            });
            main.add(button, i, j);
            i++;
            if (i >= 5) {
                j++;
                i = 0;
            }
        }
        VBox.setMargin(main, new Insets(10, 40, 10, 40));
        this.getChildren().addAll(title, main);
    }

    private ScrollPane createScrollPane(LiveArea liveArea) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/scrollbar.css")).toExternalForm());
        GridPane gridPane = new GridPane(10, 10);
        gridPane.setAlignment(Pos.CENTER);
        scrollPane.setContent(gridPane);
        int i = 0, j = 0;
        for (SubLiveArea subLiveArea : liveArea.subAreas()) {
            ScrollableText text = new ScrollableText(subLiveArea.name(), 140, true);
            text.getText().setFont(Settings.DEFAULT_FONT);
            QMButton button = new QMButton("", null, false);
            button.setOnAction(event -> {
                if (!(button == selected)) {
                    if (selected != null) {
                        selected.setBackgroundColor(null);
                        ((ScrollableText) selected.getGraphic()).getText().setFill(Paint.valueOf("BLACK"));
                    }
                    button.setBackgroundColor("#55bb55");
                    text.getText().setFill(Paint.valueOf("WHITE"));
                    selected = button;
                    this.selectedArea = subLiveArea;
                }
            });
            if (subLiveArea.equals(selectedArea)) {
                button.setBackgroundColor("#55bb55");
                text.getText().setFill(Paint.valueOf("WHITE"));
                selected = button;
            }
            button.setGraphic(text);
            gridPane.add(button, i, j);
            i++;
            if (i >= 4) {
                j++;
                i = 0;
            }
        }
        JFXScrollPane.smoothScrolling(scrollPane);
        VBox.setMargin(scrollPane, new Insets(10, 40, 10, 40));
        return scrollPane;
    }

    @Override
    public Pane getParentPage() {
        return parentPage;
    }

    @Override
    public void setParentPage(Pane parentPage) {
        this.parentPage = parentPage;
        this.selectedArea = ((WebStartLivePage) parentPage).getSelectedArea();
    }

    @Override
    public String getName() {
        return "选择分区";
    }

    @Override
    public void save() {
        WebStartLivePage page = (WebStartLivePage) parentPage;
        page.setSelectedArea(selectedArea);
        if (page.isLiveStarted()) {
            Utils.showChoosingDialog("开播过程中分区切换", "是否要更新当前直播间分区", "确认", "取消", event1 ->
                    CompletableFuture.supplyAsync(() -> LiveRoomApi.updateArea(selectedArea))
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    Platform.runLater(() -> Utils.showDialogMessage("更新失败", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                                    return;
                                }
                                Platform.runLater(() -> Utils.showDialogMessage("更新成功", false, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                            }), event1 -> {} , QueueManager.INSTANCE.getMainScene().getRootDrawer());
        }
    }
}
