package top.pigest.queuemanagerdemo.misc.dialog.ui;

import com.jfoenix.controls.JFXDialog;
import top.pigest.queuemanagerdemo.misc.dialog.DialogNode;
import top.pigest.queuemanagerdemo.util.gi.Struct;

public interface DialogDataEditor {
    Struct<DialogNode> getBindNode();

    void receiveBindNode(Struct<DialogNode> dialogNode, int index);

    void postProcess(JFXDialog dialog);
}
