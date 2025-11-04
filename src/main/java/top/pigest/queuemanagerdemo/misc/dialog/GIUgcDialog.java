package top.pigest.queuemanagerdemo.misc.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import top.pigest.queuemanagerdemo.util.gi.GIVariableUtils;
import top.pigest.queuemanagerdemo.util.gi.Struct;
import top.pigest.queuemanagerdemo.util.gi.StructList;

public class GIUgcDialog {
    private StructList<DialogNode> dialogNodeList;
    private int startNode = 0;
    private GIUgcDialog() {

    }

    public void addNode(DialogNode node) {
        dialogNodeList.addValue(node);
    }

    public void removeNode(int index) {
        dialogNodeList.remove(index);
    }

    public StructList<DialogNode> getDialogNodeList() {
        return dialogNodeList;
    }

    public int getStartNode() {
        return startNode;
    }

    public void setStartNode(int startNode) {
        this.startNode = startNode;
    }

    public static GIUgcDialog createNewDialog(String dialogNodeStructId) {
        GIUgcDialog gIUgcDialog = new GIUgcDialog();
        gIUgcDialog.dialogNodeList = new StructList<>(dialogNodeStructId);
        return gIUgcDialog;
    }

    public static Struct<GIUgcDialog> read(JsonObject obj) {
        return GIVariableUtils.readStructValue(obj, jsonArray -> {
            GIUgcDialog dialog = new GIUgcDialog();
            dialog.dialogNodeList = GIVariableUtils.readStructList(jsonArray, 0, DialogNode::read);
            dialog.setStartNode(GIVariableUtils.readInt(jsonArray, 1));
            return dialog;
        });
    }

    public static JsonObject write(Struct<GIUgcDialog> dialog) {
        return GIVariableUtils.writeStructValue(dialog, GIUgcDialog -> {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(GIVariableUtils.writeStructList(dialog.getValue().getDialogNodeList(), DialogNode::write));
            jsonArray.add(GIVariableUtils.writeInt(dialog.getValue().getStartNode()));
            return jsonArray;
        });
    }
}
