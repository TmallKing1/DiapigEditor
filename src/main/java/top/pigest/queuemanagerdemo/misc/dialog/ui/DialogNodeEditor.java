package top.pigest.queuemanagerdemo.misc.dialog.ui;

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
import top.pigest.queuemanagerdemo.QueueManager;
import top.pigest.queuemanagerdemo.Settings;
import top.pigest.queuemanagerdemo.control.QMButton;
import top.pigest.queuemanagerdemo.control.WhiteFontIcon;
import top.pigest.queuemanagerdemo.misc.dialog.DialogBranch;
import top.pigest.queuemanagerdemo.misc.dialog.DialogNode;
import top.pigest.queuemanagerdemo.util.Utils;
import top.pigest.queuemanagerdemo.util.gi.Struct;

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

    private final QMButton textButton;
    private final QMButton logicButton;
    private final QMButton cancelButton;
    private final QMButton okButton;
    private final VBox center;
    private PostActionCheckBox inheritTitle;
    private PostActionTextField title;
    private HBox subtitleContainer;
    private PostActionCheckBox subtitleCheckbox;
    private PostActionTextField subtitle;
    private PostActionTextArea content;
    private BorderPane operation;
    private BorderPane jumpNode;
    private PostActionButtonSelector jumpNodeSelector;
    private BorderPane branchEditHead;
    private VBox branchList;

    private boolean isEditingText = true;
    private int currentRequestBranchId = 0;
    private final int index;
    private JFXDialog dialog;

    public DialogNodeEditor(Struct<DialogNode> node, int index, DialogEditorPage editorPage) {
        this.node = node;
        this.editingNode = new Struct<>(node.getStructId(), node.getValue().copy());
        this.index = index;
        this.editorPage = editorPage;

        this.setPrefWidth(640);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(20, 20, 20, 20));
        Text titleNode = new Text("编辑对话节点");
        titleNode.setTextAlignment(TextAlignment.CENTER);
        titleNode.setFont(new Font(Settings.BOLD_FONT.getFamily(), 30));
        titleNode.setFill(Color.DIMGRAY);
        VBox.setMargin(titleNode, new Insets(0, 0, 20, 0));
        this.getChildren().add(titleNode);

        HBox pageSelection = new HBox(40);
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
        VBox.setMargin(pageSelection, new Insets(0, 0, 20, 0));
        this.getChildren().add(pageSelection);

        center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setPrefWidth(560);
        center.setMinWidth(USE_PREF_SIZE);
        center.setMaxWidth(USE_PREF_SIZE);

        updateCenter(true);

        VBox.setMargin(center, new Insets(0, 0, 30, 0));
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
                    subtitleCheckbox.setDisable(inheritTitle.isSelected());
                    subtitle.setDisable(inheritTitle.isSelected() || !subtitleCheckbox.isSelected());
                });
            }
            center.getChildren().add(inheritTitle);

            if (title == null) {
                title = new PostActionTextField(this.editingNode.getValue().getTitle(), "对话标题（说话人）", this.editingNode.getValue()::setTitle);
                title.setLabelFloat(true);
                title.setPadding(new Insets(45, 0, 0, 0));
            }
            center.getChildren().add(title);

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
                content.setPrefHeight(120);
                content.setLabelFloat(true);
                content.setPadding(new Insets(45, 0, 0, 0));
            }
            center.getChildren().add(content);

            title.setDisable(inheritTitle.isSelected());
            subtitleCheckbox.setDisable(inheritTitle.isSelected());
            subtitle.setDisable(inheritTitle.isSelected() || !subtitleCheckbox.isSelected());
        } else {
            if (operation == null) {
                PostActionComboBox<DialogNode.Operation> operationSelector = new PostActionComboBox<>(
                        this.editingNode.getValue().getOperation(),
                        List.of(DialogNode.Operation.values()),
                        this.editingNode.getValue()::setOperation
                );
                operationSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                        updateSpecificOperation(newValue));
                operation = createLRBorderPane("操作类型", operationSelector);
                operation.setPadding(new Insets(0, 0, 30, 0));
            }
            center.getChildren().add(operation);
            updateSpecificOperation(this.editingNode.getValue().getOperation());
        }
    }

    private void updateSpecificOperation(DialogNode.Operation operation) {
        if (isEditingText) {
            return;
        }
        center.getChildren().subList(1, center.getChildren().size()).clear();
        switch (operation) {
            case JUMP_SENTENCE -> {
                if (jumpNode == null) {
                    jumpNodeSelector = new PostActionButtonSelector(this.editingNode.getValue().getJumpIndex(), "选择跳转节点（当前：%s）".formatted(this.editingNode.getValue().getJumpIndex()), this.editingNode.getValue()::setJumpIndex);
                    jumpNodeSelector.setOnAction(event -> {
                        this.editorPage.selectMode(true, "请选择跳转的节点（选择编辑中的节点以取消）");
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
                branchList.getChildren().add(new BranchView(branch.getValue().getTitle(), id, branch.getValue().getJumpIndex()));
                id++;
            }
        }
        center.getChildren().add(branchList);
    }

    private void addNewBranch() {
        if (DialogNodeEditor.this.branchList.getChildren().size() < 4) {
            branchList.getChildren().add(new BranchView("", DialogNodeEditor.this.branchList.getChildren().size() + 1, index + 1));
        } else {
            Utils.showDialogMessage("最多只能添加 4 个分支", true, QueueManager.INSTANCE.getMainScene().getRootDrawer());
        }
    }

    public BorderPane createLRBorderPane(String left, Node right) {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefWidth(999);
        Text titleNode = new Text(left);
        titleNode.setFont(Settings.DEFAULT_FONT);
        BorderPane.setAlignment(titleNode, Pos.CENTER_LEFT);
        borderPane.setLeft(titleNode);
        BorderPane.setAlignment(right, Pos.CENTER_RIGHT);
        borderPane.setRight(right);
        return borderPane;
    }

    @Override
    public void postProcess(JFXDialog dialog) {
        this.dialog = dialog;
        okButton.setOnAction(event -> {
            this.postActionNodes.forEach(PostActionNode::postProcess);
            if (this.branchList != null) {
                this.editingNode.getValue().getDialogBranchList().clear();
                this.branchList.getChildren().forEach(branch -> {
                    if (branch instanceof BranchView branchView) {
                        this.editingNode.getValue().getDialogBranchList().addValue(new DialogBranch(branchView.textField.getText(), branchView.buttonSelector.getValue()));
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
        this.editorPage.selectMode(false, "");
        if (dialogNode == this.node) {
            return;
        }
        if (editingNode.getValue().getOperation() == DialogNode.Operation.JUMP_SENTENCE) {
            if (jumpNodeSelector != null) {
                jumpNodeSelector.setValue(index);
                jumpNodeSelector.setText("选择跳转节点（当前：%s）".formatted(index));
            }
        } else if (editingNode.getValue().getOperation() == DialogNode.Operation.OPEN_SELECTION) {
            int p = currentRequestBranchId - 1;
            if (p < branchList.getChildren().size()) {
                BranchView branchView = ((BranchView) branchList.getChildren().get(p));
                branchView.buttonSelector.setValue(index);
                branchView.buttonSelector.setText("选择跳转节点（当前：%s）".formatted(index));
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
        private final Text idText;
        private int id;

        public BranchView(String text, int id, int index) {
            this.setPadding(new Insets(5, 0, 5, 0));
            HBox idView = new HBox(2);
            idView.setAlignment(Pos.CENTER_LEFT);
            idView.getChildren().add(new FontIcon("fas-th-list:20"));
            this.id = id;
            this.idText = new Text(String.valueOf(this.id));
            this.idText.setFont(Settings.DEFAULT_FONT);
            idView.getChildren().add(this.idText);
            BorderPane.setMargin(idView, new Insets(0, 5, 0, 0));
            BorderPane.setAlignment(idView, Pos.CENTER_LEFT);
            this.setLeft(idView);

            this.textField = new JFXTextField(text);
            this.textField.setPromptText("分支文本");
            this.textField.setFont(Settings.DEFAULT_FONT);
            BorderPane.setMargin(this.textField, new Insets(0, 5, 0, 0));
            BorderPane.setAlignment(this.textField, Pos.CENTER_LEFT);
            this.setCenter(this.textField);

            HBox right = new HBox(5);
            right.setAlignment(Pos.CENTER_RIGHT);
            this.buttonSelector = new ButtonSelector(index, "选择跳转节点（当前：%s）".formatted(index));
            this.buttonSelector.setOnAction(event -> {
                DialogNodeEditor.this.currentRequestBranchId = this.id;
                DialogNodeEditor.this.editorPage.selectMode(true, "请选择跳转的节点（选择编辑中的节点以取消）");
                dialog.close();
            });
            this.removeButton = new QMButton("", "#bb5555");
            this.removeButton.setGraphic(new WhiteFontIcon("fas-trash"));
            this.removeButton.setOnAction(event -> {
                this.buttonSelector.setOnAction(null);
                this.removeButton.setOnAction(null);
                ObservableList<Node> children = DialogNodeEditor.this.branchList.getChildren();
                for (int i = id; i < children.size(); i++) {
                    ((BranchView) children.get(i)).id--;
                    ((BranchView) children.get(i)).updateId();
                }
                children.remove(this);
            });
            right.getChildren().addAll(this.buttonSelector, this.removeButton);
            BorderPane.setAlignment(this.removeButton, Pos.CENTER_RIGHT);
            this.setRight(right);
        }

        public void updateId() {
            this.idText.setText(String.valueOf(this.id));
        }
    }

    protected class PostActionTextField extends JFXTextField implements PostActionNode {
        private final Consumer<String> onAction;

        public PostActionTextField(String initialText, String prompt, Consumer<String> onAction) {
            super(initialText);
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
            this.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/combobox.css")).toExternalForm());
            this.getStylesheets().add(Objects.requireNonNull(QueueManager.class.getResource("css/scrollbar.css")).toExternalForm());
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
}
