package top.pigest.queuemanagerdemo.window.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.window.main.ChildPage;
import top.pigest.queuemanagerdemo.window.main.NamedPage;

import java.util.Objects;

public class LiveAreaSelectionPage extends VBox implements NamedPage, ChildPage {
    private Pane parentPage;

    public LiveAreaSelectionPage(JsonArray liveAreas) {
        Label title = Utils.createLabel("选择直播分区");
        VBox.setMargin(title, new Insets(10));
        GridPane main = new GridPane(10, 10);
        int i = 0, j = 0;
        for (JsonElement live : liveAreas) {
            JsonObject mainArea = live.getAsJsonObject();
            QMButton button = new QMButton(mainArea.get("name").getAsString());
            main.add(button, i, j);
            i++;
            if (i >= 4) {
                j++;
                i = 0;
            }
        }

        this.getChildren().add(title);
    }

    private static ScrollPane createScrollPane(JsonArray array) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.getStylesheets().add(Objects.requireNonNull(QMButton.class.getResource("css/scrollbar.css")).toExternalForm());
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
