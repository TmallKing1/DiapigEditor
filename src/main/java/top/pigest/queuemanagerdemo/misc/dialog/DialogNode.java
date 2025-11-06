package top.pigest.queuemanagerdemo.misc.dialog;

import com.google.gson.JsonArray;
import top.pigest.queuemanagerdemo.util.gi.GIVariableUtils;
import top.pigest.queuemanagerdemo.util.gi.Struct;
import top.pigest.queuemanagerdemo.util.gi.StructList;

public class DialogNode {
    private Operation operation;
    private int jumpIndex = 0;
    private StructList<DialogBranch> dialogBranchList;
    private boolean inheritPreviousTitleSettings = false;
    private String title = "标题";
    private boolean hasSubtitle = false;
    private String subtitle = "副标题";
    private String content = "内容";

    private DialogNode(int opCode) {
        this.operation = Operation.fromOpCode(opCode);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public int getJumpIndex() {
        return jumpIndex;
    }

    public void setJumpIndex(int jumpIndex) {
        this.jumpIndex = jumpIndex;
    }

    public void createDialogBranch(int currentIndex) {
        int size = this.dialogBranchList.size();
        if (size < 4) {
            this.addDialogBranch(size, new Struct<>(dialogBranchList.getStructId(), new DialogBranch("标题", currentIndex + 1)));
        } else {
            throw new IllegalStateException("最多支持创建4个分支");
        }
    }

    public void addDialogBranch(int index, Struct<DialogBranch> dialogBranch) {
        this.dialogBranchList.add(index, dialogBranch);
    }

    public void removeDialogBranch(int index) {
        this.dialogBranchList.remove(index);
    }

    public StructList<DialogBranch> getDialogBranchList() {
        return dialogBranchList;
    }

    public boolean isInheritPreviousTitleSettings() {
        return inheritPreviousTitleSettings;
    }

    public void setInheritPreviousTitleSettings(boolean inheritPreviousTitleSettings) {
        this.inheritPreviousTitleSettings = inheritPreviousTitleSettings;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHasSubtitle() {
        return hasSubtitle;
    }

    public void setHasSubtitle(boolean hasSubtitle) {
        this.hasSubtitle = hasSubtitle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        if (hasSubtitle) {
            return "%s（%s）：%s".formatted(title, subtitle, content);
        }
        return "%s：%s".formatted(title, content);
    }

    public static DialogNode createNewDialogNode(String dialogBranchStructId) {
        DialogNode dialogNode = new DialogNode(0);
        dialogNode.dialogBranchList = new StructList<>(dialogBranchStructId);
        return dialogNode;
    }

    public DialogNode copy() {
        DialogNode dialogNode = new DialogNode(this.getOperation().getOpCode());
        this.copyTo(dialogNode);
        return dialogNode;
    }

    public void copyTo(DialogNode dialogNode) {
        dialogNode.setOperation(this.getOperation());
        dialogNode.setJumpIndex(this.getJumpIndex());
        dialogNode.dialogBranchList = new StructList<>(this.dialogBranchList.getStructId());
        this.dialogBranchList.forEach(struct -> dialogNode.dialogBranchList.add(new Struct<>(struct.getStructId(), struct.getValue().copy())));
        dialogNode.setInheritPreviousTitleSettings(this.isInheritPreviousTitleSettings());
        dialogNode.setTitle(this.getTitle());
        dialogNode.setHasSubtitle(this.isHasSubtitle());
        dialogNode.setSubtitle(this.getSubtitle());
        dialogNode.setContent(this.getContent());
    }

    public static DialogNode read(JsonArray jsonArray) {
        DialogNode dialogNode = new DialogNode(GIVariableUtils.readInt(jsonArray, 0));
        dialogNode.setJumpIndex(GIVariableUtils.readInt(jsonArray, 1));
        dialogNode.dialogBranchList = GIVariableUtils.readStructList(jsonArray, 2, DialogBranch::read);
        dialogNode.setInheritPreviousTitleSettings(GIVariableUtils.readBoolean(jsonArray, 3));
        dialogNode.setTitle(GIVariableUtils.readString(jsonArray, 4));
        dialogNode.setHasSubtitle(GIVariableUtils.readBoolean(jsonArray, 5));
        dialogNode.setSubtitle(GIVariableUtils.readString(jsonArray, 6));
        dialogNode.setContent(GIVariableUtils.readString(jsonArray, 7));
        return dialogNode;
    }

    public static JsonArray write(DialogNode dialogNode) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(GIVariableUtils.writeInt(dialogNode.operation.getOpCode()));
        jsonArray.add(GIVariableUtils.writeInt(dialogNode.getJumpIndex()));
        jsonArray.add(GIVariableUtils.writeStructList(dialogNode.dialogBranchList, DialogBranch::write));
        jsonArray.add(GIVariableUtils.writeBoolean(dialogNode.isInheritPreviousTitleSettings()));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getTitle()));
        jsonArray.add(GIVariableUtils.writeBoolean(dialogNode.isHasSubtitle()));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getSubtitle()));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getContent()));
        return jsonArray;
    }

    public enum Operation {
        END_DIALOG("结束对话", "fas-check-circle"),
        NEXT_SENTENCE("跳转下个节点", "fas-angle-double-down"),
        JUMP_SENTENCE("跳转指定节点", "fas-map-marker-alt"),
        OPEN_SELECTION("打开分支选择", "fas-th-list");

        private final String name;
        private final String iconCode;

        Operation(final String name, String iconCode) {
            this.name = name;
            this.iconCode = iconCode;
        }

        public String getName() {
            return name;
        }

        public String getIconCode() {
            return iconCode;
        }

        public int getOpCode() {
            return this.ordinal();
        }

        public static Operation fromOpCode(final int opCode) {
            return Operation.values()[opCode];
        }

        @Override
        public String toString() {
            return this.getName();
        }
    }
}
