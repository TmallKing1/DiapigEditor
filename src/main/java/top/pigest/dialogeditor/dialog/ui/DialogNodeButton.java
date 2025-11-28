package top.pigest.dialogeditor.dialog.ui;

import com.jfoenix.controls.JFXDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.dialogeditor.DialogEditor;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.control.QMButton;
import top.pigest.dialogeditor.control.ScrollablePane;
import top.pigest.dialogeditor.control.ScrollableText;
import top.pigest.dialogeditor.control.WhiteFontIcon;
import top.pigest.dialogeditor.dialog.DialogBranch;
import top.pigest.dialogeditor.dialog.DialogNode;
import top.pigest.dialogeditor.richtext.TextParser;
import top.pigest.dialogeditor.util.Utils;
import top.pigest.dialogeditor.util.gi.Struct;
import top.pigest.dialogeditor.util.gi.StructList;

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
    private final QMButton indexButton;
    private final QMButton operationButton;
    private Node operation;
    private final ScrollablePane<HBox> mainText;
    private final List<ScrollablePane<HBox>> branchTexts = new ArrayList<>();
    private final int index;

    private DialogEditorPage.EditMode editMode = DialogEditorPage.EditMode.FREE;

    public void clean() {
        this.root.getChildren().clear();
        this.editOrSelect.setOnAction(null);
        this.add.setOnAction(null);
        this.copy.setOnAction(null);
        this.remove.setOnAction(null);
        this.mainTextInner.getChildren().clear();
        this.mainText.clean(hBox -> hBox.getChildren().clear());
        this.branchTexts.forEach(text -> text.clean(text1 -> {}));
    }

    public DialogNodeButton(int index, Struct<DialogNode> dialogNode, StructList<DialogNode> dialogNodeList, DialogEditorPage editorPage) {
        super("", null, false);
        this.index = index;
        this.dialogNode = dialogNode;
        this.editorPage = editorPage;

        this.setRipplerFill(Color.DARKGRAY);

        root = new VBox();
        root.setFillWidth(true);

        BorderPane main = new BorderPane();
        HBox left = new HBox(10);
        left.setAlignment(Pos.CENTER);
        indexButton = new QMButton(String.valueOf(index), "#000000");
        indexButton.setPrefWidth(60);
        indexButton.setFont(Settings.BOLD_FONT);
        indexButton.setWrapText(false);
        indexButton.setOnAction(event -> {
            DialogDataEditor content = this.editorPage.getCurrentEditor();
            JFXDialog dialog;
            if (content == null) {
                if (this.editMode == DialogEditorPage.EditMode.MOVE) {
                    this.editorPage.selectMode(DialogEditorPage.EditMode.FREE, "");
                    if (this.editorPage.getMovingIndex() != this.index) {
                        this.editorPage.move(this.editorPage.getMovingNode(), this.index);
                    }
                    return;
                }
                VBox vBox = new VBox(5);
                dialog = new JFXDialog(DialogEditor.INSTANCE.getMainScene().getRootDrawer(), vBox, JFXDialog.DialogTransition.CENTER);
                vBox.setStyle("-fx-background-color: #26282b");
                vBox.setPrefWidth(360);
                vBox.setAlignment(Pos.CENTER);
                vBox.setPadding(new Insets(20, 20, 20, 20));
                Text titleNode = new Text("节点编号：" + this.index);
                titleNode.setTextAlignment(TextAlignment.CENTER);
                titleNode.setFont(new Font(Settings.BOLD_FONT.getFamily(), 30));
                titleNode.setFill(Color.LIGHTGRAY);
                VBox.setMargin(titleNode, new Insets(0, 0, 10, 0));
                vBox.getChildren().add(titleNode);

                QMButton move = new QMButton("移动", null);
                move.setPrefWidth(100);
                move.setTextAlignment(TextAlignment.LEFT);
                move.setOnAction(event1 -> {
                    this.editorPage.setMovingNode(this.dialogNode);
                    this.editorPage.selectMode(DialogEditorPage.EditMode.MOVE, "请选择移动位置（选择自身以取消移动）");
                    dialog.close();
                });

                QMButton copy = new QMButton("复制", null);
                copy.setPrefWidth(100);
                copy.setTextAlignment(TextAlignment.LEFT);
                copy.setOnAction(event1 -> {
                    this.editorPage.copyToClipboard(this.dialogNode);
                    dialog.close();
                });

                QMButton paste = new QMButton("粘贴", null);
                paste.setPrefWidth(100);
                paste.disable(this.editorPage.getClipboard() == null);
                paste.setTextAlignment(TextAlignment.LEFT);
                paste.setOnAction(event1 -> {
                    this.editorPage.getClipboard().getValue().copyTo(this.dialogNode.getValue());
                    this.editorPage.updateStackDisplay(true);
                    dialog.close();
                });
                vBox.getChildren().addAll(move, copy, paste);
                dialog.show();
            } else {
                content.receiveBindNode(dialogNode, this.index);
                if (content instanceof DialogNodeEditor) {
                    dialog = new JFXDialog(DialogEditor.INSTANCE.getMainScene().getRootDrawer(), (DialogNodeEditor) content, JFXDialog.DialogTransition.CENTER);
                }
                else {
                    dialog = new JFXDialog(DialogEditor.INSTANCE.getMainScene().getRootDrawer(), (DialogMetaEditor) content, JFXDialog.DialogTransition.CENTER);
                }
                dialog.setOverlayClose(false);
                content.postProcess(dialog);
            }
        });
        operationButton = new QMButton("", Utils.colorToString(Color.DARKCYAN));
        operationButton.setRipplerFill(Color.WHITE);
        if (dialogNode == null) {
            operationButton.setPrefWidth(80);
            operationButton.setGraphic(new WhiteFontIcon("fas-check-circle"));
        } else {
            String enterEvent = this.dialogNode.getValue().getEnterEvent();
            String leaveEvent = this.dialogNode.getValue().getLeaveEvent();
            operationButton.setStyle("-fx-background-color: linear-gradient(to right, %s, %s);"
                    .formatted(Utils.colorToString(enterEvent.isEmpty() ? Color.DARKCYAN : Color.DARKVIOLET),
                            Utils.colorToString(leaveEvent.isEmpty() ? Color.DARKCYAN : Color.DARKVIOLET)));
            operationButton.setTooltipOpt(Utils.createTooltip(
                    enterEvent.isEmpty() && leaveEvent.isEmpty() ?
                            "编辑节点事件与操作" : !enterEvent.isEmpty() && !leaveEvent.isEmpty() ? "进入事件：%s\n离开事件：%s".formatted(enterEvent, leaveEvent) :
                            enterEvent.isEmpty() ? "离开事件：%s".formatted(leaveEvent) : "进入事件：%s".formatted(enterEvent)
            ));
            operationButton.setPrefWidth(80);
            operationButton.setOnAction(event -> {
                DialogDataEditor content = this.editorPage.getCurrentEditor();
                JFXDialog dialog;
                if (content == null) {
                    content = new DialogNodeEditor(dialogNode, this.index, editorPage, false);
                    dialog = new JFXDialog(DialogEditor.INSTANCE.getMainScene().getRootDrawer(), (DialogNodeEditor) content, JFXDialog.DialogTransition.CENTER);
                    dialog.setOverlayClose(false);
                    editorPage.setCurrentEditor(content);
                    content.postProcess(dialog);
                }
            });
            updateOperation();
        }
        left.getChildren().addAll(indexButton, operationButton);
        BorderPane.setAlignment(left, Pos.CENTER_LEFT);
        BorderPane.setMargin(left, new Insets(0, 5, 0, 0));
        main.setLeft(left);

        mainTextInner = new HBox();
        mainTextInner.setAlignment(Pos.CENTER_LEFT);
        mainText = new ScrollablePane<>(mainTextInner, this.editorPage.getScene().getWindow().getWidth() - 430);
        mainText.setBackDuration(0.01);
        updateMainText();
        BorderPane.setAlignment(mainText, Pos.CENTER_LEFT);
        BorderPane.setMargin(mainText, new Insets(0, 5, 0, 5));
        main.setCenter(mainText);

        HBox right = new HBox(5);

        editOrSelect = new QMButton("", "#1a8bcc");
        editOrSelect.setTooltipOpt(Utils.createTooltip("编辑节点文本"));
        editOrSelect.setRipplerFill(Color.WHITE);
        editOrSelect.setGraphic(new WhiteFontIcon("fas-pen"));
        if (dialogNode != null) {
            editOrSelect.setOnAction(event -> {
                DialogDataEditor content = this.editorPage.getCurrentEditor();
                JFXDialog dialog;
                if (content == null) {
                    content = new DialogNodeEditor(dialogNode, this.index, editorPage, true);
                    dialog = new JFXDialog(DialogEditor.INSTANCE.getMainScene().getRootDrawer(), (DialogNodeEditor) content, JFXDialog.DialogTransition.CENTER);
                    dialog.setOverlayClose(false);
                    editorPage.setCurrentEditor(content);
                    content.postProcess(dialog);
                }
            });
        }

        add = new QMButton("", "#55bb55");
        add.setTooltipOpt(Utils.createTooltip("在此节点后添加新节点"));
        add.setRipplerFill(Color.WHITE);
        add.setGraphic(new WhiteFontIcon("fas-plus"));
        if (dialogNodeList != null) {
            add.setOnAction(event -> {
                dialogNodeList.add(this.index + 1, new Struct<>(this.editorPage.getDialogNodeStructId(), DialogNode.createNewDialogNode(this.editorPage.getDialogBranchStructId())));
                this.editorPage.onAddNode(this.index + 1);
            });
        }

        copy = new QMButton("", "#58135e");
        copy.setTooltipOpt(Utils.createTooltip("在此节点后添加一份此节点的复制"));
        copy.setRipplerFill(Color.WHITE);
        copy.setGraphic(new WhiteFontIcon("fas-copy"));
        if (dialogNodeList != null && dialogNode != null) {
            copy.setOnAction(event -> {
                dialogNodeList.add(this.index + 1, new Struct<>(this.editorPage.getDialogNodeStructId(), this.dialogNode.getValue().copy()));
                this.editorPage.onAddNode(this.index + 1);
            });
        }

        remove = new QMButton("", "#bb5555");
        remove.setTooltipOpt(Utils.createTooltip("删除此节点"));
        remove.setRipplerFill(Color.WHITE);
        remove.setGraphic(new WhiteFontIcon("fas-trash"));
        if (dialogNodeList != null) {
            remove.setOnAction(event -> {
                List<String> errorList = new ArrayList<>();
                if (this.editorPage.getStartIndex() == this.index) {
                    errorList.add("开始节点");
                }
                Struct<DialogNode> clip1 = this.editorPage.getClipboard();
                if (clip1 != null) {
                    DialogNode clip = clip1.getValue();
                    switch (clip.getOperation()) {
                        case JUMP_SENTENCE -> {
                            if (clip.getJumpIndex() == this.index) {
                                errorList.add("剪贴板节点");
                            }
                        }
                        case OPEN_SELECTION -> {
                            for (Struct<DialogBranch> dialogBranch : clip.getDialogBranchList()) {
                                if (dialogBranch.getValue().getJumpIndex() == this.index) {
                                    errorList.add("剪贴板节点");
                                    break;
                                }
                            }
                        }
                    }
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
                    Utils.showChoosingDialog("安全删除", "该节点已被引用，若仍然删除则可能会使这些节点的跳转逻辑或开始节点出现问题。\n相关节点编号：%s".formatted(String.join(",", errorList)),
                            "仍然删除", "取消", event1 -> {
                                dialogNodeList.remove(this.index);
                                this.editorPage.onRemoveNode(this.index);
                            }, event1 -> {}, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                } else {
                    dialogNodeList.remove(this.index);
                    this.editorPage.onRemoveNode(this.index);
                }
            });
            remove.disable(dialogNodeList.size() <= 1);
        } else {
            remove.disable(true);
        }

        right.setAlignment(Pos.CENTER_RIGHT);
        right.getChildren().addAll(editOrSelect, add, copy, remove);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        main.setRight(right);

        root.getChildren().add(main);

        if (dialogNode != null) {
            updateBranches();
        }

        this.setGraphic(root);
    }

    public void updateOperation() {
        String iconCode = dialogNode.getValue().getOperation().getIconCode();
        if (dialogNode.getValue().getOperation() != DialogNode.Operation.JUMP_SENTENCE) {
            WhiteFontIcon fontIcon = new WhiteFontIcon(iconCode);
            fontIcon.setIconSize(20);
            this.operation = fontIcon;
        } else {
            HBox hb = new HBox(2);
            hb.setFillHeight(true);
            hb.setAlignment(Pos.CENTER);
            WhiteFontIcon fontIcon = new WhiteFontIcon(iconCode);
            fontIcon.setIconSize(20);
            Text text = new Text(String.valueOf(dialogNode.getValue().getJumpIndex()));
            text.setFill(Color.WHITE);
            text.setFont(Settings.DEFAULT_FONT);
            hb.getChildren().addAll(fontIcon, text);
            this.operation = hb;
        }
        this.operationButton.setGraphic(this.operation);
    }

    public void updateMainText() {
        this.mainTextInner.getChildren().clear();
        if (dialogNode != null && !dialogNode.getValue().isInheritPreviousTitleSettings()) {
            String titleText = dialogNode.getValue().getTitle();
            Text title = new Text(titleText.equals("<PLAYER_NAME>") ? "玩家" : titleText);
            title.setFont(Settings.BOLD_FONT);
            if (titleText.equals("<PLAYER_NAME>")) {
                title.setFill(Color.valueOf("#ffcc33"));
            } else {
                title.setFill(Color.WHITE);
            }
            this.mainTextInner.getChildren().add(title);
            if (dialogNode.getValue().isHasSubtitle()) {
                Text subtitle = new Text("（%s）".formatted(dialogNode.getValue().getSubtitle()));
                subtitle.setFont(Settings.CODE_FONT);
                subtitle.setFill(Color.WHITE);
                this.mainTextInner.getChildren().add(subtitle);
            }
        }
        Text sp = new Text("移动到最后");
        sp.setFont(Settings.DEFAULT_FONT);
        sp.setFill(Color.WHITE);
        List<Text> contents = dialogNode == null ? List.of(sp) : TextParser.parseText("「%s」".formatted(
                dialogNode.getValue().getTextMethod() == DialogNode.TextMethod.CUSTOM ?
                        dialogNode.getValue().getContent().replace("|", "") : dialogNode.getValue().getContent()), s -> Utils.showDialogMessage(s, true, DialogEditor.INSTANCE.getMainScene().getRootDrawer()));
        this.mainTextInner.getChildren().addAll(contents);
        this.mainText.resetAnimation();
    }

    public void updateBranches() {
        if (root.getChildren().size() > 1) {
            root.getChildren().subList(1, root.getChildren().size()).clear();
            branchTexts.clear();
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
                VBox.setMargin(borderPane, new Insets(10, 0, 0, 0));
                root.getChildren().add(borderPane);
            }
            for (int i = 0; i < branchSize; i++) {
                BorderPane borderPane = new BorderPane();
                borderPane.setPadding(new Insets(2, 0, 2, 20));
                DialogBranch branch = getDialogNode().getDialogBranchList().get(i).getValue();

                HBox left = new HBox();
                left.setPrefWidth(120);

                HBox id = new HBox(2);
                id.setAlignment(Pos.CENTER_LEFT);
                FontIcon e;
                Text idText = new Text(String.valueOf(i + 1));
                if (i == getDialogNode().getDefaultBranch()) {
                    e = new WhiteFontIcon("fas-star-of-david");
                    e.setIconColor(Color.valueOf("#ffcc33"));
                    idText.setFill(Color.valueOf("#ffcc33"));
                } else {
                    e = new WhiteFontIcon("fas-th-list");
                    e.setIconColor(Color.WHITE);
                    idText.setFill(Color.WHITE);
                }
                idText.setFont(Settings.DEFAULT_FONT);
                id.getChildren().add(e);
                id.getChildren().add(idText);
                id.setPadding(new Insets(0, 5, 0, 5));

                HBox jump = new HBox(2);
                jump.setAlignment(Pos.CENTER_LEFT);
                jump.getChildren().add(new WhiteFontIcon("fas-map-marker-alt"));
                Text jumpText = new Text(String.valueOf(branch.getJumpIndex()));
                jumpText.setFont(Settings.DEFAULT_FONT);
                jumpText.setFill(Color.WHITE);
                jump.getChildren().add(jumpText);
                jump.setPadding(new Insets(0, 5, 0, 5));
                left.getChildren().addAll(id, jump);
                BorderPane.setMargin(left, new Insets(0, 5, 0, 0));
                borderPane.setLeft(left);

                ScrollablePane<HBox> value = new ScrollablePane<>(new HBox(), this.editorPage.getWidth() - 250, false);
                value.getChildren().addAll(TextParser.parseText(branch.getTitle(), s -> Utils.showDialogMessage(s, true, DialogEditor.INSTANCE.getMainScene().getRootDrawer())));
                value.setBackDuration(0.01);
                value.setAlignment(Pos.CENTER_LEFT);
                branchTexts.add(value);
                BorderPane.setAlignment(value, Pos.CENTER_LEFT);
                borderPane.setCenter(value);

                VBox.setMargin(borderPane, new Insets(10, 0, 0, 0));
                root.getChildren().add(borderPane);
            }
        }
    }

    public void selectMode(DialogEditorPage.EditMode editMode) {
        this.editMode = editMode;
        switch (editMode) {
            case FREE -> {
                indexButton.setText(String.valueOf(index));
                indexButton.setStyle("-fx-background-color: #000000");
                indexButton.setGraphic(null);
                if (this.dialogNode != null) {
                    operationButton.disable(false);
                    String enterEvent = this.dialogNode.getValue().getEnterEvent();
                    String leaveEvent = this.dialogNode.getValue().getLeaveEvent();
                    operationButton.setStyle("-fx-background-color: linear-gradient(to right, %s, %s);"
                            .formatted(Utils.colorToString(enterEvent.isEmpty() ? Color.DARKCYAN : Color.DARKVIOLET),
                                    Utils.colorToString(leaveEvent.isEmpty() ? Color.DARKCYAN : Color.DARKVIOLET)));
                }
            }
            case SELECT -> {
                indexButton.setText("");
                if (this.dialogNode == editorPage.getCurrentEditor().getBindNode()) {
                    indexButton.setStyle("-fx-background-color: #bb5555");
                    indexButton.setGraphic(new WhiteFontIcon("fas-redo"));
                    indexButton.setTooltipOpt(Utils.createTooltip("取消节点选择"));
                } else {
                    indexButton.setStyle("-fx-background-color: #55bb55");
                    indexButton.setGraphic(new WhiteFontIcon("fas-check"));
                    indexButton.setTooltipOpt(Utils.createTooltip("选择此节点为目标节点"));
                }
                operationButton.disable(true);
            }
            case MOVE -> {
                indexButton.setText("");
                if (this.dialogNode == editorPage.getMovingNode()) {
                    indexButton.setStyle("-fx-background-color: #bb5555");
                    indexButton.setGraphic(new WhiteFontIcon("fas-redo"));
                    indexButton.setTooltipOpt(Utils.createTooltip("取消移动"));
                } else {
                    indexButton.setStyle("-fx-background-color: #55bb55");
                    indexButton.setGraphic(new WhiteFontIcon("fas-arrow-up"));
                    indexButton.setTooltipOpt(Utils.createTooltip(this.dialogNode == null ? "移动到最后" : "移动到此节点上方"));
                }
                operationButton.disable(true);
            }
        }
        editOrSelect.disable(editMode.isSelecting());
        add.disable(editMode.isSelecting());
        copy.disable(editMode.isSelecting());
        remove.disable(editMode.isSelecting());
    }

    public DialogNode getDialogNode() {
        if (this.dialogNode == null) {
            return null;
        }
        return this.dialogNode.getValue();
    }

    public void onWidthChanged(int width) {
        this.mainText.setDisplayWidth(width - 430);
        this.branchTexts.forEach(text -> text.setDisplayWidth(width - 250));
    }

}
