package top.pigest.queuemanagerdemo.misc.dialog.ui;

import com.jfoenix.controls.JFXDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.ScrollablePane;
import top.pigest.queuemanagerdemo.control.ScrollableText;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.misc.dialog.DialogBranch;
import top.pigest.queuemanagerdemo.misc.dialog.DialogNode;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.util.gi.Struct;
import top.pigest.queuemanagerdemo.util.gi.StructList;

import java.util.ArrayList;
import java.util.List;

public class DialogNodeButton extends QMButton {
    private final Struct<DialogNode> dialogNode;
    private final DialogEditorPage editorPage;
    private final VBox root;
    private final QMButton editOrSelect;
    private final QMButton add;
    private final QMButton copy;
    private final QMButton remove;
    private final HBox mainTextInner;
    private Node operation;
    private final Text indexText;
    private final ScrollablePane<HBox> mainText;
    private int index;
    public DialogNodeButton(int index, Struct<DialogNode> dialogNode, StructList<DialogNode> dialogNodeList, DialogEditorPage editorPage) {
        super("", null, false);
        this.index = index;
        this.dialogNode = dialogNode;
        this.editorPage = editorPage;

        this.setRipplerFill(Color.DARKGRAY);

        root = new VBox();
        root.setFillWidth(true);

        BorderPane main = new BorderPane();
        BorderPane left = new BorderPane();
        left.setPrefWidth(70);
        indexText = new Text(String.valueOf(this.index));
        indexText.setFont(Settings.BOLD_FONT);
        updateFontIcon();
        BorderPane.setAlignment(indexText, Pos.CENTER);
        BorderPane.setAlignment(operation, Pos.CENTER_RIGHT);
        left.setLeft(indexText);
        left.setCenter(operation);
        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setMargin(left, new Insets(0, 5, 0, 0));
        main.setLeft(left);

        mainTextInner = new HBox();
        mainTextInner.setAlignment(Pos.CENTER_LEFT);
        mainText = new ScrollablePane<>(mainTextInner, 450);
        mainText.setBackDuration(0.01);
        updateMainText();
        BorderPane.setAlignment(mainText, Pos.CENTER_LEFT);
        BorderPane.setMargin(mainText, new Insets(0, 5, 0, 5));
        main.setCenter(mainText);

        HBox right = new HBox(5);

        editOrSelect = new QMButton("", "#1a8bcc");
        editOrSelect.setRipplerFill(Color.WHITE);
        editOrSelect.setGraphic(new WhiteFontIcon("fas-pen"));
        editOrSelect.setOnAction(event -> {
            DialogDataEditor content = this.editorPage.getCurrentEditor();
            JFXDialog dialog;
            if (content == null) {
                content = new DialogNodeEditor(dialogNode, this.index, editorPage);
                dialog = new JFXDialog(QueueManager.INSTANCE.getMainScene().getRootDrawer(), (DialogNodeEditor) content, JFXDialog.DialogTransition.CENTER);
                dialog.setOverlayClose(false);
                editorPage.setCurrentEditor(content);
            } else {
                content.receiveBindNode(dialogNode, this.index);
                if (content instanceof DialogNodeEditor) {
                    dialog = new JFXDialog(QueueManager.INSTANCE.getMainScene().getRootDrawer(), (DialogNodeEditor) content, JFXDialog.DialogTransition.CENTER);
                }
                else {
                    dialog = new JFXDialog(QueueManager.INSTANCE.getMainScene().getRootDrawer(), (DialogMetaEditor) content, JFXDialog.DialogTransition.CENTER);
                }
                dialog.setOverlayClose(false);
            }
            content.postProcess(dialog);
        });

        add = new QMButton("", "#55bb55");
        add.setRipplerFill(Color.WHITE);
        add.setGraphic(new WhiteFontIcon("fas-plus"));
        add.setOnAction(event -> {
            dialogNodeList.add(this.index + 1, new Struct<>(this.editorPage.getDialogNodeStructId(), DialogNode.createNewDialogNode(this.editorPage.getDialogBranchStructId())));
            this.editorPage.onAddNode(this.index + 1);
        });

        copy = new QMButton("", "#58135e");
        copy.setRipplerFill(Color.WHITE);
        copy.setGraphic(new WhiteFontIcon("fas-copy"));
        copy.setOnAction(event -> {
            dialogNodeList.add(this.index + 1, new Struct<>(this.editorPage.getDialogNodeStructId(), this.dialogNode.getValue().copy()));
            this.editorPage.onAddNode(this.index + 1);
        });

        remove = new QMButton("", "#bb5555");
        remove.setRipplerFill(Color.WHITE);
        remove.setGraphic(new WhiteFontIcon("fas-trash"));
        remove.setOnAction(event -> {
            List<String> errorList = new ArrayList<>();
            if (this.editorPage.getStartIndex() == this.index) {
                errorList.add("开始节点");
            }
            for (Struct<DialogNode> n : dialogNodeList) {
                if (n != this.dialogNode) {
                    DialogNode node = n.getValue();
                    switch (node.getOperation()) {
                        case JUMP_SENTENCE -> {
                            if (node.getJumpIndex() == this.index) {
                                errorList.add(String.valueOf(dialogNodeList.indexOf(n)));
                            }
                        }
                        case OPEN_SELECTION -> {
                            for (Struct<DialogBranch> dialogBranch : node.getDialogBranchList()) {
                                if (dialogBranch.getValue().getJumpIndex() == this.index) {
                                    errorList.add(String.valueOf(dialogNodeList.indexOf(n)));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!errorList.isEmpty()) {
                Utils.showChoosingDialog("安全删除", "该节点被其他节点的跳转逻辑所引用，若仍然删除则可能会使这些节点的跳转逻辑出现问题。\n相关节点编号：%s".formatted(String.join(",", errorList)),
                        "仍然删除", "取消", event1 -> {
                            dialogNodeList.remove(this.index);
                            this.editorPage.onRemoveNode(this.index);
                        }, event1 -> {}, QueueManager.INSTANCE.getMainScene().getRootDrawer());
            } else {
                dialogNodeList.remove(this.index);
                this.editorPage.onRemoveNode(this.index);
            }
        });
        remove.disable(dialogNodeList.size() <= 1);

        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(editOrSelect, add, copy, remove);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        main.setRight(right);

        root.getChildren().add(main);

        updateBranches();

        this.setGraphic(root);
    }

    public void updateFontIcon() {
        String iconCode = dialogNode.getValue().getOperation().getIconCode();
        if (dialogNode.getValue().getOperation() != DialogNode.Operation.JUMP_SENTENCE) {
            FontIcon fontIcon = new FontIcon(iconCode);
            fontIcon.setIconSize(20);
            this.operation = fontIcon;
        } else {
            HBox hb = new HBox(2);
            hb.setFillHeight(true);
            hb.setAlignment(Pos.CENTER_RIGHT);
            FontIcon fontIcon = new FontIcon(iconCode);
            fontIcon.setIconSize(20);
            Text text = new Text(String.valueOf(dialogNode.getValue().getJumpIndex()));
            text.setFont(Settings.DEFAULT_FONT);
            hb.getChildren().addAll(fontIcon, text);
            this.operation = hb;
        }
    }

    public void updateMainText() {
        this.mainTextInner.getChildren().clear();
        if (!dialogNode.getValue().isInheritPreviousTitleSettings()) {
            String titleText = dialogNode.getValue().getTitle();
            Text title = new Text(titleText.equals("<PLAYER_NAME>") ? "玩家" : titleText);
            title.setFont(Settings.BOLD_FONT);
            if (titleText.equals("<PLAYER_NAME>")) {
                title.setFill(Color.GOLDENROD);
            }
            this.mainTextInner.getChildren().add(title);
            if (dialogNode.getValue().isHasSubtitle()) {
                Text subtitle = new Text("（%s）".formatted(dialogNode.getValue().getSubtitle()));
                subtitle.setFont(Settings.SPEC_FONT);
                this.mainTextInner.getChildren().add(subtitle);
            }
        }
        Text content = new Text("「%s」".formatted(dialogNode.getValue().getContent()));
        content.setFont(Settings.DEFAULT_FONT);
        this.mainTextInner.getChildren().add(content);
        this.mainText.resetAnimation();
    }

    public void updateBranches() {
        if (root.getChildren().size() > 1) {
            root.getChildren().subList(1, root.getChildren().size()).clear();
        }
        if (getDialogNode().getOperation() == DialogNode.Operation.OPEN_SELECTION) {
            int branchSize = getDialogNode().getDialogBranchList().size();
            if (branchSize == 0) {
                BorderPane borderPane = new BorderPane();
                borderPane.setPadding(new Insets(0, 0, 0, 20));
                HBox left = new HBox();
                left.setPrefWidth(120);
                Text id = new Text("暂无分支");
                id.setFont(Settings.DEFAULT_FONT);
                left.getChildren().add(id);
                borderPane.setLeft(left);
                root.getChildren().add(borderPane);
            }
            for (int i = 0; i < branchSize; i++) {
                BorderPane borderPane = new BorderPane();
                borderPane.setPadding(new Insets(2, 0, 2, 20));
                HBox left = new HBox();
                left.setPrefWidth(120);
                HBox id = new HBox(2);
                id.setAlignment(Pos.CENTER_LEFT);
                id.getChildren().add(new FontIcon("fas-th-list"));
                Text idText = new Text(String.valueOf(i + 1));
                idText.setFont(Settings.DEFAULT_FONT);
                id.getChildren().add(idText);
                id.setPadding(new Insets(0, 5, 0, 5));
                DialogBranch branch = getDialogNode().getDialogBranchList().get(i).getValue();
                HBox jump = new HBox(2);
                jump.setAlignment(Pos.CENTER_LEFT);
                jump.getChildren().add(new FontIcon("fas-map-marker-alt"));
                Text jumpText = new Text(String.valueOf(branch.getJumpIndex()));
                jumpText.setFont(Settings.DEFAULT_FONT);
                jump.getChildren().add(jumpText);
                jump.setPadding(new Insets(0, 5, 0, 5));
                left.getChildren().addAll(id, jump);
                BorderPane.setMargin(left, new Insets(0, 5, 0, 0));
                borderPane.setLeft(left);
                ScrollableText value = new ScrollableText(branch.getTitle(), 550);
                value.setBackDuration(0.01);
                BorderPane.setAlignment(value, Pos.CENTER_LEFT);
                borderPane.setCenter(value);

                root.getChildren().add(borderPane);
            }
        }
    }

    public void selectMode(boolean selectMode) {
        if (selectMode) {
            if (this.dialogNode == editorPage.getCurrentEditor().getBindNode()) {
                editOrSelect.setStyle("-fx-background-color: #bb5555");
                editOrSelect.setGraphic(new WhiteFontIcon("fas-redo"));
            } else {
                editOrSelect.setStyle("-fx-background-color: #55bb55");
                editOrSelect.setGraphic(new WhiteFontIcon("fas-check"));
            }

        } else {
            editOrSelect.setStyle("-fx-background-color: #1a8bcc");
            editOrSelect.setGraphic(new WhiteFontIcon("fas-pen"));
        }
        add.disable(selectMode);
        copy.disable(selectMode);
        remove.disable(selectMode);
    }

    public QMButton getEditOrSelect() {
        return editOrSelect;
    }

    private DialogNode getDialogNode() {
        return this.dialogNode.getValue();
    }

}
