package top.pigest.queuemanagerdemo.misc.dialog.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.*;
import top.pigest.queuemanagerdemo.misc.dialog.DialogBranch;
import top.pigest.queuemanagerdemo.misc.dialog.DialogNode;
import top.pigest.queuemanagerdemo.misc.dialog.GIUgcDialog;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.util.gi.Struct;
import top.pigest.queuemanagerdemo.util.gi.StructList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DialogEditorPage extends BorderPane implements NamedPage, ChildPage {
    private Pane parentPage;
    private final QMButton newButton = new QMButton("新建对话", "#55bb55");
    private final QMButton importButton = new QMButton("导入对话文件");
    private final QMButton exportButton = new QMButton("导出对话文件", "#58135E");
    private final QMButton metaEditButton = new QMButton("元数据编辑", "#3A0603");
    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox insideVBox = new VBox();
    private FileChooser fileChooser;
    private DialogDataEditor currentEditor;

    private Struct<GIUgcDialog> backStruct = null;
    protected Struct<GIUgcDialog> dialogStruct = null;
    protected final List<String> idList = new ArrayList<>(3);
    private File lastDirectory = new File(System.getProperty("user.home") + "/Documents");

    public DialogEditorPage() {
        idList.addAll(List.of("0", "0", "0"));

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER);
        top.getChildren().addAll(newButton, importButton, exportButton, metaEditButton);
        top.getChildren().forEach(child -> ((QMButton) child).setPrefWidth(180));
        newButton.setGraphic(new WhiteFontIcon("fas-plus"));
        importButton.setGraphic(new WhiteFontIcon("fas-file-import"));
        exportButton.setGraphic(new WhiteFontIcon("fas-file-export"));
        metaEditButton.setGraphic(new WhiteFontIcon("fas-pencil-alt"));
        newButton.setOnAction(event -> updateBackStruct(createNewDialogStruct()));
        importButton.setOnAction(event -> importDialog());
        exportButton.setOnAction(event -> exportDialog());
        metaEditButton.setOnAction(event -> {
            DialogDataEditor content = this.getCurrentEditor();
            if (content == null) {
                content = new DialogMetaEditor(this);
                JFXDialog dialog = new JFXDialog(QueueManager.INSTANCE.getMainScene().getRootDrawer(), (DialogMetaEditor) content, JFXDialog.DialogTransition.CENTER);
                dialog.setOverlayClose(false);
                this.setCurrentEditor(content);
                content.postProcess(dialog);
            }
        });
        exportButton.disable(true);
        metaEditButton.disable(true);
        top.setPadding(new Insets(10));
        BorderPane.setAlignment(top, Pos.CENTER);
        this.setTop(top);

        scrollPane.setBorder(new Border(new BorderStroke(Paint.valueOf("0x22222233"), Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT,
                BorderStrokeStyle.SOLID, null, null, null,
                CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
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
        BorderPane.setMargin(scrollPane, new Insets(5, 5, 5, 5));
        BorderPane.setAlignment(scrollPane, Pos.CENTER);
        this.setCenter(scrollPane);
    }

    public void onAddNode(int addIndex) {
        if (dialogStruct.getValue().getStartNode() >= addIndex && dialogStruct.getValue().getStartNode() < dialogStruct.getValue().getDialogNodeList().size() - 1) {
            dialogStruct.getValue().setStartNode(dialogStruct.getValue().getStartNode() + 1);
        }
        for (Struct<DialogNode> dialogNodeStruct : dialogStruct.getValue().getDialogNodeList()) {
            DialogNode node = dialogNodeStruct.getValue();
            switch (node.getOperation()) {
                case JUMP_SENTENCE -> {
                    if (node.getJumpIndex() >= addIndex && node.getJumpIndex() < dialogStruct.getValue().getDialogNodeList().size() - 1) {
                        node.setJumpIndex(node.getJumpIndex() + 1);
                    }
                }
                case OPEN_SELECTION -> {
                    for (Struct<DialogBranch> branchStruct : node.getDialogBranchList()) {
                        if (branchStruct.getValue().getJumpIndex() >= addIndex && branchStruct.getValue().getJumpIndex() < dialogStruct.getValue().getDialogNodeList().size() - 1) {
                            branchStruct.getValue().setJumpIndex(branchStruct.getValue().getJumpIndex() + 1);
                        }
                    }
                }
            }
        }
        updateStackDisplay(true);
    }

    public void onRemoveNode(int removeIndex) {
        if (dialogStruct.getValue().getStartNode() > removeIndex) {
            dialogStruct.getValue().setStartNode(dialogStruct.getValue().getStartNode() - 1);
        }
        for (Struct<DialogNode> dialogNodeStruct : dialogStruct.getValue().getDialogNodeList()) {
            DialogNode node = dialogNodeStruct.getValue();
            switch (node.getOperation()) {
                case JUMP_SENTENCE -> {
                    if (node.getJumpIndex() > removeIndex) {
                        node.setJumpIndex(node.getJumpIndex() - 1);
                    }
                }
                case OPEN_SELECTION -> {
                    for (Struct<DialogBranch> branchStruct : node.getDialogBranchList()) {
                        if (branchStruct.getValue().getJumpIndex() > removeIndex) {
                            branchStruct.getValue().setJumpIndex(branchStruct.getValue().getJumpIndex() - 1);
                        }
                    }
                }
            }
        }
        updateStackDisplay(true);
    }

    private Struct<GIUgcDialog> createNewDialogStruct() {
        GIUgcDialog newDialog = GIUgcDialog.createNewDialog(getDialogNodeStructId());
        newDialog.getDialogNodeList().addValue(DialogNode.createNewDialogNode(getDialogBranchStructId()));
        return new Struct<>(getDialogStructId(), newDialog);
    }

    private void importDialog() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("对话文件", "*.json"));
        }
        fileChooser.setInitialDirectory(lastDirectory);
        fileChooser.setTitle("选择对话 JSON 文件");
        File result = fileChooser.showOpenDialog(QueueManager.INSTANCE.getPrimaryStage());
        if (result != null) {
            try {
                JsonObject jsonObject = JsonParser.parseReader(new FileReader(result)).getAsJsonObject();
                Struct<GIUgcDialog> struct = GIUgcDialog.read(jsonObject);
                lastDirectory = result.getParentFile();
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
        fileChooser.setInitialDirectory(lastDirectory);
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
                lastDirectory = result.getParentFile();
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
        this.insideVBox.getChildren().clear();
        this.updateStackDisplay(false);
    }

    public void updateStackDisplay(boolean keepLocation) {
        this.exportButton.disable(false);
        this.metaEditButton.disable(false);
        double p = this.scrollPane.getVvalue();
        this.insideVBox.getChildren().forEach(node -> {
            if (node instanceof DialogNodeButton button && button.getGraphic() instanceof VBox graphic) {
                graphic.getChildren().forEach(child -> {
                    if (child instanceof ButtonBase buttonBase) {
                        buttonBase.setOnAction(null);
                    }
                });
                button.setOnAction(null);
            }
        });
        this.insideVBox.getChildren().clear();
        StructList<DialogNode> nodeList = this.dialogStruct.getValue().getDialogNodeList();
        for (int i = 0; i < nodeList.size(); i++) {
            this.insideVBox.getChildren().add(getNodeButton(i, nodeList.get(i)));
        }
        if (keepLocation) {
            this.scrollPane.setVvalue(Math.min(p, this.scrollPane.getVmax()));
        }
    }

    public void selectMode(boolean isSelectMode, String hint) {
        this.insideVBox.getChildren().forEach(node -> {
            if (node instanceof DialogNodeButton button && button.getGraphic() instanceof VBox) {
                button.selectMode(isSelectMode);
            }
        });
        newButton.disable(isSelectMode);
        importButton.disable(isSelectMode);
        exportButton.disable(isSelectMode);
        metaEditButton.disable(isSelectMode);
        if (isSelectMode) {
            Utils.showDialogMessage(hint, false, QueueManager.INSTANCE.getMainScene().getRootDrawer());
        }
    }

    private QMButton getNodeButton(int index, Struct<DialogNode> dialogNode) {
        DialogNodeButton dialogNodeButton = new DialogNodeButton(index, dialogNode, dialogStruct.getValue().getDialogNodeList(), this);
        dialogNodeButton.setPrefWidth(this.insideVBox.getWidth() - 12);
        dialogNodeButton.setMinWidth(USE_PREF_SIZE);
        dialogNodeButton.setMaxWidth(USE_PREF_SIZE);
        dialogNodeButton.setBorder(new Border(MultiMenuProvider.DEFAULT_BORDER_STROKE));
        return dialogNodeButton;
    }

    public int getStartIndex() {
        return dialogStruct == null ? 0 : dialogStruct.getValue().getStartNode();
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

    public DialogDataEditor getCurrentEditor() {
        return currentEditor;
    }

    public void setCurrentEditor(DialogDataEditor currentEditor) {
        this.currentEditor = currentEditor;
    }
}
