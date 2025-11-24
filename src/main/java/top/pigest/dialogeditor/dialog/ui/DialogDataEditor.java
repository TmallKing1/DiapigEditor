package top.pigest.dialogeditor.dialog.ui;

import com.jfoenix.controls.JFXDialog;
import top.pigest.dialogeditor.dialog.DialogNode;
import top.pigest.dialogeditor.util.gi.Struct;

public interface DialogDataEditor {
    Struct<DialogNode> getBindNode();

    void receiveBindNode(Struct<DialogNode> dialogNode, int index);

    void postProcess(JFXDialog dialog);
}
