package top.pigest.queuemanagerdemo.window.misc;

import com.jfoenix.controls.JFXScrollPane;
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
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.ScrollableText;
import top.pigest.queuemanagerdemo.liveroom.LiveArea;
import top.pigest.queuemanagerdemo.liveroom.SubLiveArea;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.window.main.ChildPage;
import top.pigest.queuemanagerdemo.window.main.NamedPage;

import java.util.List;
import java.util.Objects;

public class LiveAreaSelectionPage extends VBox implements NamedPage, ChildPage {
    private QMButton currentSelected;
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
                if (!(button == currentSelected)) {
                    if (currentSelected != null) {
                        currentSelected.setBackgroundColor(null);
                        ((ScrollableText) currentSelected.getGraphic()).getText().setFill(Paint.valueOf("BLACK"));
                    }
                    button.setBackgroundColor("#1a8bcc");
                    text.getText().setFill(Paint.valueOf("WHITE"));
                    currentSelected = button;
                    ((WebStartLivePage) parentPage).setSelectedArea(subLiveArea);
                }
            });
            if (subLiveArea.equals(((WebStartLivePage) this.parentPage).getSelectedArea())) {
                button.setBackgroundColor("#1a8bcc");
                text.getText().setFill(Paint.valueOf("WHITE"));
                currentSelected = button;
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
    }

    @Override
    public String getName() {
        return "选择分区";
    }
}
