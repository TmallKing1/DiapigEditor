package top.pigest.dialogeditor.control;

import javafx.scene.layout.Pane;

public interface ChildPage {
    Pane getParentPage();
    void setParentPage(Pane parentPage);
}
