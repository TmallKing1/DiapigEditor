package top.pigest.queuemanagerdemo.misc.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.ChildPage;
import top.pigest.queuemanagerdemo.control.NamedPage;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.misc.dialog.DialogNode;
import top.pigest.queuemanagerdemo.misc.dialog.GIUgcDialog;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.util.gi.Struct;
import top.pigest.queuemanagerdemo.util.gi.StructList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DialogEditorPage extends BorderPane implements NamedPage, ChildPage {
    private Pane parentPage;
    private final QMButton newButton = new QMButton("新建对话", "#55bb55");
    private final QMButton importButton = new QMButton("导入对话文件");
    private final QMButton exportButton = new QMButton("导出对话文件", "#58135E");
    private final QMButton idEditButton = new QMButton("编辑ID数据", "#3A0603");
    private ScrollPane scrollPane = new ScrollPane();
    private VBox insideVBox = new VBox();
    private FileChooser fileChooser;

    private Struct<GIUgcDialog> backStruct = null;
    private Struct<GIUgcDialog> dialogStruct = null;
    private List<String> idList = new ArrayList<>(3);

    public DialogEditorPage() {
        idList.addAll(List.of("0", "0", "0"));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER);
        top.getChildren().addAll(newButton, importButton, exportButton, idEditButton);
        top.getChildren().forEach(child -> ((QMButton) child).setPrefWidth(180));
        newButton.setGraphic(new WhiteFontIcon("fas-plus"));
        importButton.setGraphic(new WhiteFontIcon("fas-file-import"));
        exportButton.setGraphic(new WhiteFontIcon("fas-file-export"));
        idEditButton.setGraphic(new WhiteFontIcon("fas-pencil-alt"));
        newButton.setOnAction(event -> updateBackStruct(createNewDialogStruct()));
        importButton.setOnAction(event -> importDialog());
        exportButton.setOnAction(event -> exportDialog());
        idEditButton.setOnAction(event -> {});
        exportButton.disable(true);
        top.setPadding(new Insets(10));
        BorderPane.setAlignment(top, Pos.CENTER);
        this.setTop(top);

        scrollPane.setContent(insideVBox);
        scrollPane.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/scrollbar.css")).toExternalForm());
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        insideVBox.setAlignment(Pos.TOP_CENTER);
        Text text = new Text("- 当前没有对话实例，请先新建或导入 -");
        text.setFont(Settings.DEFAULT_FONT);
        text.setFill(Color.GRAY);
        VBox.setMargin(text, new Insets(30));
        insideVBox.getChildren().add(text);
        BorderPane.setMargin(scrollPane, new Insets(10, 10, 10, 10));
        BorderPane.setAlignment(scrollPane, Pos.CENTER);
        this.setCenter(scrollPane);
    }

    private Struct<GIUgcDialog> createNewDialogStruct() {
        return new Struct<>(getDialogStructId(), GIUgcDialog.createNewDialog(getDialogNodeStructId()));
    }

    private void importDialog() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("对话文件", "*.json"));
        }
        fileChooser.setTitle("选择对话 JSON 文件");
        File result = fileChooser.showOpenDialog(QueueManager.INSTANCE.getPrimaryStage());
        if (result != null) {
            try {
                JsonObject jsonObject = JsonParser.parseReader(new FileReader(result)).getAsJsonObject();
                Struct<GIUgcDialog> struct = GIUgcDialog.read(jsonObject);
                Platform.runLater(() -> this.updateBackStruct(struct));
            } catch (FileNotFoundException e) {
                Platform.runLater(() -> Utils.showDialogMessage("文件不存在", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
            } catch (Exception e) {
                Platform.runLater(() -> Utils.showDialogMessage("文件格式错误", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
            }
        }
    }

    private void exportDialog() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("对话文件", "*.json"));
        }
        fileChooser.setTitle("导出对话 JSON 文件");
        File result = fileChooser.showSaveDialog(QueueManager.INSTANCE.getPrimaryStage());
        if (result != null) {
            try {
                JsonObject object = GIUgcDialog.write(this.dialogStruct);
                if (!result.exists()) {
                    if (!result.createNewFile()) {
                        throw new RuntimeException();
                    }
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(object);
                Files.write(result.toPath(), json.getBytes());
                Platform.runLater(() -> Utils.showDialogMessage("成功导出文件", false, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
            } catch (FileNotFoundException e) {
                Platform.runLater(() -> Utils.showDialogMessage("文件不存在", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
            } catch (Exception e) {
                Platform.runLater(() -> Utils.showDialogMessage("导出失败", true, QueueManager.INSTANCE.getMainScene().getRootDrawer()));
            }
        }
    }

    private void updateBackStruct(Struct<GIUgcDialog> backStruct) {
        this.backStruct = backStruct;
        if (dialogStruct != null) {
            Utils.showChoosingDialog("确认操作", "请确保当前编辑的对话妥善保存\n再载入新的对话",
                    "确认", "取消",
                    event -> updateDialogStruct(), event -> {},
                    QueueManager.INSTANCE.getMainScene().getRootDrawer());
        } else {
            updateDialogStruct();
        }
    }

    private void updateDialogStruct() {
        this.dialogStruct = this.backStruct;
        this.idList.set(0, this.dialogStruct.getStructId());
        StructList<DialogNode> nodeList = this.dialogStruct.getValue().getDialogNodeList();
        this.idList.set(1, nodeList.getStructId());
        if (!nodeList.isEmpty()) {
            this.idList.set(2, nodeList.getFirst().getValue().getDialogBranchList().getStructId());
        }
        this.updateStackDisplay(false);
    }

    private void updateStackDisplay(boolean keepLocation) {
        this.exportButton.disable(false);
        double p = this.scrollPane.getVvalue();
    }

    public String getDialogStructId() {
        return idList.getFirst();
    }

    public String getDialogNodeStructId() {
        return idList.get(1);
    }

    public String getDialogBranchStructId() {
        return idList.get(2);
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
        return "对话编辑";
    }
}
