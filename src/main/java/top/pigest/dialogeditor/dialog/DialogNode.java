package top.pigest.dialogeditor.dialog;

import com.google.gson.JsonArray;
import top.pigest.dialogeditor.util.gi.GIVariableUtils;
import top.pigest.dialogeditor.util.gi.Struct;
import top.pigest.dialogeditor.util.gi.StructList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DialogNode {
    private Operation operation;
    private int jumpIndex = 0;
    private StructList<DialogBranch> dialogBranchList;
    private int defaultBranch = 0;
    private boolean inheritPreviousTitleSettings = false;
    private String title = "标题";
    private boolean hasSubtitle = false;
    private String subtitle = "副标题";
    private String content = "内容";
    private TextMethod textMethod = TextMethod.DIRECT;
    private String enterEvent = "";
    private String leaveEvent = "";
    private float duration = 0f;

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

    public StructList<DialogBranch> getDialogBranchList() {
        return dialogBranchList;
    }

    public int getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(int defaultBranch) {
        this.defaultBranch = defaultBranch;
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

    public TextMethod getTextMethod() {
        return textMethod;
    }

    public void setTextMethod(TextMethod textMethod) {
        this.textMethod = textMethod;
    }

    public String getEnterEvent() {
        return enterEvent;
    }

    public void setEnterEvent(String enterEvent) {
        this.enterEvent = enterEvent;
    }

    public String getLeaveEvent() {
        return leaveEvent;
    }

    public void setLeaveEvent(String leaveEvent) {
        this.leaveEvent = leaveEvent;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
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
        dialogNode.setDefaultBranch(this.getDefaultBranch());
        dialogNode.setInheritPreviousTitleSettings(this.isInheritPreviousTitleSettings());
        dialogNode.setTitle(this.getTitle());
        dialogNode.setHasSubtitle(this.isHasSubtitle());
        dialogNode.setSubtitle(this.getSubtitle());
        dialogNode.setContent(this.getContent());
        dialogNode.setTextMethod(this.getTextMethod());
        dialogNode.setEnterEvent(this.getEnterEvent());
        dialogNode.setLeaveEvent(this.getLeaveEvent());
        dialogNode.setDuration(this.getDuration());
    }

    public static DialogNode read(JsonArray jsonArray) {
        DialogNode dialogNode = new DialogNode(GIVariableUtils.readInt(jsonArray, 0));
        dialogNode.setJumpIndex(GIVariableUtils.readInt(jsonArray, 1));
        dialogNode.dialogBranchList = GIVariableUtils.readStructList(jsonArray, 2, DialogBranch::read);
        dialogNode.setDefaultBranch(GIVariableUtils.readInt(jsonArray, 3));
        dialogNode.setInheritPreviousTitleSettings(GIVariableUtils.readBoolean(jsonArray, 4));
        dialogNode.setTitle(GIVariableUtils.readString(jsonArray, 5));
        dialogNode.setHasSubtitle(GIVariableUtils.readBoolean(jsonArray, 6));
        dialogNode.setSubtitle(GIVariableUtils.readString(jsonArray, 7));
        List<String> content = GIVariableUtils.readStringList(jsonArray, 8);
        if (content.isEmpty()) {
            dialogNode.setTextMethod(TextMethod.DIRECT);
            dialogNode.setContent("");
        } else if (content.size() == 1) {
            dialogNode.setTextMethod(TextMethod.DIRECT);
            dialogNode.setContent(content.getFirst());
        } else {
            if (content.stream().allMatch(s -> s.length() <= 1)) {
                dialogNode.setTextMethod(TextMethod.TYPEWRITER);
                dialogNode.setContent(String.join("", content));
            } else {
                dialogNode.setTextMethod(TextMethod.CUSTOM);
                dialogNode.setContent(String.join("|", content));
            }
        }
        dialogNode.setEnterEvent(GIVariableUtils.readString(jsonArray, 9));
        dialogNode.setLeaveEvent(GIVariableUtils.readString(jsonArray, 10));
        dialogNode.setDuration(GIVariableUtils.readFloat(jsonArray, 11));
        return dialogNode;
    }

    public static JsonArray write(DialogNode dialogNode) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(GIVariableUtils.writeInt(dialogNode.operation.getOpCode()));
        jsonArray.add(GIVariableUtils.writeInt(dialogNode.getJumpIndex()));
        jsonArray.add(GIVariableUtils.writeStructList(dialogNode.dialogBranchList, DialogBranch::write));
        jsonArray.add(GIVariableUtils.writeInt(dialogNode.getDefaultBranch()));
        jsonArray.add(GIVariableUtils.writeBoolean(dialogNode.isInheritPreviousTitleSettings()));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getTitle()));
        jsonArray.add(GIVariableUtils.writeBoolean(dialogNode.isHasSubtitle()));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getSubtitle()));
        List<String> l = new ArrayList<>();
        switch (dialogNode.getTextMethod()) {
            case DIRECT -> l.add(dialogNode.getContent());
            case TYPEWRITER -> {
                for (int i = 0; i < dialogNode.getContent().length(); i++) {
                    l.add(String.valueOf(dialogNode.getContent().charAt(i)));
                }
            }
            case CUSTOM -> l = Arrays.asList(dialogNode.getContent().split("\\|"));
        }
        jsonArray.add(GIVariableUtils.writeStringList(l));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getEnterEvent()));
        jsonArray.add(GIVariableUtils.writeString(dialogNode.getLeaveEvent()));
        jsonArray.add(GIVariableUtils.writeFloat(dialogNode.getDuration()));
        return jsonArray;
    }

    public enum TextMethod {
        DIRECT("直接呈现"),
        TYPEWRITER("打字机呈现"),
        CUSTOM("自定义打字机");

        private final String name;

        TextMethod(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
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
