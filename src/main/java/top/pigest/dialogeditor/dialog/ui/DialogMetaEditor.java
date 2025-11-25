package top.pigest.dialogeditor.dialog.ui;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.control.QMButton;
import top.pigest.dialogeditor.dialog.DialogNode;
import top.pigest.dialogeditor.util.gi.Struct;

public class DialogMetaEditor extends VBox implements DialogDataEditor {
    private final QMButton okButton;
    private final QMButton cancelButton;
    private final DialogNodeEditor.ButtonSelector initNodeSelector;
    private final JFXTextField dialogStructField;
    private final JFXTextField dialogNodeField;
    private final JFXTextField dialogBranchField;
    private JFXDialog dialog;

    private final DialogEditorPage editorPage;

    public DialogMetaEditor(DialogEditorPage editorPage) {
        this.editorPage = editorPage;

        this.setStyle("-fx-background-color: #26282b");
        this.setPrefWidth(640);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 20, 20, 20));
        Text titleNode = new Text("编辑元数据");
        titleNode.setTextAlignment(TextAlignment.CENTER);
        titleNode.setFont(new Font(Settings.BOLD_FONT.getFamily(), 30));
        titleNode.setFill(Color.LIGHTGRAY);
        VBox.setMargin(titleNode, new Insets(0, 0, 20, 0));
        this.getChildren().add(titleNode);

        initNodeSelector = new DialogNodeEditor.ButtonSelector(editorPage.getStartIndex(), "选择开始节点（当前：%s）".formatted(editorPage.getStartIndex()));
        initNodeSelector.setPrefWidth(300);
        initNodeSelector.setOnAction(event -> {
            this.editorPage.selectMode(DialogEditorPage.EditMode.SELECT, "请选择开始节点");
            dialog.close();
        });
        VBox.setMargin(initNodeSelector, new Insets(0, 0, 15, 0));
        this.getChildren().add(initNodeSelector);

        Text hint = new Text("结构体 ID 可在游戏中导出的结构体文件中找到\n导入结构体文件时会自动设置结构体 ID\n保存结构体 ID 后，会更改导出文件的对应结构体 ID");
        hint.setFill(Color.WHITE);
        hint.setFont(Settings.DEFAULT_FONT);
        hint.setTextAlignment(TextAlignment.CENTER);
        VBox.setMargin(hint, new Insets(0, 0, 15, 0));
        this.getChildren().add(hint);

        dialogStructField = new JFXTextField();
        dialogStructField.setUnFocusColor(Color.LIGHTGRAY);
        dialogStructField.setFocusColor(Color.AQUA);
        dialogStructField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
        dialogStructField.setFont(Settings.DEFAULT_FONT);
        dialogStructField.setPrefWidth(200);
        dialogStructField.setMaxWidth(USE_PREF_SIZE);
        dialogStructField.setPromptText("对话结构体ID");
        dialogStructField.setLabelFloat(true);
        dialogStructField.setText(editorPage.getDialogStructId());
        VBox.setMargin(dialogStructField, new Insets(15, 0, 15, 0));
        this.getChildren().add(dialogStructField);

        dialogNodeField = new JFXTextField();
        dialogNodeField.setUnFocusColor(Color.LIGHTGRAY);
        dialogNodeField.setFocusColor(Color.AQUA);
        dialogNodeField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
        dialogNodeField.setFont(Settings.DEFAULT_FONT);
        dialogNodeField.setPrefWidth(200);
        dialogNodeField.setMaxWidth(USE_PREF_SIZE);
        dialogNodeField.setPromptText("对话节点结构体ID");
        dialogNodeField.setLabelFloat(true);
        dialogNodeField.setText(editorPage.getDialogNodeStructId());
        VBox.setMargin(dialogNodeField, new Insets(15, 0, 15, 0));
        this.getChildren().add(dialogNodeField);

        dialogBranchField = new JFXTextField();
        dialogBranchField.setUnFocusColor(Color.LIGHTGRAY);
        dialogBranchField.setFocusColor(Color.AQUA);
        dialogBranchField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
        dialogBranchField.setFont(Settings.DEFAULT_FONT);
        dialogBranchField.setPrefWidth(200);
        dialogBranchField.setMaxWidth(USE_PREF_SIZE);
        dialogBranchField.setPromptText("对话分支结构体ID");
        dialogBranchField.setLabelFloat(true);
        dialogBranchField.setText(editorPage.getDialogBranchStructId());
        VBox.setMargin(dialogBranchField, new Insets(15, 0, 15, 0));
        this.getChildren().add(dialogBranchField);

        HBox saveOrCancel = new HBox(40);
        saveOrCancel.setAlignment(Pos.CENTER);
        okButton = new QMButton("保存", QMButton.DEFAULT_COLOR);
        okButton.setPrefWidth(80);
        cancelButton = new QMButton("取消", "#bb5555");
        cancelButton.setPrefWidth(80);
        saveOrCancel.getChildren().addAll(okButton, cancelButton);
        this.getChildren().add(saveOrCancel);
    }

    @Override
    public Struct<DialogNode> getBindNode() {
        return null;
    }

    @Override
    public void receiveBindNode(Struct<DialogNode> dialogNode, int index) {
        this.editorPage.selectMode(DialogEditorPage.EditMode.FREE, "");
        initNodeSelector.setValue(index);
        initNodeSelector.setText("选择开始节点（当前：%s）".formatted(index));
    }

    @Override
    public void postProcess(JFXDialog dialog) {
        this.dialog = dialog;
        okButton.setOnAction(e -> {
            this.editorPage.idList.set(0, dialogStructField.getText());
            this.editorPage.idList.set(1, dialogNodeField.getText());
            this.editorPage.idList.set(2, dialogBranchField.getText());
            this.editorPage.dialogStruct.setStructId(dialogStructField.getText());
            this.editorPage.dialogStruct.getValue().getDialogNodeList().setStructId(dialogNodeField.getText());
            this.editorPage.dialogStruct.getValue().getDialogNodeList().forEach(node -> {
                node.setStructId(dialogNodeField.getText());
                node.getValue().getDialogBranchList().setStructId(dialogBranchField.getText());
                node.getValue().getDialogBranchList().forEach(branchNode -> branchNode.setStructId(dialogBranchField.getText()));
            });
            this.editorPage.setCurrentEditor(null);
            this.editorPage.dialogStruct.getValue().setStartNode(initNodeSelector.getValue());
            dialog.close();
        });
        cancelButton.setOnAction(e -> {
            this.editorPage.setCurrentEditor(null);
            dialog.close();
        });
        dialog.show();
    }
}
