package top.pigest.dialogeditor.dialog.ui;

import com.jfoenix.controls.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import top.pigest.dialogeditor.DialogEditor;
import top.pigest.dialogeditor.Settings;
import top.pigest.dialogeditor.control.FloatModifier;
import top.pigest.dialogeditor.control.QMButton;
import top.pigest.dialogeditor.control.WhiteFontIcon;
import top.pigest.dialogeditor.dialog.DialogBranch;
import top.pigest.dialogeditor.dialog.DialogNode;
import top.pigest.dialogeditor.util.Utils;
import top.pigest.dialogeditor.util.gi.Struct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class DialogNodeEditor extends VBox implements DialogDataEditor {
    private final Struct<DialogNode> node;
    private final Struct<DialogNode> editingNode;
    private final DialogEditorPage editorPage;
    private final List<PostActionNode> postActionNodes = new ArrayList<>();

    /*private final QMButton textButton;
    private final QMButton logicButton;*/
    private final QMButton cancelButton;
    private final QMButton okButton;
    private final VBox center;
    private PostActionCheckBox inheritTitle;
    private HBox titleContainer;
    private PostActionTextField title;
    private QMButton usePlayerName;
    private HBox subtitleContainer;
    private PostActionCheckBox subtitleCheckbox;
    private PostActionTextField subtitle;
    private PostActionTextArea content;
    private BorderPane textMethodContainer;
    private PostActionComboBox<DialogNode.TextMethod> textMethodSelector;
    private HBox eventEditorPane;
    private BorderPane durationModifierContainer;
    private BorderPane operation;
    private PostActionComboBox<DialogNode.Operation> operationSelector;
    private BorderPane jumpNode;
    private PostActionButtonSelector jumpNodeSelector;
    private BorderPane branchEditHead;
    private VBox branchList;

    private final boolean isEditingText;
    private int currentRequestBranchId = 0;
    private final int index;
    private JFXDialog dialog;

    public DialogNodeEditor(Struct<DialogNode> node, int index, DialogEditorPage editorPage, boolean isEditingText1) {
        this.node = node;
        this.editingNode = new Struct<>(node.getStructId(), node.getValue().copy());
        this.index = index;
        this.isEditingText = isEditingText1;
        this.editorPage = editorPage;

        this.setStyle("-fx-background-color: #26282b");
        this.setPrefWidth(640);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 20, 20, 20));
        Text titleNode = new Text(isEditingText ? "编辑对话内容" : "编辑节点逻辑");
        titleNode.setTextAlignment(TextAlignment.CENTER);
        titleNode.setFont(new Font(Settings.BOLD_FONT.getFamily(), 30));
        titleNode.setFill(Color.LIGHTGRAY);
        VBox.setMargin(titleNode, new Insets(0, 0, 15, 0));
        this.getChildren().add(titleNode);

        /*HBox pageSelection = new HBox(40);
        pageSelection.setAlignment(Pos.CENTER);
        textButton = new QMButton("编辑文本内容", "#1f1e33");
        textButton.setPrefWidth(150);
        logicButton = new QMButton("编辑节点逻辑", "#999999");
        logicButton.setPrefWidth(150);
        textButton.setOnAction(event -> {
            if (!isEditingText) {
                isEditingText = true;
                textButton.setStyle("-fx-background-color: #1f1e33");
                logicButton.setStyle("-fx-background-color: #999999");
                updateCenter(isEditingText);
            }
        });
        logicButton.setOnAction(event -> {
            if (isEditingText) {
                isEditingText = false;
                logicButton.setStyle("-fx-background-color: #1f1e33");
                textButton.setStyle("-fx-background-color: #999999");
                updateCenter(isEditingText);
            }
        });
        pageSelection.getChildren().addAll(textButton, logicButton);
        VBox.setMargin(pageSelection, new Insets(0, 0, 15, 0));
        this.getChildren().add(pageSelection);*/

        center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setPrefWidth(560);
        center.setMinWidth(USE_PREF_SIZE);
        center.setMaxWidth(USE_PREF_SIZE);

        updateCenter(isEditingText);

        VBox.setMargin(center, new Insets(0, 0, 20, 0));
        this.getChildren().add(center);

        HBox saveOrCancel = new HBox(40);
        saveOrCancel.setAlignment(Pos.CENTER);
        okButton = new QMButton("保存", QMButton.DEFAULT_COLOR);
        okButton.setPrefWidth(80);
        cancelButton = new QMButton("取消", "#bb5555");
        cancelButton.setPrefWidth(80);
        saveOrCancel.getChildren().addAll(okButton, cancelButton);
        this.getChildren().add(saveOrCancel);
    }

    private void updateCenter(boolean editingText) {
        center.getChildren().clear();
        if (editingText) {
            if (inheritTitle == null) {
                inheritTitle = new PostActionCheckBox(this.editingNode.getValue().isInheritPreviousTitleSettings(), "是否继承上一段对话标题信息（不覆写标题）", this.editingNode.getValue()::setInheritPreviousTitleSettings);
                inheritTitle.setOnAction(event -> {
                    title.setDisable(inheritTitle.isSelected());
                    usePlayerName.disable(inheritTitle.isSelected());
                    subtitleCheckbox.setDisable(inheritTitle.isSelected());
                    subtitle.setDisable(inheritTitle.isSelected() || !subtitleCheckbox.isSelected());
                });
            }
            center.getChildren().add(inheritTitle);

            if (titleContainer == null) {
                titleContainer = new HBox(5);
                titleContainer.setAlignment(Pos.BOTTOM_CENTER);
                title = new PostActionTextField(this.editingNode.getValue().getTitle(), "对话标题（说话人）", this.editingNode.getValue()::setTitle);
                title.setPrefWidth(999);
                title.setLabelFloat(true);
                title.setPadding(new Insets(45, 0, 0, 0));
                usePlayerName = new QMButton("使用玩家名", Utils.colorToString(Color.GOLDENROD));
                usePlayerName.setMinWidth(150);
                usePlayerName.setOnAction(event -> title.setText("<PLAYER_NAME>"));
                titleContainer.getChildren().addAll(title,  usePlayerName);
            }
            center.getChildren().add(titleContainer);

            if (subtitleContainer == null) {
                subtitleContainer = new HBox(5);
                subtitleContainer.setAlignment(Pos.BOTTOM_LEFT);
                if (subtitleCheckbox == null) {
                    subtitleCheckbox = new PostActionCheckBox(this.editingNode.getValue().isHasSubtitle(), "启用副标题", this.editingNode.getValue()::setHasSubtitle);
                    subtitleCheckbox.setOnAction(event -> subtitle.setDisable(inheritTitle.isSelected() || !subtitleCheckbox.isSelected()));
                }
                if (subtitle == null) {
                    subtitle = new PostActionTextField(this.editingNode.getValue().getSubtitle(), "副标题（身份）", this.editingNode.getValue()::setSubtitle);
                    subtitle.setPrefWidth(999);
                    subtitle.setLabelFloat(true);
                    subtitle.setPadding(new Insets(45, 0, 0, 0));
                }
                subtitleContainer.getChildren().addAll(subtitleCheckbox, subtitle);
            }
            center.getChildren().add(subtitleContainer);

            if (content == null) {
                content = new PostActionTextArea(this.editingNode.getValue().getContent(), "对话内容", this.editingNode.getValue()::setContent);
                content.setPrefHeight(180);
                content.setLabelFloat(true);
                content.setPadding(new Insets(45, 0, 0, 0));
            }
            center.getChildren().add(content);

            if (textMethodContainer == null) {
                textMethodSelector = new PostActionComboBox<>(
                        this.editingNode.getValue().getTextMethod(),
                        List.of(DialogNode.TextMethod.values()),
                        this.editingNode.getValue()::setTextMethod
                );
                textMethodSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                    if (oldValue != newValue) {
                        String s = content.getText();
                        if (newValue == DialogNode.TextMethod.CUSTOM) {
                            Utils.showDialogMessage("请将每个显现文本块以 | 分隔", false, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                            if (!s.contains("|")) {
                                s = String.join("|", s.split(""));
                            }
                        }
                        if (oldValue == DialogNode.TextMethod.CUSTOM) {
                            s = s.replace("|", "");
                        }
                        content.setText(s);
                    }
                });
                textMethodContainer = createLRBorderPane("内容呈现方式", textMethodSelector);
                textMethodContainer.setPadding(new Insets(10, 0, 0, 0));
            }
            center.getChildren().add(textMethodContainer);

            title.setDisable(inheritTitle.isSelected());
            usePlayerName.disable(inheritTitle.isSelected());
            subtitleCheckbox.setDisable(inheritTitle.isSelected());
            subtitle.setDisable(inheritTitle.isSelected() || !subtitleCheckbox.isSelected());
        } else {
            if (eventEditorPane == null) {
                eventEditorPane = new HBox(10);
                PostActionTextField enterEvent = new PostActionTextField(this.editingNode.getValue().getEnterEvent(), "节点进入事件（留空则无事件）", this.editingNode.getValue()::setEnterEvent);
                enterEvent.setLabelFloat(true);
                enterEvent.setPrefWidth(300);
                PostActionTextField leaveEvent = new PostActionTextField(this.editingNode.getValue().getLeaveEvent(), "节点离开事件（留空则无事件）", this.editingNode.getValue()::setLeaveEvent);
                leaveEvent.setLabelFloat(true);
                leaveEvent.setPrefWidth(300);
                eventEditorPane.getChildren().addAll(enterEvent, leaveEvent);
                eventEditorPane.setPadding(new Insets(15, 0, 20, 0));
            }
            center.getChildren().add(eventEditorPane);
            if (durationModifierContainer == null) {
                FloatModifier durationModifier = new PostActionFloatModifier(this.editingNode.getValue().getDuration(), 0.1f, 0, 999, this.editingNode.getValue()::setDuration);
                durationModifierContainer = createLRBorderPane("持续时间", durationModifier);
                durationModifierContainer.setPadding(new Insets(0, 0, 10, 0));
            }
            center.getChildren().add(durationModifierContainer);
            if (operation == null) {
                operationSelector = new PostActionComboBox<>(
                        this.editingNode.getValue().getOperation(),
                        List.of(DialogNode.Operation.values()),
                        this.editingNode.getValue()::setOperation
                );
                operationSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                        updateSpecificOperation(newValue));
                operation = createLRBorderPane("操作类型", operationSelector);
                operation.setPadding(new Insets(0, 0, 10, 0));
            }
            center.getChildren().add(operation);
            updateSpecificOperation(this.editingNode.getValue().getOperation());
        }
    }

    private void updateSpecificOperation(DialogNode.Operation operation) {
        if (isEditingText) {
            return;
        }
        center.getChildren().subList(3, center.getChildren().size()).clear();
        switch (operation) {
            case JUMP_SENTENCE -> {
                if (jumpNode == null) {
                    jumpNodeSelector = new PostActionButtonSelector(this.editingNode.getValue().getJumpIndex(), "", this.editingNode.getValue()::setJumpIndex);
                    this.setButtonDisplay(jumpNodeSelector, this.editingNode.getValue().getJumpIndex());
                    jumpNodeSelector.setOnAction(event -> {
                        this.editorPage.selectMode(DialogEditorPage.EditMode.SELECT, "请选择跳转的节点（选择编辑中的节点以取消）");
                        dialog.close();
                    });
                    jumpNode = createLRBorderPane("跳转节点", jumpNodeSelector);
                }
                center.getChildren().add(jumpNode);
            }
            case OPEN_SELECTION -> {
                if (branchEditHead == null) {
                    QMButton addBranchButton = new QMButton(this.editingNode.getValue().getTitle());
                    addBranchButton.setStyle("-fx-background-color: #55bb55;");
                    addBranchButton.setText("添加分支");
                    addBranchButton.setGraphic(new WhiteFontIcon("fas-plus"));
                    addBranchButton.setOnAction(event -> addNewBranch());
                    branchEditHead = createLRBorderPane("分支列表", addBranchButton);
                }
                center.getChildren().add(branchEditHead);
                updateBranches();
            }
        }
    }

    private void updateBranches() {
        if (branchList == null) {
            branchList = new VBox();
            int id = 1;
            for (Struct<DialogBranch> branch : editingNode.getValue().getDialogBranchList()) {
                BranchView e = new BranchView(branch.getValue().getTitle(), id, branch.getValue().getJumpIndex());
                branchList.getChildren().add(e);
                if (id - 1 == this.editingNode.getValue().getDefaultBranch()) {
                    e.setDefaultBranch(true);
                }
                id++;
            }
        }
        center.getChildren().add(branchList);
    }

    private void addNewBranch() {
        if (DialogNodeEditor.this.branchList.getChildren().size() < 4) {
            BranchView e = new BranchView("", DialogNodeEditor.this.branchList.getChildren().size() + 1, index + 1);
            branchList.getChildren().add(e);
            if (branchList.getChildren().size() == 1) {
                e.setDefaultBranch(true);
            }
        } else {
            Utils.showDialogMessage("最多只能添加 4 个分支", true, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
        }
    }

    public BorderPane createLRBorderPane(String left, Node right) {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefWidth(999);
        Text titleNode = new Text(left);
        titleNode.setFill(Color.WHITE);
        titleNode.setFont(Settings.DEFAULT_FONT);
        BorderPane.setAlignment(titleNode, Pos.CENTER_LEFT);
        borderPane.setLeft(titleNode);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        borderPane.setRight(right);
        return borderPane;
    }

    private void setButtonDisplay(ButtonSelector button, int index) {
        HBox hb = new HBox(2);
        hb.setFillHeight(true);
        hb.setAlignment(Pos.CENTER);
        WhiteFontIcon fontIcon = new WhiteFontIcon("fas-map-marker-alt");
        fontIcon.setIconSize(20);
        Text text = new Text(String.valueOf(index));
        text.setFill(Color.WHITE);
        text.setFont(Settings.DEFAULT_FONT);
        hb.getChildren().addAll(fontIcon, text);
        button.setPrefWidth(80);
        button.setGraphic(hb);
    }

    @Override
    public void postProcess(JFXDialog dialog) {
        this.dialog = dialog;
        okButton.setOnAction(event -> {
            if (this.isEditingText) {
                switch (this.textMethodSelector.getValue()) {
                    case TYPEWRITER -> {
                        if (this.content.getText().contains("</color>") || this.content.getText().contains("<color=")) {
                            Utils.showDialogMessage("打字机呈现的内容不可带有颜色标签", true, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                            return;
                        }
                        if (this.content.getText().length() > 87) {
                            Utils.showDialogMessage("打字机呈现的内容字数不得大于 87 字", true, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                            return;
                        }
                    }
                    case CUSTOM -> {
                        if (this.content.getText().split("\\|").length > 87) {
                            Utils.showDialogMessage("自定义打字机的文本块数量不得大于 87", true, DialogEditor.INSTANCE.getMainScene().getRootDrawer());
                            return;
                        }
                    }
                }
            }
            this.postActionNodes.forEach(PostActionNode::postProcess);
            if (this.branchList != null) {
                this.editingNode.getValue().getDialogBranchList().clear();
                this.branchList.getChildren().forEach(branch -> {
                    if (branch instanceof BranchView branchView) {
                        this.editingNode.getValue().getDialogBranchList().addValue(new DialogBranch(branchView.textField.getText(), branchView.buttonSelector.getValue()));
                        if (branchView.isDefaultBranch) {
                            this.editingNode.getValue().setDefaultBranch(branchList.getChildren().indexOf(branch));
                        }
                    }
                });
            }
            this.editingNode.getValue().copyTo(this.node.getValue());
            this.editorPage.updateStackDisplay(true);
            this.editorPage.setCurrentEditor(null);
            dialog.close();
        });
        cancelButton.setOnAction(event -> {
            this.editorPage.setCurrentEditor(null);
            dialog.close();
        });
        dialog.show();
    }

    @Override
    public void receiveBindNode(Struct<DialogNode> dialogNode, int index) {
        this.editorPage.selectMode(DialogEditorPage.EditMode.FREE, "");
        if (dialogNode == this.node) {
            return;
        }
        if (operationSelector.getValue() == DialogNode.Operation.JUMP_SENTENCE) {
            if (jumpNodeSelector != null) {
                jumpNodeSelector.setValue(index);
                this.setButtonDisplay(jumpNodeSelector, index);
            }
        } else if (operationSelector.getValue() == DialogNode.Operation.OPEN_SELECTION) {
            int p = currentRequestBranchId - 1;
            if (p < branchList.getChildren().size()) {
                BranchView branchView = ((BranchView) branchList.getChildren().get(p));
                branchView.buttonSelector.setValue(index);
                this.setButtonDisplay(branchView.buttonSelector, index);
            }
        }
    }

    @Override
    public Struct<DialogNode> getBindNode() {
        return node;
    }

    private class BranchView extends BorderPane {
        private final JFXTextField textField;
        private final ButtonSelector buttonSelector;
        private final QMButton removeButton;
        private final QMButton idButton;
        private int id;
        private boolean isDefaultBranch = false;

        public BranchView(String text, int id, int index) {
            this.setPadding(new Insets(5, 0, 5, 0));
            this.id = id;
            idButton = new QMButton(String.valueOf(this.id));
            idButton.setOnAction(event -> branchList.getChildren().forEach(branch -> {
                if (branch instanceof BranchView branchView) {
                    branchView.setDefaultBranch(branchView == this);
                }
            }));
            idButton.setTooltipOpt(Utils.createTooltip("点击设置为默认分支"));
            idButton.setPrefWidth(50);
            resetDefaultBranch();
            BorderPane.setMargin(idButton, new Insets(0, 5, 0, 0));
            BorderPane.setAlignment(idButton, Pos.CENTER_LEFT);
            this.setLeft(idButton);

            this.textField = new JFXTextField(text);
            this.textField.setUnFocusColor(Color.LIGHTGRAY);
            this.textField.setFocusColor(Color.AQUA);
            this.textField.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray");
            this.textField.setPromptText("分支文本");
            this.textField.setFont(Settings.DEFAULT_FONT);
            BorderPane.setMargin(this.textField, new Insets(0, 5, 0, 0));
            BorderPane.setAlignment(this.textField, Pos.CENTER_LEFT);
            this.setCenter(this.textField);

            HBox right = new HBox(5);
            right.setAlignment(Pos.CENTER_RIGHT);
            this.buttonSelector = new ButtonSelector(index, "");
            DialogNodeEditor.this.setButtonDisplay(buttonSelector, index);
            this.buttonSelector.setOnAction(event -> {
                DialogNodeEditor.this.currentRequestBranchId = this.id;
                DialogNodeEditor.this.editorPage.selectMode(DialogEditorPage.EditMode.SELECT, "请选择跳转的节点（选择编辑中的节点以取消）");
                dialog.close();
            });
            this.removeButton = new QMButton("", "#bb5555");
            this.removeButton.setGraphic(new WhiteFontIcon("fas-trash"));
            this.removeButton.setOnAction(event -> {
                this.idButton.setOnAction(null);
                this.buttonSelector.setOnAction(null);
                this.removeButton.setOnAction(null);
                ObservableList<Node> children = DialogNodeEditor.this.branchList.getChildren();
                for (int i = this.id; i < children.size(); i++) {
                    ((BranchView) children.get(i)).id--;
                    ((BranchView) children.get(i)).updateId();
                }
                children.remove(this);
                if (this.isDefaultBranch && !children.isEmpty()) {
                    ((BranchView) children.getFirst()).setDefaultBranch(true);
                }
            });
            right.getChildren().addAll(this.buttonSelector, this.removeButton);
            BorderPane.setAlignment(this.removeButton, Pos.CENTER_RIGHT);
            this.setRight(right);
        }

        public boolean isDefaultBranch() {
            return isDefaultBranch;
        }

        public void setDefaultBranch(boolean isDefaultBranch) {
            this.isDefaultBranch = isDefaultBranch;
            if (isDefaultBranch) {
                FontIcon fontIcon = new FontIcon("fas-star-of-david:20");
                fontIcon.setIconColor(Color.valueOf("#ffcc33"));
                idButton.setBackgroundColor("#8b0000");
                idButton.setGraphic(fontIcon);
                idButton.setTextFill(Color.valueOf("#ffcc33"));
            } else {
                resetDefaultBranch();
            }
        }

        public void resetDefaultBranch() {
            FontIcon fontIcon = new WhiteFontIcon("fas-th-list:20");
            idButton.setBackgroundColor("#1f1e33");
            idButton.setGraphic(fontIcon);
            idButton.setTextFill(Color.WHITE);
        }

        public void updateId() {
            this.idButton.setText(String.valueOf(this.id));
        }
    }

    protected class PostActionTextField extends JFXTextField implements PostActionNode {
        private final Consumer<String> onAction;

        public PostActionTextField(String initialText, String prompt, Consumer<String> onAction) {
            super(initialText);
            this.setUnFocusColor(Color.LIGHTGRAY);
            this.setFocusColor(Color.AQUA);
            this.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
            this.setPromptText(prompt);
            this.setFont(Settings.DEFAULT_FONT);
            this.onAction = onAction;
            DialogNodeEditor.this.postActionNodes.add(this);
        }

        @Override
        public void postProcess() {
            onAction.accept(this.getText());
        }
    }

    protected class PostActionTextArea extends JFXTextArea implements PostActionNode {
        private final Consumer<String> onAction;

        public PostActionTextArea(String initialText, String prompt, Consumer<String> onAction) {
            super(initialText);
            this.setUnFocusColor(Color.LIGHTGRAY);
            this.setFocusColor(Color.AQUA);
            this.setStyle("-fx-text-fill: white; -fx-prompt-text-fill: lightgray;");
            this.getStylesheets().add(Objects.requireNonNull(DialogEditor.class.getResource("css/scrollbar.css")).toExternalForm());
            this.setPromptText(prompt);
            this.setFont(Settings.DEFAULT_FONT);
            this.onAction = onAction;
            DialogNodeEditor.this.postActionNodes.add(this);
        }

        @Override
        public void postProcess() {
            onAction.accept(this.getText());
        }
    }

    protected class PostActionCheckBox extends JFXCheckBox implements PostActionNode {
        private final Consumer<Boolean> onAction;

        public PostActionCheckBox(boolean initialValue, String prompt, Consumer<Boolean> onAction) {
            super(prompt);
            this.setFont(Settings.DEFAULT_FONT);
            this.setTextFill(Color.WHITE);
            this.setSelected(initialValue);
            this.onAction = onAction;
            DialogNodeEditor.this.postActionNodes.add(this);
        }

        @Override
        public void postProcess() {
            onAction.accept(this.isSelected());
        }
    }

    protected class PostActionComboBox<T> extends JFXComboBox<T> implements PostActionNode {
        private final Consumer<T> onAction;

        public PostActionComboBox(T initialValue, Collection<T> values, Consumer<T> onAction) {
            this.getStylesheets().add(Objects.requireNonNull(DialogEditor.class.getResource("css/combobox.css")).toExternalForm());
            this.getStylesheets().add(Objects.requireNonNull(DialogEditor.class.getResource("css/scrollbar.css")).toExternalForm());
            this.onAction = onAction;
            this.setCellFactory(new Callback<>() {
                @Override
                public ListCell<T> call(ListView<T> param) {
                    return new ListCell<>() {
                        @Override
                        public void updateItem(T item, boolean empty) {
                            super.updateItem(item, empty);
                            this.setFont(Settings.DEFAULT_FONT);
                            if (item != null && !empty) {
                                this.setText(item.toString());
                            }
                        }
                    };
                }
            });
            this.getItems().addAll(values);
            this.getButtonCell().setFont(Settings.DEFAULT_FONT);
            this.getButtonCell().setTextFill(Color.WHITE);
            this.setUnFocusColor(Color.LIGHTGRAY);
            this.setFocusColor(Color.AQUA);
            this.setValue(initialValue);
            DialogNodeEditor.this.postActionNodes.add(this);
        }

        @Override
        public void postProcess() {
            onAction.accept(this.getValue());
        }
    }

    protected static class ButtonSelector extends QMButton {
        private int value;

        public ButtonSelector(int initialValue, String prompt) {
            super(prompt);
            this.value = initialValue;
            this.setPrefWidth(250);
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    protected class PostActionButtonSelector extends ButtonSelector implements PostActionNode {
        private final Consumer<Integer> onAction;

        public PostActionButtonSelector(int initialValue, String prompt, Consumer<Integer> onAction) {
            super(initialValue, prompt);
            this.onAction = onAction;
            DialogNodeEditor.this.postActionNodes.add(this);
        }

        @Override
        public void postProcess() {
            onAction.accept(this.getValue());
        }
    }

    protected class PostActionFloatModifier extends FloatModifier implements PostActionNode {
        private final Consumer<Float> onAction;

        public PostActionFloatModifier(float value, float step, float min, float max, Consumer<Float> onAction) {
            super(value, step, min, max);
            this.onAction = onAction;
            DialogNodeEditor.this.postActionNodes.add(this);
        }

        @Override
        public void postProcess() {
            onAction.accept(this.getValue());
        }
    }
}
