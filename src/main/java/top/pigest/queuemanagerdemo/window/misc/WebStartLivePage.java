package top.pigest.queuemanagerdemo.window.misc;

import com.google.gson.JsonObject;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.liveroom.LiveArea;
import top.pigest.queuemanagerdemo.liveroom.LiveRoomApi;
import top.pigest.queuemanagerdemo.liveroom.SubLiveArea;
import top.pigest.queuemanagerdemo.util.PagedContainerFactory;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.window.main.ChildPage;
import top.pigest.queuemanagerdemo.window.main.NamedPage;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WebStartLivePage extends BorderPane implements NamedPage, ChildPage {
    private final QMButton copyLink = new QMButton("复制");
    private final QMButton copyCode = new QMButton("复制");
    private List<LiveArea> areas;
    private SubLiveArea selectedArea;
    private boolean liveStarted = false;
    private String link = "";
    private String code = "";
    private String title = "";

    private Pane parentPage;
    private final QMButton areaSelect;
    private final QMButton startStopLive;
    private final JFXTextField titleField;
    private final JFXTextField linkField;
    private final JFXTextField codeField;
    public WebStartLivePage() {
        PagedContainerFactory factory = new PagedContainerFactory("c0");
        areaSelect = Utils.make(new QMButton("正在加载", "#1f1e33"), button -> {
            button.setPrefWidth(300);
            button.disable(true);
        });
        startStopLive = connectButton();
        titleField = new JFXTextField();
        titleField.setPrefWidth(400);
        titleField.setPromptText("标题");
        titleField.setDisable(true);
        titleField.setFont(Settings.DEFAULT_FONT);
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) {
                titleField.setText(oldValue);
            }
        });
        linkField = new JFXTextField();
        linkField.setPrefWidth(400);
        linkField.setEditable(false);
        linkField.setDisable(true);
        linkField.setText("开播后显示");
        linkField.setFont(Settings.DEFAULT_FONT);
        codeField = new JFXTextField();
        codeField.setPrefWidth(400);
        codeField.setEditable(false);
        codeField.setDisable(true);
        codeField.setText("开播后显示");
        codeField.setFont(Settings.DEFAULT_FONT);
        factory.addControl("直播分区", areaSelect);
        factory.addControl("直播间标题", Utils.make(new HBox(10), hBox -> {
            QMButton save = new QMButton("保存");
            save.setPrefWidth(100);
            save.setOnAction(event -> {
                save.disable(true);
                CompletableFuture.supplyAsync(() -> LiveRoomApi.updateTitle(titleField.getText()))
                        .whenComplete((status, throwable) -> Platform.runLater(() -> {
                            if (throwable != null) {
                                QueueManager.INSTANCE.getMainScene().showDialogMessage("保存失败", true);
                            } else {
                                QueueManager.INSTANCE.getMainScene().showDialogMessage("保存成功", false);
                            }
                            save.disable(false);
                        }));
            });
            hBox.getChildren().addAll(titleField, save);
        }));
        factory.addControl("操作", startStopLive);
        factory.addControl("推流地址", Utils.make(new HBox(10), hBox -> {
            copyLink.setPrefWidth(100);
            copyLink.disable(true);
            copyLink.setOnAction(event -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(linkField.getText());
                clipboard.setContent(content);
                QueueManager.INSTANCE.getMainScene().showDialogMessage("已复制到剪贴板", false);
            });
            hBox.getChildren().addAll(linkField, copyLink);
        }));
        factory.addControl("推流码", Utils.make(new HBox(10), hBox -> {
            copyCode.setPrefWidth(100);
            copyCode.disable(true);
            copyCode.setOnAction(event -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(codeField.getText());
                clipboard.setContent(content);
                QueueManager.INSTANCE.getMainScene().showDialogMessage("已复制到剪贴板", false);
            });
            hBox.getChildren().addAll(codeField, copyCode);
        }));

        refresh();

        this.setCenter(factory.build());
    }

    private void refresh() {
        CompletableFuture.runAsync(this::getInfo)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        Platform.runLater(() -> Utils.showDialogMessage("获取当前直播间状态失败", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
                        return;
                    }
                    Platform.runLater(this::milk);
                });
    }

    private void milk() {
        this.areaSelect.disable(false);
        this.setSelectedArea(this.selectedArea);
        this.areaSelect.setOnAction(event -> QueueManager.INSTANCE.getMainScene().setMainContainer(() -> new LiveAreaSelectionPage(areas), null, this));
        this.titleField.setText(title);
        this.titleField.setDisable(false);
        this.startStopLive.disable(false);
        if (liveStarted) {
            connectedButton(startStopLive);
            linkField.setText(link);
            codeField.setText(code);
            linkField.setDisable(false);
            codeField.setDisable(false);
            copyLink.disable(false);
            copyCode.disable(false);
        } else {
            disconnectedButton(startStopLive);
            linkField.setText("开播后显示");
            codeField.setText("开播后显示");
            linkField.setDisable(true);
            codeField.setDisable(true);
            copyLink.disable(true);
            copyCode.disable(true);
        }
    }

    private void getInfo() {
        // 获取直播间分区列表
        if (this.areas == null) {
            this.areas = LiveRoomApi.getLiveAreas();
        }
        // 获取已选取分区
        List<SubLiveArea> lastSelectedAreas = LiveRoomApi.getSelectedAreas();
        this.selectedArea = lastSelectedAreas.getFirst();
        // 当前是否开播
        JsonObject value = LiveRoomApi.getLiveRoomInfo(QueueManager.getSelfUid()).getValue();
        this.liveStarted = value.get("live_status").getAsInt() == 1;
        this.title = value.get("title").getAsString();
        // 若当前开播，获取推流地址与推流码
        if (this.liveStarted) {
            List<String> list = LiveRoomApi.fetchStreamAddress();
            if (!list.isEmpty()) {
                this.link = list.get(0);
                this.code = list.get(1);
            }
        }
    }

    private QMButton connectButton() {
        QMButton qmButton = new QMButton("开始直播", "#55bb55");
        qmButton.setPrefWidth(180);
        qmButton.setGraphic(new WhiteFontIcon("fas-video"));
        qmButton.disable(true);
        if (liveStarted) {
            connectedButton(qmButton);
        } else {
            qmButton.setOnAction(event -> {
                qmButton.disable(true);
                qmButton.setText("正在开始");
                qmButton.setGraphic(new WhiteFontIcon("fas-bullseye"));
                CompletableFuture.supplyAsync(() -> LiveRoomApi.startLive(selectedArea)).whenComplete((res, ex) -> {
                    if (ex != null || !res) {
                        Platform.runLater(() -> {
                            QueueManager.INSTANCE.getMainScene().showDialogMessage("开播失败", true);
                            qmButton.disable(false);
                            qmButton.setText("开始直播");
                            qmButton.setGraphic(new WhiteFontIcon("fas-video"));
                        });
                        return;
                    }
                    Platform.runLater(() -> QueueManager.INSTANCE.getMainScene().showDialogMessage("开播成功，请复制推流信息并在OBS中操作", false));
                    refresh();
                });
            });
        }
        return qmButton;
    }

    public void connectedButton(QMButton qmButton) {
        qmButton.setText("关闭直播");
        qmButton.setGraphic(new WhiteFontIcon("fas-video-slash"));
        qmButton.setBackgroundColor("#bb5555");
        qmButton.setOnAction(event1 -> {
            qmButton.disable(true);
            qmButton.setText("正在关闭");
            qmButton.setGraphic(new WhiteFontIcon("fas-bullseye"));
            CompletableFuture.supplyAsync(LiveRoomApi::stopLive).whenComplete((res, ex) -> {
                if (ex != null || !res) {
                    Platform.runLater(() -> {
                        QueueManager.INSTANCE.getMainScene().showDialogMessage("关播失败", true);
                        qmButton.disable(false);
                        qmButton.setText("关闭直播");
                        qmButton.setGraphic(new WhiteFontIcon("fas-video-slash"));
                    });
                    return;
                }
                Platform.runLater(() -> QueueManager.INSTANCE.getMainScene().showDialogMessage("关播成功", false));
                refresh();
            });
        });
        qmButton.disable(false);
    }

    public void disconnectedButton(QMButton qmButton) {
        qmButton.setText("开始直播");
        qmButton.setGraphic(new WhiteFontIcon("fas-video"));
        qmButton.setBackgroundColor("#55bb55");
        qmButton.setOnAction(event -> {
            qmButton.disable(true);
            qmButton.setText("正在开始");
            qmButton.setGraphic(new WhiteFontIcon("fas-bullseye"));
            CompletableFuture.supplyAsync(() -> LiveRoomApi.startLive(selectedArea)).whenComplete((res, ex) -> {
                if (ex != null || !res) {
                    Platform.runLater(() -> {
                        QueueManager.INSTANCE.getMainScene().showDialogMessage("开播失败", true);
                        qmButton.disable(false);
                        qmButton.setText("开始直播");
                        qmButton.setGraphic(new WhiteFontIcon("fas-video"));
                    });
                    return;
                }
                refresh();
            });
        });
    }

    public SubLiveArea getSelectedArea() {
        return selectedArea;
    }

    public boolean isLiveStarted() {
        return liveStarted;
    }

    @Override
    public Pane getParentPage() {
        return parentPage;
    }

    @Override
    public void setParentPage(Pane parentPage) {
        this.parentPage = parentPage;
    }

    @Override
    public String getName() {
        return "快捷开播";
    }

    public void setSelectedArea(SubLiveArea subLiveArea) {
        this.selectedArea = subLiveArea;
        Optional<LiveArea> liveArea = this.areas.stream().filter(area -> area.id() == selectedArea.liveArea()).findFirst();
        String text = "";
        if (liveArea.isPresent()) {
            text += liveArea.get().name() + " - ";
        }
        text += selectedArea.name();
        this.areaSelect.setText(text);
    }
}
