package top.pigest.dialogeditor.dialog;

import com.google.gson.JsonArray;
import top.pigest.dialogeditor.util.gi.GIVariableUtils;

public class DialogBranch {
    private String title;
    private int jumpIndex;
    public DialogBranch(String title, int jumpIndex) {
        this.title = title;
        this.jumpIndex = jumpIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getJumpIndex() {
        return jumpIndex;
    }

    public void setJumpIndex(int jumpIndex) {
        this.jumpIndex = jumpIndex;
    }

    public static DialogBranch read(JsonArray jsonArray) {
        return new DialogBranch(
                GIVariableUtils.readString(jsonArray, 0),
                GIVariableUtils.readInt(jsonArray, 1)
        );
    }

    public static JsonArray write(DialogBranch dialogBranch) {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(GIVariableUtils.writeString(dialogBranch.getTitle()));
        jsonArray.add(GIVariableUtils.writeInt(dialogBranch.getJumpIndex()));
        return jsonArray;
    }

    public DialogBranch copy() {
        return new DialogBranch(title, jumpIndex);
    }
}
